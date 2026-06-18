import type { Booking } from "@/lib/types";

export const bookings: Booking[] = [
  {
    id: "bk-1001",
    hotelId: "hanoi-lotus",
    hotelName: "Hanoi Lotus Grand",
    guestName: "Nguyen Minh Anh",
    checkIn: "2026-06-18",
    checkOut: "2026-06-21",
    status: "confirmed",
    total: 3750000,
  },
  {
    id: "bk-1002",
    hotelId: "danang-bay",
    hotelName: "Da Nang Bay Retreat",
    guestName: "Tran Hoang",
    checkIn: "2026-07-02",
    checkOut: "2026-07-06",
    status: "pending",
    total: 9800000,
  },
  {
    id: "bk-1003",
    hotelId: "saigon-central",
    hotelName: "Saigon Central Suites",
    guestName: "Le Thu",
    checkIn: "2026-06-10",
    checkOut: "2026-06-12",
    status: "completed",
    total: 2900000,
  },
];
