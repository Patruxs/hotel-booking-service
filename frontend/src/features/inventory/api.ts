import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

function toInventory(raw: any) {
  return {
    ...raw,
    id: String(raw?.id ?? ""),
    hotelId: String(raw?.hotelId ?? raw?.hotel?.id ?? ""),
    roomTypeId: String(raw?.roomTypeId ?? raw?.roomType?.id ?? ""),
    date: raw?.date ?? raw?.stayDate ?? "",
    totalRooms: Number(raw?.totalRooms ?? 0),
    availableRooms: Number(raw?.availableRooms ?? 0),
    stopSell: Boolean(raw?.stopSell),
  };
}

function toInventoryRequest(body: any) {
  return {
    date: body?.date ?? body?.from,
    totalRooms: Number(body?.totalRooms ?? 0),
    availableRooms: Number(body?.availableRooms ?? body?.totalRooms ?? 0),
    stopSell: Boolean(body?.stopSell),
  };
}

function eachDate(from: string, to: string) {
  const dates: string[] = [];
  const current = new Date(`${from}T00:00:00`);
  const last = new Date(`${to}T00:00:00`);
  while (
    !Number.isNaN(current.getTime()) &&
    !Number.isNaN(last.getTime()) &&
    current <= last
  ) {
    dates.push(current.toISOString().slice(0, 10));
    current.setDate(current.getDate() + 1);
  }
  return dates.length > 0 ? dates : [from];
}

export const inventoryApi: any = {
  list: async (hotelId: string, params?: any) => {
    const roomTypeId = params?.roomTypeId;
    if (!hotelId || !roomTypeId) {
      return [];
    }
    const payload: any = await mockOrRequest(
      mockApi.inventory
        .list()
        .filter(
          (item: any) =>
            item.hotelId === hotelId && item.roomTypeId === roomTypeId,
        ),
      () =>
        api.get(`/hotels/${hotelId}/room-types/${roomTypeId}/inventory`, {
          params,
        }),
    );
    const rows = Array.isArray(payload?.data)
      ? payload.data
      : Array.isArray(payload)
        ? payload
        : [];
    return rows.map(toInventory);
  },
  bulkSet: async (hotelId: string, body: any) => {
    const roomTypeId = body?.roomTypeId;
    const dates = eachDate(body?.from, body?.to ?? body?.from);
    if (dates.length === 1) {
      const payload = await mockOrRequest(
        mockApi.inventory
          .list()
          .find(
            (item: any) =>
              item.hotelId === hotelId && item.roomTypeId === roomTypeId,
          ) ?? mockApi.inventory.list()[0],
        () =>
          api.put(
            `/hotels/${hotelId}/room-types/${roomTypeId}/inventory`,
            toInventoryRequest({ ...body, date: dates[0] }),
          ),
      );
      return toInventory(payload);
    }
    const payload: any = await mockOrRequest(
      dates.map((date) =>
        toInventory(
          mockApi.inventory
            .list()
            .find(
              (item: any) =>
                item.hotelId === hotelId && item.roomTypeId === roomTypeId && item.date === date,
            ) ??
            mockApi.inventory.list().find(
              (item: any) => item.hotelId === hotelId && item.roomTypeId === roomTypeId,
            ) ?? mockApi.inventory.list()[0],
        ),
      ),
      () =>
        api.put(`/hotels/${hotelId}/room-types/${roomTypeId}/inventory/bulk`, {
          from: body?.from,
          to: body?.to ?? body?.from,
          totalRooms: Number(body?.totalRooms ?? 0),
          availableRooms: Number(body?.availableRooms ?? body?.totalRooms ?? 0),
          stopSell: Boolean(body?.stopSell),
        }),
    );
    const rows = Array.isArray(payload?.data)
      ? payload.data
      : Array.isArray(payload)
        ? payload
        : [];
    return rows.map(toInventory);
  },
  update: async (hotelId: string, _id: string, body: any) => {
    const roomTypeId = body?.roomTypeId;
    const payload = await mockOrRequest(
      mockApi.inventory.list().find((item: any) => item.id === _id) ??
        mockApi.inventory.list()[0],
      () =>
        api.put(
          `/hotels/${hotelId}/room-types/${roomTypeId}/inventory`,
          toInventoryRequest(body),
        ),
    );
    return toInventory(payload);
  },
  remove: (hotelId: string, roomTypeId: string, inventoryId: string) =>
    mockOrRequest(
      { ok: true },
      () => api.delete(`/hotels/${hotelId}/room-types/${roomTypeId}/inventory/${inventoryId}`),
    ),
};

export const getInventory = (hotelId: string, params?: unknown) =>
  inventoryApi.list(hotelId, params);
export const createInventory = (hotelId: string, body: unknown) =>
  inventoryApi.bulkSet(hotelId, body);
export const updateInventory = (hotelId: string, id: string, body: unknown) =>
  inventoryApi.update(hotelId, id, body);
export const deleteInventory = (hotelId: string, roomTypeId: string, id: string) =>
  inventoryApi.remove(hotelId, roomTypeId, id);
