import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { getMockCurrentUser } from "@/mocks/mockAuth";
import { mockOrRequest } from "@/features/shared/apiClient";
import { toBooking, toUser } from "@/features/shared/springMappers";

const toSpringProfileUpdate = (body: any) => ({
  fullName:
    body?.fullName ??
    [body?.firstName, body?.lastName].filter(Boolean).join(" ").trim(),
  phone: body?.phone,
  dob: body?.dob ?? body?.dateOfBirth,
});

const toSpringAdminUserUpdate = (body: any) => ({
  firstName: body?.firstName,
  lastName: body?.lastName,
  phone: body?.phone,
  dob: body?.dob ?? body?.dateOfBirth,
});

export const userApi: any = {
  list: async () => {
    const response: any = await mockOrRequest(
      {
        data: mockApi.users.list(),
        meta: { limit: 10, offset: 0, total: mockApi.users.list().length },
      },
      () => api.get("/users"),
    );
    const rows = Array.isArray(response?.data)
      ? response.data
      : Array.isArray(response)
        ? response
        : [];
    return rows.map((user: any) => toUser(user));
  },
  update: (_id: string, body: unknown) =>
    mockOrRequest(mockApi.users.list()[0], () =>
      api.patch(`/users/${_id}`, toSpringAdminUserUpdate(body)),
    ),
  remove: (_id: string) =>
    mockOrRequest({ ok: true }, () => api.put(`/users/${_id}/lock`)),
  me: async () =>
    toUser(
      await mockOrRequest(getMockCurrentUser(), () => api.get("/users/me")),
    ),
  bookings: async () => {
    const response: any = await mockOrRequest(
      { data: mockApi.bookings.list() },
      () => api.get("/bookings/me"),
    );
    const rows = Array.isArray(response?.data)
      ? response.data
      : Array.isArray(response)
        ? response
        : [];
    return rows.map(toBooking);
  },
  updateMe: async (body: unknown) =>
    toUser(
      await mockOrRequest(getMockCurrentUser(), () =>
        api.patch("/users/me", toSpringProfileUpdate(body)),
      ),
    ),
  changePassword: (body: unknown) =>
    mockOrRequest({ ok: true }, () =>
      api.post("/users/me/change-password", body),
    ),
  uploadAvatar: (formData: FormData) =>
    mockOrRequest({ url: "/globe.svg" }, () =>
      api.post("/uploads/avatar", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      }),
    ),
  deleteAvatar: () => mockOrRequest(null, () => api.delete("/uploads/avatar")),
};

export const getUsers = async (_params?: unknown) => ({
  data: await userApi.list(),
  meta: { limit: 10, offset: 0, total: (await userApi.list()).length },
});
export const getProfile = () => userApi.me();
export const updateUser = (id: string, body: unknown) =>
  userApi.update(id, body);
export const updateProfile = (body: unknown) => userApi.updateMe(body);
export const changePassword = (body: unknown) => userApi.changePassword(body);
export const uploadAvatar = (file: File) => {
  const formData = new FormData();
  formData.append("avatar", file);
  return userApi.uploadAvatar(formData);
};
export const deleteAvatar = () => userApi.deleteAvatar();
export const assignRoleToUser = (id: string, data: any) =>
  mockOrRequest({ ok: true, id, data }, () =>
    api.post("/roles/assign-to-user", {
      userId: id,
      roleIds: data?.roleIds ?? data?.roles ?? [],
    }),
  );
