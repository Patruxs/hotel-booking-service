import { mockApi } from "@/mocks/mockApi";
import { mockOnly } from "@/features/shared/apiClient";

export const inventoryApi: any = {
  list: (hotelId: string, _params?: unknown) => mockOnly({ data: mockApi.inventory.list().filter((item) => item.hotelId === hotelId) }),
  bulkSet: (_hotelId: string, _body: unknown) => mockOnly({ ok: true }),
  update: (_hotelId: string, _id: string, _body: unknown) => mockOnly({ ok: true }),
};

export const getInventory = (hotelId: string, params?: unknown) => inventoryApi.list(hotelId, params);
export const createInventory = (hotelId: string, body: unknown) => inventoryApi.bulkSet(hotelId, body);
export const updateInventory = (hotelId: string, id: string, body: unknown) => inventoryApi.update(hotelId, id, body);
