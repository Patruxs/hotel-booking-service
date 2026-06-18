import { mockApi } from "@/mocks/mockApi";
import { mockOnly } from "@/features/shared/apiClient";

export const reviewsApi: any = {
  listPublic: (hotelId: string, _params?: unknown) => mockOnly({ data: mockApi.reviews.list(hotelId) }),
  listModeration: (hotelId: string, _params?: unknown) => mockOnly({ data: mockApi.reviews.list(hotelId) }),
  create: (hotelId: string, _body: unknown) => mockOnly(mockApi.reviews.list(hotelId)[0]),
  moderate: (_hotelId: string, _id: string, _body: unknown) => mockOnly({ ok: true }),
  remove: (_hotelId: string, _id: string) => mockOnly({ ok: true }),
  mine: (_params?: unknown) => mockOnly({ data: mockApi.reviews.list() }),
  updateMine: (_id: string, _body: unknown) => mockOnly({ ok: true }),
};

export const listHotelReviews = (hotelId: string, params?: unknown) => reviewsApi.listPublic(hotelId, params);
export const listModerationReviews = (hotelId: string, params?: unknown) => reviewsApi.listModeration(hotelId, params);
export const createReview = (hotelId: string, body: unknown) => reviewsApi.create(hotelId, body);
export const moderateReview = (hotelId: string, id: string, body: unknown) => reviewsApi.moderate(hotelId, id, body);
export const deleteReview = (hotelId: string, id: string) => reviewsApi.remove(hotelId, id);
export const listMyReviews = (params?: unknown) => reviewsApi.mine(params);
export const updateReview = (id: string, body: unknown) => reviewsApi.updateMine(id, body);
