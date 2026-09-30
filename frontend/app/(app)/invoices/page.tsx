"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { api, ApiRequestError, backendUrl, type Paged } from "@/lib/api";
import { useI18n, statusLabel } from "@/lib/i18n";
import { hasAnyRole, useSessionUser } from "@/lib/use-user";
import { Badge, ErrorBox, Modal, money, Spinner, date } from "@/components/ui";

type Invoice = {
  id: number;
  number: string;
  customerName: string;
  status: string;
  issueDate: string;
  dueDate: string;
  total: number;
  paidAmount: number;
  balance: number;
};

type Order = { id: number; number: string; customerName: string };

export default function InvoicesPage() {
  const { t, locale } = useI18n();
  const user = useSessionUser();
  const canWrite = hasAnyRole(user, ["ADMIN", "ACCOUNTANT"]);
  const queryClient = useQueryClient();

  const [status, setStatus] = useState("");
  const [issuing, setIssuing] = useState(false);
  const [paying, setPaying] = useState<Invoice | null>(null);
  const [error, setError] = useState<string | null>(null);

  const invoices = useQuery({
    queryKey: ["invoices", status],
    queryFn: () => api<Paged<Invoice>>(`invoices?page=0&size=20${status ? `&status=${status}` : ""}`),
  });
  const confirmedOrders = useQuery({
    queryKey: ["sales-orders-confirmed"],
    queryFn: () => api<Paged<Order>>("sales-orders?status=CONFIRMED&size=50"),
    enabled: issuing,
  });

  const reload = () => queryClient.invalidateQueries({ queryKey: ["invoices"] });

  const issue = useMutation({
    mutationFn: (salesOrderId: number) => api("invoices", { method: "POST", body: { salesOrderId } }),
    onSuccess: () => { setIssuing(false); reload(); },
    onError: (err) => setError(err instanceof ApiRequestError ? err.message : t("common.error")),
  });

  const pay = useMutation({
    mutationFn: ({ id, body }: { id: number; body: Record<string, unknown> }) =>
      api(`invoices/${id}/payments`, { method: "POST", body }),
    onSuccess: () => { setPaying(null); reload(); },
    onError: (err) => setError(err instanceof ApiRequestError ? err.message : t("common.error")),
  });

  return (
    <div className="space-y-4" data-tour="crud">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-xl font-bold">{t("invoices.title")}</h1>
        <div className="flex items-center gap-2">
          <select className="input !w-44" value={status} onChange={(event) => setStatus(event.target.value)}>
            <option value="">{t("common.status")}</option>
            {["ISSUED", "PARTIALLY_PAID", "PAID"].map((value) => (
              <option key={value} value={value}>{statusLabel(locale, value)}</option>
            ))}
          </select>
          {canWrite && <button className="btn-primary" onClick={() => { setIssuing(true); setError(null); }} data-tour="new-button">+ {t("invoices.new")}</button>}
        </div>
      </div>

      {error && <ErrorBox message={error} />}

      <div className="card overflow-x-auto">
        {invoices.isLoading ? <Spinner /> : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <th className="px-4 py-2.5">{t("fields.id")}</th>
                <th className="px-4 py-2.5">{t("fields.customer")}</th>
                <th className="px-4 py-2.5">{t("fields.issueDate")}</th>
                <th className="px-4 py-2.5">{t("fields.dueDate")}</th>
                <th className="px-4 py-2.5">{t("common.status")}</th>
                <th className="px-4 py-2.5 text-right">{t("fields.total")}</th>
                <th className="px-4 py-2.5 text-right">{t("invoices.paid")}</th>
                <th className="px-4 py-2.5 text-right">{t("invoices.balance")}</th>
                <th className="px-4 py-2.5 text-right">{t("common.actions")}</th>
              </tr>
            </thead>
            <tbody>
              {(invoices.data?.content ?? []).map((invoice) => (
                <tr key={invoice.id} className="border-b border-slate-100 hover:bg-slate-50/60">
                  <td className="px-4 py-2 font-mono text-xs">{invoice.number}</td>
                  <td className="px-4 py-2">{invoice.customerName}</td>
                  <td className="px-4 py-2">{date(invoice.issueDate)}</td>
                  <td className="px-4 py-2">{date(invoice.dueDate)}</td>
                  <td className="px-4 py-2"><Badge status={invoice.status} /></td>
                  <td className="px-4 py-2 text-right">{money(invoice.total, locale)}</td>
                  <td className="px-4 py-2 text-right text-emerald-700">{money(invoice.paidAmount, locale)}</td>
                  <td className="px-4 py-2 text-right font-semibold">{money(invoice.balance, locale)}</td>
                  <td className="px-4 py-2">
                    <div className="flex justify-end gap-1.5">
                      <a className="btn-ghost !px-2 !py-1" href={backendUrl(`invoices/${invoice.id}/pdf`)} target="_blank" rel="noreferrer">
                        {t("invoices.downloadPdf")}
                      </a>
                      {canWrite && invoice.balance > 0 && (
                        <button className="btn-primary !px-2 !py-1" onClick={() => setPaying(invoice)}>{t("invoices.registerPayment")}</button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {(invoices.data?.content ?? []).length === 0 && (
                <tr><td colSpan={9} className="px-4 py-8 text-center text-slate-400">{t("common.empty")}</td></tr>
              )}
            </tbody>
          </table>
        )}
      </div>

      {issuing && (
        <Modal title={t("invoices.new")} onClose={() => setIssuing(false)}>
          <div className="space-y-2">
            <p className="text-sm text-slate-500">{t("invoices.fromOrder")}</p>
            {(confirmedOrders.data?.content ?? []).map((order) => (
              <button
                key={order.id}
                className="btn-ghost w-full justify-between"
                onClick={() => issue.mutate(order.id)}
                disabled={issue.isPending}
              >
                <span className="font-mono text-xs">{order.number}</span>
                <span>{order.customerName}</span>
              </button>
            ))}
            {(confirmedOrders.data?.content ?? []).length === 0 && (
              <p className="py-4 text-center text-sm text-slate-400">{t("common.empty")}</p>
            )}
          </div>
        </Modal>
      )}

      {paying && (
        <PaymentModal
          invoice={paying}
          onClose={() => setPaying(null)}
          onSubmit={(body) => pay.mutate({ id: paying.id, body })}
          saving={pay.isPending}
        />
      )}
    </div>
  );
}

function PaymentModal({
  invoice,
  onClose,
  onSubmit,
  saving,
}: {
  invoice: Invoice;
  onClose: () => void;
  onSubmit: (body: Record<string, unknown>) => void;
  saving: boolean;
}) {
  const { t, locale } = useI18n();
  const [amount, setAmount] = useState(String(invoice.balance));
  const [method, setMethod] = useState("TRANSFER");
  const [reference, setReference] = useState("");

  return (
    <Modal title={`${t("invoices.registerPayment")} · ${invoice.number}`} onClose={onClose}>
      <form
        className="space-y-3"
        onSubmit={(event) => {
          event.preventDefault();
          onSubmit({ amount: Number(amount), method, reference });
        }}
      >
        <div className="text-sm text-slate-500">
          {t("invoices.balance")}: <span className="font-semibold">{money(invoice.balance, locale)}</span>
        </div>
        <div>
          <label className="label">{t("common.amount")}</label>
          <input className="input" type="number" step="0.01" min={0.01} max={invoice.balance} required value={amount} onChange={(event) => setAmount(event.target.value)} />
        </div>
        <div>
          <label className="label">{t("fields.method")}</label>
          <select className="input" value={method} onChange={(event) => setMethod(event.target.value)}>
            {["TRANSFER", "CARD", "CASH", "OTHER"].map((value) => <option key={value} value={value}>{value}</option>)}
          </select>
        </div>
        <div>
          <label className="label">{t("fields.reference")}</label>
          <input className="input" value={reference} onChange={(event) => setReference(event.target.value)} />
        </div>
        <div className="flex justify-end gap-2 border-t border-slate-100 pt-3">
          <button type="button" className="btn-ghost" onClick={onClose}>{t("common.cancel")}</button>
          <button type="submit" className="btn-primary" disabled={saving}>{t("common.save")}</button>
        </div>
      </form>
    </Modal>
  );
}
