import type { FastifyInstance } from "fastify";
import { z } from "zod";
import { prisma } from "../lib/prisma.js";
import { requireAuth } from "../lib/requireAuth.js";

export async function contactRoutes(app: FastifyInstance) {
  app.addHook("preHandler", requireAuth);

  app.get("/contacts", async (request, reply) => {
    const contacts = await prisma.contact.findMany({
      where: { ownerId: request.userId },
      include: { contact: true },
      orderBy: { createdAt: "asc" },
    });

    return reply.send({
      contacts: contacts.map((c) => ({
        id: c.contact.id,
        username: c.contact.username,
        status: c.contact.status,
        statusMessage: c.contact.statusMessage,
      })),
    });
  });

  app.post("/contacts", async (request, reply) => {
    const bodySchema = z.object({ username: z.string().min(1) });
    const parsed = bodySchema.safeParse(request.body);
    if (!parsed.success) {
      return reply.code(400).send({ error: parsed.error.flatten() });
    }

    const target = await prisma.user.findUnique({
      where: { username: parsed.data.username },
    });
    if (!target) {
      return reply.code(404).send({ error: "No such user" });
    }
    if (target.id === request.userId) {
      return reply.code(400).send({ error: "Cannot add yourself" });
    }

    const contact = await prisma.contact.upsert({
      where: {
        ownerId_contactId: { ownerId: request.userId!, contactId: target.id },
      },
      create: { ownerId: request.userId!, contactId: target.id },
      update: {},
    });

    return reply.code(201).send({
      contact: {
        id: target.id,
        username: target.username,
        status: target.status,
        statusMessage: target.statusMessage,
      },
      contactRecordId: contact.id,
    });
  });
}
