"use client";

import { useEffect, useState } from "react";
import type { SessionUser } from "@/lib/session";

function readUserCookie(): SessionUser | null {
  const raw = document.cookie
    .split("; ")
    .find((cookie) => cookie.startsWith("erp_user="))
    ?.split("=")
    .slice(1)
    .join("=");
  if (!raw) return null;
  try {
    return JSON.parse(decodeURIComponent(raw)) as SessionUser;
  } catch {
    return null;
  }
}

export function useSessionUser(): SessionUser | null {
  const [user, setUser] = useState<SessionUser | null>(null);
  useEffect(() => {
    setUser(readUserCookie());
  }, []);
  return user;
}

export function hasAnyRole(user: SessionUser | null, roles: string[]): boolean {
  if (!user) return false;
  return user.roles.some((role) => roles.includes(role));
}
