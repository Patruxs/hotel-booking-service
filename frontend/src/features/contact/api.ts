import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

export const contactApi = {
  create: (body: unknown) => mockOrRequest({ id: "mock-contact", ok: true }, () => api.post("/contact", body)),
  listAdmin: (params?: unknown) => mockOrRequest({ data: mockApi.contacts.list() }, () => api.get("/admin/contacts", { params })),
  getAdmin: (id: string) => mockOrRequest(mockApi.contacts.get(id), () => api.get(`/admin/contacts/${id}`)),
  updateAdmin: (id: string, body: unknown) => mockOrRequest(mockApi.contacts.get(id), () => api.patch(`/admin/contacts/${id}`, body)),
};
