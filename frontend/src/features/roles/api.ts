import { mockApi } from "@/mocks/mockApi";
import api from "@/lib/axios";
import { mockOrRequest } from "@/features/shared/apiClient";

export const rolesApi: any = {
  list: () => mockOrRequest(mockApi.roles.list(), () => api.get("/roles")),
  create: (_body: unknown) => mockOrRequest({ ok: true }, () => api.post("/roles", _body)),
  update: (_id: string, _body: unknown) => mockOrRequest({ ok: true }, () => api.patch(`/roles/${_id}`, _body)),
  assignPermissions: (_id: string, _body: unknown) => mockOrRequest({ ok: true }, () => api.post(`/roles/${_id}/permissions`, _body)),
  assignToUser: (_body: unknown) => mockOrRequest({ ok: true }, () => api.post("/roles/assign-to-user", _body)),
  remove: (_id: string) => mockOrRequest({ ok: true }, () => api.delete(`/roles/${_id}`)),
};

export const getRoles = () => rolesApi.list();
export const createRole = (body: unknown) => rolesApi.create(body);
export const updateRole = (id: string, body: unknown) => rolesApi.update(id, body);
export const assignPermissionsToRole = (id: string, body: unknown) => rolesApi.assignPermissions(id, body);
export const deleteRole = (id: string) => rolesApi.remove(id);
