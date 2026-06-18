import { mockOnly } from "@/features/shared/apiClient";

const notifications = [{ id: "nt-1", title: "Mock notification", read: false }];

export const notificationsApi: any = {
  list: (_params?: unknown) => mockOnly({ data: notifications }),
  unreadCount: () => mockOnly({ count: 1 }),
  markRead: (_id: string) => mockOnly({ ok: true }),
  markAllRead: () => mockOnly({ ok: true }),
  remove: (_id: string) => mockOnly({ ok: true }),
};

export const getNotifications = (page?: number, limit?: number) => notificationsApi.list({ page, limit });
export const getUnreadCount = () => notificationsApi.unreadCount();
export const markAsRead = (id: string) => notificationsApi.markRead(id);
export const markAllAsRead = () => notificationsApi.markAllRead();
export const deleteNotification = (id: string) => notificationsApi.remove(id);
