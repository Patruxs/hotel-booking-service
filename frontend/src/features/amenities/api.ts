import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

export const amenitiesApi: any = {
  list: () => mockOrRequest(mockApi.amenities.list(), () => api.get("/amenities/all")),
  get: (id: string) => mockOrRequest({ id, name: mockApi.amenities.list()[0] }, () => api.get(`/amenities/${id}`)),
  create: (body: unknown) => mockOrRequest({ ok: true }, () => api.post("/amenities/create", body)),
  update: (id: string, body: unknown) => mockOrRequest({ ok: true }, () => api.put(`/amenities/update/${id}`, body)),
};

export const getAmenities = async (_params?: unknown) => ({ data: await amenitiesApi.list(), meta: { limit: 10, offset: 0, total: (await amenitiesApi.list()).length } });
export const getAmentityById = (id: string) => amenitiesApi.get(id);
export const createAmentity = (body: unknown) => amenitiesApi.create(body);
export const updateAmentity = (id: string, body: unknown) => amenitiesApi.update(id, body);
