import type { Server as HttpServer } from "node:http";
import { Server as SocketIOServer, type Socket } from "socket.io";
import type { Message, UserStatus } from "@prisma/client";
import { verifyAccessToken } from "../lib/auth.js";
import { prisma } from "../lib/prisma.js";

let io: SocketIOServer | undefined;
const onlineSockets = new Map<string, Set<string>>(); // userId -> socket ids

export function initSockets(server: HttpServer): SocketIOServer {
  io = new SocketIOServer(server, { cors: { origin: "*" } });

  io.use((socket, next) => {
    const token = socket.handshake.auth?.token as string | undefined;
    if (!token) {
      next(new Error("Missing auth token"));
      return;
    }
    try {
      const payload = verifyAccessToken(token);
      socket.data.userId = payload.userId;
      next();
    } catch {
      next(new Error("Invalid or expired token"));
    }
  });

  io.on("connection", (socket) => {
    handleConnection(socket).catch((err) => console.error("socket connection error", err));
  });

  return io;
}

async function handleConnection(socket: Socket) {
  const userId: string = socket.data.userId;
  socket.join(userRoom(userId));
  markOnline(userId, socket.id);

  await prisma.user.update({ where: { id: userId }, data: { status: "ONLINE" } });
  await broadcastPresence(userId, "ONLINE");

  socket.on(
    "message:send",
    async (
      payload: { receiverId: string; text: string },
      ack?: (response: { message?: Message; error?: string }) => void
    ) => {
      if (!payload?.receiverId || !payload.text?.trim()) {
        ack?.({ error: "Invalid payload" });
        return;
      }
      const message = await prisma.message.create({
        data: { senderId: userId, receiverId: payload.receiverId, text: payload.text.trim() },
      });
      const delivered = await deliverMessage(message);
      ack?.({ message: delivered });
    }
  );

  socket.on("typing:start", (payload: { receiverId: string }) => {
    io?.to(userRoom(payload.receiverId)).emit("typing:update", { fromUserId: userId, isTyping: true });
  });

  socket.on("typing:stop", (payload: { receiverId: string }) => {
    io?.to(userRoom(payload.receiverId)).emit("typing:update", { fromUserId: userId, isTyping: false });
  });

  socket.on("presence:update", async (payload: { status: UserStatus }) => {
    await prisma.user.update({ where: { id: userId }, data: { status: payload.status } });
    await broadcastPresence(userId, payload.status);
  });

  socket.on("disconnect", () => {
    unmarkOnline(userId, socket.id);
    if (!isOnline(userId)) {
      prisma.user
        .update({ where: { id: userId }, data: { status: "OFFLINE" } })
        .then(() => broadcastPresence(userId, "OFFLINE"))
        .catch((err) => console.error("failed to mark user offline", err));
    }
  });
}

// Shared by both the socket "message:send" path and the REST POST
// /messages fallback, so a message sent while a socket happens to be
// briefly reconnecting still reaches the other side in real time.
export async function deliverMessage(message: Message): Promise<Message> {
  const delivered =
    isOnline(message.receiverId) && !message.deliveredAt
      ? await prisma.message.update({ where: { id: message.id }, data: { deliveredAt: new Date() } })
      : message;

  if (io) {
    io.to(userRoom(delivered.receiverId)).emit("message:new", delivered);
    io.to(userRoom(delivered.senderId)).emit("message:new", delivered);
  }

  return delivered;
}

async function broadcastPresence(userId: string, status: UserStatus) {
  if (!io) return;
  const watchers = await prisma.contact.findMany({
    where: { contactId: userId },
    select: { ownerId: true },
  });
  for (const watcher of watchers) {
    io.to(userRoom(watcher.ownerId)).emit("presence:update", { userId, status });
  }
}

function userRoom(userId: string): string {
  return `user:${userId}`;
}

function markOnline(userId: string, socketId: string) {
  const sockets = onlineSockets.get(userId) ?? new Set<string>();
  sockets.add(socketId);
  onlineSockets.set(userId, sockets);
}

function unmarkOnline(userId: string, socketId: string) {
  const sockets = onlineSockets.get(userId);
  sockets?.delete(socketId);
  if (sockets && sockets.size === 0) {
    onlineSockets.delete(userId);
  }
}

function isOnline(userId: string): boolean {
  return (onlineSockets.get(userId)?.size ?? 0) > 0;
}
