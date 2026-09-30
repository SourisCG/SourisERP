"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { api, ApiRequestError, type Paged } from "@/lib/api";
import { useI18n } from "@/lib/i18n";
import { ErrorBox, Modal, Spinner } from "@/components/ui";

export type CrudField = {
  name: string;
  labelKey: string;
  type?: "text" | "number" | "select" | "checkbox" | "textarea" | "date";
  options?: { value: string; label: string }[];
  required?: boolean;
  step?: string;
  defaultValue?: unknown;
  span?: 1 | 2;
};

export type CrudColumn = {
  key: string;
  labelKey: string;
  render?: (row: Record<string, unknown>) => React.ReactNode;
  className?: string;
};

type Row = Record<string, any>;

export function CrudPage({
  endpoint,
  titleKey,
  newKey,
  columns,
  fields,
  paged = false,
  canWrite = true,
  canDelete = true,
  searchable = true,
  rowActions,
  toForm,
  extras,
  refreshKey,
}: {
  endpoint: string;
  titleKey: string;
  newKey: string;
  columns: CrudColumn[];
  fields: CrudField[];
  paged?: boolean;
  canWrite?: boolean;
  canDelete?: boolean;
  searchable?: boolean;
  rowActions?: (row: Row, reload: () => void) => React.ReactNode;
  toForm?: (row: Row) => Record<string, unknown>;
  extras?: React.ReactNode;
  refreshKey?: string;
}) {
  const { t } = useI18n();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [editing, setEditing] = useState<Row | null>(null);
  const [creating, setCreating] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const queryKey = [endpoint, search, page, refreshKey];
  const { data, isLoading, error } = useQuery({
    queryKey,
    queryFn: () => {
      const params = new URLSearchParams();
      if (search) params.set("search", search);
      if (paged) {
        params.set("page", String(page));
        params.set("size", "15");
      }
      const qs = params.toString();
      return api<Paged<Row> | Row[]>(`${endpoint}${qs ? `?${qs}` : ""}`);
    },
  });

  const rows: Row[] = useMemo(() => (Array.isArray(data) ? data : data?.content ?? []), [data]);
  const metadata = Array.isArray(data) ? null : data?.page ?? null;
  const showActions = canWrite || canDelete || Boolean(rowActions);
  const reload = () => queryClient.invalidateQueries({ queryKey: [endpoint] });

  const save = useMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      if (editing) {
        return api(`${endpoint}/${editing.id}`, { method: "PUT", body: values });
      }
      return api(endpoint, { method: "POST", body: values });
    },
    onSuccess: () => {
      setEditing(null);
      setCreating(false);
      setFormError(null);
      reload();
    },
    onError: (err) => setFormError(err instanceof ApiRequestError ? err.message : t("common.error")),
  });

  const remove = useMutation({
    mutationFn: (row: Row) => api(`${endpoint}/${row.id}`, { method: "DELETE" }),
    onSuccess: reload,
  });

  return (
    <div className="space-y-4" data-tour="crud">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-xl font-bold">{t(titleKey)}</h1>
        <div className="flex items-center gap-2">
          {extras}
          {canWrite && (
            <button className="btn-primary" onClick={() => { setCreating(true); setFormError(null); }} data-tour="new-button">
              + {t(newKey)}
            </button>
          )}
        </div>
      </div>

      {searchable && (
        <input
          className="input max-w-sm"
          placeholder={t("common.search")}
          value={search}
          onChange={(event) => { setSearch(event.target.value); setPage(0); }}
        />
      )}

      <div className="card overflow-x-auto">
        {isLoading ? (
          <Spinner />
        ) : error ? (
          <div className="p-4"><ErrorBox message={(error as Error).message} /></div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                {columns.map((column) => (
                  <th key={column.key} className={`px-4 py-2.5 font-semibold ${column.className ?? ""}`}>{t(column.labelKey)}</th>
                ))}
                {showActions && <th className="px-4 py-2.5 text-right font-semibold">{t("common.actions")}</th>}
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 && (
                <tr><td colSpan={columns.length + (showActions ? 1 : 0)} className="px-4 py-8 text-center text-slate-400">{t("common.empty")}</td></tr>
              )}
              {rows.map((row) => (
                <tr key={row.id} className="border-b border-slate-100 hover:bg-slate-50/60">
                  {columns.map((column) => (
                    <td key={column.key} className={`px-4 py-2.5 ${column.className ?? ""}`}>
                      {column.render ? column.render(row) : String(row[column.key] ?? "")}
                    </td>
                  ))}
                  {showActions && (
                    <td className="px-4 py-2.5 text-right">
                      <div className="flex justify-end gap-1.5">
                        {rowActions?.(row, reload)}
                        {canWrite && (
                          <button className="btn-ghost !px-2 !py-1" onClick={() => { setEditing(row); setFormError(null); }}>
                            {t("common.edit")}
                          </button>
                        )}
                        {canDelete && (
                          <button
                            className="btn-danger !px-2 !py-1"
                            onClick={() => { if (window.confirm(t("common.confirmDelete"))) remove.mutate(row); }}
                          >
                            {t("common.delete")}
                          </button>
                        )}
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {metadata && metadata.totalPages > 1 && (
          <div className="flex items-center justify-between border-t border-slate-200 px-4 py-2 text-sm text-slate-600">
            <span>{t("common.page")} {metadata.number + 1} {t("common.of")} {metadata.totalPages} · {metadata.totalElements}</span>
            <div className="flex gap-2">
              <button className="btn-ghost !py-1" disabled={metadata.number === 0} onClick={() => setPage((p) => p - 1)}>{t("common.prev")}</button>
              <button className="btn-ghost !py-1" disabled={metadata.number + 1 >= metadata.totalPages} onClick={() => setPage((p) => p + 1)}>{t("common.next")}</button>
            </div>
          </div>
        )}
      </div>

      {(creating || editing) && (
        <CrudForm
          fields={fields}
          row={editing}
          toForm={toForm}
          error={formError}
          saving={save.isPending}
          onClose={() => { setCreating(false); setEditing(null); setFormError(null); }}
          onSubmit={(values) => save.mutate(values)}
        />
      )}
    </div>
  );
}

function CrudForm({
  fields,
  row,
  toForm,
  error,
  saving,
  onClose,
  onSubmit,
}: {
  fields: CrudField[];
  row: Row | null;
  toForm?: (row: Row) => Record<string, unknown>;
  error: string | null;
  saving: boolean;
  onClose: () => void;
  onSubmit: (values: Record<string, unknown>) => void;
}) {
  const { t } = useI18n();
  const initial = useMemo(() => {
    if (row) {
      return toForm ? toForm(row) : { ...row };
    }
    return Object.fromEntries(fields.map((field) => [field.name, field.defaultValue ?? (field.type === "checkbox" ? true : "")]));
  }, [row, fields, toForm]);

  const [values, setValues] = useState<Record<string, unknown>>(initial);

  const update = (name: string, value: unknown) => setValues((prev) => ({ ...prev, [name]: value }));

  return (
    <Modal title={row ? t("common.edit") : t("common.new")} onClose={onClose}>
      <form
        className="grid grid-cols-2 gap-4"
        onSubmit={(event) => {
          event.preventDefault();
          onSubmit(values);
        }}
      >
        {fields.map((field) => (
          <div key={field.name} className={field.span === 2 || field.type === "textarea" ? "col-span-2" : ""}>
            <label className="label">{t(field.labelKey)}</label>
            {field.type === "select" ? (
              <select
                className="input"
                required={field.required}
                value={String(values[field.name] ?? "")}
                onChange={(event) => update(field.name, event.target.value)}
              >
                <option value="">--</option>
                {field.options?.map((option) => (
                  <option key={option.value} value={option.value}>{option.label}</option>
                ))}
              </select>
            ) : field.type === "checkbox" ? (
              <input
                type="checkbox"
                className="mt-1 h-5 w-5 accent-sky-600"
                checked={Boolean(values[field.name])}
                onChange={(event) => update(field.name, event.target.checked)}
              />
            ) : field.type === "textarea" ? (
              <textarea
                className="input min-h-20"
                value={String(values[field.name] ?? "")}
                onChange={(event) => update(field.name, event.target.value)}
              />
            ) : (
              <input
                className="input"
                type={field.type ?? "text"}
                step={field.step}
                required={field.required}
                value={String(values[field.name] ?? "")}
                onChange={(event) => update(field.name, field.type === "number" ? event.target.value : event.target.value)}
              />
            )}
          </div>
        ))}

        {error && <div className="col-span-2"><ErrorBox message={error} /></div>}

        <div className="col-span-2 flex justify-end gap-2 border-t border-slate-100 pt-4">
          <button type="button" className="btn-ghost" onClick={onClose}>{t("common.cancel")}</button>
          <button type="submit" className="btn-primary" disabled={saving}>{t("common.save")}</button>
        </div>
      </form>
    </Modal>
  );
}
