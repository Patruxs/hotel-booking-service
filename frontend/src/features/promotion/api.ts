import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

export const promotionApi = {
  list: (params?: unknown) => mockOrRequest({ data: mockApi.promotions.list() }, () => api.get("/promotions", { params })),
  listPublic: (params?: unknown) => mockOrRequest({ data: mockApi.promotions.list() }, () => api.get("/promotions/public", { params })),
  get: (id: string) => mockOrRequest(mockApi.promotions.get(id), () => api.get(`/promotions/${id}`)),
  create: (body: unknown) => mockOrRequest(mockApi.promotions.list()[0], () => api.post("/promotions", body)),
  update: (id: string, body: unknown) => mockOrRequest(mockApi.promotions.get(id), () => api.patch(`/promotions/${id}`, body)),
  remove: (id: string) => mockOrRequest({ ok: true }, () => api.delete(`/promotions/${id}`)),
};
