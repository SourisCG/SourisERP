import { redirect } from "next/navigation";
import { AppShell } from "@/components/app-shell";
import { getSession } from "@/lib/session";

export default async function AppLayout({ children }: { children: React.ReactNode }) {
  const { token } = await getSession();
  if (!token) {
    redirect("/login");
  }
  return <AppShell>{children}</AppShell>;
}
