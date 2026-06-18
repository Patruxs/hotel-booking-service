import { mockApi } from "@/mocks/mockApi";
import { mockOnly } from "@/features/shared/apiClient";

export const rolesApi: any = {
  list: () => mockOnly(mockApi.roles.list()),
  create: (_body: unknown) => mockOnly({ ok: true }),
  update: (_id: string, _body: unknown) => mockOnly({ ok: true }),
  assignPermissions: (_id: string, _body: unknown) => mockOnly({ ok: true }),
  assignToUser: (_body: unknown) => mockOnly({ ok: true }),
  remove: (_id: string) => mockOnly({ ok: true }),
};

export const getRoles = () => rolesApi.list();
export const createRole = (body: unknown) => rolesApi.create(body);
export const updateRole = (id: string, body: unknown) => rolesApi.update(id, body);
export const assignPermissionsToRole = (id: string, body: unknown) => rolesApi.assignPermissions(id, body);
export const deleteRole = (id: string) => rolesApi.remove(id);
