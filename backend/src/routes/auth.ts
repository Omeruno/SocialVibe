import type { FastifyInstance } from "fastify";
import bcrypt from "bcryptjs";
import { z } from "zod";
import { prisma } from "../lib/prisma.js";
import {
  generateRefreshToken,
  REFRESH_TOKEN_TTL_MS,
  signAccessToken,
} from "../lib/auth.js";
import { requireAuth } from "../lib/requireAuth.js";

const credentialsSchema = z.object({
  username: z
    .string()
    .min(3)
    .max(24)
    .regex(/^[a-zA-Z0-9_]+$/, "letters, digits and underscore only"),
  password: z.string().min(6).max(72),
});

async function issueTokenPair(userId: string, username: string) {
  const accessToken = signAccessToken({ userId, username });
  const refreshToken = generateRefreshToken();

  await prisma.refreshToken.create({
    data: {
      token: refreshToken,
      userId,
      expiresAt: new Date(Date.now() + REFRESH_TOKEN_TTL_MS),
    },
  });

  return { accessToken, refreshToken };
}

export async function authRoutes(app: FastifyInstance) {
  app.post("/auth/register", async (request, reply) => {
    const parsed = credentialsSchema.safeParse(request.body);
    if (!parsed.success) {
      return reply.code(400).send({ error: parsed.error.flatten() });
    }
    const { username, password } = parsed.data;

    const existing = await prisma.user.findUnique({ where: { username } });
    if (existing) {
      return reply.code(409).send({ error: "Username already taken" });
    }

    const passwordHash = await bcrypt.hash(password, 10);
    const user = await prisma.user.create({
      data: { username, passwordHash },
    });

    const tokens = await issueTokenPair(user.id, user.username);
    return reply.code(201).send({
      user: { id: user.id, username: user.username },
      ...tokens,
    });
  });

  app.post("/auth/login", async (request, reply) => {
    const parsed = credentialsSchema.safeParse(request.body);
    if (!parsed.success) {
      return reply.code(400).send({ error: parsed.error.flatten() });
    }
    const { username, password } = parsed.data;

    const user = await prisma.user.findUnique({ where: { username } });
    if (!user) {
      return reply.code(401).send({ error: "Invalid credentials" });
    }

    const passwordMatches = await bcrypt.compare(password, user.passwordHash);
    if (!passwordMatches) {
      return reply.code(401).send({ error: "Invalid credentials" });
    }

    const tokens = await issueTokenPair(user.id, user.username);
    return reply.send({
      user: { id: user.id, username: user.username },
      ...tokens,
    });
  });

  app.post("/auth/refresh", async (request, reply) => {
    const bodySchema = z.object({ refreshToken: z.string() });
    const parsed = bodySchema.safeParse(request.body);
    if (!parsed.success) {
      return reply.code(400).send({ error: parsed.error.flatten() });
    }

    const stored = await prisma.refreshToken.findUnique({
      where: { token: parsed.data.refreshToken },
      include: { user: true },
    });

    if (!stored || stored.expiresAt < new Date()) {
      return reply.code(401).send({ error: "Refresh token invalid or expired" });
    }

    // Rotate: delete the used token and issue a fresh pair.
    await prisma.refreshToken.delete({ where: { id: stored.id } });
    const tokens = await issueTokenPair(stored.user.id, stored.user.username);
    return reply.send(tokens);
  });

  app.get("/auth/me", { preHandler: requireAuth }, async (request, reply) => {
    const user = await prisma.user.findUnique({
      where: { id: request.userId },
      select: { id: true, username: true, status: true, statusMessage: true },
    });
    if (!user) {
      return reply.code(404).send({ error: "User not found" });
    }
    return reply.send({ user });
  });
}
