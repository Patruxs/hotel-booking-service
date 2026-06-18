import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

export const policiesApi = {
  listAdmin: (hotelId: string) => mockOrRequest(mockApi.policies.list(hotelId), () => api.get(`/admin/hotels/${hotelId}/policies`)),
  getAdmin: (hotelId: string, policyId: string) => mockOrRequest(mockApi.policies.list(hotelId)[0], () => api.get(`/admin/hotels/${hotelId}/policies/${policyId}`)),
  create: (hotelId: string, body: unknown) => mockOrRequest({ ok: true }, () => api.post(`/admin/hotels/${hotelId}/policies`, body)),
  update: (hotelId: string, policyId: string, body: unknown) => mockOrRequest({ ok: true }, () => api.patch(`/admin/hotels/${hotelId}/policies/${policyId}`, body)),
  remove: (hotelId: string, policyId: string) => mockOrRequest({ ok: true }, () => api.delete(`/admin/hotels/${hotelId}/policies/${policyId}`)),
  listPublic: (hotelId: string) => mockOrRequest(mockApi.policies.list(hotelId), () => api.get(`/hotels/${hotelId}/policies`)),
};
