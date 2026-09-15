import "dotenv/config";
import Fastify from "fastify";
import cors from "@fastify/cors";
import { authRoutes } from "./routes/auth.js";
import { contactRoutes } from "./routes/contacts.js";
import { messageRoutes } from "./routes/messages.js";
import { initSockets } from "./sockets/index.js";

const app = Fastify({ logger: true });

await app.register(cors, { origin: true });

app.get("/health", async () => ({ ok: true }));

await app.register(authRoutes);
await app.register(contactRoutes);
await app.register(messageRoutes);

await app.ready();
initSockets(app.server);

const port = Number(process.env.PORT) || 3000;
await app.listen({ port, host: "0.0.0.0" });
