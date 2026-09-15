import type { FastifyReply, FastifyRequest } from "fastify";
import { verifyAccessToken } from "./auth.js";

declare module "fastify" {
  interface FastifyRequest {
    userId?: string;
    username?: string;
  }
}

export async function requireAuth(request: FastifyRequest, reply: FastifyReply) {
  const header = request.headers.authorization;
  if (!header?.startsWith("Bearer ")) {
    return reply.code(401).send({ error: "Missing bearer token" });
  }

  try {
    const payload = verifyAccessToken(header.slice("Bearer ".length));
    request.userId = payload.userId;
    request.username = payload.username;
  } catch {
    return reply.code(401).send({ error: "Invalid or expired token" });
  }
}
