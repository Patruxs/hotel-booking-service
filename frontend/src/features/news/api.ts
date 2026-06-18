import { mockApi } from "@/mocks/mockApi";
import { mockOnly } from "@/features/shared/apiClient";
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

export const newsApi: any = {
  listAdmin: (params?: NewsListParams) => mockOnly(listMockNews(params)),
  getAdmin: (id: string) => mockOnly(getMockNews(id)),
  create: (_body: unknown) => mockOnly(getMockNews("")),
  update: (id: string, _body: unknown) => mockOnly(getMockNews(id)),
  remove: (_id: string) => mockOnly({ ok: true }),
  listPublic: (params?: NewsListParams) => mockOnly(listMockNews(params)),
  getPublic: (slug: string) => mockOnly(getMockNews(slug)),
};

export const getNewsList = (params?: unknown) => newsApi.listAdmin(params);
export const getNewsDetail = (id: string) => newsApi.getAdmin(id);
export const createNews = (body: unknown) => newsApi.create(body);
export const updateNews = (id: string, body: unknown) => newsApi.update(id, body);
export const deleteNews = (id: string) => newsApi.remove(id);
export const getPublicNewsList = (params?: unknown) => newsApi.listPublic(params);
export const getPublicNewsDetail = (slug: string) => newsApi.getPublic(slug);
