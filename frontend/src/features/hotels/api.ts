import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";
import { toHotel, toRoom } from "@/features/shared/springMappers";

export const hotelsApi = {
  list: async () => (await mockOrRequest(mockApi.hotels.list(), () => api.get("/hotels/all"))).map(toHotel),
  listPublic: async () => (await mockOrRequest(mockApi.hotels.list(), () => api.get("/hotels/all"))).map(toHotel),
  search: async (params: unknown) => (await mockOrRequest(mockApi.hotels.list(), () => api.get("/hotels/search", { params }))).map(toHotel),
  get: async (id: string) => toHotel(await mockOrRequest(mockApi.hotels.get(id), () => api.get(`/hotels/${id}`))),
  rooms: async (hotelId: string) => (await mockOrRequest(mockApi.rooms.list(hotelId), () => api.get(`/hotels/${hotelId}/rooms`))).map((room) => toRoom(room, hotelId)),
  create: (body: unknown) => mockOrRequest(mockApi.hotels.list()[0], () => api.post("/hotels/add", body)),
  update: (id: string, body: unknown) => mockOrRequest(mockApi.hotels.get(id), () => api.put(`/hotels/update/${id}`, body)),
  remove: (id: string) => mockOrRequest({ ok: true }, () => api.delete(`/hotels/delete/${id}`)),
  members: () => mockOrRequest(mockApi.users.list(), () => api.get("/hotels/my-hotels")),
};
