import { mockOnly } from "@/features/shared/apiClient";

export const galleryApi: any = {
  folders: () => mockOnly([{ id: "mock-folder", name: "hotel-gallery" }]),
  images: (_folderId: string) => mockOnly([]),
  createFolder: (folderName: string) => mockOnly({ id: "mock-folder", folderName }),
  uploadImage: (_folderName: string, _formData: FormData) => mockOnly({ url: "/globe.svg" }),
};

export const getFoldersGallery = () => galleryApi.folders();
export const getImagesInFolderGallery = (folderId: string) => galleryApi.images(folderId);
export const createFolderGallery = (folderName: string) => galleryApi.createFolder(folderName);
export const uploadImageGallery = (folderName: string, formData: FormData) => galleryApi.uploadImage(folderName, formData);
