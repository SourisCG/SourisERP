"use client";

import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { api, backendUrl } from "@/lib/api";
import { useI18n } from "@/lib/i18n";
import { money, Spinner } from "@/components/ui";

type SalesSummary = { period: string; invoiceCount: number; subtotal: number; taxTotal: number; total: number };
type Valuation = { warehouseId: number; warehouseCode: string; totalQuantity: number; totalCost: number; totalRetail: number };
type Receivable = { invoiceId: number; invoiceNumber: string; customerName: string; dueDate: string; total: number; paidAmount: number; balance: number; daysOverdue: number };

function defaultRange() {
  const now = new Date();
  const from = new Date(now.getFullYear(), now.getMonth() - 11, 1);
  return { from: from.toISOString().slice(0, 10), to: now.toISOString().slice(0, 10) };
}

export default function ReportsPage() {
  const { t, locale } = useI18n();
  const [range, setRange] = useState(defaultRange);

  const sales = useQuery({
    queryKey: ["report-sales", range.from, range.to],
    queryFn: () => api<SalesSummary[]>(`reports/sales-summary?from=${range.from}&to=${range.to}`),
  });
  const valuation = useQuery({ queryKey: ["report-valuation"], queryFn: () => api<Valuation[]>("reports/inventory-valuation") });
  const receivables = useQuery({ queryKey: ["report-receivables"], queryFn: () => api<Receivable[]>("reports/receivables") });

  const exportUrl = (report: string) => backendUrl(`reports/${report}/export?from=${range.from}&to=${range.to}`);

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold">{t("reports.title")}</h1>

      <section className="card p-4">
        <div className="mb-3 flex flex-wrap items-center justify-between gap-3">
          <h2 className="text-sm font-semibold">{t("reports.salesSummary")}</h2>
          <div className="flex items-center gap-2 text-sm">
            <input className="input !w-40" type="date" value={range.from} onChange={(event) => setRange((r) => ({ ...r, from: event.target.value }))} />
            <input className="input !w-40" type="date" value={range.to} onChange={(event) => setRange((r) => ({ ...r, to: event.target.value }))} />
            <a className="btn-ghost" href={exportUrl("sales-summary")}>{t("common.export")}</a>
          </div>
        </div>
        <div className="h-64">
          {sales.isLoading ? <Spinner /> : (
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={sales.data ?? []}>
                <defs>
                  <linearGradient id="salesGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#6366f1" stopOpacity={0.35} />
                    <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis dataKey="period" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} width={70} />
                <Tooltip formatter={(value: number) => money(value, locale)} />
                <Area type="monotone" dataKey="total" stroke="#6366f1" strokeWidth={2} fill="url(#salesGradient)" />
              </AreaChart>
            </ResponsiveContainer>
          )}
        </div>
        <table className="mt-3 w-full text-sm">
          <tbody>
            {(sales.data ?? []).map((item) => (
              <tr key={item.period} className="border-b border-slate-100">
                <td className="py-1.5 font-mono text-xs">{item.period}</td>
                <td className="py-1.5 text-right">{item.invoiceCount} {t("report.invoices").toLowerCase?.() ?? ""}</td>
                <td className="py-1.5 text-right">{money(item.subtotal, locale)}</td>
                <td className="py-1.5 text-right text-slate-500">{money(item.taxTotal, locale)}</td>
                <td className="py-1.5 text-right font-semibold">{money(item.total, locale)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="card p-4">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-sm font-semibold">{t("reports.inventoryValuation")}</h2>
          <a className="btn-ghost" href={exportUrl("inventory-valuation")}>{t("common.export")}</a>
        </div>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-200 text-left text-xs uppercase text-slate-400">
              <th className="py-1.5">{t("fields.warehouse")}</th>
              <th className="py-1.5 text-right">{t("fields.quantity")}</th>
              <th className="py-1.5 text-right">{t("invoices.paid")} (cost)</th>
              <th className="py-1.5 text-right">Retail</th>
            </tr>
          </thead>
          <tbody>
            {(valuation.data ?? []).map((item) => (
              <tr key={item.warehouseId} className="border-b border-slate-100">
                <td className="py-1.5">{item.warehouseCode}</td>
                <td className="py-1.5 text-right">{item.totalQuantity}</td>
                <td className="py-1.5 text-right font-semibold">{money(item.totalCost, locale)}</td>
                <td className="py-1.5 text-right text-slate-500">{money(item.totalRetail, locale)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="card p-4">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-sm font-semibold">{t("reports.receivables")}</h2>
          <a className="btn-ghost" href={exportUrl("receivables")}>{t("common.export")}</a>
        </div>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-200 text-left text-xs uppercase text-slate-400">
              <th className="py-1.5">{t("fields.id")}</th>
              <th className="py-1.5">{t("fields.customer")}</th>
              <th className="py-1.5">{t("fields.dueDate")}</th>
              <th className="py-1.5 text-right">{t("fields.total")}</th>
              <th className="py-1.5 text-right">{t("invoices.paid")}</th>
              <th className="py-1.5 text-right">{t("invoices.balance")}</th>
              <th className="py-1.5 text-right">{t("fields.daysOverdue")}</th>
            </tr>
          </thead>
          <tbody>
            {(receivables.data ?? []).slice(0, 25).map((item) => (
              <tr key={item.invoiceId} className="border-b border-slate-100">
                <td className="py-1.5 font-mono text-xs">{item.invoiceNumber}</td>
                <td className="py-1.5">{item.customerName}</td>
                <td className="py-1.5">{item.dueDate}</td>
                <td className="py-1.5 text-right">{money(item.total, locale)}</td>
                <td className="py-1.5 text-right text-emerald-700">{money(item.paidAmount, locale)}</td>
                <td className="py-1.5 text-right font-semibold">{money(item.balance, locale)}</td>
                <td className={`py-1.5 text-right font-semibold ${item.daysOverdue > 60 ? "text-red-600" : item.daysOverdue > 30 ? "text-amber-600" : "text-slate-500"}`}>
                  {item.daysOverdue}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
}
