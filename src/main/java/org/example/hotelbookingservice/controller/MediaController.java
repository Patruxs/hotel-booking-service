package org.example.hotelbookingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.api.MediaApi;
import org.example.hotelbookingservice.api.MediaApi.GalleryFolderBody;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.response.media.GalleryFolderResponse;
import org.example.hotelbookingservice.dto.response.media.ImageAssetResponse;
import org.example.hotelbookingservice.dto.request.media.ImageSnapshotRequest;
import org.example.hotelbookingservice.dto.response.media.ImageSnapshotResponse;
import org.example.hotelbookingservice.services.IMediaService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class MediaController implements MediaApi {
    private final IMediaService mediaService;

    @Override
    public ApiResponse<?> upload(
            @RequestPart(required = false) MultipartFile file,
            @RequestPart(required = false) MultipartFile image,
            @RequestPart(required = false) List<MultipartFile> files,
            Authentication authentication
    ) {
        List<MultipartFile> resolved = resolveFiles(file, image, files);
        Object data = resolved.size() == 1
                ? mediaService.upload(resolved.getFirst(), authentication)
                : mediaService.uploadMany(resolved, authentication);
        return ApiResponse.<Object>builder().status(201).message("Uploaded successfully").data(data).build();
    }

    @Override
    public ApiResponse<ImageAssetResponse> uploadAvatar(
            @RequestPart(required = false) MultipartFile file,
            @RequestPart(required = false) MultipartFile image,
            @RequestPart(required = false, name = "avatar") MultipartFile avatar,
            Authentication authentication
    ) {
        MultipartFile resolved = file != null ? file : image != null ? image : avatar;
        return ApiResponse.<ImageAssetResponse>builder().status(201).message("Avatar uploaded successfully").data(mediaService.uploadAvatar(resolved, authentication)).build();
    }

    @Override
    public void deleteAvatar(Authentication authentication) {
        mediaService.deleteAvatar(authentication);
    }

    @Override
    public ApiResponse<List<ImageAssetResponse>> listProviderAssets(Authentication authentication) {
        return ApiResponse.<List<ImageAssetResponse>>builder().status(200).message("Success").data(mediaService.listProviderAssets(authentication)).build();
    }

    @Override
    public ResponseEntity<byte[]> localPlaceholder(@PathVariable UUID id) {
        String color = Integer.toHexString(id.hashCode()).replace("-", "");
        color = (color + "6f7f8f").substring(0, 6);
        String svg = String.format(
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1200\" height=\"800\" viewBox=\"0 0 1200 800\">"
                        + "<rect width=\"1200\" height=\"800\" fill=\"#%s\"/>"
                        + "<path d=\"M0 650 L260 470 L430 590 L690 330 L1200 720 L1200 800 L0 800 Z\" fill=\"#ffffff\" opacity=\".34\"/>"
                        + "<circle cx=\"880\" cy=\"210\" r=\"90\" fill=\"#ffffff\" opacity=\".42\"/>"
                        + "</svg>",
                color);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
                .header(HttpHeaders.ETAG, '"' + id.toString() + '"')
                .body(svg.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public ApiResponse<List<GalleryFolderResponse>> galleryFolders(Authentication authentication) {
        return ApiResponse.<List<GalleryFolderResponse>>builder().status(200).message("Success").data(mediaService.listGalleryFolders(authentication)).build();
    }

    @Override
    public ApiResponse<GalleryFolderResponse> createGalleryFolder(
            @RequestParam(required = false) String folderName,
            @RequestBody(required = false) GalleryFolderBody body,
            Authentication authentication
    ) {
        String resolved = folderName != null ? folderName : body == null ? null : body.folderName();
        return ApiResponse.<GalleryFolderResponse>builder().status(201).message("Gallery folder created").data(mediaService.createGalleryFolder(resolved, authentication)).build();
    }

    @Override
    public ApiResponse<List<ImageAssetResponse>> galleryImages(@PathVariable UUID folderId, Authentication authentication) {
        return ApiResponse.<List<ImageAssetResponse>>builder().status(200).message("Success").data(mediaService.listGalleryImages(folderId, authentication)).build();
    }

    @Override
    public ApiResponse<List<ImageAssetResponse>> uploadGalleryImages(
            @PathVariable String folderName,
            @RequestPart(required = false) MultipartFile file,
            @RequestPart(required = false) MultipartFile image,
            @RequestPart(required = false) List<MultipartFile> files,
            Authentication authentication
    ) {
        return ApiResponse.<List<ImageAssetResponse>>builder().status(201).message("Gallery images uploaded").data(mediaService.uploadGalleryImages(folderName, resolveFiles(file, image, files), authentication)).build();
    }

    @Override
    public ApiResponse<List<ImageSnapshotResponse>> replaceHotelImages(@PathVariable UUID hotelId, @RequestBody @Valid ImageSnapshotRequest request, Authentication authentication) {
        return ApiResponse.<List<ImageSnapshotResponse>>builder().status(200).message("Hotel images updated").data(mediaService.replaceHotelImages(hotelId, request.imageIds(), authentication)).build();
    }

    @Override
    public ApiResponse<List<ImageSnapshotResponse>> replaceRoomTypeImages(@PathVariable UUID hotelId, @PathVariable UUID roomTypeId, @RequestBody @Valid ImageSnapshotRequest request, Authentication authentication) {
        return ApiResponse.<List<ImageSnapshotResponse>>builder().status(200).message("Room type images updated").data(mediaService.replaceRoomTypeImages(hotelId, roomTypeId, request.imageIds(), authentication)).build();
    }

    private List<MultipartFile> resolveFiles(MultipartFile file, MultipartFile image, List<MultipartFile> files) {
        List<MultipartFile> resolved = new ArrayList<>();
        if (file != null) {
            resolved.add(file);
        }
        if (image != null) {
            resolved.add(image);
        }
        if (files != null) {
            resolved.addAll(files);
        }
        return resolved;
    }
}
