import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";
import { toHotel, toRoom } from "@/features/shared/springMappers";

const toListPage = (payload: any, fallbackLimit = 10) => {
  const rows = Array.isArray(payload?.data)
    ? payload.data
    : Array.isArray(payload)
      ? payload
      : [];
  return {
    data: rows.map(toHotel),
    meta: payload?.meta ?? {
      limit: rows.length || fallbackLimit,
      offset: 0,
      total: rows.length,
    },
  };
};

const toHotelMember = (raw: any) => {
  const accountId = String(raw?.accountId ?? raw?.userId ?? raw?.user?.id ?? "");
  return {
    ...raw,
    id: accountId,
    hotelId: String(raw?.hotelId ?? ""),
    userId: accountId,
    createdAt: raw?.createdAt ?? "",
    owner: Boolean(raw?.owner),
    user: raw?.user ?? {
      id: accountId,
      email: raw?.email ?? "",
      firstName: raw?.firstName ?? "",
      lastName: raw?.lastName ?? "",
    },
  };
};

function toHotelMutationRequest(body: any) {
  return {
    name: body?.name,
    description: body?.description,
    address: body?.address,
    city: body?.city,
    country: body?.country,
    email: body?.email,
    phone: body?.phone,
    starRating: body?.starRating ?? body?.rating,
    status: body?.status,
  };
}

async function replaceHotelImages(id: string, body: any) {
  if (!Array.isArray(body?.images)) {
    return;
  }

  const imageIds = body.images.map((image: any) => image?.id);
  if (imageIds.some((imageId: unknown) => typeof imageId !== "string" || imageId.length === 0)) {
    throw new Error("Hotel images must be selected from the media gallery");
  }

  await mockOrRequest([], () => api.put(`/hotels/${id}/images`, { imageIds }));
}

export const hotelsApi: any = {
  list: async (params?: unknown) =>
    toListPage(
      await mockOrRequest({ data: mockApi.hotels.list() }, () =>
        api.get("/hotels/manageable", {
          params: {
            limit: (params as any)?.limit ?? 10,
            offset: (params as any)?.offset ?? Math.max(0, (((params as any)?.page ?? 1) - 1) * ((params as any)?.limit ?? 10)),
          },
        }),
      ),
    ),
  listPublic: async (params: any) =>
    toListPage(
      await mockOrRequest({ data: mockApi.hotels.list() }, () => {
        if (params?.city || params?.checkIn || params?.checkOut || params?.rooms || params?.adults || params?.children) {
          const queryParams = new URLSearchParams();
          if (params.city) queryParams.append("location", params.city);
          else queryParams.append("location", "");

          if (params.checkIn) queryParams.append("checkInDate", params.checkIn);

          let checkOutVal = params.checkOut;
          if (params.checkIn && params.checkOut && params.checkIn === params.checkOut) {
            const coDate = new Date(params.checkOut);
            coDate.setDate(coDate.getDate() + 1);
            checkOutVal = coDate.toISOString().split('T')[0];
          }
          if (checkOutVal) queryParams.append("checkOutDate", checkOutVal);

          const adults = params.adults ? Number(params.adults) : 1;
          const children = params.children ? Number(params.children) : 0;
          const capacity = adults + children;
          if (capacity > 0) queryParams.append("capacity", capacity.toString());

          if (params.rooms) queryParams.append("roomQuantity", params.rooms.toString());

          return api.get(`/hotels/search?${queryParams.toString()}`);
        }
          return api.get("/hotels", { params: { limit: params?.limit || 12, offset: ((params?.page || 1) - 1) * (params?.limit || 12) } });
      }),
      12,
    ),
  search: async (params: any) =>
    toListPage(
      await mockOrRequest({ data: mockApi.hotels.list() }, () => {
        const queryParams = new URLSearchParams();
        if (params?.city) queryParams.append("location", params.city);
        else queryParams.append("location", "");

        if (params?.checkIn) queryParams.append("checkInDate", params.checkIn);

        let checkOutVal = params?.checkOut;
        if (params?.checkIn && params?.checkOut && params.checkIn === params.checkOut) {
          const coDate = new Date(params.checkOut);
          coDate.setDate(coDate.getDate() + 1);
          checkOutVal = coDate.toISOString().split('T')[0];
        }
        if (checkOutVal) queryParams.append("checkOutDate", checkOutVal);

        const adults = params?.adults ? Number(params.adults) : 1;
        const children = params?.children ? Number(params.children) : 0;
        const capacity = adults + children;
        if (capacity > 0) queryParams.append("capacity", capacity.toString());

        if (params?.rooms) queryParams.append("roomQuantity", params.rooms.toString());

        return api.get(`/hotels/search?${queryParams.toString()}`);
      }),
      12,
    ),
  get: async (id: string) =>
    toHotel(
      await mockOrRequest(mockApi.hotels.get(id), () =>
        api.get(`/hotels/${id}`),
      ),
    ),
  getManage: async (id: string) =>
    toHotel(
      await mockOrRequest(mockApi.hotels.get(id), () =>
        api.get(`/hotels/${id}/manage`),
      ),
    ),
  rooms: async (hotelId: string) =>
    (
      await mockOrRequest(mockApi.rooms.list(hotelId), () =>
        api.get(`/hotels/${hotelId}/rooms`),
      )
    ).map((room) => toRoom(room, hotelId)),
  create: async (body: unknown) => {
    const hotel = await mockOrRequest(mockApi.hotels.list()[0], () =>
      api.post("/hotels", toHotelMutationRequest(body)),
    );
    if (Array.isArray((body as any)?.images)) {
      const id = String((hotel as any)?.id ?? "");
      if (!id) {
        throw new Error("Created hotel did not return an ID for image persistence");
      }
      await replaceHotelImages(id, body);
    }
    return hotel;
  },
  update: async (id: string, body: unknown) => {
    const hotel = await mockOrRequest(mockApi.hotels.get(id), () =>
      api.patch(`/hotels/${id}`, toHotelMutationRequest(body)),
    );
    await replaceHotelImages(id, body);
    return hotel;
  },
  remove: (id: string) =>
    mockOrRequest({ ok: true }, () => api.delete(`/hotels/${id}`)),
  members: async (hotelId: string) => {
    const payload = await mockOrRequest(mockApi.users.list(), () =>
      api.get(`/hotels/${hotelId}/members`),
    );
    const rows = Array.isArray((payload as any)?.data) ? (payload as any).data : Array.isArray(payload) ? payload : [];
    return rows.map((member: any) => toHotelMember({ ...member, hotelId }));
  },
  memberCandidates: async (hotelId: string, q?: string) => {
    const payload = await mockOrRequest(mockApi.users.list(), () =>
      api.get(`/hotels/${hotelId}/member-candidates`, { params: { q } }),
    );
    const rows = Array.isArray((payload as any)?.data) ? (payload as any).data : Array.isArray(payload) ? payload : [];
    return rows.map((candidate: any) => ({
      id: String(candidate?.accountId ?? candidate?.id ?? ""),
      email: candidate?.email ?? "",
      firstName: candidate?.firstName ?? "",
      lastName: candidate?.lastName ?? "",
    }));
  },
  addMembers: (hotelId: string, userIds: string[]) =>
    mockOrRequest({ ok: true, userIds }, () =>
      api.post(`/hotels/${hotelId}/members`, { accountIds: userIds }),
    ),
  removeMember: (hotelId: string, userId: string) =>
    mockOrRequest({ ok: true, userId }, () =>
      api.delete(`/hotels/${hotelId}/members/${userId}`),
    ),
};

export const getHotels = (params?: unknown) => hotelsApi.list(params);
export const getPublicHotels = (params?: unknown) =>
  hotelsApi.listPublic(params);
export const getHotelById = (id: string) => hotelsApi.get(id);
export const getManageHotelById = (id: string) => hotelsApi.getManage(id);
export const getHotelMembers = (hotelId: string) => hotelsApi.members(hotelId);
export const getHotelMemberCandidates = (hotelId: string, q?: string) => hotelsApi.memberCandidates(hotelId, q);
export const createHotel = (body: unknown) => hotelsApi.create(body);
export const updateHotel = (id: string, body: unknown) =>
  hotelsApi.update(id, body);
export const deleteHotel = (id: string) => hotelsApi.remove(id);
export const addMembersToHotel = (hotelId: string, userIds: string[]) =>
  hotelsApi.addMembers(hotelId, userIds);
export const removeMemberFromHotel = (hotelId: string, userId: string) =>
  hotelsApi.removeMember(hotelId, userId);
