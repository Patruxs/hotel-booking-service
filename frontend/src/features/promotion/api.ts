import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";
import api from "@/lib/axios";
import type { Promotion, PromotionsQueryParams } from "./types";

type PromotionListResponse = {
  data: Promotion[];
  meta: {
    limit: number;
    offset: number;
    total: number;
  };
  page: number;
  limit: number;
  total: number;
  totalPages: number;
};

function numberOrNull(value: unknown): string | null {
  if (value === null || value === undefined || value === "") {
    return null;
  }
  return String(value);
}

function toPromotion(item: any, index = 0): Promotion {
  const id = String(item?.id ?? `promotion-${index + 1}`);

  return {
    id,
    code: String(item?.code ?? ""),
    name: String(item?.name ?? item?.code ?? "Promotion"),
    description: item?.description ?? null,
    discountType: item?.discountType === "FIXED" ? "FIXED" : "PERCENT",
    discountValue: Number(item?.discountValue ?? 0),
    maxDiscountAmount: numberOrNull(item?.maxDiscountAmount ?? item?.maxDiscount),
    minBookingAmount: numberOrNull(item?.minBookingAmount),
    totalUsageLimit: item?.totalUsageLimit ?? null,
    usedCount: Number(item?.usedCount ?? 0),
    perUserLimit: item?.perUserLimit ?? item?.perUserUsageLimit ?? null,
    startAt: item?.startAt ?? item?.startsAt ?? new Date(0).toISOString(),
    endAt: item?.endAt ?? item?.endsAt ?? new Date(0).toISOString(),
    isActive: Boolean(item?.isActive ?? item?.active ?? true),
    hotelId: item?.hotelId ?? item?.hotel?.id ?? null,
    hotel: item?.hotel ?? null,
    createdAt: item?.createdAt ?? new Date(0).toISOString(),
    updatedAt: item?.updatedAt ?? new Date(0).toISOString(),
  };
}

function listMockPromotions(params?: PromotionsQueryParams): PromotionListResponse {
  const page = params?.page ?? 1;
  const limit = params?.limit ?? mockApi.promotions.list().length;
  const search = params?.search?.trim().toLowerCase();
  const allItems = mockApi.promotions.list().map(toPromotion);
  const filteredItems = allItems.filter((promotion) => {
    const matchesSearch = search
      ? [promotion.code, promotion.name].some((value) => value.toLowerCase().includes(search))
      : true;
    const matchesStatus = params?.isActive === undefined ? true : promotion.isActive === params.isActive;
    const matchesHotel = params?.hotelId ? promotion.hotelId === params.hotelId || promotion.hotelId === null : true;
    return matchesSearch && matchesStatus && matchesHotel;
  });
  const start = (page - 1) * limit;

  return {
    data: filteredItems.slice(start, start + limit),
    meta: { limit, offset: start, total: filteredItems.length },
    page,
    limit,
    total: filteredItems.length,
    totalPages: Math.ceil(filteredItems.length / limit),
  };
}

function normalizeList(payload: any): PromotionListResponse {
  if (Array.isArray(payload)) {
    const data = payload.map(toPromotion);
    return { data, meta: { limit: data.length, offset: 0, total: data.length }, page: 1, limit: data.length, total: data.length, totalPages: 1 };
  }
  if (Array.isArray(payload?.data)) {
    const data = payload.data.map(toPromotion);
    return {
      data,
      meta: payload.meta ?? { limit: payload.limit ?? data.length, offset: ((payload.page ?? 1) - 1) * (payload.limit ?? data.length), total: payload.total ?? data.length },
      page: payload.page ?? 1,
      limit: payload.limit ?? data.length,
      total: payload.total ?? payload.meta?.total ?? data.length,
      totalPages: payload.totalPages ?? 1,
    };
  }
  throw new Error("Unexpected promotion list response from Spring API");
}

function normalizePublicList(payload: any): PromotionListResponse {
  return normalizeList(Array.isArray(payload) ? payload : payload?.data ?? payload);
}

function toSpringPayload(body: any) {
  return {
    hotelId: body?.hotelId || null,
    code: body?.code,
    name: body?.name,
    description: body?.description || null,
    discountType: body?.discountType,
    discountValue: body?.discountValue,
    maxDiscountAmount: body?.maxDiscountAmount ?? null,
    minBookingAmount: body?.minBookingAmount ?? null,
    totalUsageLimit: body?.totalUsageLimit ?? null,
    perUserLimit: body?.perUserLimit ?? null,
    startAt: body?.startAt || null,
    endAt: body?.endAt || null,
    isActive: body?.isActive ?? true,
  };
}

export const promotionApi: any = {
  list: (params?: PromotionsQueryParams) => mockOrRequest(listMockPromotions(params), () => api.get("/admin/promotions", { params })).then(normalizeList),
  listPublic: (params?: PromotionsQueryParams) =>
    mockOrRequest(listMockPromotions(params), () => api.get("/promotions/public", { params })).then(normalizePublicList),
  get: (id: string) => mockOrRequest(listMockPromotions().data[0], () => api.get(`/admin/promotions/${id}`)).then((item) => toPromotion(item, 0)),
  create: (body: unknown) => mockOrRequest(listMockPromotions().data[0], () => api.post("/admin/promotions", toSpringPayload(body))).then((item) => toPromotion(item, 0)),
  update: (id: string, body: unknown) => mockOrRequest(listMockPromotions().data[0], () => api.patch(`/admin/promotions/${id}`, toSpringPayload(body))).then((item) => toPromotion(item, 0)),
  remove: (id: string) => mockOrRequest({ ok: true }, () => api.delete(`/admin/promotions/${id}`)),
};

export const getPromotions = (params?: PromotionsQueryParams) => promotionApi.list(params);
export const getPublicPromotions = (params?: PromotionsQueryParams) => promotionApi.listPublic(params);
export const getPromotionById = (id: string) => promotionApi.get(id);
export const createPromotion = (body: unknown) => promotionApi.create(body);
export const updatePromotion = (id: string, body: unknown) => promotionApi.update(id, body);
export const deletePromotion = (id: string) => promotionApi.remove(id);
