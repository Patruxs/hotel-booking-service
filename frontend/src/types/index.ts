export type {
  Booking,
  CommissionPackage,
  ContactMessage,
  Hotel,
  NewsItem,
  Policy,
  Promotion,
  Review,
  Role,
  RoomType,
  User,
} from "@/lib/types";

export type PaginatedResponse<T> = {
  data: T[];
  meta: {
    limit: number;
    offset: number;
    total: number;
  };
};

export type ApiResult<T> = Promise<T>;
export type ApiError = {
  response?: {
    data: {
      message?: string;
    };
  };
  message?: string;
};
