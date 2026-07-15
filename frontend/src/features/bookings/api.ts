import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";
import { toBooking } from "@/features/shared/springMappers";

const toBookingPage = (payload: any) => {
  const rows = Array.isArray(payload?.data) ? payload.data : Array.isArray(payload) ? payload : [];
  return {
    data: rows.map(toBooking),
    meta: payload?.meta ?? { limit: rows.length || 10, offset: 0, total: rows.length },
  };
};

const toSpringPaginationParams = (params?: any) => {
  const { page, offset, ...rest } = params ?? {};
  const limit = Math.max(1, Number(rest.limit ?? 10) || 10);
  const normalizedPage = Math.max(1, Number(page ?? 1) || 1);
  const normalizedOffset = offset == null
    ? (normalizedPage - 1) * limit
    : Math.max(0, Number(offset) || 0);

  return { ...rest, limit, offset: normalizedOffset };
};

const mockBookingPage = (hotelId: string, params?: any) => {
  const q = String(params?.q ?? "").trim().toLowerCase();
  const rows = mockApi.bookings.list().filter((booking: any) => {
    if (booking.hotelId !== hotelId) return false;
    if (!q) return true;
    return [booking.guestName, booking.guestEmail, booking.guestPhone].some((value) =>
      String(value ?? "").toLowerCase().includes(q),
    );
  });
  const limit = Number(params?.limit ?? 10);
  const offset = Number(params?.offset ?? Math.max(0, (Number(params?.page ?? 1) - 1) * limit));
  return {
    data: rows.slice(offset, offset + limit),
    meta: { limit, offset, total: rows.length },
  };
};

const mockMyBookingPage = (params?: any) => {
  const status = String(params?.status ?? "").trim().toUpperCase();
  const rows = mockApi.bookings.list().filter((booking: any) => {
    if (!status) return true;
    return String(booking.status ?? "").toUpperCase() === status;
  });
  const limit = Number(params?.limit ?? 10);
  const offset = Number(params?.offset ?? Math.max(0, (Number(params?.page ?? 1) - 1) * limit));
  return {
    data: rows.slice(offset, offset + limit),
    meta: { limit, offset, total: rows.length },
  };
};

const mockVnpayPayment = (bookingId: string) => ({
  paymentId: `mock-payment-${bookingId}`,
  merchantTxnRef: `BK_${bookingId}_${Date.now()}`,
  paymentUrl: `/payment-result?payment_status=failed&booking_id=${bookingId}`,
});

export const toSpringBookingCreateRequest = (body: any) => ({
  checkIn: body?.checkIn,
  checkOut: body?.checkOut,
  guestName: body?.guestName,
  guestEmail: body?.guestEmail,
  guestPhone: body?.guestPhone,
  note: body?.note,
  promotionCode: body?.promotionCode,
  items: Array.isArray(body?.items) ? body.items.map((item: any) => ({
    roomTypeId: item.roomTypeId,
    quantity: item.quantity,
  })) : [],
});

export const bookingsApi: any = {
  create: async (hotelId: string, body: unknown) => toBooking(await mockOrRequest(mockApi.bookings.list()[0], () => api.post(`/hotels/${hotelId}/bookings`, toSpringBookingCreateRequest(body)))),
  listByHotel: async (hotelId: string, params?: unknown) => toBookingPage(await mockOrRequest(mockBookingPage(hotelId, params), () => api.get(`/hotels/${hotelId}/bookings`, { params }))),
  getByHotel: async (hotelId: string, bookingId: string) => toBooking(await mockOrRequest(mockApi.bookings.list().find((booking: any) => booking.hotelId === hotelId && booking.id === bookingId) ?? mockApi.bookings.get(bookingId), () => api.get(`/hotels/${hotelId}/bookings/${bookingId}`))),
  updateStatus: async (hotelId: string, bookingId: string, body: unknown) => toBooking(await mockOrRequest(mockApi.bookings.get(bookingId), () => api.patch(`/hotels/${hotelId}/bookings/${bookingId}/status`, body))),
  cancel: async (hotelId: string, bookingId: string) => toBooking(await mockOrRequest(mockApi.bookings.get(bookingId), () => api.patch(`/hotels/${hotelId}/bookings/${bookingId}/cancel`))),
  cancelMine: async (bookingId: string) => toBooking(await mockOrRequest({ ...mockApi.bookings.get(bookingId), status: "CANCELLED" }, () => api.patch(`/bookings/me/${bookingId}/cancel`))),
  mine: async (params?: unknown) => toBookingPage(await mockOrRequest(mockMyBookingPage(params), () => api.get("/bookings/me", { params: toSpringPaginationParams(params) }))),
  myDetail: async (bookingId: string) => toBooking(await mockOrRequest(mockApi.bookings.get(bookingId), () => api.get(`/bookings/me/${bookingId}`))),
  createVnpayPayment: (bookingId: string) => mockOrRequest(mockVnpayPayment(bookingId), () => api.post(`/bookings/${bookingId}/payments/vnpay`, {})),
  checkIn: async (hotelId: string, bookingId: string, body: unknown) => toBooking(await mockOrRequest(mockApi.bookings.get(bookingId), () => api.post(`/hotels/${hotelId}/bookings/${bookingId}/check-in`, body))),
  checkInDetail: (hotelId: string, bookingId: string) => mockOrRequest({ checkIn: null, guests: [] }, () => api.get(`/hotels/${hotelId}/bookings/${bookingId}/check-in`)),
};

export const createBooking = (hotelId: string, body: unknown) => bookingsApi.create(hotelId, body);
export const getBookings = (hotelId: string, params?: unknown) => bookingsApi.listByHotel(hotelId, params);
export const getBookingById = (hotelId: string, bookingId: string) => bookingsApi.getByHotel(hotelId, bookingId);
export const updateBookingStatus = (hotelId: string, bookingId: string, status: unknown) => bookingsApi.updateStatus(hotelId, bookingId, { status });
export const cancelBooking = (hotelId: string, bookingId: string) => bookingsApi.cancel(hotelId, bookingId);
export const cancleBooking = cancelBooking;
export const cancelMyBooking = (bookingId: string) => bookingsApi.cancelMine(bookingId);
export const getMyBookings = (params?: unknown) => bookingsApi.mine(params);
export const getMyBookingById = (bookingId: string) => bookingsApi.myDetail(bookingId);
export const createPayment = (bookingId: string) => bookingsApi.createVnpayPayment(bookingId);
export const checkInBooking = (hotelId: string, bookingId: string, body: unknown) => bookingsApi.checkIn(hotelId, bookingId, body);
export const getCheckIn = async (hotelId: string, bookingId: string) => ({ data: await bookingsApi.checkInDetail(hotelId, bookingId) });
