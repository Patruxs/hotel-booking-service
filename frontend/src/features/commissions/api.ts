import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";
import api from "@/lib/axios";

export type CommissionRevenueParams = unknown;

export const commissionsApi: any = {
  list: () => mockOrRequest(mockApi.commissions.list(), () => api.get("/admin/commission-packages")),
  get: (id: string) => mockOrRequest(mockApi.commissions.get(id), () => api.get(`/admin/commission-packages/${id}`)),
  create: (body: unknown) => mockOrRequest(mockApi.commissions.list()[0], () => api.post("/admin/commission-packages", body)),
  update: (id: string, body: unknown) => mockOrRequest(mockApi.commissions.get(id), () => api.patch(`/admin/commission-packages/${id}`, body)),
  deactivate: (id: string) => mockOrRequest({ ok: true }, () => api.patch(`/admin/commission-packages/${id}/deactivate`)),
  assignToHotel: (hotelId: string, commissionPackageId: string) => mockOrRequest({ ok: true }, () => api.put(`/hotels/${hotelId}/commission-package/${commissionPackageId}`)),
  revenueChart: (params?: unknown) => mockOrRequest([{ month: "Jun", revenue: 12000000 }], () => api.get("/admin/commission-packages/revenue/chart", { params })),
};

export const getCommissionPackages = () => commissionsApi.list();
export const getCommissionPackageById = (id: string) => commissionsApi.get(id);
export const createCommissionPackage = (body: unknown) => commissionsApi.create(body);
export const updateCommissionPackage = (id: string, body: unknown) => commissionsApi.update(id, body);
export const deactivateCommissionPackage = (id: string) => commissionsApi.deactivate(id);
export const setHotelCommissionPackage = (hotelId: string, commissionPackageId: string) => commissionsApi.assignToHotel(hotelId, commissionPackageId);
export const getCommissionRevenue = (params?: unknown) => commissionsApi.revenueChart(params);
