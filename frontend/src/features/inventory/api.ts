import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

export const inventoryApi = {
  list: (hotelId: string, params?: unknown) => mockOrRequest({ data: mockApi.inventory.list().filter((item) => item.hotelId === hotelId) }, () => api.get(`/hotels/${hotelId}/inventories`, { params })),
  bulkSet: (hotelId: string, body: unknown) => mockOrRequest({ ok: true }, () => api.post(`/hotels/${hotelId}/inventories/bulk`, body)),
  update: (hotelId: string, id: string, body: unknown) => mockOrRequest({ ok: true }, () => api.patch(`/hotels/${hotelId}/inventories/${id}`, body)),
};
