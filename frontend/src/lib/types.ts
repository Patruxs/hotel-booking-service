export type Hotel = {
  id: number;
  name: string;
  location: string;
  description?: string;
  starRating?: number;
  email?: string;
  phone?: string;
  isActive?: boolean;
  coverImage?: string;
  averageRating?: number;
  totalReviews?: number;
  minPrice?: number;
  images?: string[];
  amenities?: string[];
  rooms?: Room[];
};

export type Room = {
  id: number;
  type?: string;
  name?: string;
  price?: number;
  capacity?: number;
  numberOfBedrooms?: number;
  description?: string;
  amount?: number;
  availableQuantity?: number;
  roomImages?: string[];
  amenities?: Amenity[];
};

export type Amenity = {
  id: number;
  name: string;
  type?: string;
};

export type Booking = {
  id: number;
  checkinDate: string;
  checkoutDate: string;
  adultAmount?: number;
  childrenAmount?: number;
  totalPrice?: number;
  bookingReference?: string;
  roomNumber?: string;
  customerName?: string;
  customerEmail?: string;
  customerPhone?: string;
  status?: string;
  specialRequire?: string;
  hotel?: Hotel;
  rooms?: Room[];
};

export type User = {
  id: number;
  fullName: string;
  email: string;
  phone?: string;
  dob?: string;
  activate?: boolean;
};

export type LoginResponse = {
  token: string;
  role: "ADMIN" | "CUSTOMER" | "RECEPTIONIST";
  expirationTime?: string;
  isActive?: boolean;
};

export type RevenueStatistic = {
  hotelId?: number;
  hotelName?: string;
  totalBookings?: number;
  totalRevenue?: number;
  adminCommission?: number;
  month?: number;
  year?: number;
};
