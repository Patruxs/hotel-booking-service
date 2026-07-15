package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.media.ImageSnapshotRequest;
import org.example.hotelbookingservice.dto.response.media.GalleryFolderResponse;
import org.example.hotelbookingservice.dto.response.media.ImageAssetResponse;
import org.example.hotelbookingservice.dto.response.media.ImageSnapshotResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1")
@Tag(name = "Media", description = "Upload media, manage gallery folders, and manage hotel image snapshots")
public interface MediaApi {

    @Operation(summary = "Upload one or more images")
    @PostMapping(value = {"/uploads", "/upload"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<?> upload(
            @RequestPart(required = false) MultipartFile file,
            @RequestPart(required = false) MultipartFile image,
            @RequestPart(required = false) List<MultipartFile> files,
            Authentication authentication
    );

    @Operation(summary = "Upload avatar")
    @PostMapping(value = "/uploads/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<ImageAssetResponse> uploadAvatar(
            @RequestPart(required = false) MultipartFile file,
            @RequestPart(required = false) MultipartFile image,
            @RequestPart(required = false, name = "avatar") MultipartFile avatar,
            Authentication authentication
    );

    @Operation(summary = "Delete avatar")
    @DeleteMapping("/uploads/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAvatar(Authentication authentication);

    @Operation(summary = "List provider assets")
    @GetMapping("/uploads/provider")
    ApiResponse<List<ImageAssetResponse>> listProviderAssets(Authentication authentication);

    @Operation(summary = "Get local placeholder image")
    @GetMapping("/uploads/local/{id}")
    ResponseEntity<byte[]> localPlaceholder(@PathVariable UUID id);

    @Operation(summary = "List gallery folders")
    @GetMapping("/gallery/folders")
    ApiResponse<List<GalleryFolderResponse>> galleryFolders(Authentication authentication);

    @Operation(summary = "Create gallery folder")
    @PostMapping("/gallery/folders")
    ApiResponse<GalleryFolderResponse> createGalleryFolder(
            @RequestParam(required = false) String folderName,
            @RequestBody(required = false) GalleryFolderBody body,
            Authentication authentication
    );

    @Operation(summary = "List gallery images")
    @GetMapping("/gallery/folders/{folderId}/images")
    ApiResponse<List<ImageAssetResponse>> galleryImages(@PathVariable UUID folderId, Authentication authentication);

    @Operation(summary = "Upload gallery images")
    @PostMapping(value = "/gallery/folders/{folderName}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<List<ImageAssetResponse>> uploadGalleryImages(
            @PathVariable String folderName,
            @RequestPart(required = false) MultipartFile file,
            @RequestPart(required = false) MultipartFile image,
            @RequestPart(required = false) List<MultipartFile> files,
            Authentication authentication
    );

    @Operation(summary = "Replace hotel images")
    @PutMapping("/hotels/{hotelId}/images")
    ApiResponse<List<ImageSnapshotResponse>> replaceHotelImages(@PathVariable UUID hotelId, @RequestBody @Valid ImageSnapshotRequest request, Authentication authentication);

    @Operation(summary = "Replace room type images")
    @PutMapping("/hotels/{hotelId}/room-types/{roomTypeId}/images")
    ApiResponse<List<ImageSnapshotResponse>> replaceRoomTypeImages(@PathVariable UUID hotelId, @PathVariable UUID roomTypeId, @RequestBody @Valid ImageSnapshotRequest request, Authentication authentication);

    record GalleryFolderBody(String folderName, String name) {
        @Override
        public String folderName() {
            return folderName == null ? name : folderName;
        }
    }
}
