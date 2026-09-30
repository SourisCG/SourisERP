"use client";

import { CrudPage } from "@/components/crud";
import { hasAnyRole, useSessionUser } from "@/lib/use-user";

export default function CustomersPage() {
  const user = useSessionUser();
  const canWrite = hasAnyRole(user, ["ADMIN", "SALES"]);

  return (
    <CrudPage
      endpoint="customers"
      paged
      titleKey="customers.title"
      newKey="customers.new"
      canWrite={canWrite}
      canDelete={canWrite}
      columns={[
        { key: "code", labelKey: "fields.code", className: "font-mono text-xs" },
        { key: "name", labelKey: "fields.name" },
        { key: "taxId", labelKey: "fields.taxId" },
        { key: "email", labelKey: "fields.email" },
        { key: "phone", labelKey: "fields.phone" },
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
