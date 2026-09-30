"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { useI18n } from "@/lib/i18n";
import { hasAnyRole, useSessionUser } from "@/lib/use-user";

type NavItem = { href: string; labelKey: string; icon: string };

const NAV_SECTIONS: { titleKey: string; items: NavItem[] }[] = [
  {
    titleKey: "nav.dashboard",
    items: [
      { href: "/", labelKey: "nav.dashboard", icon: "📊" },
      { href: "/reports", labelKey: "nav.reports", icon: "📈" },
    ],
  },
  {
    titleKey: "nav.catalog",
    items: [
      { href: "/products", labelKey: "nav.products", icon: "📦" },
      { href: "/categories", labelKey: "nav.categories", icon: "🏷️" },
    ],
  },
  {
    titleKey: "nav.inventory",
    items: [
      { href: "/inventory", labelKey: "nav.stock", icon: "🏭" },
      { href: "/warehouses", labelKey: "nav.warehouses", icon: "🏢" },
      { href: "/movements", labelKey: "nav.movements", icon: "🔁" },
    ],
  },
  {
    titleKey: "nav.sales",
    items: [
      { href: "/sales-orders", labelKey: "nav.orders", icon: "🧾" },
      { href: "/customers", labelKey: "nav.customers", icon: "🤝" },
    ],
  },
  {
    titleKey: "nav.purchasing",
    items: [
      { href: "/purchase-orders", labelKey: "nav.purchaseOrders", icon: "📥" },
      { href: "/suppliers", labelKey: "nav.suppliers", icon: "🚚" },
    ],
  },
  {
    titleKey: "nav.billing",
    items: [
      { href: "/invoices", labelKey: "nav.invoices", icon: "💶" },
      { href: "/audit", labelKey: "nav.audit", icon: "🕵️" },
    ],
  },
  {
    titleKey: "nav.admin",
    items: [
      { href: "/users", labelKey: "nav.users", icon: "👤" },
    ],
  },
];

export function AppShell({ children }: { children: React.ReactNode }) {
  const { t, locale, setLocale } = useI18n();
  const pathname = usePathname();
  const router = useRouter();
  const user = useSessionUser();
  const [tourStarted, setTourStarted] = useState(false);

  const startTour = async () => {
    const { driver } = await import("driver.js");
    await import("driver.js/dist/driver.css");
    const tour = driver({
      showProgress: true,
      nextBtnText: "_",
      prevBtnText: "_",
      steps: [
        { element: '[data-tour="nav"]', popover: { title: t("app.title"), description: t("nav.catalog") } },
        { element: '[data-tour="kpi"]', popover: { title: t("dashboard.title"), description: t("dashboard.revenue12m") } },
        { element: '[data-tour="sidebar-products"]', popover: { title: t("products.title"), description: t("products.new") } },
        { element: '[data-tour="reset-demo"]', popover: { title: t("common.resetDemo"), description: t("common.demoMode") } },
        { element: '[data-tour="language"]', popover: { title: "ES / EN", description: t("app.subtitle") } },
      ],
    });
    tour.drive();
  };

  useEffect(() => {
    if (!tourStarted && !localStorage.getItem("erp_tour_done")) {
      setTourStarted(true);
      localStorage.setItem("erp_tour_done", "1");
      const timer = setTimeout(() => { void startTour(); }, 1200);
      return () => clearTimeout(timer);
    }
  }, [tourStarted]);

  const logout = async () => {
    await fetch("/api/session", { method: "DELETE" });
    router.push("/login");
    router.refresh();
  };

  const resetDemo = async () => {
    if (!window.confirm(t("common.resetDemo") + "?")) return;
    await api("demo/reset", { method: "POST" });
    router.refresh();
    window.location.reload();
  };

  return (
    <div className="flex min-h-screen">
      <aside className="hidden w-60 shrink-0 flex-col border-r border-slate-200 bg-white lg:flex" data-tour="nav">
        <div className="flex items-center gap-2 border-b border-slate-200 px-4 py-4">
          <span className="grid h-9 w-9 place-items-center rounded-lg bg-slate-900 text-lg text-white">E</span>
          <div>
            <div className="text-sm font-bold leading-tight">{t("app.title")}</div>
            <div className="text-[11px] text-slate-500">{t("app.subtitle")}</div>
          </div>
        </div>
        <nav className="flex-1 space-y-4 overflow-y-auto p-3 text-sm">
          {NAV_SECTIONS.map((section) => (
            <div key={section.titleKey}>
              <div className="px-2 pb-1 text-[10px] font-bold uppercase tracking-widest text-slate-400">{t(section.titleKey)}</div>
              {section.items.map((item) => {
                const active = pathname === item.href;
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    data-tour={`sidebar-${item.href.replace("/", "") || "dashboard"}`}
                    className={`flex items-center gap-2 rounded-lg px-2 py-1.5 ${active ? "bg-sky-50 font-semibold text-sky-700" : "text-slate-600 hover:bg-slate-50"}`}
                  >
                    <span>{item.icon}</span>
                    {t(item.labelKey)}
                  </Link>
                );
              })}
            </div>
          ))}
        </nav>
        <div className="border-t border-slate-200 p-3 text-xs text-slate-500">
          Spring Boot 4 · Next.js · PostgreSQL
        </div>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <div className="flex items-center justify-between gap-3 bg-amber-100 px-4 py-1.5 text-xs font-medium text-amber-900">
          <span>🧪 {t("common.demoMode")}</span>
          {hasAnyRole(user, ["ADMIN"]) && (
            <button className="btn-ghost !border-amber-300 !bg-amber-50 !px-2 !py-0.5 !text-[11px]" onClick={resetDemo} data-tour="reset-demo">
              {t("common.resetDemo")}
            </button>
          )}
        </div>

        <header className="flex items-center justify-between border-b border-slate-200 bg-white px-4 py-3">
          <div className="text-sm font-semibold text-slate-700">{t("app.title")}</div>
          <div className="flex items-center gap-2 text-sm">
            <button className="btn-ghost !py-1" onClick={startTour}>{t("common.tour")}</button>
            <div className="flex overflow-hidden rounded-lg border border-slate-300" data-tour="language">
              <button className={`px-2 py-1 text-xs ${locale === "en" ? "bg-slate-900 text-white" : "bg-white text-slate-600"}`} onClick={() => setLocale("en")}>EN</button>
              <button className={`px-2 py-1 text-xs ${locale === "es" ? "bg-slate-900 text-white" : "bg-white text-slate-600"}`} onClick={() => setLocale("es")}>ES</button>
            </div>
            {user && (
              <span className="hidden items-center gap-1.5 rounded-full bg-slate-100 px-3 py-1 text-xs text-slate-600 md:flex">
                👤 {user.fullName} · {user.roles.join(", ")}
              </span>
            )}
            <button className="btn-ghost !py-1" onClick={logout}>{t("common.logout")}</button>
          </div>
        </header>

        <main className="flex-1 p-4 lg:p-6">{children}</main>
      </div>
    </div>
  );
}
