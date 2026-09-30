"use client";

import { useI18n } from "@/lib/i18n";

export function Badge({ status }: { status: string }) {
  const colors: Record<string, string> = {
    DRAFT: "bg-slate-100 text-slate-700",
    CONFIRMED: "bg-sky-100 text-sky-700",
    INVOICED: "bg-indigo-100 text-indigo-700",
    CANCELLED: "bg-red-100 text-red-700",
    ISSUED: "bg-amber-100 text-amber-700",
    PARTIALLY_PAID: "bg-orange-100 text-orange-700",
    PAID: "bg-emerald-100 text-emerald-700",
    APPROVED: "bg-sky-100 text-sky-700",
    RECEIVED: "bg-emerald-100 text-emerald-700",
  };
  return <span className={`badge ${colors[status] ?? "bg-slate-100 text-slate-700"}`}>{status}</span>;
}

export function Spinner() {
  const { t } = useI18n();
  return (
    <div className="flex items-center gap-2 p-6 text-sm text-slate-500">
      <span className="h-4 w-4 animate-spin rounded-full border-2 border-slate-300 border-t-sky-600" />
      {t("common.loading")}
    </div>
  );
}

export function ErrorBox({ message }: { message: string }) {
  return <div className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">{message}</div>;
}

export function Modal({ title, onClose, children, wide }: {
  title: string;
  onClose: () => void;
  children: React.ReactNode;
  wide?: boolean;
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto bg-slate-900/40 p-6">
      <div className={`card w-full ${wide ? "max-w-3xl" : "max-w-xl"} my-10`}>
        <div className="flex items-center justify-between border-b border-slate-200 px-5 py-3">
          <h3 className="text-base font-semibold">{title}</h3>
          <button className="text-slate-400 hover:text-slate-600" onClick={onClose} aria-label="close">✕</button>
        </div>
        <div className="p-5">{children}</div>
      </div>
    </div>
  );
}

export function money(value: number | string | undefined | null, locale = "en"): string {
  const number = typeof value === "string" ? Number(value) : (value ?? 0);
  return new Intl.NumberFormat(locale === "es" ? "es-ES" : "en-GB", {
    style: "currency",
    currency: "EUR",
  }).format(number);
}

export function date(value: string | undefined | null): string {
  if (!value) return "-";
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? value : parsed.toLocaleDateString();
}
