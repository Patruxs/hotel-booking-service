import type { Hotel, RoomType } from "@/lib/types";

export const hotels: Hotel[] = [
  {
    id: "hanoi-lotus",
    name: "Hanoi Lotus Grand",
    city: "Ha Noi",
    address: "21 Hang Trong, Hoan Kiem",
    rating: 4.8,
    priceFrom: 1250000,
    imageUrl: "/globe.svg",
    description:
      "Old-quarter hotel with breakfast service, airport transfer, and flexible cancellation policies.",
    amenities: ["Breakfast", "Airport shuttle", "Spa", "Workspace"],
  },
  {
    id: "danang-bay",
    name: "Da Nang Bay Retreat",
    city: "Da Nang",
    address: "Vo Nguyen Giap, Son Tra",
    rating: 4.7,
    priceFrom: 1680000,
    imageUrl: "/window.svg",
    description:
      "Beachfront rooms, family suites, and package promotions for longer resort stays.",
    amenities: ["Beach view", "Pool", "Restaurant", "Family rooms"],
  },
  {
    id: "saigon-central",
    name: "Saigon Central Suites",
    city: "Ho Chi Minh City",
    address: "12 Nguyen Hue, District 1",
    rating: 4.6,
    priceFrom: 1450000,
    imageUrl: "/file.svg",
    description:
      "Business-focused city hotel with meeting rooms, quick check-in, and central access.",
    amenities: ["Meeting room", "Gym", "Late checkout", "Parking"],
  },
];

export const roomTypes: RoomType[] = [
  { id: "rt-deluxe", hotelId: "hanoi-lotus", name: "Deluxe King", capacity: 2, price: 1250000, available: 8 },
  { id: "rt-family", hotelId: "danang-bay", name: "Family Ocean Suite", capacity: 4, price: 2450000, available: 4 },
  { id: "rt-business", hotelId: "saigon-central", name: "Business Studio", capacity: 2, price: 1450000, available: 6 },
];
