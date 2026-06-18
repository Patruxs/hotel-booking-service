import api from "@/lib/axios";
import { mockOrRequest } from "@/features/shared/apiClient";

export const galleryApi = {
  folders: () => mockOrRequest([{ id: "mock-folder", name: "hotel-gallery" }], () => api.get("/upload/db-folders")),
  images: (folderId: string) => mockOrRequest([], () => api.get(`/upload/db-folders/${folderId}/images`)),
  createFolder: (folderName: string) => mockOrRequest({ id: "mock-folder", folderName }, () => api.post("/upload/create-folder", { folderName })),
  uploadImage: (folderName: string, formData: FormData) =>
    mockOrRequest({ url: "/globe.svg" }, () =>
      api.post(`/upload/image/${folderName}`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      }),
    ),
};
