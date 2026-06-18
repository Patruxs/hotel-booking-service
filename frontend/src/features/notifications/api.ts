import api from "@/lib/axios";
import { mockOrRequest } from "@/features/shared/apiClient";

const notifications = [{ id: "nt-1", title: "Mock notification", read: false }];

export const notificationsApi = {
  list: (params?: unknown) => mockOrRequest({ data: notifications }, () => api.get("/notifications", { params })),
  unreadCount: () => mockOrRequest({ count: 1 }, () => api.get("/notifications/unread-count")),
  markRead: (id: string) => mockOrRequest({ ok: true }, () => api.patch(`/notifications/${id}/read`)),
  markAllRead: () => mockOrRequest({ ok: true }, () => api.patch("/notifications/read-all")),
  remove: (id: string) => mockOrRequest({ ok: true }, () => api.delete(`/notifications/${id}`)),
};
