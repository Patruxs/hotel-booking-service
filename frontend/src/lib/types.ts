export type Role = {
  id: string;
  name: string;
  permissions?: string[];
};

export type User = {
  id: string;
  name: string;
  email: string;
  phone?: string;
  avatarUrl?: string;
  roles: Role[];
  allowedActions: string[];
};

export type Hotel = {
  id: string;
  name: string;
  city: string;
  address: string;
  rating: number;
  priceFrom: number;
  imageUrl: string;
  description: string;
  amenities: string[];
};

export type RoomType = {
  id: string;
  hotelId: string;
  name: string;
  capacity: number;
  price: number;
  available: number;
};

export type Booking = {
  id: string;
  hotelId: string;
  hotelName: string;
  guestName: string;
  checkIn: string;
  checkOut: string;
  status: "pending" | "confirmed" | "checked_in" | "completed" | "cancelled";
  total: number;
};

export type NewsItem = {
  id: string;
  title: string;
  excerpt: string;
  content: string;
  publishedAt: string;
};

export type Promotion = {
  id: string;
  title: string;
  code: string;
  discountPercent: number;
  active: boolean;
};

export type Policy = {
  id: string;
  hotelId: string;
  title: string;
  description: string;
};

export type Review = {
  id: string;
  hotelId: string;
  userName: string;
  rating: number;
  comment: string;
};

export type ContactMessage = {
  id: string;
  name: string;
  email: string;
  subject: string;
  status: "new" | "processing" | "closed";
};

export type CommissionPackage = {
  id: string;
  name: string;
  rate: number;
  hotelCount: number;
};
