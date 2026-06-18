import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

export const permissionsApi = {
  list: () => mockOrRequest(mockApi.permissions.list(), () => api.get("/permissions")),
  create: (body: unknown) => mockOrRequest({ ok: true }, () => api.post("/permissions", body)),
  update: (id: string, body: unknown) => mockOrRequest({ ok: true }, () => api.patch(`/permissions/${id}`, body)),
  remove: (id: string) => mockOrRequest({ ok: true }, () => api.delete(`/permissions/${id}`)),
};
