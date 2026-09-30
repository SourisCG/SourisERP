"use client";

import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { useMemo, useState } from "react";
import {
  Area, AreaChart, Bar, BarChart, CartesianGrid, Cell, Pie, PieChart,
  ResponsiveContainer, Tooltip, XAxis, YAxis,
} from "recharts";
import { api, type Paged } from "@/lib/api";
import { useI18n } from "@/lib/i18n";
import { date, money, Spinner } from "@/components/ui";

type SalesSummary = { period: string; invoiceCount: number; subtotal: number; taxTotal: number; total: number };
type Valuation = { warehouseId: number; warehouseCode: string; totalQuantity: number; totalCost: number; totalRetail: number };
type Receivable = { invoiceId: number; invoiceNumber: string; customerName: string; dueDate: string; balance: number; daysOverdue: number };
type Invoice = { id: number; number: string; customerName: string; status: string; total: number; issueDate: string; balance: number };
type InventoryRow = { id: number; sku: string; productName: string; warehouseCode: string; quantity: number; reservedQuantity: number; availableQuantity: number };

export default function DashboardPage() {
  const { t, locale } = useI18n();
  const [range] = useState(() => {
    const now = new Date();
    const from = new Date(now.getFullYear(), now.getMonth() - 11, 1);
    return { from: from.toISOString().slice(0, 10), to: now.toISOString().slice(0, 10) };
  });

  const sales = useQuery({
    queryKey: ["dash-sales", range.from, range.to],
    queryFn: () => api<SalesSummary[]>(`reports/sales-summary?from=${range.from}&to=${range.to}`),
  });
  const valuation = useQuery({
    queryKey: ["dash-valuation"],
    queryFn: () => api<Valuation[]>("reports/inventory-valuation"),
  });
  const receivables = useQuery({
    queryKey: ["dash-receivables"],
    queryFn: () => api<Receivable[]>("reports/receivables"),
  });
  const invoices = useQuery({
    queryKey: ["dash-invoices"],
    queryFn: () => api<Paged<Invoice>>("invoices?page=0&size=6"),
  });
  const lowStock = useQuery({
    queryKey: ["dash-low-stock"],
    queryFn: () => api<InventoryRow[]>("inventory?lowStockThreshold=15"),
  });

  const revenue = useMemo(() => (sales.data ?? []).reduce((sum, item) => sum + Number(item.total), 0), [sales.data]);
  const lastMonth = sales.data?.at(-1);
  const stockValue = useMemo(() => (valuation.data ?? []).reduce((sum, item) => sum + Number(item.totalCost), 0), [valuation.data]);
  const outstanding = useMemo(() => (receivables.data ?? []).reduce((sum, item) => sum + Number(item.balance), 0), [receivables.data]);

  const agingBuckets = useMemo(() => {
    const buckets = [
      { name: "0-30", value: 0, color: "#0ea5e9" },
      { name: "31-60", value: 0, color: "#f59e0b" },
      { name: "60+", value: 0, color: "#ef4444" },
    ];
    for (const item of receivables.data ?? []) {
      const days = Number(item.daysOverdue);
      if (days <= 30) buckets[0].value += Number(item.balance);
      else if (days <= 60) buckets[1].value += Number(item.balance);
      else buckets[2].value += Number(item.balance);
    }
    return buckets;
  }, [receivables.data]);

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">{t("dashboard.title")}</h1>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4" data-tour="kpi">
        <Kpi title={t("dashboard.revenue12m")} value={money(revenue, locale)} tone="text-sky-600" hint={`${sales.data?.length ?? 0} months`} />
        <Kpi title={t("dashboard.ordersMonth")} value={String(lastMonth?.invoiceCount ?? 0)} tone="text-indigo-600" hint={lastMonth?.period ?? ""} />
        <Kpi title={t("dashboard.stockValue")} value={money(stockValue, locale)} tone="text-emerald-600" hint={`${valuation.data?.length ?? 0} warehouses`} />
        <Kpi title={t("dashboard.receivables")} value={money(outstanding, locale)} tone="text-amber-600" hint={`${receivables.data?.length ?? 0} invoices`} />
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-3">
        <div className="card p-4 xl:col-span-2">
          <h2 className="mb-3 text-sm font-semibold">{t("dashboard.salesByMonth")}</h2>
          <div className="h-64">
            {sales.isLoading ? <Spinner /> : (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={sales.data ?? []}>
                  <defs>
                    <linearGradient id="revenue" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#0ea5e9" stopOpacity={0.35} />
                      <stop offset="95%" stopColor="#0ea5e9" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                  <XAxis dataKey="period" tick={{ fontSize: 11 }} />
                  <YAxis tick={{ fontSize: 11 }} width={70} />
                  <Tooltip formatter={(value: number) => money(value, locale)} />
                  <Area type="monotone" dataKey="total" stroke="#0ea5e9" strokeWidth={2} fill="url(#revenue)" />
                </AreaChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>

        <div className="card p-4">
          <h2 className="mb-3 text-sm font-semibold">{t("dashboard.receivables")} - aging</h2>
          <div className="h-64">
            {receivables.isLoading ? <Spinner /> : (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={agingBuckets} dataKey="value" nameKey="name" innerRadius={55} outerRadius={85} paddingAngle={3}>
                    {agingBuckets.map((bucket) => <Cell key={bucket.name} fill={bucket.color} />)}
                  </Pie>
                  <Tooltip formatter={(value: number) => money(value, locale)} />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-3">
        <div className="card p-4">
          <h2 className="mb-3 text-sm font-semibold">{t("dashboard.stockValue")} - {t("fields.warehouse")}</h2>
          <div className="h-56">
            {valuation.isLoading ? <Spinner /> : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={valuation.data ?? []}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                  <XAxis dataKey="warehouseCode" tick={{ fontSize: 11 }} />
                  <YAxis tick={{ fontSize: 11 }} width={70} />
                  <Tooltip formatter={(value: number) => money(value, locale)} />
                  <Bar dataKey="totalCost" fill="#0f172a" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>

        <div className="card p-4 xl:col-span-2">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-sm font-semibold">{t("dashboard.lowStock")}</h2>
            <Link className="text-xs font-semibold text-sky-600 hover:underline" href="/inventory">{t("dashboard.viewAll")} →</Link>
          </div>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 text-left text-xs uppercase text-slate-400">
                <th className="py-1.5">SKU</th>
                <th className="py-1.5">{t("fields.product")}</th>
                <th className="py-1.5">{t("fields.warehouse")}</th>
                <th className="py-1.5 text-right">{t("fields.available")}</th>
              </tr>
            </thead>
            <tbody>
              {(lowStock.data ?? []).slice(0, 6).map((row) => (
                <tr key={row.id} className="border-b border-slate-100">
                  <td className="py-1.5 font-mono text-xs">{row.sku}</td>
                  <td className="py-1.5">{row.productName}</td>
                  <td className="py-1.5">{row.warehouseCode}</td>
                  <td className={`py-1.5 text-right font-semibold ${row.availableQuantity <= 5 ? "text-red-600" : "text-amber-600"}`}>{row.availableQuantity}</td>
                </tr>
              ))}
              {(lowStock.data ?? []).length === 0 && (
                <tr><td colSpan={4} className="py-6 text-center text-slate-400">{t("common.empty")}</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card p-4">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-sm font-semibold">{t("dashboard.recentInvoices")}</h2>
          <Link className="text-xs font-semibold text-sky-600 hover:underline" href="/invoices">{t("dashboard.viewAll")} →</Link>
        </div>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-200 text-left text-xs uppercase text-slate-400">
              <th className="py-1.5">{t("fields.id")}</th>
              <th className="py-1.5">{t("fields.customer")}</th>
              <th className="py-1.5">{t("fields.issueDate")}</th>
              <th className="py-1.5">{t("common.status")}</th>
              <th className="py-1.5 text-right">{t("fields.total")}</th>
              <th className="py-1.5 text-right">{t("invoices.balance")}</th>
            </tr>
          </thead>
          <tbody>
            {(invoices.data?.content ?? []).map((invoice) => (
              <tr key={invoice.id} className="border-b border-slate-100">
                <td className="py-1.5 font-mono text-xs">{invoice.number}</td>
                <td className="py-1.5">{invoice.customerName}</td>
                <td className="py-1.5">{date(invoice.issueDate)}</td>
                <td className="py-1.5"><StatusChip status={invoice.status} /></td>
                <td className="py-1.5 text-right">{money(invoice.total, locale)}</td>
                <td className="py-1.5 text-right font-semibold">{money(invoice.balance, locale)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function Kpi({ title, value, tone, hint }: { title: string; value: string; tone: string; hint: string }) {
  return (
    <div className="card p-4">
      <div className="text-xs font-semibold uppercase tracking-wide text-slate-400">{title}</div>
      <div className={`mt-1 text-2xl font-bold ${tone}`}>{value}</div>
      <div className="mt-1 text-xs text-slate-400">{hint}</div>
    </div>
  );
}

function StatusChip({ status }: { status: string }) {
  const colors: Record<string, string> = {
    ISSUED: "bg-amber-100 text-amber-700",
    PARTIALLY_PAID: "bg-orange-100 text-orange-700",
    PAID: "bg-emerald-100 text-emerald-700",
  };
  return <span className={`badge ${colors[status] ?? "bg-slate-100 text-slate-600"}`}>{status}</span>;
}
