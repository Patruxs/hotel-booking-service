import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOnly, mockOrRequest } from "@/features/shared/apiClient";
import { toHotel, toRoom } from "@/features/shared/springMappers";

export const hotelsApi: any = {
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

export const getHotels = async (params?: unknown) => ({ data: await hotelsApi.list(), meta: { limit: 10, offset: 0, total: (await hotelsApi.list()).length } });
export const getPublicHotels = async (params?: unknown) => ({ data: params ? await hotelsApi.search(params) : await hotelsApi.listPublic(), meta: { limit: 12, offset: 0, total: (await hotelsApi.listPublic()).length } });
export const getHotelById = (id: string) => hotelsApi.get(id);
export const getHotelMembers = (_hotelId: string) => hotelsApi.members();
export const createHotel = (body: unknown) => hotelsApi.create(body);
export const updateHotel = (id: string, body: unknown) => hotelsApi.update(id, body);
export const deleteHotel = (id: string) => hotelsApi.remove(id);
export const addMembersToHotel = (_hotelId: string, userIds: string[]) => mockOnly({ ok: true, userIds });
export const removeMemberFromHotel = (_hotelId: string, userId: string) => mockOnly({ ok: true, userId });
