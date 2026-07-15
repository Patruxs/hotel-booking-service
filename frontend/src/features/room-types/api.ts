import { mockApi } from "@/mocks/mockApi";
import api from "@/lib/axios";
import { mockOrRequest } from "@/features/shared/apiClient";

const fallbackRoomImages = ["/globe.svg", "/window.svg", "/file.svg"];

function toKinyiasRoomType(raw: any, index = 0) {
  const id = String(raw?.id ?? `room-type-${index + 1}`);
  const hotelId = String(raw?.hotelId ?? raw?.hotel?.id ?? "");
  const capacity = Number(raw?.capacity ?? raw?.max_guests ?? 1);
  const price = Number(
    raw?.price_per_night ?? raw?.price ?? raw?.priceFrom ?? 0,
  );

  return {
    ...raw,
    id,
    hotelId,
    name: raw?.name ?? raw?.type ?? "Room",
    price_per_night: price,
    max_guests: Number(raw?.max_guests ?? capacity),
    description:
      raw?.description ?? "Comfortable room with essential amenities.",
    amenities: Array.isArray(raw?.amenities)
      ? raw.amenities.map((item: any, amenityIndex: number) => {
          if (item?.amenity) {
            return item;
          }

          const label =
            typeof item === "string"
              ? item
              : (item?.label ?? `Amenity ${amenityIndex + 1}`);

          return {
            amenity: {
              id: item?.id ?? `${id}-amenity-${amenityIndex + 1}`,
              label,
              value: label,
            },
            amenityId: item?.id ?? `${id}-amenity-${amenityIndex + 1}`,
            typeId: id,
          };
        })
      : [],
    images:
      Array.isArray(raw?.images) && raw.images.length > 0
        ? raw.images
        : [
            {
              image_id: `${id}-image`,
              roomtype_id: id,
              url: fallbackRoomImages[index % fallbackRoomImages.length],
            },
          ],
    availableRooms: Number(
      raw?.availableRooms ?? raw?.available ?? raw?.availableQuantity ?? 0,
    ),
  };
}

function listMockRoomTypes(hotelId?: string) {
  return mockApi.rooms.list(hotelId).map(toKinyiasRoomType);
}

function getMockRoomType(id: string) {
  return toKinyiasRoomType(mockApi.rooms.get(id), 0);
}

export const roomTypesApi: any = {
  publicListByHotel: async (hotelId: string) => {
    const payload = await mockOrRequest(
      { data: listMockRoomTypes(hotelId) },
      () => api.get(`/hotels/${hotelId}/room-types`),
    );
    const rows = Array.isArray(payload?.data)
      ? payload.data
      : Array.isArray(payload)
        ? payload
        : [];
    return rows.map(toKinyiasRoomType);
  },
  listByHotel: async (hotelId: string) => {
    const payload = await mockOrRequest(
      { data: listMockRoomTypes(hotelId) },
      () => api.get(`/hotels/${hotelId}/room-types`, { params: { manage: true } }),
    );
    const rows = Array.isArray(payload?.data)
      ? payload.data
      : Array.isArray(payload)
        ? payload
        : [];
    return rows.map(toKinyiasRoomType);
  },
  list: async (params?: any) => {
    const hotelId = params?.hotelId;
    if (!hotelId) {
      return { data: [] };
    }
    return { data: await roomTypesApi.listByHotel(hotelId) };
  },
  available: async (hotelId: string, params?: unknown) => {
    const payload = await mockOrRequest(
      { data: listMockRoomTypes(hotelId) },
      () => api.get(`/hotels/${hotelId}/room-types/available`, { params }),
    );
    const rows = Array.isArray(payload?.data)
      ? payload.data
      : Array.isArray(payload)
        ? payload
        : [];
    return rows.map(toKinyiasRoomType);
  },
  get: async (hotelId: string, id: string) =>
    toKinyiasRoomType(
      await mockOrRequest(getMockRoomType(id), () =>
        api.get(`/hotels/${hotelId}/room-types/${id}`, {
          params: { manage: true },
        }),
      ),
    ),
  create: async (hotelId: string, body: unknown) =>
    toKinyiasRoomType(
      await mockOrRequest(listMockRoomTypes(hotelId)[0], () =>
        api.post(`/hotels/${hotelId}/room-types`, body),
      ),
    ),
  update: async (_hotelId: string, id: string, body: unknown) =>
    toKinyiasRoomType(
      await mockOrRequest(getMockRoomType(id), () =>
        api.patch(`/hotels/${_hotelId}/room-types/${id}`, body),
      ),
    ),
  remove: (_hotelId: string, _id: string) =>
    mockOrRequest({ ok: true }, () =>
      api.delete(`/hotels/${_hotelId}/room-types/${_id}`),
    ),
};

export const getRoomTypes = async (hotelId: string) => ({
  data: await roomTypesApi.listByHotel(hotelId),
});
export const getPublicRoomTypes = async (hotelId: string) => ({
  data: await roomTypesApi.publicListByHotel(hotelId),
});
export const listAllRoomTypes = async (limit = 3) => {
  const payload = await mockOrRequest({ data: listMockRoomTypes() }, () =>
    api.get("/room-types", { params: { limit } }).catch(() => ({ data: [] })),
  );
  const rows = Array.isArray(payload?.data)
    ? payload.data
    : Array.isArray(payload)
      ? payload
      : [];
  return {
    data: rows.map(toKinyiasRoomType).slice(0, limit),
  };
};
export const getRoomTypesAvailable = async (
  hotelId: string,
  params?: unknown,
) => ({ data: await roomTypesApi.available(hotelId, params) });
export const getRoomTypeById = (hotelId: string, id: string) =>
  roomTypesApi.get(hotelId, id);
export const createRoomType = (hotelId: string, body: unknown) =>
  roomTypesApi.create(hotelId, body);
export const updateRoomType = (hotelId: string, id: string, body: unknown) =>
  roomTypesApi.update(hotelId, id, body);
export const deleteRoomType = (hotelId: string, id: string) =>
  roomTypesApi.remove(hotelId, id);
