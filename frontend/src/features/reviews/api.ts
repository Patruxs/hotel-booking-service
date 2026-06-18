import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

export const reviewsApi = {
  listPublic: (hotelId: string, params?: unknown) => mockOrRequest({ data: mockApi.reviews.list(hotelId) }, () => api.get(`/hotels/${hotelId}/reviews`, { params })),
  listModeration: (hotelId: string, params?: unknown) => mockOrRequest({ data: mockApi.reviews.list(hotelId) }, () => api.get(`/hotels/${hotelId}/reviews/moderation`, { params })),
  create: (hotelId: string, body: unknown) => mockOrRequest(mockApi.reviews.list(hotelId)[0], () => api.post(`/hotels/${hotelId}/reviews`, body)),
  moderate: (hotelId: string, id: string, body: unknown) => mockOrRequest({ ok: true }, () => api.patch(`/hotels/${hotelId}/reviews/${id}/moderate`, body)),
  remove: (hotelId: string, id: string) => mockOrRequest({ ok: true }, () => api.delete(`/hotels/${hotelId}/reviews/${id}`)),
  mine: (params?: unknown) => mockOrRequest({ data: mockApi.reviews.list() }, () => api.get("/users/me/reviews", { params })),
  updateMine: (id: string, body: unknown) => mockOrRequest({ ok: true }, () => api.patch(`/reviews/${id}`, body)),
};
