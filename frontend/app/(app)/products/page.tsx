"use client";

import { useQuery } from "@tanstack/react-query";
import { CrudPage } from "@/components/crud";
import { money } from "@/components/ui";
import { api } from "@/lib/api";
import { useI18n } from "@/lib/i18n";
import { hasAnyRole, useSessionUser } from "@/lib/use-user";

export default function ProductsPage() {
  const { t, locale } = useI18n();
  const user = useSessionUser();
  const canWrite = hasAnyRole(user, ["ADMIN", "WAREHOUSE"]);

  const categories = useQuery({
    queryKey: ["categories"],
    queryFn: () => api<{ id: number; name: string }[]>("categories"),
  });
  const categoryOptions = (categories.data ?? []).map((category) => ({
    value: String(category.id),
    label: category.name,
  }));
  const unitOptions = ["UNIT", "KG", "LITER", "METER", "BOX", "PACK"].map((unit) => ({ value: unit, label: unit }));

  return (
    <CrudPage
      endpoint="products"
      paged
      titleKey="products.title"
      newKey="products.new"
      canWrite={canWrite}
      canDelete={canWrite}
      columns={[
        { key: "sku", labelKey: "fields.sku", className: "font-mono text-xs" },
        { key: "name", labelKey: "fields.name" },
        { key: "categoryName", labelKey: "fields.category" },
        { key: "salePrice", labelKey: "fields.salePrice", className: "text-right", render: (row) => money(Number(row.salePrice), locale) },
        { key: "costPrice", labelKey: "fields.costPrice", className: "text-right", render: (row) => money(Number(row.costPrice), locale) },
        { key: "taxRate", labelKey: "fields.taxRate", className: "text-right", render: (row) => `${row.taxRate}%` },
        { key: "active", labelKey: "fields.active", render: (row) => (row.active ? "✅" : "—") },
      ]}
      fields={[
        { name: "sku", labelKey: "fields.sku", required: true },
        { name: "name", labelKey: "fields.name", required: true },
        { name: "categoryId", labelKey: "fields.category", type: "select", options: categoryOptions, required: true },
        { name: "unit", labelKey: "fields.unit", type: "select", options: unitOptions, required: true },
        { name: "salePrice", labelKey: "fields.salePrice", type: "number", step: "0.01", required: true },
        { name: "costPrice", labelKey: "fields.costPrice", type: "number", step: "0.01", required: true },
        { name: "taxRate", labelKey: "fields.taxRate", type: "number", step: "0.01", defaultValue: 21 },
        { name: "active", labelKey: "fields.active", type: "checkbox", defaultValue: true },
        { name: "description", labelKey: "fields.description", type: "textarea", span: 2 },
      ]}
      toForm={(row) => ({
        sku: row.sku,
        name: row.name,
        categoryId: String(row.categoryId),
        unit: row.unit,
        salePrice: row.salePrice,
        costPrice: row.costPrice,
        taxRate: row.taxRate,
        active: row.active,
        description: row.description,
      })}
    />
  );
}
