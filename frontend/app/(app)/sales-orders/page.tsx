"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { api, ApiRequestError, type Paged } from "@/lib/api";
import { useI18n, statusLabel } from "@/lib/i18n";
import { hasAnyRole, useSessionUser } from "@/lib/use-user";
import { Badge, ErrorBox, Modal, money, Spinner, date } from "@/components/ui";
import { LinesEditor, type LineRow, type Option } from "@/components/lines-editor";

type SalesOrder = {
  id: number;
  number: string;
  customerId: number;
  customerName: string;
  status: string;
  orderDate: string;
  total: number;
  lines: { productName: string; quantity: number; lineTotal: number }[];
};

export default function SalesOrdersPage() {
  const { t, locale } = useI18n();
  const user = useSessionUser();
  const canWrite = hasAnyRole(user, ["ADMIN", "SALES"]);
  const queryClient = useQueryClient();
  const router = useRouter();

  const [status, setStatus] = useState("");
  const [creating, setCreating] = useState(false);
  const [detail, setDetail] = useState<SalesOrder | null>(null);
  const [error, setError] = useState<string | null>(null);

  const orders = useQuery({
    queryKey: ["sales-orders", status],
    queryFn: () => api<Paged<SalesOrder>>(`sales-orders?page=0&size=20${status ? `&status=${status}` : ""}`),
  });
  const customers = useQuery({
    queryKey: ["customers-all"],
    queryFn: () => api<Paged<Option>>("customers?size=100&active=true"),
  });
  const products = useQuery({
    queryKey: ["products-all"],
    queryFn: () => api<Paged<Option>>("products?size=100&active=true"),
  });
  const warehouses = useQuery({ queryKey: ["warehouses"], queryFn: () => api<Option[]>("warehouses") });

  const reload = () => queryClient.invalidateQueries({ queryKey: ["sales-orders"] });

  const action = useMutation({
    mutationFn: async ({ id, operation }: { id: number; operation: "confirm" | "cancel" }) =>
      api(`sales-orders/${id}/${operation}`, { method: "POST" }),
    onSuccess: reload,
    onError: (err) => setError(err instanceof ApiRequestError ? err.message : t("common.error")),
  });

  const createInvoice = useMutation({
    mutationFn: (orderId: number) => api("invoices", { method: "POST", body: { salesOrderId: orderId } }),
    onSuccess: () => router.push("/invoices"),
    onError: (err) => setError(err instanceof ApiRequestError ? err.message : t("common.error")),
  });

  return (
    <div className="space-y-4" data-tour="crud">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-xl font-bold">{t("salesOrders.title")}</h1>
        <div className="flex items-center gap-2">
          <select className="input !w-44" value={status} onChange={(event) => setStatus(event.target.value)}>
            <option value="">{t("common.status")}</option>
            {["DRAFT", "CONFIRMED", "INVOICED", "CANCELLED"].map((value) => (
              <option key={value} value={value}>{statusLabel(locale, value)}</option>
            ))}
          </select>
          {canWrite && <button className="btn-primary" onClick={() => { setCreating(true); setError(null); }} data-tour="new-button">+ {t("salesOrders.new")}</button>}
        </div>
      </div>

      {error && <ErrorBox message={error} />}

      <div className="card overflow-x-auto">
        {orders.isLoading ? <Spinner /> : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <th className="px-4 py-2.5">{t("salesOrders.number")}</th>
                <th className="px-4 py-2.5">{t("salesOrders.customer")}</th>
                <th className="px-4 py-2.5">{t("common.date")}</th>
                <th className="px-4 py-2.5">{t("common.status")}</th>
                <th className="px-4 py-2.5 text-right">{t("fields.total")}</th>
                <th className="px-4 py-2.5 text-right">{t("common.actions")}</th>
              </tr>
            </thead>
            <tbody>
              {(orders.data?.content ?? []).map((order) => (
                <tr key={order.id} className="border-b border-slate-100 hover:bg-slate-50/60">
                  <td className="px-4 py-2 font-mono text-xs">{order.number}</td>
                  <td className="px-4 py-2">{order.customerName}</td>
                  <td className="px-4 py-2">{date(order.orderDate)}</td>
                  <td className="px-4 py-2"><Badge status={order.status} /></td>
                  <td className="px-4 py-2 text-right">{money(order.total, locale)}</td>
                  <td className="px-4 py-2">
                    <div className="flex justify-end gap-1.5">
                      <button className="btn-ghost !px-2 !py-1" onClick={() => setDetail(order)}>{t("common.actions")}</button>
                      {canWrite && order.status === "DRAFT" && (
                        <>
                          <button className="btn-primary !px-2 !py-1" onClick={() => action.mutate({ id: order.id, operation: "confirm" })}>{t("salesOrders.confirm")}</button>
                          <button className="btn-danger !px-2 !py-1" onClick={() => action.mutate({ id: order.id, operation: "cancel" })}>{t("salesOrders.cancel")}</button>
                        </>
                      )}
                      {canWrite && order.status === "CONFIRMED" && (
                        <>
                          <button className="btn-primary !px-2 !py-1" onClick={() => createInvoice.mutate(order.id)}>{t("salesOrders.invoice")}</button>
                          <button className="btn-danger !px-2 !py-1" onClick={() => action.mutate({ id: order.id, operation: "cancel" })}>{t("salesOrders.cancel")}</button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {(orders.data?.content ?? []).length === 0 && (
                <tr><td colSpan={6} className="px-4 py-8 text-center text-slate-400">{t("common.empty")}</td></tr>
              )}
            </tbody>
          </table>
        )}
        <div className="border-t border-slate-100 px-4 py-2 text-xs text-slate-400">
          {t("salesOrders.reserved")}
        </div>
      </div>

      {detail && (
        <Modal title={`${detail.number} · ${detail.customerName}`} onClose={() => setDetail(null)} wide>
          <div className="space-y-3 text-sm">
            <div className="flex items-center gap-3">
              <Badge status={detail.status} />
              <span className="text-slate-500">{date(detail.orderDate)}</span>
              <span className="ml-auto font-semibold">{money(detail.total, locale)}</span>
            </div>
            <table className="w-full">
              <thead>
                <tr className="border-b border-slate-200 text-left text-xs uppercase text-slate-400">
                  <th className="py-1.5">{t("fields.product")}</th>
                  <th className="py-1.5 text-right">{t("fields.quantity")}</th>
                  <th className="py-1.5 text-right">{t("fields.lineTotal")}</th>
                </tr>
              </thead>
              <tbody>
                {detail.lines.map((line, index) => (
                  <tr key={index} className="border-b border-slate-100">
                    <td className="py-1.5">{line.productName}</td>
                    <td className="py-1.5 text-right">{line.quantity}</td>
                    <td className="py-1.5 text-right">{money(line.lineTotal, locale)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Modal>
      )}

      {creating && (
        <CreateOrderModal
          customers={customers.data?.content ?? []}
          products={products.data?.content ?? []}
          warehouses={warehouses.data ?? []}
          onClose={() => setCreating(false)}
          onCreated={() => { setCreating(false); reload(); }}
        />
      )}
    </div>
  );
}

function CreateOrderModal({
  customers,
  products,
  warehouses,
  onClose,
  onCreated,
}: {
  customers: Option[];
  products: Option[];
  warehouses: Option[];
  onClose: () => void;
  onCreated: () => void;
}) {
  const { t } = useI18n();
  const [customerId, setCustomerId] = useState("");
  const [orderDate, setOrderDate] = useState(new Date().toISOString().slice(0, 10));
  const [notes, setNotes] = useState("");
  const [lines, setLines] = useState<LineRow[]>([{ productId: "", warehouseId: "", quantity: "1", price: "" }]);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  return (
    <Modal title={t("salesOrders.new")} onClose={onClose} wide>
      <form
        className="space-y-4"
        onSubmit={async (event) => {
          event.preventDefault();
          setSaving(true);
          setError(null);
          try {
            await api("sales-orders", {
              method: "POST",
              body: {
                customerId: Number(customerId),
                orderDate,
                notes,
                lines: lines.map((line) => ({
                  productId: Number(line.productId),
                  warehouseId: Number(line.warehouseId),
                  quantity: Number(line.quantity),
                  unitPrice: line.price ? Number(line.price) : undefined,
                })),
              },
            });
            onCreated();
          } catch (err) {
            setError(err instanceof ApiRequestError ? err.message : t("common.error"));
          } finally {
            setSaving(false);
          }
        }}
      >
        <div className="grid grid-cols-3 gap-3">
          <div className="col-span-2">
            <label className="label">{t("salesOrders.customer")}</label>
            <select className="input" required value={customerId} onChange={(event) => setCustomerId(event.target.value)}>
              <option value="">--</option>
              {customers.map((customer) => <option key={customer.id} value={customer.id}>{customer.name}</option>)}
            </select>
          </div>
          <div>
            <label className="label">{t("salesOrders.orderDate")}</label>
            <input className="input" type="date" value={orderDate} onChange={(event) => setOrderDate(event.target.value)} />
          </div>
        </div>
        <LinesEditor lines={lines} products={products} warehouses={warehouses} priceMode="sale" onChange={setLines} />
        <div>
          <label className="label">{t("fields.notes")}</label>
          <input className="input" value={notes} onChange={(event) => setNotes(event.target.value)} />
        </div>
        {error && <ErrorBox message={error} />}
        <div className="flex justify-end gap-2 border-t border-slate-100 pt-3">
          <button type="button" className="btn-ghost" onClick={onClose}>{t("common.cancel")}</button>
          <button type="submit" className="btn-primary" disabled={saving}>{t("common.save")}</button>
        </div>
      </form>
    </Modal>
  );
}

export { type SalesOrder };
