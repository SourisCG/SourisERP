"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { useI18n } from "@/lib/i18n";

const DEMO_ROLES = [
  { key: "login.admin", username: "admin", password: "admin123", icon: "🛡️" },
  { key: "login.sales", username: "sales", password: "sales123", icon: "💼" },
  { key: "login.warehouse", username: "warehouse", password: "warehouse123", icon: "📦" },
  { key: "login.accountant", username: "accountant", password: "accountant123", icon: "💶" },
  { key: "login.viewer", username: "viewer", password: "viewer123", icon: "👀" },
];

export default function LoginPage() {
  const { t, locale, setLocale } = useI18n();
  const router = useRouter();
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("admin123");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const doLogin = async (user: string, pass: string) => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch("/api/session", {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({ username: user, password: pass }),
      });
      if (!response.ok) {
        setError(t("login.error"));
        return;
      }
      router.push("/");
      router.refresh();
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="grid min-h-screen place-items-center bg-gradient-to-br from-slate-900 via-slate-800 to-sky-900 p-6">
      <div className="w-full max-w-md space-y-4">
        <div className="flex items-center justify-between text-white">
          <div>
            <h1 className="text-2xl font-bold">ERP Core</h1>
            <p className="text-sm text-slate-300">{t("login.subtitle")}</p>
          </div>
          <div className="flex overflow-hidden rounded-lg border border-white/20">
            <button className={`px-2 py-1 text-xs ${locale === "en" ? "bg-white text-slate-900" : "text-white"}`} onClick={() => setLocale("en")}>EN</button>
            <button className={`px-2 py-1 text-xs ${locale === "es" ? "bg-white text-slate-900" : "text-white"}`} onClick={() => setLocale("es")}>ES</button>
          </div>
        </div>

        <div className="card p-6">
          <h2 className="mb-4 text-lg font-semibold">{t("login.title")}</h2>
          <form
            className="space-y-3"
            onSubmit={(event) => {
              event.preventDefault();
              void doLogin(username, password);
            }}
          >
            <div>
              <label className="label">{t("login.username")}</label>
              <input className="input" value={username} onChange={(event) => setUsername(event.target.value)} autoComplete="username" />
            </div>
            <div>
              <label className="label">{t("login.password")}</label>
              <input className="input" type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="current-password" />
            </div>
            {error && <div className="rounded-lg bg-red-50 p-2 text-xs text-red-700">{error}</div>}
            <button className="btn-primary w-full justify-center" type="submit" disabled={loading}>
              {t("login.submit")}
            </button>
          </form>

          <div className="mt-6 border-t border-slate-100 pt-4">
            <div className="label">{t("login.quick")}</div>
            <div className="grid grid-cols-2 gap-2">
              {DEMO_ROLES.map((role) => (
                <button
                  key={role.username}
                  className="btn-ghost justify-start"
                  disabled={loading}
                  onClick={() => void doLogin(role.username, role.password)}
                >
                  <span>{role.icon}</span> {t(role.key)}
                </button>
              ))}
            </div>
          </div>
        </div>

        <p className="text-center text-xs text-slate-400">
          Spring Boot 4 · PostgreSQL 16 · Next.js · Testcontainers · Hexagonal
        </p>
      </div>
    </div>
  );
}
