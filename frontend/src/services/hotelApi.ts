import { api, unwrapResponse } from "@/lib/axios";
import type { Amenity, Booking, Hotel, LoginResponse, RevenueStatistic, Room, User } from "@/lib/types";

export const endpoints = {
  auth: {
    login: "/api/v1/auth/login",
    register: "/api/v1/auth/register",
    logout: "/api/v1/auth/logout",
  },
  hotels: {
    all: "/api/v1/hotels/all",
    search: "/api/v1/hotels/search",
    detail: (hotelId: number | string) => `/api/v1/hotels/${hotelId}`,
    rooms: (hotelId: number | string) => `/api/v1/hotels/${hotelId}/rooms`,
    myHotels: "/api/v1/hotels/my-hotels",
    add: "/api/v1/hotels/add",
    update: (hotelId: number | string) => `/api/v1/hotels/update/${hotelId}`,
    delete: (hotelId: number | string) => `/api/v1/hotels/delete/${hotelId}`,
  },
  rooms: {
    all: "/api/v1/rooms/all",
    detail: (roomId: number | string) => `/api/v1/rooms/${roomId}`,
    available: "/api/v1/rooms/all-available-rooms",
    types: "/api/v1/rooms/types",
    search: "/api/v1/rooms/search",
    hotelAvailable: (hotelId: number | string) => `/api/v1/rooms/hotel/${hotelId}/available`,
  },
  bookings: {
    all: "/api/v1/bookings/all",
    create: "/api/v1/bookings/create",
    byCode: (confirmationCode: string) => `/api/v1/bookings/get-by-confirmation-code/${confirmationCode}`,
    update: (bookingId: number | string) => `/api/v1/bookings/update/${bookingId}`,
    cancel: (bookingId: number | string) => `/api/v1/bookings/cancel/${bookingId}`,
  },
  users: {
    all: "/api/v1/users/all",
    profile: "/api/v1/users/get-logged-in-profile-info",
    bookings: "/api/v1/users/get-user-bookings",
    changePassword: "/api/v1/users/change-password",
    createStaff: "/api/v1/users/create-staff",
    update: "/api/v1/users/update",
  },
  amenities: {
    all: "/api/v1/amenities/all",
    detail: (id: number | string) => `/api/v1/amenities/${id}`,
    create: "/api/v1/amenities/create",
    update: (id: number | string) => `/api/v1/amenities/update/${id}`,
    delete: (id: number | string) => `/api/v1/amenities/delete/${id}`,
    hotelAmenities: (hotelId: number | string) => `/api/v1/amenities/hotel/${hotelId}/hotel-amenities`,
    roomAmenities: (hotelId: number | string) => `/api/v1/amenities/hotel/${hotelId}/room-amenities`,
  },
  revenue: {
    yearly: "/api/v1/revenue/yearly",
    dateRange: "/api/v1/revenue/date-range",
  },
  physicalRooms: {
    all: "/api/v1/physical-rooms/all",
    byRoom: (roomId: number | string) => `/api/v1/physical-rooms/by-room/${roomId}`,
  },
};

export async function login(payload: { email: string; password: string }) {
  const response = await api.post(endpoints.auth.login, payload);
  return unwrapResponse<LoginResponse>(response.data);
}

export async function register(payload: { fullName: string; email: string; phone: string; password: string; dob: string }) {
  const response = await api.post(endpoints.auth.register, payload);
  return unwrapResponse<User>(response.data);
}

export async function getHotels() {
  const response = await api.get(endpoints.hotels.all);
  return unwrapResponse<Hotel[]>(response.data);
}

export async function searchHotels(params: {
  location: string;
  checkInDate: string;
  checkOutDate: string;
  capacity?: number;
  roomQuantity?: number;
}) {
  const response = await api.get(endpoints.hotels.search, { params });
  return unwrapResponse<Hotel[]>(response.data);
}

export async function getHotel(hotelId: string | number) {
  const response = await api.get(endpoints.hotels.detail(hotelId));
  return unwrapResponse<Hotel>(response.data);
}

export async function getHotelRooms(hotelId: string | number) {
  const response = await api.get(endpoints.hotels.rooms(hotelId));
  return unwrapResponse<Room[]>(response.data);
}

export async function createBooking(payload: {
  checkinDate: string;
  checkoutDate: string;
  adultAmount: number;
  childrenAmount: number;
  hotelId: number;
  roomId: number;
  roomQuantity: number;
  specialRequire?: string;
}) {
  const response = await api.post(endpoints.bookings.create, payload);
  return unwrapResponse<Booking>(response.data);
}

export async function getUserBookings() {
  const response = await api.get(endpoints.users.bookings);
  return unwrapResponse<Booking[]>(response.data);
}

export async function getCurrentUser() {
  const response = await api.get(endpoints.users.profile);
  return unwrapResponse<User>(response.data);
}

export async function getAdminBookings() {
  const response = await api.get(endpoints.bookings.all);
  return unwrapResponse<Booking[]>(response.data);
}

export async function getAmenities() {
  const response = await api.get(endpoints.amenities.all);
  return unwrapResponse<Amenity[]>(response.data);
}

export async function getRevenue() {
  const response = await api.get(endpoints.revenue.yearly);
  return unwrapResponse<RevenueStatistic[]>(response.data);
}
