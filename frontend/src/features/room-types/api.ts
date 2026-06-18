import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

export const roomTypesApi = {
  listByHotel: (hotelId: string) => mockOrRequest(mockApi.rooms.list(hotelId), () => api.get(`/hotels/${hotelId}/room-types`)),
  list: (params?: unknown) => mockOrRequest({ data: mockApi.rooms.list() }, () => api.get("/room-types", { params })),
  available: (hotelId: string, params?: unknown) => mockOrRequest(mockApi.rooms.list(hotelId), () => api.get(`/hotels/${hotelId}/room-types/available`, { params })),
  get: (hotelId: string, id: string) => mockOrRequest(mockApi.rooms.get(id), () => api.get(`/hotels/${hotelId}/room-types/${id}`)),
  create: (hotelId: string, body: unknown) => mockOrRequest(mockApi.rooms.list(hotelId)[0], () => api.post(`/hotels/${hotelId}/room-types`, body)),
  update: (hotelId: string, id: string, body: unknown) => mockOrRequest(mockApi.rooms.get(id), () => api.patch(`/hotels/${hotelId}/room-types/${id}`, body)),
  remove: (hotelId: string, id: string) => mockOrRequest({ ok: true }, () => api.delete(`/hotels/${hotelId}/room-types/${id}`)),
};
