import "dotenv/config";
import Fastify from "fastify";
import cors from "@fastify/cors";
import rateLimit from "@fastify/rate-limit";
import { authRoutes } from "./routes/auth.js";
import { contactRoutes } from "./routes/contacts.js";
import { messageRoutes } from "./routes/messages.js";
import { initSockets } from "./sockets/index.js";

const app = Fastify({ logger: true });

await app.register(cors, { origin: true });

// Generous global ceiling against runaway clients/bugs; the auth routes
// layer a much tighter limit on top of this (see routes/auth.ts) since
// credential-guessing is the realistic threat there, not general traffic.
await app.register(rateLimit, {
  max: 300,
  timeWindow: "1 minute",
});

app.get("/health", async () => ({ ok: true }));

await app.register(authRoutes);
await app.register(contactRoutes);
await app.register(messageRoutes);

await app.ready();
initSockets(app.server);

const port = Number(process.env.PORT) || 3000;
await app.listen({ port, host: "0.0.0.0" });
