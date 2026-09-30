import { cookies } from "next/headers";

export const TOKEN_COOKIE = "erp_token";
export const USER_COOKIE = "erp_user";

export type SessionUser = {
  id: number;
  username: string;
  fullName: string;
  email: string;
  roles: string[];
};

export async function getSession(): Promise<{ token?: string; user?: SessionUser }> {
  const store = await cookies();
  const token = store.get(TOKEN_COOKIE)?.value;
  const raw = store.get(USER_COOKIE)?.value;
  let user: SessionUser | undefined;
  if (raw) {
    try {
      user = JSON.parse(decodeURIComponent(raw)) as SessionUser;
    } catch {
      user = undefined;
    }
  }
  return { token, user };
}
