import { mockApi } from "@/mocks/mockApi";
import api from "@/lib/axios";
import { mockOrRequest } from "@/features/shared/apiClient";

export const actionsApi: any = {
  list: () => mockOrRequest(mockApi.actions.list(), () => api.get("/actions")),
  assignPermissions: (_actionId: string, _permissionIds: string[]) =>
    mockOrRequest({ ok: true }, () => api.patch(`/actions/${_actionId}/permissions`, { permissionIds: _permissionIds })),
};

export const getActions = () => actionsApi.list();
export const assignPermissionToAction = (actionId: string, permissionIds: string[]) => actionsApi.assignPermissions(actionId, permissionIds);
