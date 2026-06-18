import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

export const rolesApi = {
  list: () => mockOrRequest(mockApi.roles.list(), () => api.get("/roles")),
  create: (body: unknown) => mockOrRequest({ ok: true }, () => api.post("/roles", body)),
  update: (id: string, body: unknown) => mockOrRequest({ ok: true }, () => api.patch(`/roles/${id}`, body)),
  assignPermissions: (id: string, body: unknown) => mockOrRequest({ ok: true }, () => api.post(`/roles/${id}/permissions`, body)),
  assignToUser: (body: unknown) => mockOrRequest({ ok: true }, () => api.post("/roles/assign-to-user", body)),
  remove: (id: string) => mockOrRequest({ ok: true }, () => api.delete(`/roles/${id}`)),
};
