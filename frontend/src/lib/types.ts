export type Role = {
  id: string;
  name: string;
  permissions?: string[];
  [key: string]: any;
};

export type User = {
  id: string;
  name: string;
  email: string;
  phone?: string;
  avatarUrl?: string;
  firstName?: string;
  lastName?: string;
  avatar?: { url?: string };
  roles: Role[];
  allowedActions: string[];
  createdAt?: string;
  updatedAt?: string;
  [key: string]: any;
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
  country?: string;
  status?: string;
  images?: Array<Record<string, unknown>>;
  owner?: Record<string, unknown>;
  [key: string]: any;
};

export type RoomType = {
  id: string;
  hotelId: string;
  name: string;
  capacity: number;
  price: number;
  available: number;
  [key: string]: any;
};

export type Booking = {
  id: string;
  hotelId: string;
  hotelName: string;
  guestName: string;
  checkIn: string;
  checkOut: string;
  status: string;
  total: number;
  [key: string]: any;
};

export type NewsItem = {
  id: string;
  title: string;
  excerpt: string;
  content: string;
  publishedAt: string;
  [key: string]: any;
};

export type Promotion = {
  id: string;
  title: string;
  code: string;
  discountPercent: number;
  active: boolean;
  [key: string]: any;
};

export type Policy = {
  id: string;
  hotelId: string;
  title: string;
  description: string;
  [key: string]: any;
};

export type Review = {
  id: string;
  hotelId: string;
  userName: string;
  rating: number;
  comment: string;
  [key: string]: any;
};

export type ContactMessage = {
  id: string;
  name: string;
  email: string;
  subject: string;
  status: string;
  [key: string]: any;
};

export type CommissionPackage = {
  id: string;
  name: string;
  rate: number;
  hotelCount: number;
  [key: string]: any;
};
