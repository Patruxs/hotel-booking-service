import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";
import { toRoom } from "@/features/shared/springMappers";

export const roomsApi = {
  list: async (hotelId?: string) => {
    const rooms = await mockOrRequest(mockApi.rooms.list(hotelId), () => (hotelId ? api.get(`/hotels/${hotelId}/rooms`) : api.get("/rooms/all")));
    return rooms.map((room) => toRoom(room, hotelId));
  },
  get: async (_hotelId: string, roomId: string) => toRoom(await mockOrRequest(mockApi.rooms.get(roomId), () => api.get(`/rooms/${roomId}`))),
  types: () => mockOrRequest(["SINGLE", "DOUBLE", "TRIPLE", "SUIT"], () => api.get("/rooms/types")),
  available: async (params?: unknown) => (await mockOrRequest(mockApi.rooms.list(), () => api.get("/rooms/all-available-rooms", { params }))).map((room) => toRoom(room)),
  create: (_hotelId: string, body: unknown) => mockOrRequest(mockApi.rooms.list()[0], () => api.post("/rooms/add", body)),
  update: (_hotelId: string, roomId: string, body: unknown) => mockOrRequest(mockApi.rooms.get(roomId), () => api.put(`/rooms/update/${roomId}`, body)),
  remove: (_hotelId: string, roomId: string) => mockOrRequest({ ok: true }, () => api.delete(`/rooms/delete/${roomId}`)),
};
