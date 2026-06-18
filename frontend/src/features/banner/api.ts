import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

export const bannerApi = {
  listPublic: () => mockOrRequest(mockApi.banners.list(), () => api.get("/banners")),
  listAdmin: () => mockOrRequest(mockApi.banners.list(), () => api.get("/admin/banners")),
  create: (body: unknown) => mockOrRequest({ ok: true }, () => api.post("/admin/banners", body)),
  update: (id: string, body: unknown) => mockOrRequest({ ok: true }, () => api.patch(`/admin/banners/${id}`, body)),
  remove: (id: string) => mockOrRequest({ ok: true }, () => api.delete(`/admin/banners/${id}`)),
};
