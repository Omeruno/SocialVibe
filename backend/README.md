# SocialVibe backend

Node.js + TypeScript + Fastify + Prisma + PostgreSQL + Socket.IO. No Firebase,
no Google services anywhere in this project — push delivery is handled by the
Android app keeping a live socket connection open (see the app's foreground
service), not by a third-party push provider.

## Local setup

1. **Postgres.** Either run it via Docker:
   ```
   docker compose up -d
   ```
   or point `DATABASE_URL` in `.env` at any Postgres instance you already have
   running (the Docker service and a local install both listen on `5432` by
   default, so don't run both at once).

2. **Env file.**
   ```
   cp .env.example .env
   ```
   Fill in real secrets for `JWT_ACCESS_SECRET` before this ever leaves your
   machine — the checked-in `.env.example` values are placeholders only.

3. **Install + migrate.**
   ```
   npm install
   npx prisma migrate dev
   ```

4. **Run.**
   ```
   npm run dev
   ```
   Server listens on `http://localhost:3000` (health check at `/health`).

## API surface (v0)

- `POST /auth/register` `{ username, password }` → `{ user, accessToken, refreshToken }`
- `POST /auth/login` `{ username, password }` → same shape
- `POST /auth/refresh` `{ refreshToken }` → rotates it, returns a new pair
- `GET /auth/me` (Bearer token) → current user
- `GET /contacts` (Bearer) → your contact list
- `POST /contacts` `{ username }` (Bearer) → add a contact by username
- `GET /messages/:contactId?since=<ISO>&limit=<n>` (Bearer) → history, optionally only what's newer than `since` (for reconnect sync)
- `POST /messages/:contactId` `{ text }` (Bearer) → REST fallback if the socket happens to be down

## Socket.IO (real-time layer)

Connect with `auth: { token: <accessToken> }`. Events:

- `message:send` `{ receiverId, text }` (ack) → persists + delivers instantly if the recipient is connected
- `message:new` (server → client) → a message that concerns you, either as sender or receiver
- `typing:start` / `typing:stop` `{ receiverId }` → relayed to the other side as `typing:update`
- `presence:update` `{ status }` (client → server) to change your own status; broadcast as `{ userId, status }` to everyone who has you as a contact

## Known gaps (intentionally not built yet)

- No message editing/deletion
- No group chats — everything here is 1:1
- No TLS — plain HTTP/WS, fine for local dev, must sit behind a reverse
  proxy (e.g. Nginx + Let's Encrypt) with HTTPS/WSS before this ever
  touches the open internet
- `readAt` is tracked in the DB but nothing sets it yet — no read receipts

Rate limiting (5/min on auth routes, 300/min globally) and hashed refresh
tokens (selector/verifier split, bcrypt) are done — see the auth routes and
`prisma/schema.prisma`.
