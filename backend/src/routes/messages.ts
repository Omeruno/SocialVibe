import type { FastifyInstance } from "fastify";
import { z } from "zod";
import { prisma } from "../lib/prisma.js";
import { requireAuth } from "../lib/requireAuth.js";
import { deliverMessage } from "../sockets/index.js";

export async function messageRoutes(app: FastifyInstance) {
  app.addHook("preHandler", requireAuth);

  // History with a contact, newest last. `since` (ISO timestamp) lets a
  // client that reconnects after being offline ask for only what it missed
  // instead of the whole thread — the same query the socket layer would
  // need for reconnect-sync, exposed here so it works even without a live
  // socket connection.
  app.get("/messages/:contactId", async (request, reply) => {
    const paramsSchema = z.object({ contactId: z.string() });
    const querySchema = z.object({
      since: z.string().datetime().optional(),
      limit: z.coerce.number().int().min(1).max(200).default(50),
    });

    const params = paramsSchema.safeParse(request.params);
    const query = querySchema.safeParse(request.query);
    if (!params.success || !query.success) {
      return reply.code(400).send({ error: "Invalid request" });
    }

    const { contactId } = params.data;
    const { since, limit } = query.data;

    const messages = await prisma.message.findMany({
      where: {
        OR: [
          { senderId: request.userId, receiverId: contactId },
          { senderId: contactId, receiverId: request.userId },
        ],
        ...(since ? { createdAt: { gt: new Date(since) } } : {}),
      },
      orderBy: { createdAt: "asc" },
      take: limit,
    });

    return reply.send({ messages });
  });

  app.post("/messages/:contactId", async (request, reply) => {
    const paramsSchema = z.object({ contactId: z.string() });
    const bodySchema = z.object({ text: z.string().min(1).max(4000) });

    const params = paramsSchema.safeParse(request.params);
    const body = bodySchema.safeParse(request.body);
    if (!params.success || !body.success) {
      return reply.code(400).send({ error: "Invalid request" });
    }

    const message = await prisma.message.create({
      data: {
        senderId: request.userId!,
        receiverId: params.data.contactId,
        text: body.data.text,
      },
    });

    const delivered = await deliverMessage(message);
    return reply.code(201).send({ message: delivered });
  });
}
