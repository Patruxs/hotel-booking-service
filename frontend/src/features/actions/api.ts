import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

export const actionsApi = {
  list: () => mockOrRequest(mockApi.actions.list(), () => api.get("/actions")),
  assignPermissions: (actionId: string, permissionIds: string[]) =>
    mockOrRequest({ ok: true }, () => api.patch(`/actions/${actionId}/permissions`, { permissionIds })),
};
