import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";
import { toBooking } from "@/features/shared/springMappers";

export const bookingsApi = {
  create: async (_hotelId: string, body: unknown) => toBooking(await mockOrRequest(mockApi.bookings.list()[0], () => api.post("/bookings/create", body))),
  list: async () => (await mockOrRequest(mockApi.bookings.list(), () => api.get("/bookings/all"))).map(toBooking),
  listByHotel: async (hotelId: string) => (await bookingsApi.list()).filter((booking) => booking.hotelId === hotelId),
  getByHotel: async (_hotelId: string, bookingId: string) => (await bookingsApi.list()).find((booking) => booking.id === bookingId) ?? mockApi.bookings.get(bookingId),
  updateStatus: async (_hotelId: string, bookingId: string, body: unknown) => toBooking(await mockOrRequest(mockApi.bookings.get(bookingId), () => api.put(`/bookings/update/${bookingId}`, body))),
  cancel: (_hotelId: string, bookingId: string, reason?: string) => mockOrRequest({ ok: true }, () => api.delete(`/bookings/cancel/${bookingId}`, { params: { reason } })),
  mine: async () => (await mockOrRequest(mockApi.bookings.list(), () => api.get("/users/get-user-bookings"))).map(toBooking),
  myDetail: async (bookingId: string) => (await bookingsApi.mine()).find((booking) => booking.id === bookingId) ?? mockApi.bookings.get(bookingId),
  createVnpayPayment: (bookingId: string) => mockOrRequest({ paymentUrl: "/payment-result?status=success" }, () => api.post(`/bookings/${bookingId}/payments/vnpay`, { locale: "vn", bankCode: "NCB" })),
  checkIn: (bookingId: string, body: unknown) => mockOrRequest({ ok: true }, () => api.post(`/bookings/${bookingId}/check-in`, body)),
  checkInDetail: (bookingId: string) => mockOrRequest({ bookingId, guests: [] }, () => api.get(`/bookings/${bookingId}/check-in`)),
};
