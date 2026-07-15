package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.config.UploadProperties;


import org.example.hotelbookingservice.services.IFileStorageService;



import lombok.extern.slf4j.Slf4j;
import org.example.hotelbookingservice.entity.Banner;
import org.example.hotelbookingservice.entity.BannerImage;
import org.example.hotelbookingservice.entity.Hotel;
import org.example.hotelbookingservice.entity.HotelImage;
import org.example.hotelbookingservice.entity.Image;
import org.example.hotelbookingservice.entity.News;
import org.example.hotelbookingservice.entity.NewsImage;
import org.example.hotelbookingservice.entity.Review;
import org.example.hotelbookingservice.entity.ReviewImage;
import org.example.hotelbookingservice.entity.Room;
import org.example.hotelbookingservice.entity.RoomTypeImage;
import org.example.hotelbookingservice.repository.BannerImageRepository;
import org.example.hotelbookingservice.repository.HotelImageRepository;
import org.example.hotelbookingservice.repository.NewsImageRepository;
import org.example.hotelbookingservice.repository.ReviewImageRepository;
import org.example.hotelbookingservice.repository.RoomTypeImageRepository;
import org.example.hotelbookingservice.dto.response.common.AccountSummary;
import org.example.hotelbookingservice.dto.response.common.AvatarSummary;
import org.example.hotelbookingservice.dto.response.content.BannerImageResponse;
import org.example.hotelbookingservice.dto.request.content.BannerMutationRequest;
import org.example.hotelbookingservice.dto.response.content.BannerResponse;
import org.example.hotelbookingservice.dto.response.content.CommissionAssignmentResponse;
import org.example.hotelbookingservice.dto.request.content.CommissionPackageRequest;
import org.example.hotelbookingservice.dto.response.content.CommissionPackageResponse;
import org.example.hotelbookingservice.dto.request.content.ContactCreateRequest;
import org.example.hotelbookingservice.dto.response.content.ContactCreateResponse;
import org.example.hotelbookingservice.dto.response.content.ContactResponse;
import org.example.hotelbookingservice.dto.request.content.ContactUpdateRequest;
import org.example.hotelbookingservice.dto.response.dashboard.DashboardStatsResponse;
import org.example.hotelbookingservice.dto.response.media.GalleryFolderResponse;
import org.example.hotelbookingservice.dto.response.media.ImageAssetResponse;
import org.example.hotelbookingservice.dto.response.media.ImageSnapshotResponse;
import org.example.hotelbookingservice.dto.response.dashboard.LatestReviewResponse;
import org.example.hotelbookingservice.dto.response.common.ListResponse;
import org.example.hotelbookingservice.dto.response.content.NewsImageResponse;
import org.example.hotelbookingservice.dto.request.content.NewsMutationRequest;
import org.example.hotelbookingservice.dto.response.content.NewsResponse;
import org.example.hotelbookingservice.dto.response.dashboard.NewestBookingItemResponse;
import org.example.hotelbookingservice.dto.response.dashboard.NewestBookingResponse;
import org.example.hotelbookingservice.dto.response.content.NotificationResponse;
import org.example.hotelbookingservice.dto.response.common.PageMeta;
import org.example.hotelbookingservice.dto.request.content.PolicyMutationRequest;
import org.example.hotelbookingservice.dto.response.content.PolicyResponse;
import org.example.hotelbookingservice.dto.response.review.RatingSummaryResponse;
import org.example.hotelbookingservice.dto.response.dashboard.RevenuePointResponse;
import org.example.hotelbookingservice.dto.request.review.ReviewModerationRequest;
import org.example.hotelbookingservice.dto.request.review.ReviewRequest;
import org.example.hotelbookingservice.dto.response.review.ReviewResponse;
import org.example.hotelbookingservice.dto.request.review.ReviewUpdateRequest;
import org.example.hotelbookingservice.dto.response.dashboard.RoomTypeSummary;
import org.example.hotelbookingservice.security.AccountAuthUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
abstract class Milestone6ServiceSupport {
    protected static final Set<String> NEWS_STATUSES = Set.of("DRAFT", "PUBLISHED", "ARCHIVED");
    protected static final Set<String> CONTACT_STATUSES = Set.of("NEW", "IN_PROGRESS", "RESOLVED", "SPAM");
    protected static final Set<String> POLICY_TYPES = Set.of("CHECK_IN", "CANCELLATION", "PAYMENT", "CHILDREN", "PET", "SMOKING", "GENERAL");
    protected static final Set<String> LINK_TYPES = Set.of("URL", "HOTEL", "NEWS");
    protected static final long MAX_IMAGE_BYTES = 2L * 1024 * 1024;
    protected static final int MAX_GALLERY_UPLOAD_FILES = 20;

    protected final NamedParameterJdbcTemplate jdbcTemplate;
    protected final IFileStorageService fileStorageService;
    protected final UploadProperties uploadProperties;
    protected final HotelImageRepository hotelImageRepository;
    protected final RoomTypeImageRepository roomTypeImageRepository;
    protected final ReviewImageRepository reviewImageRepository;
    protected final NewsImageRepository newsImageRepository;
    protected final BannerImageRepository bannerImageRepository;

    protected Milestone6ServiceSupport(NamedParameterJdbcTemplate jdbcTemplate, IFileStorageService fileStorageService, UploadProperties uploadProperties) {
        this(jdbcTemplate, fileStorageService, uploadProperties, null, null, null, null, null);
    }

    protected Milestone6ServiceSupport(NamedParameterJdbcTemplate jdbcTemplate,
                                       IFileStorageService fileStorageService,
                                       UploadProperties uploadProperties,
                                       HotelImageRepository hotelImageRepository,
                                       RoomTypeImageRepository roomTypeImageRepository,
                                       ReviewImageRepository reviewImageRepository,
                                       NewsImageRepository newsImageRepository,
                                       BannerImageRepository bannerImageRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.fileStorageService = fileStorageService;
        this.uploadProperties = uploadProperties;
        this.hotelImageRepository = hotelImageRepository;
        this.roomTypeImageRepository = roomTypeImageRepository;
        this.reviewImageRepository = reviewImageRepository;
        this.newsImageRepository = newsImageRepository;
        this.bannerImageRepository = bannerImageRepository;
    }


    protected ImageAssetResponse createImageAsset(MultipartFile file, UUID ownerId) {
        validateImageFile(file);
        UUID id = UUID.randomUUID();
        String provider = "CLOUDINARY".equalsIgnoreCase(uploadProperties.mode()) ? "CLOUDINARY" : "LOCAL";
        String publicId = provider.toLowerCase(Locale.ROOT) + "/" + id;
        String url = "LOCAL".equals(provider) ? "/api/v1/uploads/local/" + id : fileStorageService.uploadFile(file, publicId);
        jdbcTemplate.update("""
                insert into image_assets (id, owner_account_id, provider, public_id, url, secure_url, width, height, bytes)
                values (:id, :ownerId, :provider, :publicId, :url, :secureUrl, :width, :height, :bytes)
                """, new MapSqlParameterSource("id", id)
                .addValue("ownerId", ownerId)
                .addValue("provider", provider)
                .addValue("publicId", publicId)
                .addValue("url", url)
                .addValue("secureUrl", url)
                .addValue("width", 1200)
                .addValue("height", 800)
                .addValue("bytes", file == null ? 0 : file.getSize()));
        return requireImageAsset(id);
    }

    protected void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw badRequest("Image file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !Set.of("image/png", "image/jpeg", "image/webp", "image/gif").contains(contentType)) {
            throw badRequest("Only png, jpeg, webp, and gif images are supported");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw badRequest("Image file must be 2MB or smaller");
        }
    }

    protected List<ImageAssetResponse> requireOwnedImages(List<UUID> imageIds, CurrentUser user) {
        if (imageIds == null) {
            return List.of();
        }
        if (imageIds.size() > Math.max(1, uploadProperties.maxImageCount())) {
            throw badRequest("Too many images");
        }
        Set<UUID> unique = new HashSet<>();
        for (UUID imageId : imageIds) {
            if (imageId == null || !unique.add(imageId)) {
                throw badRequest("Image IDs must be non-null and unique");
            }
        }
        if (unique.isEmpty()) {
            return List.of();
        }
        MapSqlParameterSource params = new MapSqlParameterSource("ids", unique).addValue("ownerId", user.accountId());
        String ownerClause = isAdmin(user) ? "" : "and owner_account_id = :ownerId\n";
        List<ImageAssetResponse> images = jdbcTemplate.query("""
                select *
                from image_assets
                where id in (:ids)
                """ + ownerClause + """
                order by created_at
                """, params, (rs, rowNum) -> mapImageAsset(rs));
        if (images.size() != unique.size()) {
            throw badRequest("Image IDs must reference images you own");
        }
        return images;
    }

    protected List<HotelImageSource> resolveHotelImages(UUID hotelId, List<UUID> imageIds, CurrentUser user) {
        validateHotelImageIds(imageIds);
        if (imageIds == null || imageIds.isEmpty()) {
            return List.of();
        }

        Set<UUID> requestedIds = new HashSet<>(imageIds);
        List<ExistingHotelImage> existingImages = hotelImageRepository == null
                ? jdbcTemplate.query("""
                        select id, image_asset_id, url
                        from hotel_images
                        where hotel_id = :hotelId and id in (:ids)
                        """, new MapSqlParameterSource("hotelId", hotelId).addValue("ids", requestedIds),
                (rs, rowNum) -> new ExistingHotelImage(
                        rs.getObject("id", UUID.class),
                        rs.getObject("image_asset_id", UUID.class),
                        rs.getString("url")
                ))
                : hotelImageRepository.findByHotel_IdOrderBySortOrderAsc(hotelId).stream()
                .filter(image -> requestedIds.contains(image.getId()))
                .map(image -> new ExistingHotelImage(image.getId(), image.getImageAssetId(), image.getUrl()))
                .toList();

        Set<UUID> existingIds = existingImages.stream()
                .map(ExistingHotelImage::id)
                .collect(java.util.stream.Collectors.toSet());
        List<UUID> galleryImageIds = imageIds.stream()
                .filter(imageId -> !existingIds.contains(imageId))
                .toList();
        List<ImageAssetResponse> ownedImages = galleryImageIds.isEmpty()
                ? List.of()
                : requireOwnedImages(galleryImageIds, user);

        List<HotelImageSource> resolved = new ArrayList<>();
        for (UUID imageId : imageIds) {
            ExistingHotelImage existingImage = existingImages.stream()
                    .filter(candidate -> candidate.id().equals(imageId))
                    .findFirst()
                    .orElse(null);
            if (existingImage != null) {
                resolved.add(new HotelImageSource(existingImage.imageAssetId(), existingImage.url()));
                continue;
            }

            ImageAssetResponse galleryImage = ownedImages.stream()
                    .filter(candidate -> candidate.id().equals(imageId))
                    .findFirst()
                    .orElseThrow(() -> badRequest("Image IDs must reference images you own"));
            resolved.add(new HotelImageSource(
                    galleryImage.id(),
                    galleryImage.secureUrl() == null ? galleryImage.url() : galleryImage.secureUrl()
            ));
        }
        return resolved;
    }

    private void validateHotelImageIds(List<UUID> imageIds) {
        if (imageIds == null) {
            return;
        }
        if (imageIds.size() > Math.max(1, uploadProperties.maxImageCount())) {
            throw badRequest("Too many images");
        }
        Set<UUID> unique = new HashSet<>();
        for (UUID imageId : imageIds) {
            if (imageId == null || !unique.add(imageId)) {
                throw badRequest("Image IDs must be non-null and unique");
            }
        }
    }

    protected MapSqlParameterSource snapshotParams(String targetName, UUID targetId, ImageAssetResponse image, int sortOrder) {
        return new MapSqlParameterSource("id", UUID.randomUUID())
                .addValue(targetName, targetId)
                .addValue("imageAssetId", image.id())
                .addValue("url", normalizeImageUrl(image.secureUrl() == null ? image.url() : image.secureUrl()))
                .addValue("sortOrder", sortOrder);
    }

    protected List<ImageSnapshotResponse> listHotelImages(UUID hotelId) {
        if (hotelImageRepository != null) {
            return hotelImageRepository.findByHotel_IdOrderBySortOrderAsc(hotelId).stream()
                    .map(this::toImageSnapshot)
                    .toList();
        }
        return jdbcTemplate.query("""
                select id, image_asset_id, url, sort_order
                from hotel_images
                where hotel_id = :hotelId
                order by sort_order
                """, new MapSqlParameterSource("hotelId", hotelId), (rs, rowNum) -> mapSnapshot(rs));
    }

    protected List<ImageSnapshotResponse> listRoomTypeImages(UUID roomTypeId) {
        if (roomTypeImageRepository != null) {
            return roomTypeImageRepository.findByRoomType_IdOrderBySortOrderAsc(roomTypeId).stream()
                    .map(this::toImageSnapshot)
                    .toList();
        }
        return jdbcTemplate.query("""
                select id, image_asset_id, url, sort_order
                from room_type_images
                where room_type_id = :roomTypeId
                order by sort_order
                """, new MapSqlParameterSource("roomTypeId", roomTypeId), (rs, rowNum) -> mapSnapshot(rs));
    }

    protected void insertReviewImages(UUID reviewId, List<UUID> imageIds, CurrentUser user) {
        List<ImageAssetResponse> images = requireOwnedImages(imageIds, user);
        if (reviewImageRepository != null) {
            List<ReviewImage> reviewImages = new ArrayList<>();
            int order = 0;
            for (ImageAssetResponse image : images) {
                reviewImages.add(newReviewImage(reviewId, image, order++));
            }
            reviewImageRepository.saveAll(reviewImages);
            return;
        }
        int order = 0;
        for (ImageAssetResponse image : images) {
            jdbcTemplate.update("""
                    insert into review_images (id, review_id, image_asset_id, url, sort_order)
                    values (:id, :reviewId, :imageAssetId, :url, :sortOrder)
                    """, snapshotParams("reviewId", reviewId, image, order++));
        }
    }

    protected void replaceNewsImages(UUID newsId, List<UUID> imageIds, CurrentUser user) {
        if (imageIds == null) {
            return;
        }
          List<BannerImageSource> images = resolveNewsImages(newsId, imageIds, user);
          if (newsImageRepository != null) {
              newsImageRepository.deleteByNews_Id(newsId);
              newsImageRepository.flush();
              List<NewsImage> newsImages = new ArrayList<>();
            int order = 0;
            for (BannerImageSource image : images) {
                newsImages.add(newNewsImage(newsId, image, order++));
            }
            newsImageRepository.saveAll(newsImages);
            return;
        }
        jdbcTemplate.update("delete from news_images where news_id = :newsId", new MapSqlParameterSource("newsId", newsId));
        int order = 0;
        for (BannerImageSource image : images) {
            jdbcTemplate.update("""
                    insert into news_images (id, news_id, image_asset_id, url, sort_order)
                    values (:id, :newsId, :imageAssetId, :url, :sortOrder)
                    """, new MapSqlParameterSource("id", UUID.randomUUID())
                    .addValue("newsId", newsId)
                    .addValue("imageAssetId", image.imageAssetId())
                    .addValue("url", normalizeImageUrl(image.url()))
                    .addValue("sortOrder", order++));
        }
    }

    protected List<BannerImageSource> resolveNewsImages(UUID newsId, List<UUID> imageIds, CurrentUser user) {
        if (imageIds == null) {
            return List.of();
        }
        if (imageIds.size() > Math.max(1, uploadProperties.maxImageCount())) {
            throw badRequest("Too many images");
        }
        Set<UUID> unique = new HashSet<>();
        for (UUID imageId : imageIds) {
            if (imageId == null || !unique.add(imageId)) {
                throw badRequest("Image IDs must be non-null and unique");
            }
        }
        if (unique.isEmpty()) {
            return List.of();
        }
        List<NewsImageSource> existing = newsImageRepository == null
                ? jdbcTemplate.query("""
                    select id, image_asset_id, url
                    from news_images
                    where news_id = :newsId and id in (:ids)
                    """, new MapSqlParameterSource("newsId", newsId).addValue("ids", unique), (rs, rowNum) ->
                    new NewsImageSource(rs.getObject("id", UUID.class), rs.getObject("image_asset_id", UUID.class), rs.getString("url")))
                : newsImageRepository.findByNews_IdAndIdIn(newsId, unique).stream()
                .map(image -> new NewsImageSource(image.getId(), image.getImageAssetId(), image.getUrl()))
                .toList();
        Set<UUID> existingIds = new HashSet<>();
        for (NewsImageSource image : existing) {
            existingIds.add(image.id());
        }
        Set<UUID> galleryIds = new HashSet<>(unique);
        galleryIds.removeAll(existingIds);
        List<ImageAssetResponse> owned = galleryIds.isEmpty() ? List.of() : requireOwnedImages(new ArrayList<>(galleryIds), user);
        List<BannerImageSource> resolved = new ArrayList<>();
        for (UUID imageId : imageIds) {
            NewsImageSource existingImage = existing.stream()
                    .filter(candidate -> candidate.id().equals(imageId))
                    .findFirst()
                    .orElse(null);
            if (existingImage != null) {
                resolved.add(new BannerImageSource(existingImage.imageAssetId(), existingImage.url()));
                continue;
            }
            ImageAssetResponse galleryImage = owned.stream()
                    .filter(candidate -> candidate.id().equals(imageId))
                    .findFirst()
                    .orElseThrow(() -> badRequest("Image IDs must reference images you own"));
            resolved.add(new BannerImageSource(galleryImage.id(), galleryImage.secureUrl() == null ? galleryImage.url() : galleryImage.secureUrl()));
        }
        return resolved;
    }

    protected void replaceBannerImages(UUID bannerId, List<BannerImageSource> images) {
          if (bannerImageRepository != null) {
              bannerImageRepository.deleteByBanner_Id(bannerId);
              bannerImageRepository.flush();
              List<BannerImage> bannerImages = new ArrayList<>();
            int order = 0;
            for (BannerImageSource image : images) {
                bannerImages.add(newBannerImage(bannerId, image, order++));
            }
            bannerImageRepository.saveAll(bannerImages);
            return;
        }
        jdbcTemplate.update("delete from banner_images where banner_id = :bannerId", new MapSqlParameterSource("bannerId", bannerId));
        int order = 0;
        for (BannerImageSource image : images) {
            jdbcTemplate.update("""
                    insert into banner_images (id, banner_id, image_asset_id, url, sort_order)
                    values (:id, :bannerId, :imageAssetId, :url, :sortOrder)
                    """, new MapSqlParameterSource("id", UUID.randomUUID())
                    .addValue("bannerId", bannerId)
                    .addValue("imageAssetId", image.imageAssetId())
                    .addValue("url", normalizeImageUrl(image.url()))
                    .addValue("sortOrder", order++));
        }
    }

    protected List<BannerImageSource> resolveBannerImages(BannerMutationRequest request, CurrentUser user) {
        if (request.imageIds() != null) {
            return requireOwnedImages(request.imageIds(), user).stream()
                    .map(image -> new BannerImageSource(image.id(), image.secureUrl() == null ? image.url() : image.secureUrl()))
                    .toList();
        }
        if (request.images() == null) {
            return List.of();
        }
        return request.images().stream()
                .map(this::normalizeImageUrl)
                .map(url -> new BannerImageSource(null, url))
                .toList();
    }

    protected BookingReviewSource requireCompletedBookingForReview(UUID hotelId, UUID bookingId, UUID accountId) {
        return jdbcTemplate.query("""
                select id, hotel_id
                from bookings
                where id = :bookingId
                  and hotel_id = :hotelId
                  and account_id = :accountId
                  and status = 'COMPLETED'
                  and exists (
                      select 1
                      from hotels h
                      where h.id = bookings.hotel_id
                        and h.deleted_at is null
                        and h.status in ('ACTIVE', 'SUSPENDED')
                  )
                """, new MapSqlParameterSource("bookingId", bookingId).addValue("hotelId", hotelId).addValue("accountId", accountId),
                rs -> {
                    if (!rs.next()) {
                        throw conflict("Only completed bookings can be reviewed");
                    }
                    return new BookingReviewSource(rs.getObject("id", UUID.class), rs.getObject("hotel_id", UUID.class));
                });
    }

    protected ListResponse<ReviewResponse> listReviews(UUID hotelId, boolean visibleOnly, int page, int limit, UUID accountId) {
        int boundedLimit = boundedLimit(limit);
        int boundedPage = boundedPage(page);
        MapSqlParameterSource params = new MapSqlParameterSource("hotelId", hotelId)
                .addValue("accountId", accountId)
                .addValue("limit", boundedLimit)
                .addValue("offset", offset(boundedPage, boundedLimit));
        String where = """
                where (cast(:hotelId as uuid) is null or r.hotel_id = :hotelId)
                  and (cast(:accountId as uuid) is null or r.account_id = :accountId)
                  and r.deleted_at is null
                """ + (visibleOnly ? """
                  and r.visible
                  and exists (
                      select 1
                      from hotels h
                      where h.id = r.hotel_id and h.status = 'ACTIVE' and h.deleted_at is null
                  )
                """ : "");
        long total = jdbcTemplate.queryForObject("select count(*) from reviews r " + where, params, Long.class);
        List<ReviewResponse> data = jdbcTemplate.query("""
                select r.*, a.email, a.first_name, a.last_name, a.avatar_url,
                       h.name hotel_name, h.address hotel_address, h.city hotel_city, h.country hotel_country
                from reviews r
                join accounts a on a.id = r.account_id
                join hotels h on h.id = r.hotel_id
                """ + where + """
                order by r.created_at desc
                limit :limit offset :offset
                """, params, (rs, rowNum) -> mapReview(rs));
        return paginated(data, boundedPage, boundedLimit, total);
    }

    protected ReviewResponse reviewDetail(UUID reviewId, boolean visibleOnly) {
        return jdbcTemplate.query("""
                select r.*, a.email, a.first_name, a.last_name, a.avatar_url,
                       h.name hotel_name, h.address hotel_address, h.city hotel_city, h.country hotel_country
                from reviews r
                join accounts a on a.id = r.account_id
                join hotels h on h.id = r.hotel_id
                where r.id = :id
                  and r.deleted_at is null
                """ + (visibleOnly ? " and r.visible\n" : ""),
                new MapSqlParameterSource("id", reviewId), rs -> {
                    if (!rs.next()) {
                        throw notFound("Review not found");
                    }
                    return mapReview(rs);
                });
    }

    protected ReviewResponse mapReview(ResultSet rs) throws SQLException {
        UUID id = rs.getObject("id", UUID.class);
        return new ReviewResponse(
                id,
                rs.getObject("booking_id", UUID.class),
                rs.getObject("hotel_id", UUID.class),
                rs.getObject("account_id", UUID.class),
                rs.getBigDecimal("rating"),
                rs.getString("comment"),
                rs.getBoolean("visible"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                listReviewImages(id),
                accountSummary(rs, "", "avatar_url"),
                new org.example.hotelbookingservice.dto.response.booking.operations.HotelSummary(
                        rs.getObject("hotel_id", UUID.class),
                        rs.getString("hotel_name"),
                        rs.getString("hotel_address"),
                        rs.getString("hotel_city"),
                        rs.getString("hotel_country")
                )
        );
    }

    protected List<ImageSnapshotResponse> listReviewImages(UUID reviewId) {
        if (reviewImageRepository != null) {
            return reviewImageRepository.findByReview_IdOrderBySortOrderAsc(reviewId).stream()
                    .map(this::toImageSnapshot)
                    .toList();
        }
        return jdbcTemplate.query("""
                select id, image_asset_id, url, sort_order
                from review_images
                where review_id = :reviewId
                order by sort_order
                """, new MapSqlParameterSource("reviewId", reviewId), (rs, rowNum) -> mapSnapshot(rs));
    }

    protected ListResponse<NewsResponse> listNews(String status, String q, int page, int limit, boolean publicOnly) {
        int boundedLimit = boundedLimit(limit);
        int boundedPage = boundedPage(page);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("status", normalizeOptionalStatus(status, NEWS_STATUSES, "Invalid news status"))
                .addValue("q", like(q))
                .addValue("limit", boundedLimit)
                .addValue("offset", offset(boundedPage, boundedLimit));
        String where = """
                where (cast(:status as text) is null or n.status = :status)
                  and (cast(:q as text) is null or lower(n.title || ' ' || coalesce(n.summary, '') || ' ' || coalesce(n.content, '')) like :q)
                """ + (publicOnly ? " and n.published_at <= now()\n" : "");
        long total = jdbcTemplate.queryForObject("select count(*) from news n " + where, params, Long.class);
        List<NewsResponse> items = jdbcTemplate.query("""
                select n.*
                from news n
                """ + where + """
                order by coalesce(n.published_at, n.created_at) desc
                limit :limit offset :offset
                """, params, (rs, rowNum) -> mapNews(rs));
        return paginated(items, boundedPage, boundedLimit, total);
    }

    protected NewsResponse queryNews(String where, MapSqlParameterSource params, boolean publicOnly) {
        return jdbcTemplate.query("""
                select n.*
                from news n
                """ + where,
                params, rs -> {
                    if (!rs.next()) {
                        throw notFound("News not found");
                    }
                    return mapNews(rs);
                });
    }

    protected NewsResponse mapNews(ResultSet rs) throws SQLException {
        UUID id = rs.getObject("id", UUID.class);
        return new NewsResponse(
                id,
                rs.getString("title"),
                rs.getString("slug"),
                rs.getString("summary"),
                rs.getString("content"),
                rs.getString("status"),
                instantOrNull(rs, "published_at"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                listNewsImages(id)
        );
    }

    protected List<NewsImageResponse> listNewsImages(UUID newsId) {
        if (newsImageRepository != null) {
            return newsImageRepository.findByNews_IdOrderBySortOrderAsc(newsId).stream()
                    .map(image -> new NewsImageResponse(image.getId(), image.getNewsId(), image.getUrl()))
                    .toList();
        }
        return jdbcTemplate.query("""
                select id, news_id, url
                from news_images
                where news_id = :newsId
                order by sort_order
                """, new MapSqlParameterSource("newsId", newsId), (rs, rowNum) ->
                new NewsImageResponse(rs.getObject("id", UUID.class), rs.getObject("news_id", UUID.class), rs.getString("url")));
    }

    protected BannerResponse bannerDetail(UUID id) {
        return jdbcTemplate.query("select * from banners where id = :id", new MapSqlParameterSource("id", id), rs -> {
            if (!rs.next()) {
                throw notFound("Banner not found");
            }
            return mapBanner(rs);
        });
    }

    protected BannerResponse mapBanner(ResultSet rs) throws SQLException {
        UUID id = rs.getObject("id", UUID.class);
        return new BannerResponse(
                id,
                rs.getString("title"),
                rs.getString("subtitle"),
                rs.getString("link_url"),
                rs.getString("link_type"),
                rs.getInt("position"),
                rs.getBoolean("active"),
                instantOrNull(rs, "starts_at"),
                instantOrNull(rs, "ends_at"),
                listBannerImages(id, rs.getString("image_url")),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    protected List<BannerImageResponse> listBannerImages(UUID bannerId, String fallbackUrl) {
        List<BannerImageResponse> images = bannerImageRepository == null
                ? jdbcTemplate.query("""
                    select id, banner_id, url
                    from banner_images
                    where banner_id = :bannerId
                    order by sort_order
                    """, new MapSqlParameterSource("bannerId", bannerId), (rs, rowNum) ->
                    new BannerImageResponse(rs.getObject("id", UUID.class), rs.getObject("banner_id", UUID.class), rs.getString("url")))
                : bannerImageRepository.findByBanner_IdOrderBySortOrderAsc(bannerId).stream()
                .map(image -> new BannerImageResponse(image.getId(), image.getBannerId(), image.getUrl()))
                .toList();
        if (images.isEmpty() && fallbackUrl != null) {
            return List.of(new BannerImageResponse(bannerId, bannerId, fallbackUrl));
        }
        return images;
    }

    protected ContactResponse queryContact(UUID id) {
        return jdbcTemplate.query("""
                select cm.*, a.email handled_email, a.first_name handled_first_name, a.last_name handled_last_name, a.avatar_url handled_avatar_url
                from contact_messages cm
                left join accounts a on a.id = cm.handled_by_account_id
                where cm.id = :id
                """, new MapSqlParameterSource("id", id), rs -> {
            if (!rs.next()) {
                throw notFound("Contact not found");
            }
            return mapContact(rs);
        });
    }

    protected ContactResponse mapContact(ResultSet rs) throws SQLException {
        UUID handledById = rs.getObject("handled_by_account_id", UUID.class);
        AccountSummary handledBy = handledById == null ? null : new AccountSummary(
                handledById,
                rs.getString("handled_email"),
                rs.getString("handled_first_name"),
                rs.getString("handled_last_name"),
                avatar(rs.getString("handled_avatar_url"))
        );
        return new ContactResponse(
                rs.getObject("id", UUID.class),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("subject"),
                rs.getString("message"),
                rs.getString("status"),
                rs.getObject("account_id", UUID.class),
                handledById,
                handledBy,
                rs.getString("note"),
                rs.getString("ip_address"),
                rs.getString("user_agent"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    protected NotificationResponse notificationDetail(UUID id, UUID accountId) {
        return jdbcTemplate.query("""
                select *
                from notifications
                where id = :id and recipient_account_id = :accountId
                """, new MapSqlParameterSource("id", id).addValue("accountId", accountId), rs -> {
            if (!rs.next()) {
                throw notFound("Notification not found");
            }
            return mapNotification(rs);
        });
    }

    protected NotificationResponse mapNotification(ResultSet rs) throws SQLException {
        Instant readAt = instantOrNull(rs, "read_at");
        return new NotificationResponse(
                rs.getObject("id", UUID.class),
                rs.getString("type"),
                rs.getString("title"),
                rs.getString("body"),
                rs.getString("link_url"),
                readAt != null,
                readAt,
                rs.getTimestamp("created_at").toInstant()
        );
    }

    protected List<NewestBookingItemResponse> bookingItems(UUID bookingId) {
        return jdbcTemplate.query("""
                select room_type_name, quantity
                from booking_items
                where booking_id = :bookingId
                order by created_at
                """, new MapSqlParameterSource("bookingId", bookingId), (rs, rowNum) ->
                new NewestBookingItemResponse(
                        new RoomTypeSummary(rs.getString("room_type_name")),
                        rs.getInt("quantity")
                ));
    }

    protected CommissionPackageResponse queryCommissionPackage(UUID id) {
        return jdbcTemplate.query("select * from commission_packages where id = :id", new MapSqlParameterSource("id", id), rs -> {
            if (!rs.next()) {
                throw notFound("Commission package not found");
            }
            return mapCommissionPackage(rs);
        });
    }

    protected CommissionPackageResponse mapCommissionPackage(ResultSet rs) throws SQLException {
        return new CommissionPackageResponse(
                rs.getObject("id", UUID.class),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("commission_rate"),
                rs.getBoolean("active"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    protected CommissionAssignmentResponse commissionAssignment(UUID hotelId) {
        return jdbcTemplate.query("""
                select hcp.hotel_id, hcp.commission_package_id, cp.code, cp.commission_rate, hcp.assigned_at
                from hotel_commission_packages hcp
                join commission_packages cp on cp.id = hcp.commission_package_id
                where hcp.hotel_id = :hotelId
                """, new MapSqlParameterSource("hotelId", hotelId), rs -> {
            if (!rs.next()) {
                throw notFound("Commission assignment not found");
            }
            return new CommissionAssignmentResponse(
                    rs.getObject("hotel_id", UUID.class),
                    rs.getObject("commission_package_id", UUID.class),
                    rs.getString("code"),
                    rs.getBigDecimal("commission_rate"),
                    rs.getTimestamp("assigned_at").toInstant()
            );
        });
    }

    protected PolicyResponse queryPolicy(UUID hotelId, UUID policyId) {
        return jdbcTemplate.query("""
                select *
                from hotel_policies
                where hotel_id = :hotelId and id = :id
                """, new MapSqlParameterSource("hotelId", hotelId).addValue("id", policyId), rs -> {
            if (!rs.next()) {
                throw notFound("Policy not found");
            }
            return mapPolicy(rs);
        });
    }

    protected PolicyResponse mapPolicy(ResultSet rs) throws SQLException {
        return new PolicyResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("hotel_id", UUID.class),
                denormalizePolicyType(rs.getString("type")),
                rs.getString("title"),
                rs.getString("content"),
                rs.getBoolean("enabled"),
                rs.getInt("sort_order"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                null
        );
    }

    protected MapSqlParameterSource policyParams(UUID id, UUID hotelId, PolicyMutationRequest request, PolicyResponse current) {
        return new MapSqlParameterSource("id", id)
                .addValue("hotelId", hotelId)
                .addValue("type", normalizePolicyType(request.type() == null && current != null ? current.type() : request.type()))
                .addValue("title", request.title() == null && current != null ? current.title() : trimRequired(request.title(), "Policy title is required"))
                .addValue("content", request.content() == null && current != null ? current.content() : trimRequired(request.content(), "Policy content is required"))
                .addValue("enabled", request.enabled() == null ? current == null || current.enabled() : request.enabled())
                .addValue("sortOrder", request.sortOrder() == null ? current == null ? 0 : current.order() : request.sortOrder());
    }

    protected ImageAssetResponse requireImageAsset(UUID id) {
        return jdbcTemplate.query("select * from image_assets where id = :id", new MapSqlParameterSource("id", id), rs -> {
            if (!rs.next()) {
                throw notFound("Image asset not found");
            }
            return mapImageAsset(rs);
        });
    }

    protected ImageAssetResponse mapImageAsset(ResultSet rs) throws SQLException {
        return new ImageAssetResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("owner_account_id", UUID.class),
                rs.getString("provider"),
                rs.getString("public_id"),
                rs.getString("url"),
                rs.getString("secure_url"),
                (Integer) rs.getObject("width"),
                (Integer) rs.getObject("height"),
                (Long) rs.getObject("bytes"),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    protected GalleryFolderResponse mapGalleryFolder(ResultSet rs) throws SQLException {
        String folderName = rs.getString("folder_name");
        return new GalleryFolderResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("owner_account_id", UUID.class),
                folderName,
                folderName,
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    protected GalleryFolderResponse queryGalleryFolder(UUID ownerId, String folderName) {
        return jdbcTemplate.queryForObject("""
                select *
                from gallery_folders
                where owner_account_id = :ownerId and folder_name = :folderName
                """, new MapSqlParameterSource("ownerId", ownerId).addValue("folderName", folderName),
                (rs, rowNum) -> mapGalleryFolder(rs));
    }

    protected GalleryFolderResponse getOrCreateGalleryFolder(String folderName, UUID ownerId) {
        List<GalleryFolderResponse> existing = jdbcTemplate.query("""
                select *
                from gallery_folders
                where owner_account_id = :ownerId and folder_name = :folderName
                """, new MapSqlParameterSource("ownerId", ownerId).addValue("folderName", folderName),
                (rs, rowNum) -> mapGalleryFolder(rs));
        if (!existing.isEmpty()) {
            return existing.getFirst();
        }
        try {
            jdbcTemplate.update("""
                    insert into gallery_folders (id, owner_account_id, folder_name)
                    values (:id, :ownerId, :folderName)
                    """, new MapSqlParameterSource("id", UUID.randomUUID())
                    .addValue("ownerId", ownerId)
                    .addValue("folderName", folderName));
        } catch (DuplicateKeyException ignored) {
            return queryGalleryFolder(ownerId, folderName);
        }
        return queryGalleryFolder(ownerId, folderName);
    }

    protected ImageSnapshotResponse mapSnapshot(ResultSet rs) throws SQLException {
        return new ImageSnapshotResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("image_asset_id", UUID.class),
                rs.getString("url"),
                rs.getInt("sort_order")
        );
    }

    protected ImageSnapshotResponse toImageSnapshot(HotelImage image) {
        return new ImageSnapshotResponse(image.getId(), image.getImageAssetId(), image.getUrl(), image.getSortOrder());
    }

    protected ImageSnapshotResponse toImageSnapshot(RoomTypeImage image) {
        return new ImageSnapshotResponse(image.getId(), image.getImageAssetId(), image.getUrl(), image.getSortOrder());
    }

    protected ImageSnapshotResponse toImageSnapshot(ReviewImage image) {
        return new ImageSnapshotResponse(image.getId(), image.getImageAssetId(), image.getUrl(), image.getSortOrder());
    }

    protected HotelImage newHotelImage(UUID hotelId, ImageAssetResponse image, int sortOrder) {
        return newHotelImage(
                hotelId,
                new HotelImageSource(image.id(), image.secureUrl() == null ? image.url() : image.secureUrl()),
                sortOrder
        );
    }

    protected HotelImage newHotelImage(UUID hotelId, HotelImageSource image, int sortOrder) {
        HotelImage hotelImage = new HotelImage();
        Hotel hotel = new Hotel();
        hotel.setId(hotelId);
        hotelImage.setHotel(hotel);
        hotelImage.setImageAsset(image.imageAssetId() == null ? null : imageReference(image.imageAssetId()));
        hotelImage.setUrl(normalizeImageUrl(image.url()));
        hotelImage.setSortOrder(sortOrder);
        return hotelImage;
    }

      protected RoomTypeImage newRoomTypeImage(UUID roomTypeId, ImageAssetResponse image, int sortOrder) {
          RoomTypeImage roomTypeImage = new RoomTypeImage();
          Room roomType = new Room();
          roomType.setId(roomTypeId);
        roomTypeImage.setRoomType(roomType);
        roomTypeImage.setImageAsset(imageReference(image.id()));
        roomTypeImage.setUrl(normalizeImageUrl(image.secureUrl() == null ? image.url() : image.secureUrl()));
        roomTypeImage.setSortOrder(sortOrder);
        return roomTypeImage;
    }

      protected ReviewImage newReviewImage(UUID reviewId, ImageAssetResponse image, int sortOrder) {
          ReviewImage reviewImage = new ReviewImage();
          Review review = new Review();
          review.setId(reviewId);
        reviewImage.setReview(review);
        reviewImage.setImageAsset(imageReference(image.id()));
        reviewImage.setUrl(normalizeImageUrl(image.secureUrl() == null ? image.url() : image.secureUrl()));
        reviewImage.setSortOrder(sortOrder);
        return reviewImage;
    }

      protected NewsImage newNewsImage(UUID newsId, BannerImageSource image, int sortOrder) {
          NewsImage newsImage = new NewsImage();
          News news = new News();
          news.setId(newsId);
        newsImage.setNews(news);
        newsImage.setImageAsset(image.imageAssetId() == null ? null : imageReference(image.imageAssetId()));
        newsImage.setUrl(normalizeImageUrl(image.url()));
        newsImage.setSortOrder(sortOrder);
        return newsImage;
    }

      protected BannerImage newBannerImage(UUID bannerId, BannerImageSource image, int sortOrder) {
          BannerImage bannerImage = new BannerImage();
          Banner banner = new Banner();
          banner.setId(bannerId);
        bannerImage.setBanner(banner);
        bannerImage.setImageAsset(image.imageAssetId() == null ? null : imageReference(image.imageAssetId()));
        bannerImage.setUrl(normalizeImageUrl(image.url()));
        bannerImage.setSortOrder(sortOrder);
        return bannerImage;
    }

    private Image imageReference(UUID imageAssetId) {
        Image image = new Image();
        image.setId(imageAssetId);
        return image;
    }

    protected AccountSummary accountSummary(ResultSet rs, String prefix, String avatarColumn) throws SQLException {
        UUID id = rs.getObject(prefix + "account_id", UUID.class);
        if (id == null) {
            id = rs.getObject("id", UUID.class);
        }
        return new AccountSummary(
                id,
                rs.getString("email"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                avatar(rs.getString(avatarColumn))
        );
    }

    protected AvatarSummary avatar(String secureUrl) {
        return secureUrl == null ? null : new AvatarSummary(secureUrl);
    }

    protected CurrentUser requireUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AccountAuthUser account)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return new CurrentUser(account.getAccountId());
    }

    protected CurrentUser currentUser(Authentication authentication) {
        return requireUser(authentication);
    }

    protected void requireAdmin(Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        if (!isAdmin(user)) {
            throw forbidden("ADMIN role required");
        }
    }

    protected boolean isAdmin(CurrentUser user) {
        Boolean admin = jdbcTemplate.queryForObject("""
                select exists (
                    select 1
                    from account_roles ar
                    join roles r on r.id = ar.role_id
                    where ar.account_id = :accountId and r.name = 'ADMIN'
                )
                """, new MapSqlParameterSource("accountId", user.accountId()), Boolean.class);
        return Boolean.TRUE.equals(admin);
    }

    protected void requireCanManageHotel(UUID hotelId, CurrentUser user) {
        if (isAdmin(user)) {
            return;
        }
        Boolean allowed = jdbcTemplate.queryForObject("""
                select exists (
                    select 1
                    from hotels h
                    left join hotel_members hm on hm.hotel_id = h.id and hm.account_id = :accountId
                    where h.id = :hotelId
                      and (h.owner_id = :accountId or hm.account_id is not null)
                )
                """, new MapSqlParameterSource("hotelId", hotelId).addValue("accountId", user.accountId()), Boolean.class);
        if (!Boolean.TRUE.equals(allowed)) {
            throw forbidden("Hotel access denied");
        }
    }

    protected ReportScope reportScope(UUID hotelId, CurrentUser user) {
        if (hotelId == null) {
            if (!isAdmin(user)) {
                throw forbidden("Hotel-scoped reports require a hotelId");
            }
            return ReportScope.global();
        }
        requireAction(user, "reports.hotel.view", hotelId);
        return ReportScope.hotel(hotelId);
    }

    protected void requireAction(CurrentUser user, String actionKey, UUID hotelId) {
        // Platform administrators bypass all fine-grained action/scope checks (grant-admin-full-access).
        if (isAdmin(user)) {
            return;
        }
        Boolean allowed = jdbcTemplate.queryForObject("""
                select exists (
                    select 1
                    from api_actions a
                    join action_policies ap on ap.action_id = a.id
                    join role_permissions rp on rp.permission_id = ap.permission_id
                    join account_roles ar on ar.role_id = rp.role_id
                    where a.key = :actionKey
                      and a.enabled
                      and ar.account_id = :accountId
                      and (
                          ap.scope = 'GLOBAL'
                          or ap.scope = 'SELF'
                          or (
                              cast(:hotelId as uuid) is not null
                              and ap.scope = 'HOTEL_MEMBER'
                              and exists (
                                  select 1
                                  from hotel_members hm
                                  where hm.hotel_id = :hotelId and hm.account_id = :accountId
                              )
                          )
                          or (
                              cast(:hotelId as uuid) is not null
                              and ap.scope = 'HOTEL_OWNER'
                              and exists (
                                  select 1
                                  from hotels h
                                  where h.id = :hotelId and h.owner_id = :accountId
                              )
                          )
                      )
                )
                """, new MapSqlParameterSource("actionKey", actionKey)
                .addValue("accountId", user.accountId())
                .addValue("hotelId", hotelId, Types.OTHER), Boolean.class);
        if (!Boolean.TRUE.equals(allowed)) {
            throw forbidden("Action not allowed: " + actionKey);
        }
    }

    protected void requireFolderOwner(UUID folderId, UUID accountId) {
        Boolean exists = jdbcTemplate.queryForObject("""
                select exists(select 1 from gallery_folders where id = :folderId and owner_account_id = :accountId)
                """, new MapSqlParameterSource("folderId", folderId).addValue("accountId", accountId), Boolean.class);
        if (!Boolean.TRUE.equals(exists)) {
            throw notFound("Gallery folder not found");
        }
    }

    protected void requireAccountExists(UUID accountId) {
        Boolean exists = jdbcTemplate.queryForObject("select exists(select 1 from accounts where id = :id)",
                new MapSqlParameterSource("id", accountId), Boolean.class);
        if (!Boolean.TRUE.equals(exists)) {
            throw badRequest("Account does not exist");
        }
    }

    protected void requireHotelExists(UUID hotelId) {
        Boolean exists = jdbcTemplate.queryForObject("select exists(select 1 from hotels where id = :id and deleted_at is null)",
                new MapSqlParameterSource("id", hotelId), Boolean.class);
        if (!Boolean.TRUE.equals(exists)) {
            throw notFound("Hotel not found");
        }
    }

    protected void requireReviewWritableHotel(UUID hotelId) {
        Boolean exists = jdbcTemplate.queryForObject("""
                select exists(
                    select 1
                    from hotels
                    where id = :hotelId
                      and deleted_at is null
                      and status in ('ACTIVE', 'SUSPENDED')
                )
                """, new MapSqlParameterSource("hotelId", hotelId), Boolean.class);
        if (!Boolean.TRUE.equals(exists)) {
            throw conflict("Reviews are not allowed for this hotel");
        }
    }

    protected void notifyAdmins(String type, String title, String body, String linkUrl) {
        List<UUID> admins = jdbcTemplate.query("""
                select ar.account_id
                from account_roles ar
                join roles r on r.id = ar.role_id
                where r.name = 'ADMIN'
                """, (rs, rowNum) -> rs.getObject("account_id", UUID.class));
        for (UUID adminId : admins) {
            createNotification(adminId, type, title, body, linkUrl);
        }
    }

    protected void notifyContactRecipients(String type, String title, String body, String linkUrl) {
        List<UUID> recipients = jdbcTemplate.query("""
                select distinct a.id
                from accounts a
                join account_roles ar on ar.account_id = a.id
                join roles r on r.id = ar.role_id
                where r.name = 'ADMIN'
                  and a.email_verified
                """, (rs, rowNum) -> rs.getObject("id", UUID.class));
        for (UUID recipientId : recipients) {
            createNotification(recipientId, type, title, body, linkUrl);
        }
    }

    protected void notifyHotelOperators(UUID hotelId, String type, String title, String body, String linkUrl) {
        List<UUID> recipients = jdbcTemplate.query("""
                select owner_id account_id from hotels where id = :hotelId
                union
                select account_id from hotel_members where hotel_id = :hotelId
                """, new MapSqlParameterSource("hotelId", hotelId), (rs, rowNum) -> rs.getObject("account_id", UUID.class));
        for (UUID recipient : recipients) {
            createNotification(recipient, type, title, body, linkUrl);
        }
    }

    protected void createNotification(UUID recipientId, String type, String title, String body, String linkUrl) {
        jdbcTemplate.update("""
                insert into notifications (id, recipient_account_id, type, title, body, link_url)
                values (:id, :recipientId, :type, :title, :body, :linkUrl)
                """, new MapSqlParameterSource("id", UUID.randomUUID())
                .addValue("recipientId", recipientId)
                .addValue("type", type)
                .addValue("title", title)
                .addValue("body", body)
                .addValue("linkUrl", linkUrl));
    }

    protected ImageAssetResponse currentAvatarImage(UUID accountId) {
        List<ImageAssetResponse> images = jdbcTemplate.query("""
                select ia.*
                from accounts a
                join image_assets ia on ia.owner_account_id = a.id
                    and (ia.url = a.avatar_url or ia.secure_url = a.avatar_url)
                where a.id = :accountId
                order by ia.created_at desc
                limit 1
                """, new MapSqlParameterSource("accountId", accountId), (rs, rowNum) -> mapImageAsset(rs));
        return images.isEmpty() ? null : images.getFirst();
    }

    protected void cleanupProviderImage(ImageAssetResponse image) {
        if (image == null || !"CLOUDINARY".equalsIgnoreCase(image.provider()) || trimToNull(image.publicId()) == null) {
            return;
        }
        try {
            fileStorageService.deleteFile(image.publicId());
        } catch (RuntimeException ex) {
            log.warn("Provider cleanup failed for image asset {}", image.id(), ex);
        }
    }

    protected void validateGalleryUploadBatch(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw badRequest("At least one file is required");
        }
        if (files.size() > MAX_GALLERY_UPLOAD_FILES) {
            throw badRequest("At most 20 images can be uploaded at once");
        }
    }

    protected String normalizeFolderName(String folderName) {
        String normalized = trimRequired(folderName, "Folder name is required");
        if (normalized.length() > 80) {
            throw badRequest("Folder name must be 80 characters or fewer");
        }
        if (".".equals(normalized) || "..".equals(normalized)
                || normalized.contains("/") || normalized.contains("\\")
                || normalized.chars().anyMatch(Character::isISOControl)) {
            throw badRequest("Folder name is invalid");
        }
        return normalized;
    }

    protected ResponseStatusException policyCollision(DataIntegrityViolationException ex, Integer sortOrder) {
        String message = ex.getMostSpecificCause() == null ? "" : ex.getMostSpecificCause().getMessage();
        if (message.contains("hotel_policies_type")) {
            return conflict("Policy type already exists for this hotel");
        }
        if (message.contains("hotel_policies_order") || message.contains("sort_order")) {
            return badRequest("Order " + (sortOrder == null ? 0 : sortOrder) + " is already taken in this hotel");
        }
        return badRequest("Policy type or order already exists for this hotel");
    }

    protected String uniqueSlug(String table, String base) {
        String slug = base;
        int suffix = 2;
        while (slugExists(table, slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    protected boolean slugExists(String table, String slug) {
        Boolean exists = jdbcTemplate.queryForObject("select exists(select 1 from " + table + " where slug = :slug)",
                new MapSqlParameterSource("slug", slug), Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    protected int nextBannerPosition() {
        Integer next = jdbcTemplate.queryForObject("select coalesce(max(position), 0) + 1 from banners", new MapSqlParameterSource(), Integer.class);
        return next == null ? 1 : next;
    }

    protected String normalizeImageUrl(String url) {
        String value = trimRequired(url, "Image URL is required");
        if (value.startsWith("/api/v1/uploads/local/") || value.startsWith("http://") || value.startsWith("https://") || value.startsWith("/")) {
            return value;
        }
        throw badRequest("Image URL must be absolute or app-relative");
    }

    protected String normalizeLooseLink(String link) {
        String value = trimToNull(link);
        if (value == null) {
            return null;
        }
        if (value.startsWith("#") || value.startsWith("/") || value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        throw badRequest("Link URL is invalid");
    }

    protected String normalizePolicyType(String type) {
        String normalized = trimRequired(type, "Policy type is required").toUpperCase(Locale.ROOT);
        if ("CHECKIN".equals(normalized)) {
            normalized = "CHECK_IN";
        }
        if (!POLICY_TYPES.contains(normalized)) {
            throw badRequest("Invalid policy type");
        }
        return normalized;
    }

    protected String denormalizePolicyType(String type) {
        return "CHECK_IN".equals(type) ? "CHECKIN" : type;
    }

    protected String normalizeOptionalStatus(String value, Set<String> allowed, String message) {
        return value == null || value.isBlank() ? null : normalizeStatus(value, null, allowed, message);
    }

    protected String normalizeStatus(String value, String fallback, Set<String> allowed, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return fallback;
        }
        normalized = normalized.toUpperCase(Locale.ROOT);
        if (!allowed.contains(normalized)) {
            throw badRequest(message);
        }
        return normalized;
    }

    protected BigDecimal requireRate(BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw badRequest("Commission rate must be between 0 and 1");
        }
        return rate.setScale(4, RoundingMode.HALF_UP);
    }

    protected String immutableCode(String code) {
        return trimRequired(code, "Code is required").toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_\\-]", "_");
    }

    protected String slugify(String value) {
        String slug = trimRequired(value, "Slug source is required")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "item" : slug;
    }

    protected String trimRequired(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw badRequest(message);
        }
        return trimmed;
    }

    protected String trimOrDefault(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    protected String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    protected String like(String q) {
        String value = trimToNull(q);
        return value == null ? null : "%" + value.toLowerCase(Locale.ROOT) + "%";
    }

    protected int boundedLimit(int limit) {
        if (limit <= 0) {
            return 10;
        }
        return Math.min(limit, 100);
    }

    protected int boundedPage(int page) {
        return Math.max(page, 1);
    }

    protected int offset(int page, int limit) {
        return (page - 1) * limit;
    }

    protected int totalPages(long total, int limit) {
        return (int) Math.ceil(total / (double) Math.max(limit, 1));
    }

    protected <T> ListResponse<T> paginated(List<T> data, int page, int limit, long total) {
        return new ListResponse<>(
                data,
                new PageMeta(limit, offset(page, limit), total),
                page,
                limit,
                total,
                totalPages(total, limit)
        );
    }

    protected String formatPeriod(Instant period, String groupBy) {
        LocalDate date = period.atZone(ZoneOffset.UTC).toLocalDate();
        if ("day".equals(groupBy)) {
            return date.toString();
        }
        if ("week".equals(groupBy)) {
            int week = date.get(WeekFields.ISO.weekOfWeekBasedYear());
            return date.getYear() + "-W" + String.format("%02d", week);
        }
        return date.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH));
    }

    protected Instant instantOrNull(ResultSet rs, String column) throws SQLException {
        var timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }

    protected OffsetDateTime timestamptz(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    protected ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    protected ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    protected ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    protected ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    protected record CurrentUser(UUID accountId) {
    }

    protected record BookingReviewSource(UUID id, UUID hotelId) {
    }

    protected record BannerImageSource(UUID imageAssetId, String url) {
    }

    protected record NewsImageSource(UUID id, UUID imageAssetId, String url) {
    }

    protected record HotelImageSource(UUID imageAssetId, String url) {
    }

    private record ExistingHotelImage(UUID id, UUID imageAssetId, String url) {
    }

    protected record ReportScope(UUID hotelId) {
        static ReportScope global() {
            return new ReportScope(null);
        }

        static ReportScope hotel(UUID hotelId) {
            return new ReportScope(hotelId);
        }

        MapSqlParameterSource params() {
            return new MapSqlParameterSource("hotelId", hotelId);
        }

        String condition(String alias) {
            return hotelId == null ? " true " : " " + alias + ".hotel_id = :hotelId ";
        }

        String where(String alias) {
            return hotelId == null ? "" : " where " + alias + ".hotel_id = :hotelId ";
        }
    }
}
