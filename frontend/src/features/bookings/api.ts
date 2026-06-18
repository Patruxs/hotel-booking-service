import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOnly, mockOrRequest } from "@/features/shared/apiClient";
import { toBooking } from "@/features/shared/springMappers";

export const bookingsApi: any = {
  create: async (_hotelId: string, body: unknown) => toBooking(await mockOrRequest(mockApi.bookings.list()[0], () => api.post("/bookings/create", body))),
  list: async () => (await mockOrRequest(mockApi.bookings.list(), () => api.get("/bookings/all"))).map(toBooking),
  listByHotel: async (hotelId: string) => (await bookingsApi.list()).filter((booking: any) => booking.hotelId === hotelId),
  getByHotel: async (_hotelId: string, bookingId: string) => (await bookingsApi.list()).find((booking: any) => booking.id === bookingId) ?? mockApi.bookings.get(bookingId),
  updateStatus: async (_hotelId: string, bookingId: string, body: unknown) => toBooking(await mockOrRequest(mockApi.bookings.get(bookingId), () => api.put(`/bookings/update/${bookingId}`, body))),
  cancel: (_hotelId: string, bookingId: string, reason?: string) => mockOrRequest({ ok: true }, () => api.delete(`/bookings/cancel/${bookingId}`, { params: { reason } })),
  mine: async () => (await mockOrRequest(mockApi.bookings.list(), () => api.get("/users/get-user-bookings"))).map(toBooking),
  myDetail: async (bookingId: string) => (await bookingsApi.mine()).find((booking: any) => booking.id === bookingId) ?? mockApi.bookings.get(bookingId),
  createVnpayPayment: (_bookingId: string) => mockOnly({ paymentUrl: "/payment-result?status=success" }),
  checkIn: (_bookingId: string, _body: unknown) => mockOnly({ ok: true }),
  checkInDetail: (bookingId: string) => mockOnly({ bookingId, guests: [] }),
};

export const createBooking = (hotelId: string, body: unknown) => bookingsApi.create(hotelId, body);
export const getBookings = async (hotelId: string, _params?: unknown) => ({ data: await bookingsApi.listByHotel(hotelId), meta: { limit: 10, offset: 0, total: (await bookingsApi.listByHotel(hotelId)).length } });
export const getBookingById = (hotelId: string, bookingId: string) => bookingsApi.getByHotel(hotelId, bookingId);
export const updateBookingStatus = (hotelId: string, bookingId: string, status: unknown) => bookingsApi.updateStatus(hotelId, bookingId, { status });
export const cancleBooking = (hotelId: string, bookingId: string) => bookingsApi.cancel(hotelId, bookingId);
export const getMyBookings = async (_params?: unknown) => ({ data: await bookingsApi.mine(), meta: { limit: 10, offset: 0, total: (await bookingsApi.mine()).length } });
export const getMyBookingById = (bookingId: string) => bookingsApi.myDetail(bookingId);
export const createPayment = (bookingId: string) => bookingsApi.createVnpayPayment(bookingId);
export const checkInBooking = (bookingId: string, body: unknown) => bookingsApi.checkIn(bookingId, body);
export const getCheckIn = (bookingId: string) => bookingsApi.checkInDetail(bookingId);
