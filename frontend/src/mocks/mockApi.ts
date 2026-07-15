import { bookings } from "@/mocks/data/bookings";
import {
  actions,
  amenities,
  banners,
  commissions,
  contacts,
  inventory,
  news,
  permissions,
  policies,
  promotions,
  reviews,
  roles,
} from "@/mocks/data/content";
import { hotels, roomTypes } from "@/mocks/data/hotels";
import { users } from "@/mocks/data/users";

export const useMocks = import.meta.env.VITE_USE_MOCKS === "true";

export const mockApi = {
  hotels: {
    list: () => hotels,
    get: (id?: string) => hotels.find((hotel) => hotel.id === id) ?? hotels[0],
  },
  rooms: {
    list: (hotelId?: string) => roomTypes.filter((room) => !hotelId || room.hotelId === hotelId),
    get: (id?: string) => roomTypes.find((room) => room.id === id) ?? roomTypes[0],
  },
  bookings: {
    list: () => bookings,
    get: (id?: string) => bookings.find((booking) => booking.id === id) ?? bookings[0],
  },
  users: { list: () => users },
  news: {
    list: () => news,
    get: (id?: string) => news.find((item) => item.id === id) ?? news[0],
  },
  promotions: {
    list: () => promotions,
    get: (id?: string) => promotions.find((promotion) => promotion.id === id) ?? promotions[0],
  },
  policies: { list: (hotelId?: string) => policies.filter((policy) => !hotelId || policy.hotelId === hotelId) },
  reviews: { list: (hotelId?: string) => reviews.filter((review) => !hotelId || review.hotelId === hotelId) },
  contacts: {
    list: () => contacts,
    get: (id?: string) => contacts.find((contact) => contact.id === id) ?? contacts[0],
  },
  commissions: {
    list: () => commissions,
    get: (id?: string) => commissions.find((commission) => commission.id === id) ?? commissions[0],
  },
  dashboard: {
    summary: () => ({
      hotels: hotels.length,
      bookings: bookings.length,
      revenue: bookings.reduce((sum, booking) => sum + booking.total, 0),
      pendingContacts: contacts.filter((contact) => contact.status !== "closed").length,
    }),
  },
  amenities: { list: () => amenities },
  banners: { list: () => banners },
  inventory: { list: () => inventory },
  permissions: { list: () => permissions },
  roles: { list: () => roles },
  actions: { list: () => actions },
};
