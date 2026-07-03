package org.example.hotelbookingservice.dto.operations;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class Milestone6Dtos {
    private Milestone6Dtos() {
    }

    public record ListResponse<T>(
            List<T> data,
            PageMeta meta,
            Integer page,
            Integer limit,
            Long total,
            Integer totalPages
    ) {
    }

    public record PageMeta(
            int limit,
            int offset,
            long total
    ) {
    }

    public record ImageAssetResponse(
            UUID id,
            UUID ownerAccountId,
            String provider,
            String publicId,
            String url,
            String secureUrl,
            Integer width,
            Integer height,
            Long bytes,
            Instant createdAt
    ) {
    }

    public record GalleryFolderResponse(
            UUID id,
            UUID ownerAccountId,
            String name,
            String folderName,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record ImageSnapshotRequest(
            @NotNull List<UUID> imageIds
    ) {
    }

    public record ImageSnapshotResponse(
            UUID id,
            UUID imageAssetId,
            String url,
            int sortOrder
    ) {
    }

    public record ReviewRequest(
            @NotNull UUID bookingId,
            @NotNull @Min(1) @Max(5) BigDecimal rating,
            @Size(max = 5000) String comment,
            List<UUID> imageIds
    ) {
    }

    public record ReviewUpdateRequest(
            @Min(1) @Max(5) BigDecimal rating,
            @Size(max = 5000) String comment
    ) {
    }

    public record ReviewModerationRequest(
            Boolean visible
    ) {
    }

    public record ReviewResponse(
            UUID id,
            UUID bookingId,
            UUID hotelId,
            UUID accountId,
            BigDecimal rating,
            String comment,
            boolean visible,
            Instant createdAt,
            Instant updatedAt,
            List<ImageSnapshotResponse> images,
            AccountSummary user
    ) {
    }

    public record RatingSummaryResponse(
            BigDecimal averageRating,
            long reviewCount
    ) {
    }

    public record NewsMutationRequest(
            @Size(max = 180) String title,
            @Size(max = 500) String summary,
            String content,
            String status,
            List<UUID> imageIds
    ) {
    }

    public record NewsImageResponse(UUID id, UUID newsId, String url) {
    }

    public record NewsResponse(
            UUID id,
            String title,
            String slug,
            String summary,
            String content,
            String status,
            Instant publishedAt,
            Instant createdAt,
            Instant updatedAt,
            List<NewsImageResponse> images
    ) {
    }

    public record BannerMutationRequest(
            String title,
            String subtitle,
            @JsonAlias("link") String linkUrl,
            String linkType,
            Integer position,
            @JsonAlias("isActive") Boolean active,
            @JsonAlias("startAt") Instant startsAt,
            @JsonAlias("endAt") Instant endsAt,
            List<UUID> imageIds,
            List<String> images
    ) {
    }

    public record BannerImageResponse(UUID id, UUID bannerId, String url) {
    }

    public record BannerResponse(
            UUID id,
            String title,
            String subtitle,
            String link,
            String linkType,
            int position,
            boolean isActive,
            Instant startAt,
            Instant endAt,
            List<BannerImageResponse> images,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record ContactCreateRequest(
            @NotBlank @Size(max = 120) String name,
            @Email @Size(max = 320) String email,
            @Size(max = 32) String phone,
            @Size(max = 180) String subject,
            @NotBlank String message
    ) {
    }

    public record ContactUpdateRequest(
            String status,
            UUID handledById,
            String note
    ) {
    }

    public record ContactCreateResponse(UUID id, boolean ok) {
    }

    public record ContactResponse(
            UUID id,
            String name,
            String email,
            String phone,
            String subject,
            String message,
            String status,
            UUID userId,
            UUID handledById,
            AccountSummary handledBy,
            String note,
            String ip,
            String userAgent,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record NotificationResponse(
            UUID id,
            String type,
            String title,
            String body,
            String linkUrl,
            boolean read,
            Instant readAt,
            Instant createdAt
    ) {
    }

    public record UnreadCountResponse(long count) {
    }

    public record DashboardStatsResponse(
            long totalUsers,
            long totalBookings,
            BigDecimal revenue,
            long activeHotels
    ) {
    }

    public record RevenuePointResponse(String period, String month, BigDecimal revenue) {
    }

    public record LatestReviewResponse(
            UUID id,
            BigDecimal rating,
            String content,
            Instant createdAt,
            AccountSummary user
    ) {
    }

    public record NewestBookingItemResponse(RoomTypeSummary roomType, int quantity) {
    }

    public record NewestBookingResponse(
            UUID id,
            String guestName,
            java.time.LocalDate checkIn,
            java.time.LocalDate checkOut,
            Instant createdAt,
            List<NewestBookingItemResponse> items
    ) {
    }

    public record RoomTypeSummary(String name) {
    }

    public record CommissionPackageRequest(
            String code,
            @Size(max = 120) String name,
            String description,
            BigDecimal commissionRate,
            @JsonAlias("isActive") Boolean active
    ) {
    }

    public record CommissionPackageResponse(
            UUID id,
            String code,
            String name,
            String description,
            BigDecimal commissionRate,
            boolean isActive,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record CommissionAssignmentResponse(
            UUID hotelId,
            UUID commissionPackageId,
            String packageCode,
            BigDecimal commissionRate,
            Instant assignedAt
    ) {
    }

    public record PolicyMutationRequest(
            String type,
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 5000) String content,
            Boolean enabled,
            @Min(0) @JsonAlias("order") Integer sortOrder
    ) {
    }

    public record PolicyResponse(
            UUID id,
            UUID hotelId,
            String type,
            String title,
            String content,
            boolean enabled,
            int order,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
    }

    public record AccountSummary(
            UUID id,
            String email,
            String firstName,
            String lastName,
            AvatarSummary avatar
    ) {
    }

    public record AvatarSummary(String secureUrl) {
    }
}
