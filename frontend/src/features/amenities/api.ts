import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

export const amenitiesApi = {
  list: () => mockOrRequest(mockApi.amenities.list(), () => api.get("/amenities/all")),
  get: (id: string) => mockOrRequest({ id, name: mockApi.amenities.list()[0] }, () => api.get(`/amenities/${id}`)),
  create: (body: unknown) => mockOrRequest({ ok: true }, () => api.post("/amenities/create", body)),
  update: (id: string, body: unknown) => mockOrRequest({ ok: true }, () => api.put(`/amenities/update/${id}`, body)),
};
