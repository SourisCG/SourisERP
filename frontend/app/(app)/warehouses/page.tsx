"use client";

import { CrudPage } from "@/components/crud";
import { hasAnyRole, useSessionUser } from "@/lib/use-user";

export default function WarehousesPage() {
  const user = useSessionUser();
  const canWrite = hasAnyRole(user, ["ADMIN", "WAREHOUSE"]);

  return (
    <CrudPage
      endpoint="warehouses"
      titleKey="warehouses.title"
      newKey="warehouses.new"
      canWrite={canWrite}
      canDelete={canWrite}
      columns={[
        { key: "code", labelKey: "fields.code", className: "font-mono text-xs" },
        { key: "name", labelKey: "fields.name" },
        { key: "address", labelKey: "fields.address" },
        { key: "active", labelKey: "fields.active", render: (row) => (row.active ? "✅" : "—") },
      ]}
      fields={[
        { name: "code", labelKey: "fields.code", required: true },
        { name: "name", labelKey: "fields.name", required: true },
        { name: "address", labelKey: "fields.address", span: 2 },
        { name: "active", labelKey: "fields.active", type: "checkbox", defaultValue: true },
      ]}
    />
  );
}
