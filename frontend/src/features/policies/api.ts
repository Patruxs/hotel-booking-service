import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";
import api from "@/lib/axios";

export const policiesApi: any = {
  listAdmin: (hotelId: string) => mockOrRequest(mockApi.policies.list(hotelId), () => api.get(`/admin/hotels/${hotelId}/policies`)),
  getAdmin: (hotelId: string, policyId: string) => mockOrRequest(mockApi.policies.list(hotelId)[0], () => api.get(`/admin/hotels/${hotelId}/policies/${policyId}`)),
  create: (hotelId: string, body: unknown) => mockOrRequest({ ok: true }, () => api.post(`/admin/hotels/${hotelId}/policies`, body)),
  update: (hotelId: string, policyId: string, body: unknown) => mockOrRequest({ ok: true }, () => api.put(`/admin/hotels/${hotelId}/policies/${policyId}`, body)),
  remove: (hotelId: string, policyId: string) => mockOrRequest({ ok: true }, () => api.delete(`/admin/hotels/${hotelId}/policies/${policyId}`)),
  listPublic: (hotelId: string) => mockOrRequest(mockApi.policies.list(hotelId), () => api.get(`/hotels/${hotelId}/policies`)),
};

export const getPoliciesByHotel = (hotelId: string) => policiesApi.listAdmin(hotelId);
export const getPolicyById = (hotelId: string, policyId: string) => policiesApi.getAdmin(hotelId, policyId);
export const createPolicy = (hotelId: string, body: unknown) => policiesApi.create(hotelId, body);
export const updatePolicy = (hotelId: string, policyId: string, body: unknown) => policiesApi.update(hotelId, policyId, body);
export const deletePolicy = (hotelId: string, policyId: string) => policiesApi.remove(hotelId, policyId);
export const getPublicPoliciesByHotel = (hotelId: string) => policiesApi.listPublic(hotelId);
