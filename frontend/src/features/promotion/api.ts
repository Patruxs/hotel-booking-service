import { mockApi } from "@/mocks/mockApi";
import { mockOnly } from "@/features/shared/apiClient";

export const promotionApi: any = {
  list: (_params?: unknown) => mockOnly({ data: mockApi.promotions.list() }),
  listPublic: (_params?: unknown) => mockOnly({ data: mockApi.promotions.list() }),
  get: (id: string) => mockOnly(mockApi.promotions.get(id)),
  create: (_body: unknown) => mockOnly(mockApi.promotions.list()[0]),
  update: (id: string, _body: unknown) => mockOnly(mockApi.promotions.get(id)),
  remove: (_id: string) => mockOnly({ ok: true }),
};

export const getPromotions = (params?: unknown) => promotionApi.list(params);
export const getPublicPromotions = (params?: unknown) => promotionApi.listPublic(params);
export const getPromotionById = (id: string) => promotionApi.get(id);
export const createPromotion = (body: unknown) => promotionApi.create(body);
export const updatePromotion = (id: string, body: unknown) => promotionApi.update(id, body);
export const deletePromotion = (id: string) => promotionApi.remove(id);
