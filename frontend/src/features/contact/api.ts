import { mockApi } from "@/mocks/mockApi";
import { mockOnly } from "@/features/shared/apiClient";

export const contactApi: any = {
  create: (_body: unknown) => mockOnly({ id: "mock-contact", ok: true }),
  listAdmin: (_params?: unknown) => mockOnly({ data: mockApi.contacts.list() }),
  getAdmin: (id: string) => mockOnly(mockApi.contacts.get(id)),
  updateAdmin: (id: string, _body: unknown) => mockOnly(mockApi.contacts.get(id)),
};

export const createContact = (body: unknown) => contactApi.create(body);
export const getContacts = (params?: unknown) => contactApi.listAdmin(params);
export const getContactById = (id: string) => contactApi.getAdmin(id);
export const updateContact = (id: string, body: unknown) => contactApi.updateAdmin(id, body);
