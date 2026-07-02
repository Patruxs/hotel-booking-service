import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { getMockCurrentUser } from "@/mocks/mockAuth";
import { mockOnly, mockOrRequest } from "@/features/shared/apiClient";
import { toBooking, toUser } from "@/features/shared/springMappers";

export const userApi: any = {
  list: async () => (await mockOrRequest(mockApi.users.list(), () => api.get("/users/all"))).map((user) => toUser(user)),
  update: (_id: string, body: unknown) => mockOrRequest(mockApi.users.list()[0], () => api.put("/users/update", body)),
  remove: (_id: string) => mockOnly({ ok: true }),
  me: async () => toUser(await mockOrRequest(getMockCurrentUser(), () => api.get("/users/get-logged-in-profile-info"))),
  bookings: async () => {
    const response: any = await mockOrRequest({ data: mockApi.bookings.list() }, () => api.get("/bookings/me"));
    const rows = Array.isArray(response?.data) ? response.data : Array.isArray(response) ? response : [];
    return rows.map(toBooking);
  },
  updateMe: (body: unknown) => mockOrRequest(getMockCurrentUser(), () => api.put("/users/update", body)),
  changePassword: (body: unknown) => mockOrRequest({ ok: true }, () => api.put("/users/change-password", body)),
  uploadAvatar: (_formData: FormData) => mockOnly({ avatarUrl: "/globe.svg" }),
};

export const getUsers = async (_params?: unknown) => ({ data: await userApi.list(), meta: { limit: 10, offset: 0, total: (await userApi.list()).length } });
export const getProfile = () => userApi.me();
export const updateUser = (id: string, body: unknown) => userApi.update(id, body);
export const updateProfile = (body: unknown) => userApi.updateMe(body);
export const changePassword = (body: unknown) => userApi.changePassword(body);
export const uploadAvatar = (file: File) => {
  const formData = new FormData();
  formData.append("avatar", file);
  return userApi.uploadAvatar(formData);
};
export const assignRoleToUser = (id: string, data: unknown) => mockOnly({ ok: true, id, data });
