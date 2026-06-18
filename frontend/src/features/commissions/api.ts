import { mockApi } from "@/mocks/mockApi";
import { mockOnly } from "@/features/shared/apiClient";

export type CommissionRevenueParams = unknown;

export const commissionsApi: any = {
  list: () => mockOnly(mockApi.commissions.list()),
  get: (id: string) => mockOnly(mockApi.commissions.get(id)),
  create: (_body: unknown) => mockOnly(mockApi.commissions.list()[0]),
  update: (id: string, _body: unknown) => mockOnly(mockApi.commissions.get(id)),
  deactivate: (_id: string) => mockOnly({ ok: true }),
  assignToHotel: (_hotelId: string, _commissionPackageId: string) => mockOnly({ ok: true }),
  revenueChart: (_params?: unknown) => mockOnly([{ month: "Jun", revenue: 12000000 }]),
};

export const getCommissionPackages = () => commissionsApi.list();
export const getCommissionPackageById = (id: string) => commissionsApi.get(id);
export const createCommissionPackage = (body: unknown) => commissionsApi.create(body);
export const updateCommissionPackage = (id: string, body: unknown) => commissionsApi.update(id, body);
export const deactivateCommissionPackage = (id: string) => commissionsApi.deactivate(id);
export const setHotelCommissionPackage = (hotelId: string, commissionPackageId: string) => commissionsApi.assignToHotel(hotelId, commissionPackageId);
export const getCommissionRevenue = (params?: unknown) => commissionsApi.revenueChart(params);
