import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";
import api from "@/lib/axios";

export const reviewsApi: any = {
  listPublic: (hotelId: string, params?: unknown) => mockOrRequest({ data: mockApi.reviews.list(hotelId) }, () => api.get(`/hotels/${hotelId}/reviews`, { params })),
  listModeration: (hotelId: string, params?: unknown) => mockOrRequest({ data: mockApi.reviews.list(hotelId) }, () => api.get(`/admin/hotels/${hotelId}/reviews`, { params })),
  create: (hotelId: string, body: unknown) => mockOrRequest(mockApi.reviews.list(hotelId)[0], () => api.post(`/hotels/${hotelId}/reviews`, body)),
  moderate: (hotelId: string, id: string, body: unknown) => mockOrRequest({ ok: true }, () => api.patch(`/hotels/${hotelId}/reviews/${id}/moderation`, body)),
  remove: (hotelId: string, id: string) => mockOrRequest({ ok: true }, () => api.delete(`/hotels/${hotelId}/reviews/${id}`)),
  mine: (params?: unknown) => mockOrRequest({ data: mockApi.reviews.list() }, () => api.get("/reviews/mine", { params })),
  updateMine: (id: string, body: unknown) => mockOrRequest({ ok: true }, () => api.patch(`/reviews/${id}/mine`, body)),
};

export const listHotelReviews = (hotelId: string, params?: unknown) => reviewsApi.listPublic(hotelId, params);
export const listModerationReviews = (hotelId: string, params?: unknown) => reviewsApi.listModeration(hotelId, params);
export const createReview = (hotelId: string, body: unknown) => reviewsApi.create(hotelId, body);
export const moderateReview = (hotelId: string, id: string, body: unknown) => reviewsApi.moderate(hotelId, id, body);
export const deleteReview = (hotelId: string, id: string) => reviewsApi.remove(hotelId, id);
export const listMyReviews = (params?: unknown) => reviewsApi.mine(params);
export const updateReview = (id: string, body: unknown) => reviewsApi.updateMine(id, body);
