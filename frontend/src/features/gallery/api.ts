import { mockOrRequest } from "@/features/shared/apiClient";
import api from "@/lib/axios";

export const galleryApi: any = {
  folders: () => mockOrRequest([{ id: "mock-folder", name: "hotel-gallery" }], () => api.get("/gallery/folders")),
  images: (folderId: string) => mockOrRequest([], () => api.get(`/gallery/folders/${folderId}/images`)),
  createFolder: (folderName: string) => mockOrRequest({ id: "mock-folder", folderName }, () => api.post("/gallery/folders", { folderName })),
  uploadImage: (folderName: string, formData: FormData) =>
    mockOrRequest({ url: "/globe.svg" }, () =>
      api.post(`/gallery/folders/${folderName}/images`, formData, { headers: { "Content-Type": "multipart/form-data" } }),
    ),
};

export const getFoldersGallery = () => galleryApi.folders();
export const getImagesInFolderGallery = (folderId: string) => galleryApi.images(folderId);
export const createFolderGallery = (folderName: string) => galleryApi.createFolder(folderName);
export const uploadImageGallery = (folderName: string, formData: FormData) => galleryApi.uploadImage(folderName, formData);
