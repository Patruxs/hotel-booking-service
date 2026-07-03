import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";
import api from "@/lib/axios";

export const contactApi: any = {
  create: (body: unknown) => mockOrRequest({ id: "mock-contact", ok: true }, () => api.post("/contacts", body)),
  listAdmin: (params?: unknown) => mockOrRequest({ data: mockApi.contacts.list() }, () => api.get("/admin/contacts", { params })),
  getAdmin: (id: string) => mockOrRequest(mockApi.contacts.get(id), () => api.get(`/admin/contacts/${id}`)),
  updateAdmin: (id: string, body: unknown) => mockOrRequest(mockApi.contacts.get(id), () => api.patch(`/admin/contacts/${id}`, body)),
};

export const createContact = (body: unknown) => contactApi.create(body);
export const getContacts = (params?: unknown) => contactApi.listAdmin(params);
export const getContactById = (id: string) => contactApi.getAdmin(id);
export const updateContact = (id: string, body: unknown) => contactApi.updateAdmin(id, body);
