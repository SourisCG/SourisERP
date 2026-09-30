"use client";

import { CrudPage } from "@/components/crud";
import { date } from "@/components/ui";

export default function MovementsPage() {
  return (
    <CrudPage
      endpoint="inventory/movements"
      paged
      titleKey="inventory.movements"
      newKey="common.new"
      canWrite={false}
      canDelete={false}
      columns={[
        { key: "createdAt", labelKey: "fields.createdAt", render: (row) => date(String(row.createdAt)) },
        { key: "type", labelKey: "fields.type" },
        { key: "sku", labelKey: "fields.sku", className: "font-mono text-xs" },
        { key: "productName", labelKey: "fields.product" },
        { key: "warehouseCode", labelKey: "fields.warehouse" },
        { key: "quantity", labelKey: "fields.quantity", className: "text-right font-semibold" },
        { key: "referenceType", labelKey: "fields.reference", render: (row) => (row.referenceId ? `${row.referenceType} #${row.referenceId}` : String(row.referenceType ?? "")) },
        { key: "username", labelKey: "audit.user", render: (row) => (row.createdBy ? `#${row.createdBy}` : "-") },
      ]}
      fields={[]}
    />
  );
}
