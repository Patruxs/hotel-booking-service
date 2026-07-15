import { mockApi } from "@/mocks/mockApi";
import api from "@/lib/axios";
import { mockOrRequest } from "@/features/shared/apiClient";

function normalizeActionList(payload: unknown) {
  if (Array.isArray(payload)) {
    return payload;
  }
  if (payload && typeof payload === "object" && Array.isArray((payload as { data?: unknown }).data)) {
    return (payload as { data: unknown[] }).data;
  }
  throw new Error("Unexpected actions list response from Spring API");
}

export const actionsApi: any = {
  list: () => mockOrRequest(mockApi.actions.list(), () => api.get("/actions")).then(normalizeActionList),
  assignPermissions: (_actionId: string, _permissionIds: string[]) =>
    mockOrRequest({ ok: true }, () => api.patch(`/actions/${_actionId}/permissions`, { permissionIds: _permissionIds })),
};

export const getActions = () => actionsApi.list();
export const assignPermissionToAction = (actionId: string, permissionIds: string[]) => actionsApi.assignPermissions(actionId, permissionIds);
