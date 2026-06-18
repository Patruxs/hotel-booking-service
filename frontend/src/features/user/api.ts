import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { getMockCurrentUser } from "@/mocks/mockAuth";
import { mockOrRequest } from "@/features/shared/apiClient";
import { toBooking, toUser } from "@/features/shared/springMappers";

export const userApi = {
  list: async () => (await mockOrRequest(mockApi.users.list(), () => api.get("/users/all"))).map((user) => toUser(user)),
  update: (_id: string, body: unknown) => mockOrRequest(mockApi.users.list()[0], () => api.put("/users/update", body)),
  remove: (id: string) => mockOrRequest({ ok: true }, () => api.delete("/users/delete", { params: { userId: id } })),
  me: async () => toUser(await mockOrRequest(getMockCurrentUser(), () => api.get("/users/get-logged-in-profile-info"))),
  bookings: async () => (await mockOrRequest(mockApi.bookings.list(), () => api.get("/users/get-user-bookings"))).map(toBooking),
  updateMe: (body: unknown) => mockOrRequest(getMockCurrentUser(), () => api.put("/users/update", body)),
  changePassword: (body: unknown) => mockOrRequest({ ok: true }, () => api.put("/users/change-password", body)),
  uploadAvatar: (formData: FormData) =>
    mockOrRequest({ avatarUrl: "/globe.svg" }, () =>
      api.post("/users/me/avatar", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      }),
    ),
};
