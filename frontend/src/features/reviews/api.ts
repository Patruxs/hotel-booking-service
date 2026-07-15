import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";
import api from "@/lib/axios";

const toReview = (raw: any) => ({
  id: String(raw.id ?? ""),
  bookingId: String(raw.bookingId ?? ""),
  hotelId: String(raw.hotelId ?? raw.hotel?.id ?? ""),
  userId: String(raw.userId ?? raw.accountId ?? raw.user?.id ?? ""),
  rating: Number(raw.rating ?? 0),
  title: raw.title ?? null,
  content: raw.content ?? raw.comment ?? null,
  isHidden: raw.isHidden ?? raw.visible === false,
  createdAt: raw.createdAt,
  updatedAt: raw.updatedAt,
  user: raw.user,
  hotel: raw.hotel,
  images: Array.isArray(raw.images) ? raw.images : [],
});

const toReviewPage = (payload: any) => {
  const rows = Array.isArray(payload?.data) ? payload.data : Array.isArray(payload?.items) ? payload.items : Array.isArray(payload) ? payload : [];
  const limit = Number(payload?.limit ?? payload?.meta?.limit ?? rows.length);
  const total = Number(payload?.total ?? payload?.meta?.total ?? rows.length);
  const page = Number(payload?.page ?? 1);
  return {
    items: rows.map(toReview),
    total,
    page,
    limit,
  };
};

const toSpringReviewCreate = (body: any) => ({
  bookingId: body?.bookingId,
  rating: body?.rating,
  comment: body?.content ?? body?.comment ?? body?.title,
  imageIds: body?.imageIds,
});

const toSpringReviewUpdate = (body: any) => ({
  rating: body?.rating,
  comment: body?.content ?? body?.comment ?? body?.title,
});

const mockReviewEligibility = (hotelId: string) => {
  const reviewedBookingIds = new Set(mockApi.reviews.list().map((review: any) => review.bookingId));
  const completedStays = mockApi.bookings.list().filter((booking: any) =>
    booking.hotelId === hotelId && booking.status === "COMPLETED",
  );
  const eligibleStay = completedStays.find((booking: any) => !reviewedBookingIds.has(booking.id));
  if (eligibleStay) {
    return { canReview: true, bookingId: eligibleStay.id, reason: "ELIGIBLE" };
  }
  return {
    canReview: false,
    bookingId: null,
    reason: completedStays.length > 0 ? "ALL_STAYS_REVIEWED" : "NO_COMPLETED_STAY",
  };
};

export const reviewsApi: any = {
  listPublic: async (hotelId: string, params?: unknown) => toReviewPage(await mockOrRequest({ data: mockApi.reviews.list(hotelId) }, () => api.get(`/hotels/${hotelId}/reviews`, { params }))),
  listModeration: async (hotelId: string, params?: unknown) => toReviewPage(await mockOrRequest({ data: mockApi.reviews.list(hotelId) }, () => api.get(`/admin/hotels/${hotelId}/reviews`, { params }))),
    create: async (hotelId: string, body: unknown) => toReview(await mockOrRequest(mockApi.reviews.list(hotelId)[0], () => api.post(`/hotels/${hotelId}/reviews`, toSpringReviewCreate(body)))),
    eligibility: async (hotelId: string) => mockOrRequest(
      mockReviewEligibility(hotelId),
      () => api.get(`/hotels/${hotelId}/reviews/eligibility`),
    ),
  moderate: (hotelId: string, id: string, body: any) => mockOrRequest({ ok: true }, () => api.patch(`/admin/hotels/${hotelId}/reviews/${id}/moderation`, { visible: body?.visible ?? !body?.isHidden })),
  remove: (hotelId: string, id: string) => mockOrRequest({ ok: true }, () => api.delete(`/admin/hotels/${hotelId}/reviews/${id}`)),
  mine: async (params?: unknown) => toReviewPage(await mockOrRequest({ data: mockApi.reviews.list() }, () => api.get("/reviews/mine", { params }))),
  updateMine: async (id: string, body: unknown) => toReview(await mockOrRequest({ ...mockApi.reviews.list()[0], id }, () => api.patch(`/reviews/${id}/mine`, toSpringReviewUpdate(body)))),
};

export const listHotelReviews = (hotelId: string, params?: unknown) => reviewsApi.listPublic(hotelId, params);
export const listModerationReviews = (hotelId: string, params?: unknown) => reviewsApi.listModeration(hotelId, params);
export const createReview = (hotelId: string, body: unknown) => reviewsApi.create(hotelId, body);
export const getReviewEligibility = (hotelId: string) => reviewsApi.eligibility(hotelId);
export const moderateReview = (hotelId: string, id: string, body: unknown) => reviewsApi.moderate(hotelId, id, body);
export const deleteReview = (hotelId: string, id: string) => reviewsApi.remove(hotelId, id);
export const listMyReviews = (params?: unknown) => reviewsApi.mine(params);
export const updateReview = (id: string, body: unknown) => reviewsApi.updateMine(id, body);
