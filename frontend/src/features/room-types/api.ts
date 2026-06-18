import { mockApi } from "@/mocks/mockApi";
import { mockOnly } from "@/features/shared/apiClient";

const fallbackRoomImages = ["/globe.svg", "/window.svg", "/file.svg"];

function toKinyiasRoomType(raw: any, index = 0) {
  const id = String(raw?.id ?? `room-type-${index + 1}`);
  const hotelId = String(raw?.hotelId ?? raw?.hotel?.id ?? "");
  const capacity = Number(raw?.capacity ?? raw?.max_guests ?? 1);
  const price = Number(raw?.price_per_night ?? raw?.price ?? raw?.priceFrom ?? 0);

  return {
    ...raw,
    id,
    hotelId,
    name: raw?.name ?? raw?.type ?? "Room",
    price_per_night: price,
    max_guests: Number(raw?.max_guests ?? capacity),
    description: raw?.description ?? "Comfortable room with essential amenities.",
    amenities: Array.isArray(raw?.amenities)
      ? raw.amenities.map((item: any, amenityIndex: number) => {
          if (item?.amenity) {
            return item;
          }

          const label = typeof item === "string" ? item : item?.label ?? `Amenity ${amenityIndex + 1}`;

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
    images: Array.isArray(raw?.images) && raw.images.length > 0
      ? raw.images
      : [
          {
            image_id: `${id}-image`,
            roomtype_id: id,
            url: fallbackRoomImages[index % fallbackRoomImages.length],
          },
        ],
    availableRooms: Number(raw?.availableRooms ?? raw?.available ?? raw?.availableQuantity ?? 0),
  };
}

function listMockRoomTypes(hotelId?: string) {
  return mockApi.rooms.list(hotelId).map(toKinyiasRoomType);
}

function getMockRoomType(id: string) {
  return toKinyiasRoomType(mockApi.rooms.get(id), 0);
}

export const roomTypesApi: any = {
  listByHotel: (hotelId: string) => mockOnly(listMockRoomTypes(hotelId)),
  list: (_params?: unknown) => mockOnly({ data: listMockRoomTypes() }),
  available: (hotelId: string, _params?: unknown) => mockOnly(listMockRoomTypes(hotelId)),
  get: (_hotelId: string, id: string) => mockOnly(getMockRoomType(id)),
  create: (hotelId: string, _body: unknown) => mockOnly(listMockRoomTypes(hotelId)[0]),
  update: (_hotelId: string, id: string, _body: unknown) => mockOnly(getMockRoomType(id)),
  remove: (_hotelId: string, _id: string) => mockOnly({ ok: true }),
};

export const getRoomTypes = async (hotelId: string) => ({ data: await roomTypesApi.listByHotel(hotelId) });
export const listAllRoomTypes = async (limit = 3) => ({ data: (await roomTypesApi.list()).data?.slice?.(0, limit) ?? [] });
export const getRoomTypesAvailable = async (hotelId: string, params?: unknown) => ({ data: await roomTypesApi.available(hotelId, params) });
export const getRoomTypeById = (hotelId: string, id: string) => roomTypesApi.get(hotelId, id);
export const createRoomType = (hotelId: string, body: unknown) => roomTypesApi.create(hotelId, body);
export const updateRoomType = (hotelId: string, id: string, body: unknown) => roomTypesApi.update(hotelId, id, body);
export const deleteRoomType = (hotelId: string, id: string) => roomTypesApi.remove(hotelId, id);
