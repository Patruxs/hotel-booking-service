import type { Booking, Hotel, RoomType, User } from "@/lib/types";

type AnyRecord = Record<string, any>;

export function toHotel(raw: AnyRecord): Hotel {
  const id = String(raw.id ?? raw.hotelId ?? "");
  const rawImages = Array.isArray(raw.images) ? raw.images : [];
  const imageUrl = raw.coverImage ?? raw.imageUrl ?? rawImages[0]?.url ?? rawImages[0] ?? "/globe.svg";
  const images = rawImages.length > 0
    ? rawImages.map((image: any, index: number) =>
        typeof image === "string"
          ? { id: `${id}-image-${index + 1}`, hotelId: id, url: image }
          : { id: image.id ?? `${id}-image-${index + 1}`, hotelId: id, ...image, url: image.url ?? imageUrl },
      )
    : [{ id: `${id}-image-1`, hotelId: id, url: imageUrl }];
  const priceFrom = Number(raw.minPrice ?? raw.priceFrom ?? 0);

  return {
    id,
    name: raw.name ?? "Unnamed hotel",
    city: raw.location ?? raw.city ?? "",
    address: raw.location ?? raw.address ?? "",
    rating: Number(raw.averageRating ?? raw.starRating ?? 0),
    priceFrom,
    minPrice: priceFrom,
    imageUrl,
    images,
    description: raw.description ?? "",
    owner: raw.owner ?? {
      id: raw.ownerId ?? "mock-owner",
      name: raw.ownerName ?? "Hotel Owner",
      email: raw.ownerEmail ?? "owner@example.com",
    },
    status: raw.status ?? "ACTIVE",
    commissionPackage: raw.commissionPackage,
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
  const id = String(raw.id ?? raw.bookingReference ?? "");
  const hotelId = String(raw.hotel?.id ?? raw.hotelId ?? "");
  const hotelName = raw.hotel?.name ?? raw.hotelName ?? "Hotel";
  const checkIn = raw.checkinDate ?? raw.checkIn ?? "";
  const checkOut = raw.checkoutDate ?? raw.checkOut ?? "";
  const total = Number(raw.totalAmount ?? raw.totalPrice ?? raw.total ?? 0);
  const status = normalizeBookingStatus(raw.status);
  const kinyiasStatus = normalizeKinyiasBookingStatus(raw.status);
  const items = Array.isArray(raw.items) && raw.items.length > 0
    ? raw.items
    : [
        {
          id: `${id || "booking"}-item-1`,
          quantity: 1,
          roomTypeId: raw.roomTypeId ?? "room-type",
          roomType: {
            id: raw.roomTypeId ?? "room-type",
            name: raw.roomTypeName ?? "Standard Room",
          },
        },
      ];

  return {
    id,
    hotelId,
    hotelName,
    guestName: raw.customerName ?? raw.guestName ?? raw.user?.fullName ?? "Guest",
    checkIn,
    checkOut,
    checkinDate: checkIn,
    checkoutDate: checkOut,
    status: kinyiasStatus,
    normalizedStatus: status,
    total,
    totalAmount: total,
    discountAmount: Number(raw.discountAmount ?? 0),
    hotel: raw.hotel ?? {
      id: hotelId,
      name: hotelName,
      address: raw.hotelAddress ?? raw.address ?? "",
    },
    items,
    payments: Array.isArray(raw.payments) ? raw.payments : [],
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

function normalizeKinyiasBookingStatus(status?: string): string {
  switch ((status ?? "").toUpperCase()) {
    case "BOOKED":
    case "CONFIRMED":
      return "CONFIRMED";
    case "CHECKED_IN":
      return "CHECKED_IN";
    case "CHECKED_OUT":
    case "COMPLETED":
      return "COMPLETED";
    case "CANCELLED":
      return "CANCELLED";
    case "NO_SHOW":
      return "NO_SHOW";
    default:
      return "PENDING";
  }
}
