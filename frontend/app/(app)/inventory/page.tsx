"use client";

import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { api, ApiRequestError } from "@/lib/api";
import { useI18n } from "@/lib/i18n";
import { hasAnyRole, useSessionUser } from "@/lib/use-user";
import { ErrorBox, Modal, Spinner } from "@/components/ui";

type InventoryRow = {
  id: number;
  productId: number;
  sku: string;
  productName: string;
  warehouseId: number;
  warehouseCode: string;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  updatedAt: string;
};

type Option = { id: number; name?: string; code?: string; salePrice?: number; taxRate?: number };

export default function InventoryPage() {
  const { t, locale } = useI18n();
  const user = useSessionUser();
  const canWrite = hasAnyRole(user, ["ADMIN", "WAREHOUSE"]);
  const queryClient = useQueryClient();

  const [lowStock, setLowStock] = useState(false);
  const [adjusting, setAdjusting] = useState<InventoryRow | null>(null);
  const [adding, setAdding] = useState(false);

  const inventory = useQuery({
    queryKey: ["inventory", lowStock],
    queryFn: () => api<InventoryRow[]>(`inventory${lowStock ? "?lowStockThreshold=15" : ""}`),
  });
  const warehouses = useQuery({ queryKey: ["warehouses"], queryFn: () => api<Option[]>("warehouses") });
  const products = useQuery({ queryKey: ["products-all"], queryFn: () => api<{ content: Option[] }>("products?size=100&active=true") });

  const reload = () => queryClient.invalidateQueries({ queryKey: ["inventory"] });

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-xl font-bold">{t("inventory.title")}</h1>
        <label className="flex items-center gap-2 text-sm text-slate-600">
          <input type="checkbox" className="h-4 w-4 accent-sky-600" checked={lowStock} onChange={(event) => setLowStock(event.target.checked)} />
          {t("inventory.lowOnly")}
        </label>
        {canWrite && (
          <button className="btn-primary" onClick={() => setAdding(true)}>+ {t("inventory.adjust")}</button>
        )}
      </div>

      <div className="card overflow-x-auto">
        {inventory.isLoading ? <Spinner /> : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <th className="px-4 py-2.5">SKU</th>
                <th className="px-4 py-2.5">{t("fields.product")}</th>
                <th className="px-4 py-2.5">{t("fields.warehouse")}</th>
                <th className="px-4 py-2.5 text-right">{t("fields.quantity")}</th>
                <th className="px-4 py-2.5 text-right">{t("fields.reserved")}</th>
                <th className="px-4 py-2.5 text-right">{t("fields.available")}</th>
                {canWrite && <th className="px-4 py-2.5 text-right">{t("common.actions")}</th>}
              </tr>
            </thead>
            <tbody>
              {(inventory.data ?? []).map((row) => (
                <tr key={row.id} className="border-b border-slate-100 hover:bg-slate-50/60">
                  <td className="px-4 py-2 font-mono text-xs">{row.sku}</td>
                  <td className="px-4 py-2">{row.productName}</td>
                  <td className="px-4 py-2">{row.warehouseCode}</td>
                  <td className="px-4 py-2 text-right">{row.quantity}</td>
                  <td className="px-4 py-2 text-right text-indigo-600">{row.reservedQuantity}</td>
                  <td className={`px-4 py-2 text-right font-semibold ${row.availableQuantity <= 5 ? "text-red-600" : row.availableQuantity <= 15 ? "text-amber-600" : "text-emerald-600"}`}>
                    {row.availableQuantity}
                  </td>
                  {canWrite && (
                    <td className="px-4 py-2 text-right">
                      <button className="btn-ghost !px-2 !py-1" onClick={() => setAdjusting(row)}>
                        {t("inventory.adjust")}
                      </button>
                    </td>
                  )}
                </tr>
              ))}
              {(inventory.data ?? []).length === 0 && (
                <tr><td colSpan={7} className="px-4 py-8 text-center text-slate-400">{t("common.empty")}</td></tr>
              )}
            </tbody>
          </table>
        )}
      </div>

      {adding && (
        <StockModal
          title={t("inventory.adjust")}
          warehouses={warehouses.data ?? []}
          products={products.data?.content ?? []}
          onClose={() => setAdding(false)}
          onSubmit={async (values) => {
            await api("inventory/adjustments", { method: "POST", body: values });
            setAdding(false);
            reload();
          }}
        />
      )}

      {adjusting && (
        <StockModal
          title={`${t("inventory.adjust")} · ${adjusting.sku}`}
          warehouses={warehouses.data ?? []}
          products={products.data?.content ?? []}
          fixedProduct={{ id: adjusting.productId, name: adjusting.productName }}
          fixedWarehouse={{ id: adjusting.warehouseId, code: adjusting.warehouseCode }}
          initialQuantity={adjusting.quantity}
          onClose={() => setAdjusting(null)}
          onSubmit={async (values) => {
            await api("inventory/adjustments", { method: "POST", body: values });
            setAdjusting(null);
            reload();
          }}
        />
      )}
    </div>
  );
}

function StockModal({
  title,
  warehouses,
  products,
  fixedProduct,
  fixedWarehouse,
  initialQuantity,
  onClose,
  onSubmit,
}: {
  title: string;
  warehouses: Option[];
  products: Option[];
  fixedProduct?: { id: number; name: string };
  fixedWarehouse?: { id: number; code: string };
  initialQuantity?: number;
  onClose: () => void;
  onSubmit: (values: Record<string, unknown>) => Promise<void>;
}) {
  const { t } = useI18n();
  const [productId, setProductId] = useState(String(fixedProduct?.id ?? ""));
  const [warehouseId, setWarehouseId] = useState(String(fixedWarehouse?.id ?? ""));
  const [quantity, setQuantity] = useState(String(initialQuantity ?? 0));
  const [reason, setReason] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  return (
    <Modal title={title} onClose={onClose}>
      <form
        className="space-y-3"
        onSubmit={async (event) => {
          event.preventDefault();
          setSaving(true);
          setError(null);
          try {
            await onSubmit({
              productId: Number(productId),
              warehouseId: Number(warehouseId),
              newQuantity: Number(quantity),
              reason: reason || "Manual adjustment",
            });
          } catch (err) {
            setError(err instanceof ApiRequestError ? err.message : t("common.error"));
          } finally {
            setSaving(false);
          }
        }}
      >
        {fixedProduct ? (
          <div className="text-sm text-slate-600">{fixedProduct.name}</div>
        ) : (
          <div>
            <label className="label">{t("fields.product")}</label>
            <select className="input" required value={productId} onChange={(event) => setProductId(event.target.value)}>
              <option value="">--</option>
              {products.map((product) => <option key={product.id} value={product.id}>{product.name}</option>)}
            </select>
          </div>
        )}
        {fixedWarehouse ? (
          <div className="text-sm text-slate-600">{fixedWarehouse.code}</div>
        ) : (
          <div>
            <label className="label">{t("fields.warehouse")}</label>
            <select className="input" required value={warehouseId} onChange={(event) => setWarehouseId(event.target.value)}>
              <option value="">--</option>
              {warehouses.map((warehouse) => <option key={warehouse.id} value={warehouse.id}>{warehouse.code}</option>)}
            </select>
          </div>
        )}
        <div>
          <label className="label">{t("fields.newQuantity")}</label>
          <input className="input" type="number" min={0} required value={quantity} onChange={(event) => setQuantity(event.target.value)} />
        </div>
        <div>
          <label className="label">{t("fields.reason")}</label>
          <input className="input" value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Cycle count / breakage / opening stock" />
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
