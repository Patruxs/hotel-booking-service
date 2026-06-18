import { mockApi } from "@/mocks/mockApi";
import { mockOnly } from "@/features/shared/apiClient";

export const permissionsApi: any = {
  list: () => mockOnly(mockApi.permissions.list()),
  create: (_body: unknown) => mockOnly({ ok: true }),
  update: (_id: string, _body: unknown) => mockOnly({ ok: true }),
  remove: (_id: string) => mockOnly({ ok: true }),
};

export const getPermissions = () => permissionsApi.list();
export const createPermission = (body: unknown) => permissionsApi.create(body);
export const updatePermission = (id: string, body: unknown) => permissionsApi.update(id, body);
export const deletePermission = (id: string) => permissionsApi.remove(id);
