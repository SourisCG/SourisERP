"use client";

import { CrudPage } from "@/components/crud";
import { hasAnyRole, useSessionUser } from "@/lib/use-user";

export default function SuppliersPage() {
  const user = useSessionUser();
  const canWrite = hasAnyRole(user, ["ADMIN", "WAREHOUSE"]);

  return (
    <CrudPage
      endpoint="suppliers"
      paged
      titleKey="suppliers.title"
      newKey="suppliers.new"
      canWrite={canWrite}
      canDelete={canWrite}
      columns={[
        { key: "code", labelKey: "fields.code", className: "font-mono text-xs" },
        { key: "name", labelKey: "fields.name" },
        { key: "taxId", labelKey: "fields.taxId" },
        { key: "email", labelKey: "fields.email" },
        { key: "active", labelKey: "fields.active", render: (row) => (row.active ? "✅" : "—") },
      ]}
      fields={[
        { name: "code", labelKey: "fields.code", required: true },
        { name: "name", labelKey: "fields.name", required: true },
        { name: "taxId", labelKey: "fields.taxId" },
        { name: "email", labelKey: "fields.email" },
        { name: "phone", labelKey: "fields.phone" },
        { name: "active", labelKey: "fields.active", type: "checkbox", defaultValue: true },
        { name: "address", labelKey: "fields.address", type: "textarea", span: 2 },
      ]}
    />
  );
}
