import { mockApi } from "@/mocks/mockApi";
import { mockOrRequest } from "@/features/shared/apiClient";
import api from "@/lib/axios";
import { BannerLinkType, type Banner } from "@/features/banner/types";

const fallbackImages = ["/hero-bg.jpg", "/hero-bg-2.jpg", "/hero-bg-3.jpg"];

function toBanner(item: unknown, index: number): Banner {
  if (item && typeof item === "object") {
    const banner = item as Partial<Banner>;

    return {
      id: banner.id ?? `banner-${index + 1}`,
      title: banner.title ?? `Banner ${index + 1}`,
      subtitle: banner.subtitle ?? "",
      link: banner.link ?? "#",
      linkType: banner.linkType ?? BannerLinkType.URL,
      position: banner.position ?? index + 1,
      isActive: banner.isActive ?? true,
      startAt: banner.startAt,
      endAt: banner.endAt,
      images: Array.isArray(banner.images) && banner.images.length > 0
        ? banner.images
        : [
            {
              id: `banner-${index + 1}-image`,
              bannerId: banner.id ?? `banner-${index + 1}`,
              url: fallbackImages[index % fallbackImages.length],
            },
          ],
      createdById: banner.createdById,
      createdAt: banner.createdAt ?? new Date(0).toISOString(),
      updatedAt: banner.updatedAt ?? new Date(0).toISOString(),
    };
  }

  const title = typeof item === "string" ? item : `Banner ${index + 1}`;

  return {
    id: `banner-${index + 1}`,
    title,
    subtitle: "Experience comfortable stays with Kinyias.",
    link: "#hotels",
    linkType: BannerLinkType.URL,
    position: index + 1,
    isActive: true,
    images: [
      {
        id: `banner-${index + 1}-image`,
        bannerId: `banner-${index + 1}`,
        url: fallbackImages[index % fallbackImages.length],
      },
    ],
    createdAt: new Date(0).toISOString(),
    updatedAt: new Date(0).toISOString(),
  };
}

function listMockBanners(): Banner[] {
  return mockApi.banners.list().map(toBanner);
}

export const bannerApi: any = {
  listPublic: () => mockOrRequest(listMockBanners(), () => api.get("/banners")).then((items) => items.map(toBanner)),
  listAdmin: () => mockOrRequest(listMockBanners(), () => api.get("/admin/banners")).then((items) => items.map(toBanner)),
  create: (body: unknown) => mockOrRequest({ ok: true }, () => api.post("/admin/banners", body)),
  update: (id: string, body: unknown) => mockOrRequest({ ok: true }, () => api.put(`/admin/banners/${id}`, body)),
  remove: (id: string) => mockOrRequest({ ok: true }, () => api.delete(`/admin/banners/${id}`)),
};

export const getPublicBanners = () => bannerApi.listPublic();
export const getAdminBanners = () => bannerApi.listAdmin();
export const createBanner = (body: unknown) => bannerApi.create(body);
export const updateBanner = (id: string, body: unknown) => bannerApi.update(id, body);
export const deleteBanner = (id: string) => bannerApi.remove(id);
