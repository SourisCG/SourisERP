"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { api, ApiRequestError, type Paged } from "@/lib/api";
import { useI18n } from "@/lib/i18n";
import { hasAnyRole, useSessionUser } from "@/lib/use-user";
import { ErrorBox, Modal, Spinner, date } from "@/components/ui";

type User = {
  id: number;
  username: string;
  fullName: string;
  email: string;
  roles: string[];
  enabled: boolean;
  createdAt: string;
};

type Role = { name: string; description: string };

export default function UsersPage() {
  const { t } = useI18n();
  const user = useSessionUser();
  const canWrite = hasAnyRole(user, ["ADMIN"]);
  const queryClient = useQueryClient();
  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<User | null>(null);
  const [error, setError] = useState<string | null>(null);

  const users = useQuery({ queryKey: ["users"], queryFn: () => api<Paged<User>>("users?page=0&size=50") });
  const roles = useQuery({ queryKey: ["roles"], queryFn: () => api<Role[]>("roles") });

  const reload = () => queryClient.invalidateQueries({ queryKey: ["users"] });

  const save = useMutation({
    mutationFn: async ({ id, body }: { id?: number; body: Record<string, unknown> }) =>
      id ? api(`users/${id}`, { method: "PUT", body }) : api("users", { method: "POST", body }),
    onSuccess: () => { setCreating(false); setEditing(null); reload(); },
    onError: (err) => setError(err instanceof ApiRequestError ? err.message : t("common.error")),
  });

  const assignRoles = useMutation({
    mutationFn: ({ id, roles: nextRoles }: { id: number; roles: string[] }) =>
      api(`users/${id}/roles`, { method: "PUT", body: { roles: nextRoles } }),
    onSuccess: () => { setEditing(null); reload(); },
    onError: (err) => setError(err instanceof ApiRequestError ? err.message : t("common.error")),
  });

  const disable = useMutation({
    mutationFn: (id: number) => api(`users/${id}`, { method: "DELETE" }),
    onSuccess: reload,
  });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">{t("users.title")}</h1>
        {canWrite && <button className="btn-primary" onClick={() => { setCreating(true); setError(null); }}>+ {t("users.new")}</button>}
      </div>

      {error && <ErrorBox message={error} />}

      <div className="card overflow-x-auto">
        {users.isLoading ? <Spinner /> : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <th className="px-4 py-2.5">{t("fields.username")}</th>
                <th className="px-4 py-2.5">{t("fields.name")}</th>
                <th className="px-4 py-2.5">{t("fields.email")}</th>
                <th className="px-4 py-2.5">{t("users.roles")}</th>
                <th className="px-4 py-2.5">{t("fields.active")}</th>
                <th className="px-4 py-2.5">{t("fields.createdAt")}</th>
                {canWrite && <th className="px-4 py-2.5 text-right">{t("common.actions")}</th>}
              </tr>
            </thead>
            <tbody>
              {(users.data?.content ?? []).map((row) => (
                <tr key={row.id} className="border-b border-slate-100 hover:bg-slate-50/60">
                  <td className="px-4 py-2 font-mono text-xs">{row.username}</td>
                  <td className="px-4 py-2">{row.fullName}</td>
                  <td className="px-4 py-2">{row.email}</td>
                  <td className="px-4 py-2">
                    <div className="flex gap-1">
                      {row.roles.map((role) => <span key={role} className="badge bg-indigo-100 text-indigo-700">{role}</span>)}
                    </div>
                  </td>
                  <td className="px-4 py-2">{row.enabled ? "✅" : "—"}</td>
                  <td className="px-4 py-2">{date(row.createdAt)}</td>
                  {canWrite && (
                    <td className="px-4 py-2">
                      <div className="flex justify-end gap-1.5">
                        <button className="btn-ghost !px-2 !py-1" onClick={() => { setEditing(row); setError(null); }}>{t("common.edit")}</button>
                        <button className="btn-danger !px-2 !py-1" onClick={() => disable.mutate(row.id)}>{t("common.delete")}</button>
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {creating && (
        <Modal title={t("users.new")} onClose={() => setCreating(false)}>
          <UserForm
            roleOptions={roles.data ?? []}
            onClose={() => setCreating(false)}
            onSubmit={(values) => save.mutate({ body: values })}
            saving={save.isPending}
          />
        </Modal>
      )}

      {editing && (
        <Modal title={`${t("common.edit")} · ${editing.username}`} onClose={() => setEditing(null)}>
          <UserEditForm
            user={editing}
            roleOptions={roles.data ?? []}
            onClose={() => setEditing(null)}
            onSaveProfile={(values) => save.mutate({ id: editing.id, body: values })}
            onSaveRoles={(nextRoles) => assignRoles.mutate({ id: editing.id, roles: nextRoles })}
            saving={save.isPending || assignRoles.isPending}
          />
        </Modal>
      )}
    </div>
  );
}

function UserForm({
  roleOptions,
  onClose,
  onSubmit,
  saving,
}: {
  roleOptions: Role[];
  onClose: () => void;
  onSubmit: (values: Record<string, unknown>) => void;
  saving: boolean;
}) {
  const { t } = useI18n();
  const [values, setValues] = useState<Record<string, string>>({
    username: "",
    email: "",
    firstName: "",
    lastName: "",
    password: "",
    role: "VIEWER",
  });

  const set = (name: string, value: string) => setValues((prev) => ({ ...prev, [name]: value }));

  return (
    <form
      className="grid grid-cols-2 gap-3"
      onSubmit={(event) => {
        event.preventDefault();
        onSubmit({
          username: values.username,
          email: values.email,
          firstName: values.firstName,
          lastName: values.lastName,
          password: values.password,
          roles: [values.role],
          enabled: true,
        });
      }}
    >
      <div><label className="label">{t("fields.username")}</label><input className="input" required value={values.username} onChange={(event) => set("username", event.target.value)} /></div>
      <div><label className="label">{t("fields.email")}</label><input className="input" type="email" required value={values.email} onChange={(event) => set("email", event.target.value)} /></div>
      <div><label className="label">{t("fields.firstName")}</label><input className="input" required value={values.firstName} onChange={(event) => set("firstName", event.target.value)} /></div>
      <div><label className="label">{t("fields.lastName")}</label><input className="input" required value={values.lastName} onChange={(event) => set("lastName", event.target.value)} /></div>
      <div><label className="label">{t("fields.password")}</label><input className="input" type="password" minLength={8} required value={values.password} onChange={(event) => set("password", event.target.value)} /></div>
      <div>
        <label className="label">{t("users.roles")}</label>
        <select className="input" value={values.role} onChange={(event) => set("role", event.target.value)}>
          {roleOptions.map((role) => <option key={role.name} value={role.name}>{role.name}</option>)}
        </select>
      </div>
      <div className="col-span-2 flex justify-end gap-2 border-t border-slate-100 pt-3">
        <button type="button" className="btn-ghost" onClick={onClose}>{t("common.cancel")}</button>
        <button type="submit" className="btn-primary" disabled={saving}>{t("common.save")}</button>
      </div>
    </form>
  );
}

function UserEditForm({
  user,
  roleOptions,
  onClose,
  onSaveProfile,
  onSaveRoles,
  saving,
}: {
  user: User;
  roleOptions: Role[];
  onClose: () => void;
  onSaveProfile: (values: Record<string, unknown>) => void;
  onSaveRoles: (roles: string[]) => void;
  saving: boolean;
}) {
  const { t } = useI18n();
  const [email, setEmail] = useState(user.email);
  const [firstName, setFirstName] = useState(user.fullName.split(" ")[0] ?? "");
  const [lastName, setLastName] = useState(user.fullName.split(" ").slice(1).join(" "));
  const [enabled, setEnabled] = useState(user.enabled);
  const [selected, setSelected] = useState<string[]>(user.roles);

  const toggleRole = (role: string) => {
    setSelected((prev) => (prev.includes(role) ? prev.filter((item) => item !== role) : [...prev, role]));
  };

  return (
    <div className="space-y-4">
      <form
        className="grid grid-cols-2 gap-3"
        onSubmit={(event) => {
          event.preventDefault();
          onSaveProfile({ email, firstName, lastName, enabled });
        }}
      >
        <div><label className="label">{t("fields.email")}</label><input className="input" type="email" required value={email} onChange={(event) => setEmail(event.target.value)} /></div>
        <div className="flex items-end gap-2 pb-0.5">
          <label className="flex items-center gap-2 text-sm"><input type="checkbox" className="h-4 w-4 accent-sky-600" checked={enabled} onChange={(event) => setEnabled(event.target.checked)} /> {t("fields.active")}</label>
        </div>
        <div><label className="label">{t("fields.firstName")}</label><input className="input" required value={firstName} onChange={(event) => setFirstName(event.target.value)} /></div>
        <div><label className="label">{t("fields.lastName")}</label><input className="input" required value={lastName} onChange={(event) => setLastName(event.target.value)} /></div>
        <div className="col-span-2 flex justify-end gap-2 border-t border-slate-100 pt-3">
          <button type="button" className="btn-ghost" onClick={onClose}>{t("common.cancel")}</button>
          <button type="submit" className="btn-primary" disabled={saving}>{t("common.save")}</button>
        </div>
      </form>

      <div className="border-t border-slate-100 pt-3">
        <div className="label">{t("users.roles")}</div>
        <div className="flex flex-wrap gap-2">
          {roleOptions.map((role) => (
            <label key={role.name} className={`flex cursor-pointer items-center gap-1.5 rounded-lg border px-2.5 py-1 text-xs ${selected.includes(role.name) ? "border-sky-500 bg-sky-50 text-sky-700" : "border-slate-200 text-slate-600"}`}>
              <input type="checkbox" className="hidden" checked={selected.includes(role.name)} onChange={() => toggleRole(role.name)} />
              {role.name}
            </label>
          ))}
        </div>
        <div className="mt-2 flex justify-end">
          <button className="btn-primary !py-1" disabled={saving || selected.length === 0} onClick={() => onSaveRoles(selected)}>
            {t("common.save")}
          </button>
        </div>
      </div>
    </div>
  );
}
