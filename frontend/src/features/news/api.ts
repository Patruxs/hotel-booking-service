import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";
import api from "@/lib/axios";
import { NewsStatus, type News, type NewsListParams, type NewsListResponse } from "@/features/news/types";

function slugify(value: string) {
  return value
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

function toNews(item: any, index: number): News {
  const id = String(item?.id ?? `news-${index + 1}`);
  const title = String(item?.title ?? `News ${index + 1}`);

  return {
    id,
    title,
    slug: item?.slug ?? slugify(id || title),
    summary: item?.summary ?? item?.excerpt ?? null,
    content: item?.content ?? null,
    status: item?.status ?? NewsStatus.PUBLISHED,
    publishedAt: item?.publishedAt ?? item?.createdAt ?? null,
    createdAt: item?.createdAt ?? item?.publishedAt ?? new Date(0).toISOString(),
    updatedAt: item?.updatedAt ?? item?.publishedAt ?? new Date(0).toISOString(),
    images: Array.isArray(item?.images) ? item.images : [],
  };
}

function listMockNews(params?: NewsListParams): NewsListResponse {
  const page = params?.page ?? 1;
  const limit = params?.limit ?? mockApi.news.list().length;
  const allItems = mockApi.news.list().map(toNews);
  const q = params?.q?.toLowerCase().trim();
  const filteredItems = q
    ? allItems.filter((item) =>
        [item.title, item.summary, item.content].some((value) =>
          value?.toLowerCase().includes(q),
        ),
      )
    : allItems;
  const start = (page - 1) * limit;

  return {
    page,
    limit,
    total: filteredItems.length,
    items: filteredItems.slice(start, start + limit),
  };
}

function getMockNews(idOrSlug: string): News {
  const items = mockApi.news.list().map(toNews);

  return (
    items.find((item) => item.id === idOrSlug || item.slug === idOrSlug) ??
    items[0] ??
    toNews(null, 0)
  );
}

function normalizeList(payload: any): NewsListResponse {
  if (Array.isArray(payload?.data) && !payload.items) {
    return { page: payload.page ?? 1, limit: payload.limit ?? payload.data.length, total: payload.total ?? payload.data.length, items: payload.data.map(toNews) };
  }
  if (Array.isArray(payload?.items)) {
    return { ...payload, items: payload.items.map(toNews) };
  }
  return listMockNews();
}

export const newsApi: any = {
  listAdmin: (params?: NewsListParams) => mockOrRequest(listMockNews(params), () => api.get("/admin/news", { params })).then(normalizeList),
  getAdmin: (id: string) => mockOrRequest(getMockNews(id), () => api.get(`/admin/news/${id}`)).then((item) => toNews(item, 0)),
  create: (body: unknown) => mockOrRequest(getMockNews(""), () => api.post("/admin/news", body)).then((item) => toNews(item, 0)),
  update: (id: string, body: unknown) => mockOrRequest(getMockNews(id), () => api.put(`/admin/news/${id}`, body)).then((item) => toNews(item, 0)),
  remove: (id: string) => mockOrRequest({ ok: true }, () => api.delete(`/admin/news/${id}`)),
  listPublic: (params?: NewsListParams) => mockOrRequest(listMockNews(params), () => api.get("/news", { params })).then(normalizeList),
  getPublic: (slug: string) => mockOrRequest(getMockNews(slug), () => api.get(`/news/${slug}`)).then((item) => toNews(item, 0)),
};

export const getNewsList = (params?: unknown) => newsApi.listAdmin(params);
export const getNewsDetail = (id: string) => newsApi.getAdmin(id);
export const createNews = (body: unknown) => newsApi.create(body);
export const updateNews = (id: string, body: unknown) => newsApi.update(id, body);
export const deleteNews = (id: string) => newsApi.remove(id);
export const getPublicNewsList = (params?: unknown) => newsApi.listPublic(params);
export const getPublicNewsDetail = (slug: string) => newsApi.getPublic(slug);
