"use client";

import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { api, type Paged } from "@/lib/api";
import { useI18n } from "@/lib/i18n";
import { date, Spinner } from "@/components/ui";

type AuditEntry = {
  id: number;
  entityType: string;
  action: string;
  entityRef: string;
  username: string;
  userId: number;
  afterJson: string;
  createdAt: string;
};

export default function AuditPage() {
  const { t } = useI18n();
  const [page, setPage] = useState(0);
  const [entityType, setEntityType] = useState("");
  const [expanded, setExpanded] = useState<number | null>(null);

  const entries = useQuery({
    queryKey: ["audit", page, entityType],
    queryFn: () => api<Paged<AuditEntry>>(`audit?page=${page}&size=20${entityType ? `&entityType=${entityType}` : ""}`),
  });

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-xl font-bold">{t("audit.title")}</h1>
        <select className="input !w-48" value={entityType} onChange={(event) => { setEntityType(event.target.value); setPage(0); }}>
          <option value="">{t("audit.entity")}</option>
          {["Product", "SalesOrder", "PurchaseOrder", "Invoice"].map((value) => (
            <option key={value} value={value}>{value}</option>
          ))}
        </select>
      </div>

      <div className="card overflow-hidden">
        {entries.isLoading ? <Spinner /> : (
          <>
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                  <th className="px-4 py-2.5">{t("fields.createdAt")}</th>
                  <th className="px-4 py-2.5">{t("audit.entity")}</th>
                  <th className="px-4 py-2.5">{t("audit.action")}</th>
                  <th className="px-4 py-2.5">{t("fields.reference")}</th>
                  <th className="px-4 py-2.5">{t("audit.user")}</th>
                  <th className="px-4 py-2.5 text-right">{t("audit.snapshot")}</th>
                </tr>
              </thead>
              <tbody>
                {(entries.data?.content ?? []).map((entry) => (
                  <tr key={entry.id} className="border-b border-slate-100 align-top hover:bg-slate-50/60">
                    <td className="px-4 py-2 whitespace-nowrap">{date(entry.createdAt)}</td>
                    <td className="px-4 py-2"><span className="badge bg-slate-100 text-slate-700">{entry.entityType}</span></td>
                    <td className="px-4 py-2 font-mono text-xs">{entry.action}</td>
                    <td className="px-4 py-2 font-mono text-xs">#{entry.entityRef ?? "-"}</td>
                    <td className="px-4 py-2">{entry.username ?? "-"}</td>
                    <td className="px-4 py-2 text-right">
                      <button className="btn-ghost !px-2 !py-1" onClick={() => setExpanded(expanded === entry.id ? null : entry.id)}>
                        {expanded === entry.id ? t("common.close") : "JSON"}
                      </button>
                    </td>
                  </tr>
                ))}
                {(entries.data?.content ?? []).length === 0 && (
                  <tr><td colSpan={6} className="px-4 py-8 text-center text-slate-400">{t("common.empty")}</td></tr>
                )}
              </tbody>
            </table>
            {expanded !== null && (
              <pre className="max-h-72 overflow-auto border-t border-slate-200 bg-slate-900 p-4 text-xs text-emerald-300">
                {formatJson((entries.data?.content ?? []).find((entry) => entry.id === expanded)?.afterJson)}
              </pre>
            )}
            <div className="flex items-center justify-between border-t border-slate-200 px-4 py-2 text-sm text-slate-600">
              <span>{t("common.page")} {page + 1} {t("common.of")} {entries.data?.page.totalPages ?? 1}</span>
              <div className="flex gap-2">
                <button className="btn-ghost !py-1" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>{t("common.prev")}</button>
                <button className="btn-ghost !py-1" disabled={page + 1 >= (entries.data?.page.totalPages ?? 1)} onClick={() => setPage((p) => p + 1)}>{t("common.next")}</button>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

function formatJson(value?: string): string {
  if (!value) return "-";
  try {
    return JSON.stringify(JSON.parse(value), null, 2);
  } catch {
    return value;
  }
}
