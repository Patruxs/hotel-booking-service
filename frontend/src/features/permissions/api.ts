import { mockApi } from "@/mocks/mockApi";
import api from "@/lib/axios";
import { mockOrRequest } from "@/features/shared/apiClient";

export const permissionsApi: any = {
  list: () => mockOrRequest(mockApi.permissions.list(), () => api.get("/permissions")),
  create: (_body: unknown) => mockOrRequest({ ok: true }, () => api.post("/permissions", _body)),
  update: (_id: string, _body: unknown) => mockOrRequest({ ok: true }, () => api.patch(`/permissions/${_id}`, _body)),
  remove: (_id: string) => mockOrRequest({ ok: true }, () => api.delete(`/permissions/${_id}`)),
};

export const getPermissions = () => permissionsApi.list();
export const createPermission = (body: unknown) => permissionsApi.create(body);
export const updatePermission = (id: string, body: unknown) => permissionsApi.update(id, body);
export const deletePermission = (id: string) => permissionsApi.remove(id);
