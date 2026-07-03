import { mockOrRequest } from "@/features/shared/apiClient";
import api from "@/lib/axios";

const notifications = [{ id: "nt-1", title: "Mock notification", read: false }];

export const notificationsApi: any = {
  list: (params?: unknown) => mockOrRequest({ data: notifications }, () => api.get("/notifications", { params })),
  unreadCount: () => mockOrRequest({ count: 1 }, () => api.get("/notifications/unread-count")),
  markRead: (id: string) => mockOrRequest({ ok: true }, () => api.patch(`/notifications/${id}/read`)),
  markAllRead: () => mockOrRequest({ ok: true }, () => api.patch("/notifications/read-all")),
  remove: (id: string) => mockOrRequest({ ok: true }, () => api.delete(`/notifications/${id}`)),
};

export const getNotifications = (page?: number, limit?: number) => notificationsApi.list({ page, limit });
export const getUnreadCount = () => notificationsApi.unreadCount();
export const markAsRead = (id: string) => notificationsApi.markRead(id);
export const markAllAsRead = () => notificationsApi.markAllRead();
export const deleteNotification = (id: string) => notificationsApi.remove(id);
