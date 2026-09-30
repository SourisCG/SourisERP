import { NextRequest, NextResponse } from "next/server";
import { TOKEN_COOKIE, USER_COOKIE } from "@/lib/session";

const BACKEND = process.env.INTERNAL_API_URL ?? process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export async function POST(request: NextRequest) {
  const body = await request.text();
  const locale = request.cookies.get("locale")?.value;
  const response = await fetch(`${BACKEND}/api/v1/auth/login`, {
    method: "POST",
    headers: {
      "content-type": "application/json",
      "accept-language": locale === "es" ? "es" : "en",
    },
    body,
    cache: "no-store",
  });

  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    return NextResponse.json(payload, { status: response.status });
  }

  const out = NextResponse.json({ user: payload.user });
  out.cookies.set(TOKEN_COOKIE, payload.accessToken, {
    httpOnly: true,
    sameSite: "lax",
    path: "/",
    maxAge: payload.expiresIn ?? 1800,
  });
  out.cookies.set(USER_COOKIE, JSON.stringify(payload.user), {
    sameSite: "lax",
    path: "/",
    maxAge: payload.expiresIn ?? 1800,
  });
  return out;
}

export async function DELETE() {
  const out = NextResponse.json({ ok: true });
  out.cookies.delete(TOKEN_COOKIE);
  out.cookies.delete(USER_COOKIE);
  return out;
}
