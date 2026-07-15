import api from "@/lib/axios";
import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";

function toAmenity(raw: any, index = 0) {
  const id = String(raw?.id ?? `amenity-${index + 1}`);
  const label = String(raw?.label ?? raw?.name ?? raw?.key ?? `Amenity ${index + 1}`);
  return {
    ...raw,
      id,
      key: String(raw?.key ?? label.toLowerCase().replace(/[^a-z0-9]+/g, "_")),
      iconKey: raw?.iconKey ?? raw?.icon_key ?? null,
    label,
    name: label,
    type: String(raw?.type ?? "GENERAL"),
    isActive: raw?.isActive ?? raw?.active ?? true,
    sortOrder: Number(raw?.sortOrder ?? index),
  };
}

function toAmenityRequest(body: any) {
  const label = String(body?.name ?? body?.label ?? "").trim();
  return {
      key: body?.key || label.toLowerCase().replace(/[^a-z0-9]+/g, "_").replace(/^_+|_+$/g, ""),
      name: label,
      type: body?.type ?? "GENERAL",
      active: body?.active ?? body?.isActive ?? true,
      iconKey: body?.iconKey,
    };
}

export const amenitiesApi: any = {
  list: async (params?: any) => {
    const payload = await mockOrRequest(
      mockApi.amenities.list().map((item: any, index: number) => toAmenity(item, index)),
      () => api.get("/amenities", { params: { isActive: params?.isActive } }),
    );
    const rows = Array.isArray((payload as any)?.data) ? (payload as any).data : Array.isArray(payload) ? payload : [];
    const query = String(params?.q ?? "").trim().toLowerCase();
    return rows
      .map(toAmenity)
      .filter((item: any) => !query || [item.label, item.key].some((value) => String(value).toLowerCase().includes(query)));
  },
  get: (id: string) => mockOrRequest(toAmenity({ id, name: mockApi.amenities.list()[0] }), () => api.get(`/amenities/${id}`)).then(toAmenity),
  create: (body: unknown) => mockOrRequest(toAmenity(body), () => api.post("/amenities", toAmenityRequest(body))).then(toAmenity),
  update: (id: string, body: unknown) => mockOrRequest(toAmenity({ ...(body as any), id }), () => api.put(`/amenities/${id}`, toAmenityRequest(body))).then(toAmenity),
  remove: (id: string) => mockOrRequest({ ok: true }, () => api.delete(`/amenities/${id}`)),
};

export const getAmenities = async (params?: unknown) => {
  const rows = await amenitiesApi.list(params);
  return { data: rows, meta: { limit: rows.length || 10, offset: 0, total: rows.length } };
};
export const getAmentityById = (id: string) => amenitiesApi.get(id);
export const createAmentity = (body: unknown) => amenitiesApi.create(body);
export const updateAmentity = (id: string, body: unknown) => amenitiesApi.update(id, body);
export const deleteAmentity = (id: string) => amenitiesApi.remove(id);
