import { mockApi } from "@/mocks/mockApi";
import { mockOnly } from "@/features/shared/apiClient";

export const actionsApi: any = {
  list: () => mockOnly(mockApi.actions.list()),
  assignPermissions: (_actionId: string, _permissionIds: string[]) =>
    mockOnly({ ok: true }),
};

export const getActions = () => actionsApi.list();
export const assignPermissionToAction = (actionId: string, permissionIds: string[]) => actionsApi.assignPermissions(actionId, permissionIds);
