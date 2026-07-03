import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";
import api from "@/lib/axios";

export const dashboardApi: any = {
  stats: (params?: unknown) => mockOrRequest(mockApi.dashboard.summary(), () => api.get("/dashboard/stats", { params })),
  revenueChart: (params?: unknown) => mockOrRequest([{ month: "Jun", revenue: mockApi.dashboard.summary().revenue }], () => api.get("/dashboard/revenue-chart", { params })),
  latestReviews: (params?: unknown) => mockOrRequest(mockApi.reviews.list(), () => api.get("/dashboard/latest-reviews", { params })),
  newestBookings: (params?: unknown) => mockOrRequest(mockApi.bookings.list(), () => api.get("/dashboard/newest-bookings", { params })),
};

export const getDashboardStats = (hotelId?: string) => dashboardApi.stats({ hotelId });
export const getRevenueChart = (params?: unknown) => dashboardApi.revenueChart(params);
export const getLatestReviews = (hotelId?: string) => dashboardApi.latestReviews({ hotelId });
export const getNewestBookings = (hotelId?: string) => dashboardApi.newestBookings({ hotelId });
