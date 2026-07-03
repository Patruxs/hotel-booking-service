package org.example.hotelbookingservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.BannerMutationRequest;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.BannerResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.CommissionAssignmentResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.CommissionPackageRequest;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.CommissionPackageResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ContactCreateRequest;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ContactCreateResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ContactResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ContactUpdateRequest;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.DashboardStatsResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.GalleryFolderResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ImageAssetResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ImageSnapshotRequest;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ImageSnapshotResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.LatestReviewResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ListResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.NewsMutationRequest;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.NewsResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.NewestBookingResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.NotificationResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.PolicyMutationRequest;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.PolicyResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.RatingSummaryResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.RevenuePointResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ReviewModerationRequest;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ReviewRequest;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ReviewResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ReviewUpdateRequest;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.UnreadCountResponse;
import org.example.hotelbookingservice.services.Milestone6Service;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class Milestone6Controller {
    private final Milestone6Service service;

    @PostMapping(value = {"/uploads", "/upload"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> upload(
            @RequestPart(required = false) MultipartFile file,
            @RequestPart(required = false) MultipartFile image,
            @RequestPart(required = false) List<MultipartFile> files,
            Authentication authentication
    ) {
        List<MultipartFile> resolved = resolveFiles(file, image, files);
        Object data = resolved.size() == 1
                ? service.upload(resolved.getFirst(), authentication)
                : service.uploadMany(resolved, authentication);
        return response(HttpStatus.CREATED, "Uploaded successfully", data);
    }

    @PostMapping(value = "/uploads/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageAssetResponse> uploadAvatar(
            @RequestPart(required = false) MultipartFile file,
            @RequestPart(required = false) MultipartFile image,
            @RequestPart(required = false, name = "avatar") MultipartFile avatar,
            Authentication authentication
    ) {
        MultipartFile resolved = file != null ? file : image != null ? image : avatar;
        return response(HttpStatus.CREATED, "Avatar uploaded successfully", service.uploadAvatar(resolved, authentication));
    }

    @DeleteMapping("/uploads/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvatar(Authentication authentication) {
        service.deleteAvatar(authentication);
    }

    @GetMapping("/uploads/provider")
    public ApiResponse<List<ImageAssetResponse>> listProviderAssets(Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.listProviderAssets(authentication));
    }

    @GetMapping("/uploads/local/{id}")
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

    @GetMapping("/gallery/folders")
    public ApiResponse<List<GalleryFolderResponse>> galleryFolders(Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.listGalleryFolders(authentication));
    }

    @PostMapping("/gallery/folders")
    public ApiResponse<GalleryFolderResponse> createGalleryFolder(@RequestParam(required = false) String folderName, @RequestBody(required = false) GalleryFolderBody body, Authentication authentication) {
        String resolved = folderName != null ? folderName : body == null ? null : body.folderName();
        return response(HttpStatus.CREATED, "Gallery folder created", service.createGalleryFolder(resolved, authentication));
    }

    @GetMapping("/gallery/folders/{folderId}/images")
    public ApiResponse<List<ImageAssetResponse>> galleryImages(@PathVariable UUID folderId, Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.listGalleryImages(folderId, authentication));
    }

    @PostMapping(value = "/gallery/folders/{folderName}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<ImageAssetResponse>> uploadGalleryImages(
            @PathVariable String folderName,
            @RequestPart(required = false) MultipartFile file,
            @RequestPart(required = false) MultipartFile image,
            @RequestPart(required = false) List<MultipartFile> files,
            Authentication authentication
    ) {
        return response(HttpStatus.CREATED, "Gallery images uploaded", service.uploadGalleryImages(folderName, resolveFiles(file, image, files), authentication));
    }

    @PutMapping("/hotels/{hotelId}/images")
    public ApiResponse<List<ImageSnapshotResponse>> replaceHotelImages(@PathVariable UUID hotelId, @RequestBody @Valid ImageSnapshotRequest request, Authentication authentication) {
        return response(HttpStatus.OK, "Hotel images updated", service.replaceHotelImages(hotelId, request.imageIds(), authentication));
    }

    @PutMapping("/hotels/{hotelId}/room-types/{roomTypeId}/images")
    public ApiResponse<List<ImageSnapshotResponse>> replaceRoomTypeImages(@PathVariable UUID hotelId, @PathVariable UUID roomTypeId, @RequestBody @Valid ImageSnapshotRequest request, Authentication authentication) {
        return response(HttpStatus.OK, "Room type images updated", service.replaceRoomTypeImages(hotelId, roomTypeId, request.imageIds(), authentication));
    }

    @GetMapping("/hotels/{hotelId}/reviews")
    public ApiResponse<ListResponse<ReviewResponse>> publicReviews(@PathVariable UUID hotelId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        return response(HttpStatus.OK, "Success", service.listPublicReviews(hotelId, page, limit));
    }

    @GetMapping("/hotels/{hotelId}/reviews/summary")
    public ApiResponse<RatingSummaryResponse> reviewSummary(@PathVariable UUID hotelId) {
        return response(HttpStatus.OK, "Success", service.visibleRatingSummary(hotelId));
    }

    @PostMapping("/hotels/{hotelId}/reviews")
    public ApiResponse<ReviewResponse> createReview(@PathVariable UUID hotelId, @RequestBody @Valid ReviewRequest request, Authentication authentication) {
        return response(HttpStatus.CREATED, "Review created", service.createReview(hotelId, request, authentication));
    }

    @GetMapping("/admin/hotels/{hotelId}/reviews")
    public ApiResponse<ListResponse<ReviewResponse>> moderationReviews(@PathVariable UUID hotelId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit, Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.listModerationReviews(hotelId, page, limit, authentication));
    }

    @GetMapping("/reviews/mine")
    public ApiResponse<ListResponse<ReviewResponse>> myReviews(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit, Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.listMyReviews(page, limit, authentication));
    }

    @PatchMapping("/reviews/{reviewId}/mine")
    public ApiResponse<ReviewResponse> updateMyReview(@PathVariable UUID reviewId, @RequestBody @Valid ReviewUpdateRequest request, Authentication authentication) {
        return response(HttpStatus.OK, "Review updated", service.updateMyReview(reviewId, request, authentication));
    }

    @PatchMapping("/hotels/{hotelId}/reviews/{reviewId}/moderation")
    public ApiResponse<ReviewResponse> moderateReview(@PathVariable UUID hotelId, @PathVariable UUID reviewId, @RequestBody ReviewModerationRequest request, Authentication authentication) {
        return response(HttpStatus.OK, "Review moderated", service.moderateReview(hotelId, reviewId, request, authentication));
    }

    @DeleteMapping("/hotels/{hotelId}/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable UUID hotelId, @PathVariable UUID reviewId, Authentication authentication) {
        service.deleteReview(hotelId, reviewId, authentication);
    }

    @GetMapping("/news")
    public ApiResponse<ListResponse<NewsResponse>> publicNews(@RequestParam(required = false) String q, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        return response(HttpStatus.OK, "Success", service.listNewsPublic(q, page, limit));
    }

    @GetMapping("/news/{slug}")
    public ApiResponse<NewsResponse> publicNewsDetail(@PathVariable String slug) {
        return response(HttpStatus.OK, "Success", service.newsDetailPublic(slug));
    }

    @GetMapping("/admin/news")
    public ApiResponse<ListResponse<NewsResponse>> adminNews(@RequestParam(required = false) String status, @RequestParam(required = false) String q, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit, Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.listNewsAdmin(status, q, page, limit, authentication));
    }

    @GetMapping("/admin/news/{id}")
    public ApiResponse<NewsResponse> adminNewsDetail(@PathVariable UUID id, Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.newsDetailAdmin(id, authentication));
    }

    @PostMapping("/admin/news")
    public ApiResponse<NewsResponse> createNews(@RequestBody @Valid NewsMutationRequest request, Authentication authentication) {
        return response(HttpStatus.CREATED, "News created", service.createNews(request, authentication));
    }

    @RequestMapping(path = "/admin/news/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ApiResponse<NewsResponse> updateNews(@PathVariable UUID id, @RequestBody NewsMutationRequest request, Authentication authentication) {
        return response(HttpStatus.OK, "News updated", service.updateNews(id, request, authentication));
    }

    @DeleteMapping("/admin/news/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNews(@PathVariable UUID id, Authentication authentication) {
        service.deleteNews(id, authentication);
    }

    @GetMapping("/banners")
    public ApiResponse<List<BannerResponse>> publicBanners() {
        return response(HttpStatus.OK, "Success", service.listPublicBanners());
    }

    @GetMapping("/admin/banners")
    public ApiResponse<List<BannerResponse>> adminBanners(Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.listAdminBanners(authentication));
    }

    @PostMapping("/admin/banners")
    public ApiResponse<BannerResponse> createBanner(@RequestBody BannerMutationRequest request, Authentication authentication) {
        return response(HttpStatus.CREATED, "Banner created", service.createBanner(request, authentication));
    }

    @RequestMapping(path = "/admin/banners/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ApiResponse<BannerResponse> updateBanner(@PathVariable UUID id, @RequestBody BannerMutationRequest request, Authentication authentication) {
        return response(HttpStatus.OK, "Banner updated", service.updateBanner(id, request, authentication));
    }

    @DeleteMapping("/admin/banners/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBanner(@PathVariable UUID id, Authentication authentication) {
        service.deleteBanner(id, authentication);
    }

    @PostMapping({"/contacts", "/contact"})
    public ApiResponse<ContactCreateResponse> createContact(@RequestBody @Valid ContactCreateRequest request, HttpServletRequest servletRequest, Authentication authentication) {
        return response(HttpStatus.CREATED, "Contact submitted", service.createContact(request, servletRequest.getRemoteAddr(), servletRequest.getHeader(HttpHeaders.USER_AGENT), authentication));
    }

    @GetMapping("/admin/contacts")
    public ApiResponse<ListResponse<ContactResponse>> contacts(@RequestParam(required = false) String status, @RequestParam(required = false) String q, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit, Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.listContacts(status, q, page, limit, authentication));
    }

    @GetMapping("/admin/contacts/{id}")
    public ApiResponse<ContactResponse> contactDetail(@PathVariable UUID id, Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.contactDetail(id, authentication));
    }

    @PatchMapping("/admin/contacts/{id}")
    public ApiResponse<ContactResponse> updateContact(@PathVariable UUID id, @RequestBody ContactUpdateRequest request, Authentication authentication) {
        return response(HttpStatus.OK, "Contact updated", service.updateContact(id, request, authentication));
    }

    @GetMapping("/notifications")
    public ApiResponse<ListResponse<NotificationResponse>> notifications(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit, Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.listNotifications(page, limit, authentication));
    }

    @GetMapping("/notifications/unread-count")
    public ApiResponse<UnreadCountResponse> unreadCount(Authentication authentication) {
        return response(HttpStatus.OK, "Success", new UnreadCountResponse(service.unreadCount(authentication)));
    }

    @PatchMapping("/notifications/{id}/read")
    public ApiResponse<NotificationResponse> markRead(@PathVariable UUID id, Authentication authentication) {
        return response(HttpStatus.OK, "Notification read", service.markRead(id, authentication));
    }

    @PatchMapping("/notifications/read-all")
    public ApiResponse<Void> markAllRead(Authentication authentication) {
        service.markAllRead(authentication);
        return response(HttpStatus.OK, "Notifications read", null);
    }

    @DeleteMapping("/notifications/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(@PathVariable UUID id, Authentication authentication) {
        service.deleteNotification(id, authentication);
    }

    @GetMapping("/dashboard/stats")
    public ApiResponse<DashboardStatsResponse> dashboardStats(@RequestParam(required = false) UUID hotelId, Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.dashboardStats(hotelId, authentication));
    }

    @GetMapping("/dashboard/revenue-chart")
    public ApiResponse<List<RevenuePointResponse>> revenueChart(
            @RequestParam(required = false) UUID hotelId,
            @RequestParam(required = false) String groupBy,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication
    ) {
        return response(HttpStatus.OK, "Success", service.revenueChart(hotelId, groupBy, year, from, to, authentication));
    }

    @GetMapping("/dashboard/latest-reviews")
    public ApiResponse<List<LatestReviewResponse>> latestReviews(@RequestParam(required = false) UUID hotelId, Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.latestReviews(hotelId, authentication));
    }

    @GetMapping("/dashboard/newest-bookings")
    public ApiResponse<List<NewestBookingResponse>> newestBookings(@RequestParam(required = false) UUID hotelId, Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.newestBookings(hotelId, authentication));
    }

    @GetMapping({"/admin/commission-packages", "/commission-packages"})
    public ApiResponse<List<CommissionPackageResponse>> commissionPackages(Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.listCommissionPackages(authentication));
    }

    @GetMapping({"/admin/commission-packages/{id}", "/commission-packages/{id}"})
    public ApiResponse<CommissionPackageResponse> commissionPackage(@PathVariable UUID id, Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.commissionPackageDetail(id, authentication));
    }

    @PostMapping({"/admin/commission-packages", "/commission-packages"})
    public ApiResponse<CommissionPackageResponse> createCommissionPackage(@RequestBody CommissionPackageRequest request, Authentication authentication) {
        return response(HttpStatus.CREATED, "Commission package created", service.createCommissionPackage(request, authentication));
    }

    @RequestMapping(path = {"/admin/commission-packages/{id}", "/commission-packages/{id}"}, method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ApiResponse<CommissionPackageResponse> updateCommissionPackage(@PathVariable UUID id, @RequestBody CommissionPackageRequest request, Authentication authentication) {
        return response(HttpStatus.OK, "Commission package updated", service.updateCommissionPackage(id, request, authentication));
    }

    @RequestMapping(
            path = {"/admin/commission-packages/{id}", "/commission-packages/{id}", "/admin/commission-packages/{id}/deactivate", "/commission-packages/{id}/deactivate"},
            method = {RequestMethod.DELETE, RequestMethod.PATCH}
    )
    public ApiResponse<CommissionPackageResponse> deactivateCommissionPackage(@PathVariable UUID id, Authentication authentication) {
        return response(HttpStatus.OK, "Commission package deactivated", service.deactivateCommissionPackage(id, authentication));
    }

    @PutMapping("/hotels/{hotelId}/commission-package/{packageId}")
    public ApiResponse<CommissionAssignmentResponse> assignCommissionPackage(@PathVariable UUID hotelId, @PathVariable UUID packageId, Authentication authentication) {
        return response(HttpStatus.OK, "Commission package assigned", service.assignCommissionPackage(hotelId, packageId, authentication));
    }

    @GetMapping({"/admin/commission-packages/revenue/chart", "/commission-revenue"})
    public ApiResponse<Object> commissionRevenue(
            @RequestParam(required = false) UUID hotelId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication
    ) {
        return response(HttpStatus.OK, "Success", service.commissionRevenue(hotelId, year, from, to, authentication));
    }

    @GetMapping("/hotels/{hotelId}/policies")
    public ApiResponse<List<PolicyResponse>> policies(@PathVariable UUID hotelId, @RequestParam(defaultValue = "false") boolean manage, Authentication authentication) {
        List<PolicyResponse> data = manage ? service.listPoliciesAdmin(hotelId, authentication) : service.listPoliciesPublic(hotelId);
        return response(HttpStatus.OK, "Success", data);
    }

    @GetMapping("/admin/hotels/{hotelId}/policies")
    public ApiResponse<List<PolicyResponse>> adminPolicies(@PathVariable UUID hotelId, Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.listPoliciesAdmin(hotelId, authentication));
    }

    @GetMapping("/admin/hotels/{hotelId}/policies/{policyId}")
    public ApiResponse<PolicyResponse> policyDetail(@PathVariable UUID hotelId, @PathVariable UUID policyId, Authentication authentication) {
        return response(HttpStatus.OK, "Success", service.policyDetail(hotelId, policyId, authentication));
    }

    @PostMapping("/admin/hotels/{hotelId}/policies")
    public ApiResponse<PolicyResponse> createPolicy(@PathVariable UUID hotelId, @RequestBody @Valid PolicyMutationRequest request, Authentication authentication) {
        return response(HttpStatus.CREATED, "Policy created", service.createPolicy(hotelId, request, authentication));
    }

    @RequestMapping(path = "/admin/hotels/{hotelId}/policies/{policyId}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ApiResponse<PolicyResponse> updatePolicy(@PathVariable UUID hotelId, @PathVariable UUID policyId, @RequestBody @Valid PolicyMutationRequest request, Authentication authentication) {
        return response(HttpStatus.OK, "Policy updated", service.updatePolicy(hotelId, policyId, request, authentication));
    }

    @DeleteMapping("/admin/hotels/{hotelId}/policies/{policyId}")
    public ApiResponse<PolicyResponse> deletePolicy(@PathVariable UUID hotelId, @PathVariable UUID policyId, Authentication authentication) {
        return response(HttpStatus.OK, "Policy deleted", service.deletePolicy(hotelId, policyId, authentication));
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

    private <T> ApiResponse<T> response(HttpStatus status, String message, T data) {
        return ApiResponse.<T>builder()
                .status(status.value())
                .message(message)
                .data(data)
                .build();
    }

    public record GalleryFolderBody(String folderName, String name) {
        @Override
        public String folderName() {
            return folderName == null ? name : folderName;
        }
    }
}
