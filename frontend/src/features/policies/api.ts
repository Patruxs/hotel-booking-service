import { mockApi } from "@/mocks/mockApi";
import { mockOnly } from "@/features/shared/apiClient";

export const policiesApi: any = {
  listAdmin: (hotelId: string) => mockOnly(mockApi.policies.list(hotelId)),
  getAdmin: (hotelId: string, _policyId: string) => mockOnly(mockApi.policies.list(hotelId)[0]),
  create: (_hotelId: string, _body: unknown) => mockOnly({ ok: true }),
  update: (_hotelId: string, _policyId: string, _body: unknown) => mockOnly({ ok: true }),
  remove: (_hotelId: string, _policyId: string) => mockOnly({ ok: true }),
  listPublic: (hotelId: string) => mockOnly(mockApi.policies.list(hotelId)),
};

export const getPoliciesByHotel = (hotelId: string) => policiesApi.listAdmin(hotelId);
export const getPolicyById = (hotelId: string, policyId: string) => policiesApi.getAdmin(hotelId, policyId);
export const createPolicy = (hotelId: string, body: unknown) => policiesApi.create(hotelId, body);
export const updatePolicy = (hotelId: string, policyId: string, body: unknown) => policiesApi.update(hotelId, policyId, body);
export const deletePolicy = (hotelId: string, policyId: string) => policiesApi.remove(hotelId, policyId);
export const getPublicPoliciesByHotel = (hotelId: string) => policiesApi.listPublic(hotelId);
