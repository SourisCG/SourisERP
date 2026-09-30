"use client";

import { useQuery } from "@tanstack/react-query";
import { CrudPage } from "@/components/crud";
import { api } from "@/lib/api";
import { hasAnyRole, useSessionUser } from "@/lib/use-user";

export default function CategoriesPage() {
  const user = useSessionUser();
  const canWrite = hasAnyRole(user, ["ADMIN", "WAREHOUSE"]);

  const categories = useQuery({
    queryKey: ["categories"],
    queryFn: () => api<{ id: number; name: string }[]>("categories"),
  });
  const options = (categories.data ?? []).map((category) => ({ value: String(category.id), label: category.name }));

  return (
    <CrudPage
      endpoint="categories"
      titleKey="categories.title"
      newKey="categories.new"
      canWrite={canWrite}
      canDelete={canWrite}
      columns={[
        { key: "id", labelKey: "fields.id", className: "w-16 text-slate-400" },
        { key: "name", labelKey: "fields.name" },
        { key: "description", labelKey: "fields.description" },
      ]}
      fields={[
        { name: "name", labelKey: "fields.name", required: true },
        { name: "parentId", labelKey: "fields.category", type: "select", options },
        { name: "description", labelKey: "fields.description", type: "textarea", span: 2 },
      ]}
      toForm={(row) => ({
        name: row.name,
        parentId: row.parentId ? String(row.parentId) : "",
        description: row.description,
      })}
    />
  );
}
