import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

export const newsApi = {
  listAdmin: (params?: unknown) => mockOrRequest({ data: mockApi.news.list() }, () => api.get("/admin/news", { params })),
  getAdmin: (id: string) => mockOrRequest(mockApi.news.get(id), () => api.get(`/admin/news/id/${id}`)),
  create: (body: unknown) => mockOrRequest(mockApi.news.list()[0], () => api.post("/admin/news", body)),
  update: (id: string, body: unknown) => mockOrRequest(mockApi.news.get(id), () => api.patch(`/admin/news/${id}`, body)),
  remove: (id: string) => mockOrRequest({ ok: true }, () => api.delete(`/admin/news/${id}`)),
  listPublic: (params?: unknown) => mockOrRequest({ data: mockApi.news.list() }, () => api.get("/news", { params })),
  getPublic: (slug: string) => mockOrRequest(mockApi.news.get(slug), () => api.get(`/news/${slug}`)),
};
