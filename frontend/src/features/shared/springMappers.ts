import type { Booking, Hotel, RoomType, User } from "@/lib/types";

type AnyRecord = Record<string, any>;

export function toHotel(raw: AnyRecord): Hotel {
  const id = String(raw.id ?? raw.hotelId ?? "");
  const images = Array.isArray(raw.images) ? raw.images : [];
  return {
    id,
    name: raw.name ?? "Unnamed hotel",
    city: raw.location ?? raw.city ?? "",
    address: raw.location ?? raw.address ?? "",
    rating: Number(raw.averageRating ?? raw.starRating ?? 0),
    priceFrom: Number(raw.minPrice ?? raw.priceFrom ?? 0),
    imageUrl: raw.coverImage ?? images[0] ?? "/globe.svg",
    description: raw.description ?? "",
    amenities: Array.isArray(raw.amenities)
      ? raw.amenities.map(String)
      : typeof raw.amenities === "string"
        ? raw.amenities.split(/\s{2,}|,|;/).filter(Boolean)
        : [],
  };
}

export function toRoom(raw: AnyRecord, hotelId?: string): RoomType {
  return {
    id: String(raw.id ?? ""),
    hotelId: String(raw.hotelId ?? raw.hotel?.id ?? hotelId ?? ""),
    name: raw.name ?? raw.type ?? "Room",
    capacity: Number(raw.capacity ?? 1),
    price: Number(raw.price ?? 0),
    available: Number(raw.availableQuantity ?? raw.amount ?? 0),
  };
}

export function toBooking(raw: AnyRecord): Booking {
  return {
    id: String(raw.id ?? raw.bookingReference ?? ""),
    hotelId: String(raw.hotel?.id ?? raw.hotelId ?? ""),
    hotelName: raw.hotel?.name ?? raw.hotelName ?? "Hotel",
    guestName: raw.customerName ?? raw.guestName ?? raw.user?.fullName ?? "Guest",
    checkIn: raw.checkinDate ?? raw.checkIn ?? "",
    checkOut: raw.checkoutDate ?? raw.checkOut ?? "",
    status: normalizeBookingStatus(raw.status),
    total: Number(raw.totalPrice ?? raw.total ?? 0),
  };
}

export function toUser(raw: AnyRecord, roleName?: string): User {
  const role = roleName ?? raw.role ?? raw.roles?.[0]?.name ?? "USER";
  return {
    id: String(raw.id ?? ""),
    name: raw.fullName ?? raw.name ?? raw.email ?? "User",
    email: raw.email ?? "",
    phone: raw.phone,
    roles: [{ id: role, name: role, permissions: role === "ADMIN" ? ["*"] : [] }],
    allowedActions: role === "ADMIN" ? ["*"] : [],
  };
}

function normalizeBookingStatus(status?: string): Booking["status"] {
  switch ((status ?? "").toUpperCase()) {
    case "BOOKED":
      return "confirmed";
    case "CHECKED_IN":
      return "checked_in";
    case "CHECKED_OUT":
      return "completed";
    case "CANCELLED":
      return "cancelled";
    default:
      return "pending";
  }
}
