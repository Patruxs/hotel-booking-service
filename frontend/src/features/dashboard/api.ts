import { mockApi } from "@/mocks/mockApi";
import { mockOnly } from "@/features/shared/apiClient";

export const dashboardApi: any = {
  stats: (_params?: unknown) => mockOnly(mockApi.dashboard.summary()),
  revenueChart: (_params?: unknown) => mockOnly([{ month: "Jun", revenue: mockApi.dashboard.summary().revenue }]),
  latestReviews: (_params?: unknown) => mockOnly(mockApi.reviews.list()),
  newestBookings: (_params?: unknown) => mockOnly(mockApi.bookings.list()),
};

export const getDashboardStats = (hotelId?: string) => dashboardApi.stats({ hotelId });
export const getRevenueChart = (params?: unknown) => dashboardApi.revenueChart(params);
export const getLatestReviews = (hotelId?: string) => dashboardApi.latestReviews({ hotelId });
export const getNewestBookings = (hotelId?: string) => dashboardApi.newestBookings({ hotelId });
