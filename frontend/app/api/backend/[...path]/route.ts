import { NextRequest, NextResponse } from "next/server";
import { TOKEN_COOKIE } from "@/lib/session";

const BACKEND = process.env.INTERNAL_API_URL ?? process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export const dynamic = "force-dynamic";

async function proxy(request: NextRequest, context: { params: Promise<{ path: string[] }> }) {
  const { path } = await context.params;
  const target = `${BACKEND}/api/v1/${path.join("/")}${request.nextUrl.search}`;
  const locale = request.cookies.get("locale")?.value;

  const headers: HeadersInit = {
    "accept-language": locale === "es" ? "es" : "en",
  };
  const contentType = request.headers.get("content-type");
  if (contentType) {
    (headers as Record<string, string>)["content-type"] = contentType;
  }
  const token = request.cookies.get(TOKEN_COOKIE)?.value;
  if (token) {
    (headers as Record<string, string>)["authorization"] = `Bearer ${token}`;
  }

  const hasBody = !["GET", "HEAD"].includes(request.method);
  const response = await fetch(target, {
    method: request.method,
    headers,
    body: hasBody ? await request.text() : undefined,
    cache: "no-store",
  });

  const out = new NextResponse(response.body, { status: response.status });
  for (const header of ["content-type", "content-disposition"]) {
    const value = response.headers.get(header);
    if (value) {
      out.headers.set(header, value);
    }
  }
  return out;
}

export {
  proxy as GET,
  proxy as POST,
  proxy as PUT,
  proxy as PATCH,
  proxy as DELETE,
};
