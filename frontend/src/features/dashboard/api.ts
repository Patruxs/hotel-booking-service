import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

export const dashboardApi = {
  stats: (params?: unknown) => mockOrRequest(mockApi.dashboard.summary(), () => api.get("/dashboard/stats", { params })),
  revenueChart: (params?: unknown) => mockOrRequest([{ month: "Jun", revenue: mockApi.dashboard.summary().revenue }], () => api.get("/dashboard/revenue-chart", { params })),
  latestReviews: (params?: unknown) => mockOrRequest(mockApi.reviews.list(), () => api.get("/dashboard/latest-reviews", { params })),
  newestBookings: (params?: unknown) => mockOrRequest(mockApi.bookings.list(), () => api.get("/dashboard/newest-bookings", { params })),
};
