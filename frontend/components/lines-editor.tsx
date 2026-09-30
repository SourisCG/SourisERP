"use client";

import { useI18n } from "@/lib/i18n";

export type LineRow = {
  productId: string;
  warehouseId: string;
  quantity: string;
  price: string;
};

export type Option = { id: number; name: string; sku?: string; code?: string; salePrice?: number; costPrice?: number; taxRate?: number };

export function LinesEditor({
  lines,
  products,
  warehouses,
  priceMode,
  onChange,
}: {
  lines: LineRow[];
  products: Option[];
  warehouses: Option[];
  priceMode: "sale" | "cost";
  onChange: (lines: LineRow[]) => void;
}) {
  const { t } = useI18n();

  const update = (index: number, patch: Partial<LineRow>) => {
    onChange(lines.map((line, i) => (i === index ? { ...line, ...patch } : line)));
  };

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between">
        <span className="label !mb-0">{t("salesOrders.lines")}</span>
        <button
          type="button"
          className="btn-ghost !py-1"
          onClick={() => onChange([...lines, { productId: "", warehouseId: "", quantity: "1", price: "" }])}
        >
          + {t("salesOrders.addLine")}
        </button>
      </div>
      {lines.map((line, index) => (
        <div key={index} className="grid grid-cols-12 gap-2">
          <select
            className="input col-span-5"
            required
            value={line.productId}
            onChange={(event) => {
              const product = products.find((candidate) => String(candidate.id) === event.target.value);
              const defaultPrice = priceMode === "sale" ? product?.salePrice : product?.costPrice;
              update(index, {
                productId: event.target.value,
                price: defaultPrice !== undefined ? String(defaultPrice) : line.price,
              });
            }}
          >
            <option value="">{t("fields.product")}</option>
            {products.map((product) => <option key={product.id} value={product.id}>{product.name}</option>)}
          </select>
          <select className="input col-span-3" required value={line.warehouseId} onChange={(event) => update(index, { warehouseId: event.target.value })}>
            <option value="">{t("fields.warehouse")}</option>
            {warehouses.map((warehouse) => <option key={warehouse.id} value={warehouse.id}>{warehouse.code}</option>)}
          </select>
          <input className="input col-span-2" type="number" min={1} required value={line.quantity} onChange={(event) => update(index, { quantity: event.target.value })} placeholder={t("fields.quantity")} />
          <input className="input col-span-2" type="number" step="0.01" min={0} required value={line.price} onChange={(event) => update(index, { price: event.target.value })} placeholder={priceMode === "sale" ? t("fields.unitPrice") : t("fields.unitCost")} />
        </div>
      ))}
    </div>
  );
}
