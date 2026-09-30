"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { api, ApiRequestError, type Paged } from "@/lib/api";
import { useI18n, statusLabel } from "@/lib/i18n";
import { hasAnyRole, useSessionUser } from "@/lib/use-user";
import { Badge, ErrorBox, Modal, money, Spinner, date } from "@/components/ui";
import { LinesEditor, type LineRow, type Option } from "@/components/lines-editor";

type PurchaseOrder = {
  id: number;
  number: string;
  supplierName: string;
  status: string;
  orderDate: string;
  subtotal: number;
};

export default function PurchaseOrdersPage() {
  const { t, locale } = useI18n();
  const user = useSessionUser();
  const canWrite = hasAnyRole(user, ["ADMIN", "WAREHOUSE"]);
  const queryClient = useQueryClient();

  const [status, setStatus] = useState("");
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const orders = useQuery({
    queryKey: ["purchase-orders", status],
    queryFn: () => api<Paged<PurchaseOrder>>(`purchase-orders?page=0&size=20${status ? `&status=${status}` : ""}`),
  });
  const suppliers = useQuery({ queryKey: ["suppliers-all"], queryFn: () => api<Paged<Option>>("suppliers?size=100&active=true") });
  const products = useQuery({ queryKey: ["products-all"], queryFn: () => api<Paged<Option>>("products?size=100&active=true") });
  const warehouses = useQuery({ queryKey: ["warehouses"], queryFn: () => api<Option[]>("warehouses") });

  const reload = () => queryClient.invalidateQueries({ queryKey: ["purchase-orders"] });

  const action = useMutation({
    mutationFn: async ({ id, operation }: { id: number; operation: "approve" | "receive" | "cancel" }) =>
      api(`purchase-orders/${id}/${operation}`, { method: "POST" }),
    onSuccess: reload,
    onError: (err) => setError(err instanceof ApiRequestError ? err.message : t("common.error")),
  });

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-xl font-bold">{t("purchaseOrders.title")}</h1>
        <div className="flex items-center gap-2">
          <select className="input !w-44" value={status} onChange={(event) => setStatus(event.target.value)}>
            <option value="">{t("common.status")}</option>
            {["DRAFT", "APPROVED", "RECEIVED", "CANCELLED"].map((value) => (
              <option key={value} value={value}>{statusLabel(locale, value)}</option>
            ))}
          </select>
          {canWrite && <button className="btn-primary" onClick={() => { setCreating(true); setError(null); }}>+ {t("purchaseOrders.new")}</button>}
        </div>
      </div>

      {error && <ErrorBox message={error} />}

      <div className="card overflow-x-auto">
        {orders.isLoading ? <Spinner /> : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <th className="px-4 py-2.5">{t("fields.reference")}</th>
                <th className="px-4 py-2.5">{t("purchaseOrders.supplier")}</th>
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
                  <td className="px-4 py-2">{order.supplierName}</td>
                  <td className="px-4 py-2">{date(order.orderDate)}</td>
                  <td className="px-4 py-2"><Badge status={order.status} /></td>
                  <td className="px-4 py-2 text-right">{money(order.subtotal, locale)}</td>
                  <td className="px-4 py-2">
                    <div className="flex justify-end gap-1.5">
                      {canWrite && order.status === "DRAFT" && (
                        <button className="btn-primary !px-2 !py-1" onClick={() => action.mutate({ id: order.id, operation: "approve" })}>{t("purchaseOrders.approve")}</button>
                      )}
                      {canWrite && order.status === "APPROVED" && (
                        <button className="btn-primary !px-2 !py-1" onClick={() => action.mutate({ id: order.id, operation: "receive" })}>{t("purchaseOrders.receive")}</button>
                      )}
                      {canWrite && order.status !== "RECEIVED" && order.status !== "CANCELLED" && (
                        <button className="btn-danger !px-2 !py-1" onClick={() => action.mutate({ id: order.id, operation: "cancel" })}>{t("salesOrders.cancel")}</button>
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
      </div>

      {creating && (
        <Modal title={t("purchaseOrders.new")} onClose={() => setCreating(false)} wide>
          <PurchaseOrderForm
            suppliers={suppliers.data?.content ?? []}
            products={products.data?.content ?? []}
            warehouses={warehouses.data ?? []}
            onClose={() => setCreating(false)}
            onCreated={() => { setCreating(false); reload(); }}
          />
        </Modal>
      )}
    </div>
  );
}

function PurchaseOrderForm({
  suppliers,
  products,
  warehouses,
  onClose,
  onCreated,
}: {
  suppliers: Option[];
  products: Option[];
  warehouses: Option[];
  onClose: () => void;
  onCreated: () => void;
}) {
  const { t } = useI18n();
  const [supplierId, setSupplierId] = useState("");
  const [orderDate, setOrderDate] = useState(new Date().toISOString().slice(0, 10));
  const [notes, setNotes] = useState("");
  const [lines, setLines] = useState<LineRow[]>([{ productId: "", warehouseId: "", quantity: "1", price: "" }]);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  return (
    <form
      className="space-y-4"
      onSubmit={async (event) => {
        event.preventDefault();
        setSaving(true);
        setError(null);
        try {
          await api("purchase-orders", {
            method: "POST",
            body: {
              supplierId: Number(supplierId),
              orderDate,
              notes,
              lines: lines.map((line) => ({
                productId: Number(line.productId),
                warehouseId: Number(line.warehouseId),
                quantity: Number(line.quantity),
                unitCost: Number(line.price),
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
          <label className="label">{t("purchaseOrders.supplier")}</label>
          <select className="input" required value={supplierId} onChange={(event) => setSupplierId(event.target.value)}>
            <option value="">--</option>
            {suppliers.map((supplier) => <option key={supplier.id} value={supplier.id}>{supplier.name}</option>)}
          </select>
        </div>
        <div>
          <label className="label">{t("salesOrders.orderDate")}</label>
          <input className="input" type="date" value={orderDate} onChange={(event) => setOrderDate(event.target.value)} />
        </div>
      </div>
      <LinesEditor lines={lines} products={products} warehouses={warehouses} priceMode="cost" onChange={setLines} />
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
  );
}
