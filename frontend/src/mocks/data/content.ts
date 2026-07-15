import type {
  CommissionPackage,
  ContactMessage,
  NewsItem,
  Policy,
  Promotion,
  Review,
} from "@/lib/types";

export const news: NewsItem[] = [
  {
    id: "news-summer",
    title: "Summer travel demand rises across coastal hotels",
    excerpt:
      "Hotels prepare more flexible inventory and promotion campaigns for family travelers.",
    content:
      "Mock migration content for the old news module. The Spring Boot endpoint will provide rich content later.",
    publishedAt: "2026-06-01",
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660118/hotel_assets/azrfyphyu5u1vhtsot2a.jpg",
      },
    ],
  },
  {
    id: "news-partner",
    title: "Partner onboarding flow adds policy review",
    excerpt:
      "Hotel partners can prepare amenities, policies, and gallery assets before review.",
    content:
      "This placeholder keeps the public news detail route render-safe in mock mode.",
    publishedAt: "2026-05-20",
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660119/hotel_assets/hsdob6uibiwskqfedtyr.jpg",
      },
    ],
  },
];

export const promotions: Promotion[] = [
  {
    id: "promo-summer",
    title: "Summer Stay",
    code: "SUMMER20",
    discountPercent: 20,
    active: true,
  },
  {
    id: "promo-business",
    title: "Business Week",
    code: "WORK15",
    discountPercent: 15,
    active: true,
  },
];

export const policies: Policy[] = [
  {
    id: "policy-cancel",
    hotelId: "hanoi-lotus",
    title: "Cancellation",
    description: "Free cancellation before 48 hours.",
  },
  {
    id: "policy-checkin",
    hotelId: "danang-bay",
    title: "Check-in",
    description: "Check-in from 14:00 with valid identification.",
  },
];

export const reviews: Review[] = [
  {
    id: "rv-1",
    hotelId: "hanoi-lotus",
    userName: "Demo Guest",
    rating: 5,
    comment: "Clean rooms and helpful staff.",
  },
  {
    id: "rv-2",
    hotelId: "danang-bay",
    userName: "Business Traveler",
    rating: 4,
    comment: "Great beach access and breakfast.",
  },
];

export const contacts: ContactMessage[] = [
  {
    id: "ct-1",
    name: "Pham Linh",
    email: "linh@example.com",
    subject: "Partner inquiry",
    status: "new",
  },
  {
    id: "ct-2",
    name: "Do Nam",
    email: "nam@example.com",
    subject: "Payment support",
    status: "processing",
  },
];

export const commissions: CommissionPackage[] = [
  { id: "cm-basic", name: "Basic Partner", rate: 8, hotelCount: 12 },
  { id: "cm-premium", name: "Premium Growth", rate: 12, hotelCount: 5 },
];

export const amenities = [
  "Breakfast",
  "Pool",
  "Spa",
  "Parking",
  "Airport shuttle",
  "Workspace",
];
export const banners = ["Homepage hero", "Partner CTA", "Promotion strip"];
export const inventory = [
  {
    hotelId: "hanoi-lotus",
    roomTypeId: "rt-deluxe",
    date: "2026-06-18",
    available: 8,
    price: 1250000,
  },
  {
    hotelId: "danang-bay",
    roomTypeId: "rt-family",
    date: "2026-07-02",
    available: 4,
    price: 2450000,
  },
];
export const roles = ["ADMIN", "OWNER", "MANAGER", "RECEPTIONIST", "STAFF", "CUSTOMER"];
export const permissions = [
  "hotel:read",
  "hotel:write",
  "booking:read",
  "booking:write",
  "policy:write",
];
export const actions = [
  "HOTEL_CREATE",
  "HOTEL_UPDATE",
  "BOOKING_CHECK_IN",
  "PROMOTION_APPROVE",
];
