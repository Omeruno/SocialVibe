import jwt from "jsonwebtoken";
import crypto from "node:crypto";

const ACCESS_SECRET = process.env.JWT_ACCESS_SECRET!;

export const ACCESS_TOKEN_TTL = "15m";
export const REFRESH_TOKEN_TTL_MS = 30 * 24 * 60 * 60 * 1000; // 30 days

export interface AccessTokenPayload {
  userId: string;
  username: string;
}

export function signAccessToken(payload: AccessTokenPayload): string {
  return jwt.sign(payload, ACCESS_SECRET, { expiresIn: ACCESS_TOKEN_TTL });
}

export function verifyAccessToken(token: string): AccessTokenPayload {
  return jwt.verify(token, ACCESS_SECRET) as AccessTokenPayload;
}

// Refresh tokens are opaque random strings stored in the DB (RefreshToken
// table), not JWTs — that way a single stolen/rotated token can be revoked
// by deleting its row, which a self-verifying JWT would not allow.
//
// Split into a `selector` (indexed lookup key, safe in plaintext) and a
// `verifier` (the actual secret half, only ever stored as a bcrypt hash —
// see routes/auth.ts). The client gets `${selector}.${verifier}` as one
// opaque string and never sees them as separate fields.
export interface RawRefreshToken {
  selector: string;
  verifier: string;
  token: string;
}

export function generateRefreshToken(): RawRefreshToken {
  const selector = crypto.randomBytes(16).toString("hex");
  const verifier = crypto.randomBytes(32).toString("hex");
  return { selector, verifier, token: `${selector}.${verifier}` };
}

export function splitRefreshToken(token: string): { selector: string; verifier: string } | null {
  const parts = token.split(".");
  if (parts.length !== 2 || !parts[0] || !parts[1]) return null;
  return { selector: parts[0], verifier: parts[1] };
}
