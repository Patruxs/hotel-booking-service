import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

function toAdminRoom(raw: any, hotelId?: string) {
  const active = raw?.active ?? raw?.status !== "INACTIVE";
  const condition = raw?.condition ?? raw?.cleanStatus ?? "CLEAN";
  return {
    ...raw,
    id: String(raw?.id ?? ""),
    hotelId: String(raw?.hotelId ?? raw?.hotel?.id ?? hotelId ?? ""),
    roomTypeId: String(raw?.roomTypeId ?? raw?.roomType?.id ?? ""),
    code: raw?.code ?? raw?.roomNumber ?? "",
    floor: raw?.floor ?? "",
    note: raw?.note ?? "",
    status: raw?.status ?? (active ? "ACTIVE" : "INACTIVE"),
    cleanStatus: condition,
  };
}

export const roomsApi: any = {
  list: async (hotelId?: string) => {
    const rooms = await mockOrRequest(mockApi.rooms.list(hotelId), () =>
      hotelId ? api.get(`/hotels/${hotelId}/rooms`) : api.get("/rooms/all"),
    );
    return rooms.map((room) => toAdminRoom(room, hotelId));
  },
  get: async (_hotelId: string, roomId: string) =>
    toAdminRoom(
      await mockOrRequest(mockApi.rooms.get(roomId), () =>
        api.get(`/hotels/${_hotelId}/rooms/${roomId}`),
      ),
    ),
  types: () =>
    mockOrRequest(["SINGLE", "DOUBLE", "TRIPLE", "SUIT"], () =>
      api.get("/rooms/types"),
    ),
  available: async (params?: unknown) =>
    (
      await mockOrRequest(mockApi.rooms.list(), () =>
        api.get("/rooms/all-available-rooms", { params }),
      )
    ).map((room) => toAdminRoom(room)),
  create: (_hotelId: string, body: any) =>
    mockOrRequest(mockApi.rooms.list()[0], () =>
      api.post(`/hotels/${_hotelId}/rooms`, {
        roomTypeId: body?.roomTypeId,
        roomNumber: body?.roomNumber ?? body?.code,
        condition: body?.condition ?? body?.cleanStatus,
        active: body?.active ?? body?.status !== "INACTIVE",
      }),
    ),
    update: (_hotelId: string, roomId: string, body: any) =>
    mockOrRequest(mockApi.rooms.get(roomId), () =>
      api.patch(`/hotels/${_hotelId}/rooms/${roomId}`, {
        roomTypeId: body?.roomTypeId,
        roomNumber: body?.roomNumber ?? body?.code,
        condition: body?.condition ?? body?.cleanStatus,
        active: body?.active ?? body?.status !== "INACTIVE",
      }),
      ),
    updateCondition: (_hotelId: string, roomId: string, condition: string) =>
      mockOrRequest(mockApi.rooms.get(roomId), () =>
        api.patch(`/hotels/${_hotelId}/rooms/${roomId}/condition`, { condition }),
      ),
  remove: (_hotelId: string, roomId: string) =>
    mockOrRequest({ ok: true }, () =>
      api.delete(`/hotels/${_hotelId}/rooms/${roomId}`),
    ),
};

export const getRooms = async (hotelId: string, params?: any) => {
  const rooms = await roomsApi.list(hotelId);
  return {
    data: params?.roomTypeId
      ? rooms.filter((room: any) => room.roomTypeId === params.roomTypeId)
      : rooms,
  };
};
export const getRoomById = (hotelId: string, roomId: string) =>
  roomsApi.get(hotelId, roomId);
export const createRoom = (hotelId: string, body: unknown) =>
  roomsApi.create(hotelId, body);
export const updateRoom = (hotelId: string, roomId: string, body: unknown) =>
  roomsApi.update(hotelId, roomId, body);
export const updateRoomCondition = (hotelId: string, roomId: string, condition: string) =>
  roomsApi.updateCondition(hotelId, roomId, condition);
export const deleteRoom = (hotelId: string, roomId: string) =>
  roomsApi.remove(hotelId, roomId);
