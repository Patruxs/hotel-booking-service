package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.dto.response.media.GalleryFolderResponse;
import org.example.hotelbookingservice.dto.response.media.ImageAssetResponse;
import org.example.hotelbookingservice.dto.response.media.ImageSnapshotResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IMediaService {
    ImageAssetResponse upload(MultipartFile file, Authentication authentication);

    List<ImageAssetResponse> uploadMany(List<MultipartFile> files, Authentication authentication);

    ImageAssetResponse uploadAvatar(MultipartFile file, Authentication authentication);

    void deleteAvatar(Authentication authentication);

    List<ImageAssetResponse> listProviderAssets(Authentication authentication);

    List<GalleryFolderResponse> listGalleryFolders(Authentication authentication);

    GalleryFolderResponse createGalleryFolder(String folderName, Authentication authentication);

    List<ImageAssetResponse> listGalleryImages(UUID folderId, Authentication authentication);

    List<ImageAssetResponse> uploadGalleryImages(String folderName, List<MultipartFile> files, Authentication authentication);

    List<ImageSnapshotResponse> replaceHotelImages(UUID hotelId, List<UUID> imageIds, Authentication authentication);

    List<ImageSnapshotResponse> replaceRoomTypeImages(UUID hotelId, UUID roomTypeId, List<UUID> imageIds, Authentication authentication);
}
