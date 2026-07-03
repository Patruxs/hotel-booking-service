package org.example.hotelbookingservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.AccountSummary;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.AvatarSummary;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.BannerImageResponse;
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
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ImageSnapshotResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.LatestReviewResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ListResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.NewsImageResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.NewsMutationRequest;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.NewsResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.NewestBookingItemResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.NewestBookingResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.NotificationResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.PageMeta;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.PolicyMutationRequest;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.PolicyResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.RatingSummaryResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.RevenuePointResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ReviewModerationRequest;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ReviewRequest;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ReviewResponse;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.ReviewUpdateRequest;
import org.example.hotelbookingservice.dto.operations.Milestone6Dtos.RoomTypeSummary;
import org.example.hotelbookingservice.security.AccountAuthUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class Milestone6Service {
    private static final Set<String> NEWS_STATUSES = Set.of("DRAFT", "PUBLISHED", "ARCHIVED");
    private static final Set<String> CONTACT_STATUSES = Set.of("NEW", "IN_PROGRESS", "RESOLVED", "SPAM");
    private static final Set<String> POLICY_TYPES = Set.of("CHECK_IN", "CANCELLATION", "PAYMENT", "CHILDREN", "PET", "SMOKING", "GENERAL");
    private static final Set<String> LINK_TYPES = Set.of("URL", "HOTEL", "NEWS");

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final IFileStorageService fileStorageService;

    @Value("${app.upload.mode:LOCAL}")
    private String uploadMode;

    @Value("${app.upload.max-image-count:12}")
    private int maxImageCount;

    @Transactional
    public ImageAssetResponse upload(MultipartFile file, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        return createImageAsset(file, user.accountId());
    }

    @Transactional
    public List<ImageAssetResponse> uploadMany(List<MultipartFile> files, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        if (files == null || files.isEmpty()) {
            throw badRequest("At least one file is required");
        }
        List<ImageAssetResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            responses.add(createImageAsset(file, user.accountId()));
        }
        return responses;
    }

    @Transactional
    public ImageAssetResponse uploadAvatar(MultipartFile file, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        ImageAssetResponse image = createImageAsset(file, user.accountId());
        jdbcTemplate.update("""
                update accounts
                set avatar_url = :avatarUrl, updated_at = now()
                where id = :accountId
                """, new MapSqlParameterSource("avatarUrl", image.secureUrl() == null ? image.url() : image.secureUrl())
                .addValue("accountId", user.accountId()));
        return image;
    }

    @Transactional
    public void deleteAvatar(Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        jdbcTemplate.update("""
                update accounts
                set avatar_url = null, updated_at = now()
                where id = :accountId
                """, new MapSqlParameterSource("accountId", user.accountId()));
    }

    public List<ImageAssetResponse> listProviderAssets(Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        MapSqlParameterSource params = new MapSqlParameterSource("accountId", user.accountId());
        String ownership = isAdmin(user) ? "" : "where owner_account_id = :accountId";
        return jdbcTemplate.query("""
                select *
                from image_assets
                """ + ownership + """
                order by created_at desc
                limit 200
                """, params, (rs, rowNum) -> mapImageAsset(rs));
    }

    public List<GalleryFolderResponse> listGalleryFolders(Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        return jdbcTemplate.query("""
                select *
                from gallery_folders
                where owner_account_id = :accountId
                order by folder_name
                """, new MapSqlParameterSource("accountId", user.accountId()), (rs, rowNum) -> mapGalleryFolder(rs));
    }

    @Transactional
    public GalleryFolderResponse createGalleryFolder(String folderName, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        String normalized = trimRequired(folderName, "Folder name is required");
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into gallery_folders (id, owner_account_id, folder_name)
                values (:id, :ownerId, :folderName)
                on conflict (owner_account_id, folder_name)
                do update set updated_at = gallery_folders.updated_at
                """, new MapSqlParameterSource("id", id)
                .addValue("ownerId", user.accountId())
                .addValue("folderName", normalized));
        return jdbcTemplate.queryForObject("""
                select *
                from gallery_folders
                where owner_account_id = :ownerId and folder_name = :folderName
                """, new MapSqlParameterSource("ownerId", user.accountId()).addValue("folderName", normalized),
                (rs, rowNum) -> mapGalleryFolder(rs));
    }

    public List<ImageAssetResponse> listGalleryImages(UUID folderId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireFolderOwner(folderId, user.accountId());
        return jdbcTemplate.query("""
                select ia.*
                from gallery_images gi
                join image_assets ia on ia.id = gi.image_asset_id
                where gi.folder_id = :folderId
                order by gi.created_at desc
                """, new MapSqlParameterSource("folderId", folderId), (rs, rowNum) -> mapImageAsset(rs));
    }

    @Transactional
    public List<ImageAssetResponse> uploadGalleryImages(String folderName, List<MultipartFile> files, Authentication authentication) {
        GalleryFolderResponse folder = createGalleryFolder(folderName, authentication);
        List<ImageAssetResponse> uploaded = uploadMany(files, authentication);
        for (ImageAssetResponse image : uploaded) {
            jdbcTemplate.update("""
                    insert into gallery_images (id, folder_id, image_asset_id)
                    values (:id, :folderId, :imageAssetId)
                    on conflict do nothing
                    """, new MapSqlParameterSource("id", UUID.randomUUID())
                    .addValue("folderId", folder.id())
                    .addValue("imageAssetId", image.id()));
        }
        return uploaded;
    }

    @Transactional
    public List<ImageSnapshotResponse> replaceHotelImages(UUID hotelId, List<UUID> imageIds, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireCanManageHotel(hotelId, user);
        List<ImageAssetResponse> images = requireOwnedImages(imageIds, user);
        jdbcTemplate.update("delete from hotel_images where hotel_id = :hotelId", new MapSqlParameterSource("hotelId", hotelId));
        int order = 0;
        for (ImageAssetResponse image : images) {
            jdbcTemplate.update("""
                    insert into hotel_images (id, hotel_id, image_asset_id, url, sort_order)
                    values (:id, :hotelId, :imageAssetId, :url, :sortOrder)
                    """, snapshotParams("hotelId", hotelId, image, order++));
        }
        return listHotelImages(hotelId);
    }

    public List<ImageSnapshotResponse> listHotelImages(UUID hotelId) {
        return jdbcTemplate.query("""
                select id, image_asset_id, url, sort_order
                from hotel_images
                where hotel_id = :hotelId
                order by sort_order
                """, new MapSqlParameterSource("hotelId", hotelId), (rs, rowNum) -> mapSnapshot(rs));
    }

    @Transactional
    public List<ImageSnapshotResponse> replaceRoomTypeImages(UUID hotelId, UUID roomTypeId, List<UUID> imageIds, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireCanManageHotel(hotelId, user);
        Boolean belongs = jdbcTemplate.queryForObject("""
                select exists(select 1 from room_types where id = :roomTypeId and hotel_id = :hotelId and deleted_at is null)
                """, new MapSqlParameterSource("roomTypeId", roomTypeId).addValue("hotelId", hotelId), Boolean.class);
        if (!Boolean.TRUE.equals(belongs)) {
            throw notFound("Room type not found");
        }
        List<ImageAssetResponse> images = requireOwnedImages(imageIds, user);
        jdbcTemplate.update("delete from room_type_images where room_type_id = :roomTypeId", new MapSqlParameterSource("roomTypeId", roomTypeId));
        int order = 0;
        for (ImageAssetResponse image : images) {
            jdbcTemplate.update("""
                    insert into room_type_images (id, room_type_id, image_asset_id, url, sort_order)
                    values (:id, :roomTypeId, :imageAssetId, :url, :sortOrder)
                    """, snapshotParams("roomTypeId", roomTypeId, image, order++));
        }
        return listRoomTypeImages(roomTypeId);
    }

    public List<ImageSnapshotResponse> listRoomTypeImages(UUID roomTypeId) {
        return jdbcTemplate.query("""
                select id, image_asset_id, url, sort_order
                from room_type_images
                where room_type_id = :roomTypeId
                order by sort_order
                """, new MapSqlParameterSource("roomTypeId", roomTypeId), (rs, rowNum) -> mapSnapshot(rs));
    }

    @Transactional
    public ReviewResponse createReview(UUID hotelId, ReviewRequest request, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        UUID bookingId = request.bookingId();
        BookingReviewSource booking = requireCompletedBookingForReview(hotelId, bookingId, user.accountId());
        UUID reviewId = UUID.randomUUID();
        try {
            jdbcTemplate.update("""
                    insert into reviews (id, booking_id, hotel_id, account_id, rating, comment)
                    values (:id, :bookingId, :hotelId, :accountId, :rating, :comment)
                    """, new MapSqlParameterSource("id", reviewId)
                    .addValue("bookingId", booking.id())
                    .addValue("hotelId", booking.hotelId())
                    .addValue("accountId", user.accountId())
                    .addValue("rating", request.rating())
                    .addValue("comment", trimToNull(request.comment())));
        } catch (DuplicateKeyException ex) {
            throw conflict("Booking already has a review");
        }
        insertReviewImages(reviewId, request.imageIds(), user);
        notifyHotelOperators(hotelId, "REVIEW_CREATED", "New review received", "A customer left a review.", "/admin/hotels/" + hotelId + "/reviews");
        return reviewDetail(reviewId, false);
    }

    public ListResponse<ReviewResponse> listPublicReviews(UUID hotelId, int page, int limit) {
        return listReviews(hotelId, true, page, limit, null);
    }

    public ListResponse<ReviewResponse> listModerationReviews(UUID hotelId, int page, int limit, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireCanManageHotel(hotelId, user);
        return listReviews(hotelId, false, page, limit, null);
    }

    public ListResponse<ReviewResponse> listMyReviews(int page, int limit, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        return listReviews(null, false, page, limit, user.accountId());
    }

    @Transactional
    public ReviewResponse updateMyReview(UUID reviewId, ReviewUpdateRequest request, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        ReviewResponse current = reviewDetail(reviewId, false);
        if (!current.accountId().equals(user.accountId())) {
            throw forbidden("Cannot edit another customer's review");
        }
        jdbcTemplate.update("""
                update reviews
                set rating = coalesce(:rating, rating),
                    comment = coalesce(:comment, comment),
                    updated_at = now()
                where id = :id
                """, new MapSqlParameterSource("id", reviewId)
                .addValue("rating", request.rating())
                .addValue("comment", trimToNull(request.comment())));
        return reviewDetail(reviewId, false);
    }

    @Transactional
    public ReviewResponse moderateReview(UUID hotelId, UUID reviewId, ReviewModerationRequest request, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireCanManageHotel(hotelId, user);
        boolean visible = request.visible() == null || request.visible();
        int updated = jdbcTemplate.update("""
                update reviews
                set visible = :visible, updated_at = now()
                where id = :id and hotel_id = :hotelId and deleted_at is null
                """, new MapSqlParameterSource("id", reviewId).addValue("hotelId", hotelId).addValue("visible", visible));
        if (updated == 0) {
            throw notFound("Review not found");
        }
        return reviewDetail(reviewId, false);
    }

    @Transactional
    public void deleteReview(UUID hotelId, UUID reviewId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireCanManageHotel(hotelId, user);
        int updated = jdbcTemplate.update("""
                update reviews
                set visible = false,
                    deleted_at = coalesce(deleted_at, now()),
                    updated_at = now()
                where id = :id and hotel_id = :hotelId and deleted_at is null
                """,
                new MapSqlParameterSource("id", reviewId).addValue("hotelId", hotelId));
        if (updated == 0) {
            throw notFound("Review not found");
        }
    }

    public RatingSummaryResponse visibleRatingSummary(UUID hotelId) {
        return jdbcTemplate.queryForObject("""
                select coalesce(round(avg(rating), 1), 0) average_rating, count(*) review_count
                from reviews
                where hotel_id = :hotelId and visible and deleted_at is null
                """, new MapSqlParameterSource("hotelId", hotelId), (rs, rowNum) ->
                new RatingSummaryResponse(rs.getBigDecimal("average_rating"), rs.getLong("review_count")));
    }

    @Transactional
    public NewsResponse createNews(NewsMutationRequest request, Authentication authentication) {
        requireAdmin(authentication);
        String title = trimRequired(request.title(), "Title is required");
        String status = normalizeStatus(request.status(), "DRAFT", NEWS_STATUSES, "Invalid news status");
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into news (id, author_account_id, title, slug, summary, content, status, published_at)
                values (:id, :authorId, :title, :slug, :summary, :content, :status, :publishedAt)
                """, new MapSqlParameterSource("id", id)
                .addValue("authorId", currentUser(authentication).accountId())
                .addValue("title", title)
                .addValue("slug", uniqueSlug("news", slugify(title)))
                .addValue("summary", trimToNull(request.summary()))
                .addValue("content", trimOrDefault(request.content(), ""))
                .addValue("status", status)
                .addValue("publishedAt", timestamptz("PUBLISHED".equals(status) ? Instant.now() : null), Types.TIMESTAMP_WITH_TIMEZONE));
        replaceNewsImages(id, request.imageIds(), currentUser(authentication));
        return newsDetailAdmin(id, authentication);
    }

    @Transactional
    public NewsResponse updateNews(UUID id, NewsMutationRequest request, Authentication authentication) {
        requireAdmin(authentication);
        NewsResponse current = newsDetailAdmin(id, authentication);
        String nextStatus = request.status() == null ? current.status() : normalizeStatus(request.status(), current.status(), NEWS_STATUSES, "Invalid news status");
        Instant publishedAt = current.publishedAt();
        if ("PUBLISHED".equals(nextStatus) && publishedAt == null) {
            publishedAt = Instant.now();
        }
        if (!"PUBLISHED".equals(nextStatus)) {
            publishedAt = null;
        }
        String title = request.title() == null ? current.title() : trimRequired(request.title(), "Title is required");
        jdbcTemplate.update("""
                update news
                set title = :title,
                    summary = :summary,
                    content = :content,
                    status = :status,
                    published_at = :publishedAt,
                    updated_at = now()
                where id = :id
                """, new MapSqlParameterSource("id", id)
                .addValue("title", title)
                .addValue("summary", request.summary() == null ? current.summary() : trimToNull(request.summary()))
                .addValue("content", request.content() == null ? current.content() : trimOrDefault(request.content(), ""))
                .addValue("status", nextStatus)
                .addValue("publishedAt", timestamptz(publishedAt), Types.TIMESTAMP_WITH_TIMEZONE));
        if (request.imageIds() != null) {
            replaceNewsImages(id, request.imageIds(), currentUser(authentication));
        }
        return newsDetailAdmin(id, authentication);
    }

    @Transactional
    public void deleteNews(UUID id, Authentication authentication) {
        requireAdmin(authentication);
        int updated = jdbcTemplate.update("delete from news where id = :id", new MapSqlParameterSource("id", id));
        if (updated == 0) {
            throw notFound("News not found");
        }
    }

    public ListResponse<NewsResponse> listNewsAdmin(String status, String q, int page, int limit, Authentication authentication) {
        requireAdmin(authentication);
        return listNews(status, q, page, limit, false);
    }

    public ListResponse<NewsResponse> listNewsPublic(String q, int page, int limit) {
        return listNews("PUBLISHED", q, page, limit, true);
    }

    public NewsResponse newsDetailAdmin(UUID id, Authentication authentication) {
        requireAdmin(authentication);
        return queryNews("where n.id = :id", new MapSqlParameterSource("id", id), false);
    }

    public NewsResponse newsDetailPublic(String slug) {
        return queryNews("where n.slug = :slug and n.status = 'PUBLISHED' and n.published_at <= now()",
                new MapSqlParameterSource("slug", slug), true);
    }

    @Transactional
    public BannerResponse createBanner(BannerMutationRequest request, Authentication authentication) {
        requireAdmin(authentication);
        UUID id = UUID.randomUUID();
        List<BannerImageSource> images = resolveBannerImages(request, currentUser(authentication));
        if (images.isEmpty()) {
            throw badRequest("At least one banner image is required");
        }
        try {
            jdbcTemplate.update("""
                    insert into banners (id, title, subtitle, image_url, link_url, link_type, position, active, starts_at, ends_at)
                    values (:id, :title, :subtitle, :imageUrl, :linkUrl, :linkType, :position, :active, :startsAt, :endsAt)
                    """, new MapSqlParameterSource("id", id)
                    .addValue("title", trimOrDefault(request.title(), "Banner"))
                    .addValue("subtitle", trimToNull(request.subtitle()))
                    .addValue("imageUrl", images.getFirst().url())
                    .addValue("linkUrl", normalizeLooseLink(request.linkUrl()))
                    .addValue("linkType", normalizeStatus(request.linkType(), "URL", LINK_TYPES, "Invalid banner link type"))
                    .addValue("position", request.position() == null ? nextBannerPosition() : request.position())
                    .addValue("active", request.active() == null || request.active())
                    .addValue("startsAt", timestamptz(request.startsAt()), Types.TIMESTAMP_WITH_TIMEZONE)
                    .addValue("endsAt", timestamptz(request.endsAt()), Types.TIMESTAMP_WITH_TIMEZONE));
        } catch (DuplicateKeyException ex) {
            throw conflict("Banner position already exists");
        }
        replaceBannerImages(id, images);
        return bannerDetail(id);
    }

    @Transactional
    public BannerResponse updateBanner(UUID id, BannerMutationRequest request, Authentication authentication) {
        requireAdmin(authentication);
        BannerResponse current = bannerDetail(id);
        List<BannerImageSource> images = request.imageIds() == null && request.images() == null
                ? List.of(new BannerImageSource(null, current.images().getFirst().url()))
                : resolveBannerImages(request, currentUser(authentication));
        if (images.isEmpty()) {
            throw badRequest("At least one banner image is required");
        }
        try {
            jdbcTemplate.update("""
                    update banners
                    set title = :title,
                        subtitle = :subtitle,
                        image_url = :imageUrl,
                        link_url = :linkUrl,
                        link_type = :linkType,
                        position = :position,
                        active = :active,
                        starts_at = :startsAt,
                        ends_at = :endsAt,
                        updated_at = now()
                    where id = :id
                    """, new MapSqlParameterSource("id", id)
                    .addValue("title", request.title() == null ? current.title() : trimOrDefault(request.title(), "Banner"))
                    .addValue("subtitle", request.subtitle() == null ? current.subtitle() : trimToNull(request.subtitle()))
                    .addValue("imageUrl", images.getFirst().url())
                    .addValue("linkUrl", request.linkUrl() == null ? current.link() : normalizeLooseLink(request.linkUrl()))
                    .addValue("linkType", request.linkType() == null ? current.linkType() : normalizeStatus(request.linkType(), current.linkType(), LINK_TYPES, "Invalid banner link type"))
                    .addValue("position", request.position() == null ? current.position() : request.position())
                    .addValue("active", request.active() == null ? current.isActive() : request.active())
                    .addValue("startsAt", timestamptz(request.startsAt() == null ? current.startAt() : request.startsAt()), Types.TIMESTAMP_WITH_TIMEZONE)
                    .addValue("endsAt", timestamptz(request.endsAt() == null ? current.endAt() : request.endsAt()), Types.TIMESTAMP_WITH_TIMEZONE));
        } catch (DuplicateKeyException ex) {
            throw conflict("Banner position already exists");
        }
        if (request.imageIds() != null || request.images() != null) {
            replaceBannerImages(id, images);
        }
        return bannerDetail(id);
    }

    @Transactional
    public void deleteBanner(UUID id, Authentication authentication) {
        requireAdmin(authentication);
        int updated = jdbcTemplate.update("delete from banners where id = :id", new MapSqlParameterSource("id", id));
        if (updated == 0) {
            throw notFound("Banner not found");
        }
    }

    public List<BannerResponse> listAdminBanners(Authentication authentication) {
        requireAdmin(authentication);
        return jdbcTemplate.query("select * from banners order by position", (rs, rowNum) -> mapBanner(rs));
    }

    public List<BannerResponse> listPublicBanners() {
        return jdbcTemplate.query("""
                select *
                from banners
                where active
                  and (starts_at is null or starts_at <= now())
                  and (ends_at is null or ends_at >= now())
                order by position
                """, (rs, rowNum) -> mapBanner(rs));
    }

    @Transactional
    public ContactCreateResponse createContact(ContactCreateRequest request, String ipAddress, String userAgent, Authentication authentication) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into contact_messages (id, account_id, name, email, phone, subject, message, ip_address, user_agent)
                values (:id, :accountId, :name, :email, :phone, :subject, :message, :ip, :userAgent)
                """, new MapSqlParameterSource("id", id)
                .addValue("accountId", null)
                .addValue("name", trimRequired(request.name(), "Name is required"))
                .addValue("email", trimToNull(request.email()))
                .addValue("phone", trimToNull(request.phone()))
                .addValue("subject", trimToNull(request.subject()))
                .addValue("message", trimRequired(request.message(), "Message is required"))
                .addValue("ip", trimToNull(ipAddress))
                .addValue("userAgent", trimToNull(userAgent)));
        try {
            log.info("Contact notification email requested for {}", trimToNull(request.email()));
            notifyContactRecipients("SYSTEM", "New contact message", "A visitor submitted a contact message.", "/admin/contacts/" + id);
        } catch (RuntimeException ex) {
            log.warn("Contact notification failed for {}, public submission preserved", id, ex);
        }
        return new ContactCreateResponse(id, true);
    }

    public ListResponse<ContactResponse> listContacts(String status, String q, int page, int limit, Authentication authentication) {
        requireAdmin(authentication);
        int boundedLimit = boundedLimit(limit);
        int boundedPage = boundedPage(page);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("status", normalizeOptionalStatus(status, CONTACT_STATUSES, "Invalid contact status"))
                .addValue("q", like(q))
                .addValue("limit", boundedLimit)
                .addValue("offset", offset(boundedPage, boundedLimit));
        String where = """
                where (cast(:status as text) is null or cm.status = :status)
                  and (cast(:q as text) is null or lower(cm.name || ' ' || coalesce(cm.email, '') || ' ' || coalesce(cm.subject, '')) like :q)
                """;
        long total = jdbcTemplate.queryForObject("select count(*) from contact_messages cm " + where, params, Long.class);
        List<ContactResponse> data = jdbcTemplate.query("""
                select cm.*, a.email handled_email, a.first_name handled_first_name, a.last_name handled_last_name, a.avatar_url handled_avatar_url
                from contact_messages cm
                left join accounts a on a.id = cm.handled_by_account_id
                """ + where + """
                order by cm.created_at desc
                limit :limit offset :offset
                """, params, (rs, rowNum) -> mapContact(rs));
        return paginated(data, boundedPage, boundedLimit, total);
    }

    public ContactResponse contactDetail(UUID id, Authentication authentication) {
        requireAdmin(authentication);
        return queryContact(id);
    }

    @Transactional
    public ContactResponse updateContact(UUID id, ContactUpdateRequest request, Authentication authentication) {
        requireAdmin(authentication);
        String status = request.status() == null ? queryContact(id).status() : normalizeStatus(request.status(), "NEW", CONTACT_STATUSES, "Invalid contact status");
        if (request.handledById() != null) {
            requireAccountExists(request.handledById());
        }
        jdbcTemplate.update("""
                update contact_messages
                set status = :status,
                    handled_by_account_id = :handledById,
                    note = :note,
                    updated_at = now()
                where id = :id
                """, new MapSqlParameterSource("id", id)
                .addValue("status", status)
                .addValue("handledById", request.handledById())
                .addValue("note", trimToNull(request.note())));
        return queryContact(id);
    }

    public ListResponse<NotificationResponse> listNotifications(int page, int limit, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        int boundedLimit = boundedLimit(limit);
        int boundedPage = boundedPage(page);
        MapSqlParameterSource params = new MapSqlParameterSource("accountId", user.accountId())
                .addValue("limit", boundedLimit)
                .addValue("offset", offset(boundedPage, boundedLimit));
        long total = jdbcTemplate.queryForObject("""
                select count(*) from notifications where recipient_account_id = :accountId
                """, params, Long.class);
        List<NotificationResponse> data = jdbcTemplate.query("""
                select *
                from notifications
                where recipient_account_id = :accountId
                order by created_at desc
                limit :limit offset :offset
                """, params, (rs, rowNum) -> mapNotification(rs));
        return paginated(data, boundedPage, boundedLimit, total);
    }

    public long unreadCount(Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        return jdbcTemplate.queryForObject("""
                select count(*)
                from notifications
                where recipient_account_id = :accountId and read_at is null
                """, new MapSqlParameterSource("accountId", user.accountId()), Long.class);
    }

    @Transactional
    public NotificationResponse markRead(UUID id, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        int updated = jdbcTemplate.update("""
                update notifications
                set read_at = coalesce(read_at, now())
                where id = :id and recipient_account_id = :accountId
                """, new MapSqlParameterSource("id", id).addValue("accountId", user.accountId()));
        if (updated == 0) {
            throw notFound("Notification not found");
        }
        return notificationDetail(id, user.accountId());
    }

    @Transactional
    public void markAllRead(Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        jdbcTemplate.update("""
                update notifications
                set read_at = coalesce(read_at, now())
                where recipient_account_id = :accountId and read_at is null
                """, new MapSqlParameterSource("accountId", user.accountId()));
    }

    @Transactional
    public void deleteNotification(UUID id, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        jdbcTemplate.update("delete from notifications where id = :id and recipient_account_id = :accountId",
                new MapSqlParameterSource("id", id).addValue("accountId", user.accountId()));
    }

    public DashboardStatsResponse dashboardStats(UUID hotelId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        ReportScope scope = reportScope(hotelId, user);
        MapSqlParameterSource params = scope.params();
        return jdbcTemplate.queryForObject("""
                select
                    (select count(*) from accounts) total_users,
                    (select count(*) from bookings b """ + scope.where("b") + """
                    ) total_bookings,
                    (select coalesce(sum(b.total_amount), 0)
                     from bookings b
                     where """ + scope.condition("b") + """
                       and exists (
                           select 1
                           from payments p
                           where p.booking_id = b.id and p.status in ('SUCCEEDED', 'REFUNDED', 'LATE_SUCCEEDED')
                       )) revenue,
                    (select count(*) from hotels h where h.status = 'ACTIVE' and h.deleted_at is null) active_hotels
                """, params, (rs, rowNum) -> new DashboardStatsResponse(
                rs.getLong("total_users"),
                rs.getLong("total_bookings"),
                rs.getBigDecimal("revenue"),
                rs.getLong("active_hotels")
        ));
    }

    public List<RevenuePointResponse> revenueChart(UUID hotelId, String groupBy, Integer year, LocalDate from, LocalDate to, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        ReportScope scope = reportScope(hotelId, user);
        String normalizedGroup = Set.of("day", "week", "month").contains(String.valueOf(groupBy)) ? groupBy : "month";
        LocalDate start = from == null ? LocalDate.of(year == null ? LocalDate.now().getYear() : year, 1, 1) : from;
        LocalDate end = to == null ? start.plusYears(1).minusDays(1) : to;
        MapSqlParameterSource params = scope.params()
                .addValue("from", start)
                .addValue("to", end);
        List<RevenuePointResponse> data = jdbcTemplate.query("""
                select date_trunc(:groupBy, b.created_at) period, coalesce(sum(b.total_amount), 0) revenue
                from bookings b
                where """ + scope.condition("b") + """
                  and b.created_at::date between :from and :to
                  and exists (
                      select 1
                      from payments p
                      where p.booking_id = b.id and p.status in ('SUCCEEDED', 'REFUNDED', 'LATE_SUCCEEDED')
                  )
                group by period
                order by period
                """, params.addValue("groupBy", normalizedGroup), (rs, rowNum) -> {
            Instant period = rs.getTimestamp("period").toInstant();
            String label = formatPeriod(period, normalizedGroup);
            return new RevenuePointResponse(label, label, rs.getBigDecimal("revenue"));
        });
        return data;
    }

    public List<LatestReviewResponse> latestReviews(UUID hotelId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        ReportScope scope = reportScope(hotelId, user);
        return jdbcTemplate.query("""
                select r.*, a.email, a.first_name, a.last_name, a.avatar_url
                from reviews r
                join accounts a on a.id = r.account_id
                where """ + scope.condition("r") + """
                  and r.deleted_at is null
                order by r.created_at desc
                limit 10
                """, scope.params(), (rs, rowNum) -> new LatestReviewResponse(
                rs.getObject("id", UUID.class),
                rs.getBigDecimal("rating"),
                rs.getString("comment"),
                rs.getTimestamp("created_at").toInstant(),
                accountSummary(rs, "", "avatar_url")
        ));
    }

    public List<NewestBookingResponse> newestBookings(UUID hotelId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        ReportScope scope = reportScope(hotelId, user);
        return jdbcTemplate.query("""
                select b.*
                from bookings b
                where """ + scope.condition("b") + """
                order by b.created_at desc
                limit 10
                """, scope.params(), (rs, rowNum) -> {
            UUID bookingId = rs.getObject("id", UUID.class);
            return new NewestBookingResponse(
                    bookingId,
                    rs.getString("guest_name"),
                    rs.getObject("check_in", LocalDate.class),
                    rs.getObject("check_out", LocalDate.class),
                    rs.getTimestamp("created_at").toInstant(),
                    bookingItems(bookingId)
            );
        });
    }

    public List<CommissionPackageResponse> listCommissionPackages(Authentication authentication) {
        requireAdmin(authentication);
        return jdbcTemplate.query("""
                select *
                from commission_packages
                order by active desc, code
                """, (rs, rowNum) -> mapCommissionPackage(rs));
    }

    public CommissionPackageResponse commissionPackageDetail(UUID id, Authentication authentication) {
        requireAdmin(authentication);
        return queryCommissionPackage(id);
    }

    @Transactional
    public CommissionPackageResponse createCommissionPackage(CommissionPackageRequest request, Authentication authentication) {
        requireAdmin(authentication);
        UUID id = UUID.randomUUID();
        try {
            jdbcTemplate.update("""
                    insert into commission_packages (id, code, name, description, commission_rate, active)
                    values (:id, :code, :name, :description, :rate, :active)
                    """, new MapSqlParameterSource("id", id)
                    .addValue("code", immutableCode(request.code()))
                    .addValue("name", trimRequired(request.name(), "Name is required"))
                    .addValue("description", trimToNull(request.description()))
                    .addValue("rate", requireRate(request.commissionRate()))
                    .addValue("active", request.active() == null || request.active()));
        } catch (DuplicateKeyException ex) {
            throw conflict("Commission package code already exists");
        }
        return queryCommissionPackage(id);
    }

    @Transactional
    public CommissionPackageResponse updateCommissionPackage(UUID id, CommissionPackageRequest request, Authentication authentication) {
        requireAdmin(authentication);
        CommissionPackageResponse current = queryCommissionPackage(id);
        if (request.code() != null && !immutableCode(request.code()).equals(current.code())) {
            throw badRequest("Commission package code is immutable");
        }
        jdbcTemplate.update("""
                update commission_packages
                set name = :name,
                    description = :description,
                    commission_rate = :rate,
                    active = :active,
                    updated_at = now()
                where id = :id
                """, new MapSqlParameterSource("id", id)
                .addValue("name", request.name() == null ? current.name() : trimRequired(request.name(), "Name is required"))
                .addValue("description", request.description() == null ? current.description() : trimToNull(request.description()))
                .addValue("rate", request.commissionRate() == null ? current.commissionRate() : requireRate(request.commissionRate()))
                .addValue("active", request.active() == null ? current.isActive() : request.active()));
        return queryCommissionPackage(id);
    }

    @Transactional
    public CommissionPackageResponse deactivateCommissionPackage(UUID id, Authentication authentication) {
        requireAdmin(authentication);
        jdbcTemplate.update("update commission_packages set active = false, updated_at = now() where id = :id", new MapSqlParameterSource("id", id));
        return queryCommissionPackage(id);
    }

    @Transactional
    public CommissionAssignmentResponse assignCommissionPackage(UUID hotelId, UUID packageId, Authentication authentication) {
        requireAdmin(authentication);
        CommissionPackageResponse pack = queryCommissionPackage(packageId);
        if (!pack.isActive()) {
            throw conflict("Cannot assign an inactive commission package");
        }
        requireHotelExists(hotelId);
        jdbcTemplate.update("""
                insert into hotel_commission_packages (hotel_id, commission_package_id)
                values (:hotelId, :packageId)
                on conflict (hotel_id)
                do update set commission_package_id = excluded.commission_package_id, assigned_at = now()
                """, new MapSqlParameterSource("hotelId", hotelId).addValue("packageId", packageId));
        return commissionAssignment(hotelId);
    }

    public Object commissionRevenue(UUID hotelId, Integer year, LocalDate from, LocalDate to, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        ReportScope scope = reportScope(hotelId, user);
        if (from != null && to != null) {
            MapSqlParameterSource params = scope.params()
                    .addValue("from", from)
                    .addValue("to", to);
            return jdbcTemplate.query("""
                    select b.created_at::date revenue_date, coalesce(sum(b.commission_amount), 0) revenue
                    from bookings b
                    where """ + scope.condition("b") + """
                      and b.created_at::date between :from and :to
                      and exists (
                          select 1
                          from payments p
                          where p.booking_id = b.id and p.status in ('SUCCEEDED', 'REFUNDED', 'LATE_SUCCEEDED')
                      )
                    group by revenue_date
                    order by revenue_date
                    """, params, (rs, rowNum) -> Map.of(
                    "date", rs.getObject("revenue_date", LocalDate.class).toString(),
                    "revenue", rs.getBigDecimal("revenue")
            ));
        }

        int selectedYear = year == null ? LocalDate.now().getYear() : year;
        return jdbcTemplate.query("""
                select months.month_number, coalesce(sum(b.commission_amount), 0) revenue
                from generate_series(1, 12) months(month_number)
                left join bookings b on extract(month from b.created_at) = months.month_number
                    and extract(year from b.created_at) = :year
                    and """ + scope.condition("b") + """
                    and exists (
                        select 1
                        from payments p
                        where p.booking_id = b.id and p.status in ('SUCCEEDED', 'REFUNDED', 'LATE_SUCCEEDED')
                    )
                group by months.month_number
                order by months.month_number
                """, scope.params().addValue("year", selectedYear), (rs, rowNum) -> rs.getBigDecimal("revenue"));
    }

    public List<PolicyResponse> listPoliciesPublic(UUID hotelId) {
        return jdbcTemplate.query("""
                select *
                from hotel_policies
                where hotel_id = :hotelId and enabled and deleted_at is null
                order by sort_order
                """, new MapSqlParameterSource("hotelId", hotelId), (rs, rowNum) -> mapPolicy(rs));
    }

    public List<PolicyResponse> listPoliciesAdmin(UUID hotelId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireCanManageHotel(hotelId, user);
        return jdbcTemplate.query("""
                select *
                from hotel_policies
                where hotel_id = :hotelId and deleted_at is null
                order by sort_order
                """, new MapSqlParameterSource("hotelId", hotelId), (rs, rowNum) -> mapPolicy(rs));
    }

    public PolicyResponse policyDetail(UUID hotelId, UUID policyId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireCanManageHotel(hotelId, user);
        return queryPolicy(hotelId, policyId);
    }

    @Transactional
    public PolicyResponse createPolicy(UUID hotelId, PolicyMutationRequest request, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireCanManageHotel(hotelId, user);
        UUID id = UUID.randomUUID();
        try {
            jdbcTemplate.update("""
                    insert into hotel_policies (id, hotel_id, type, title, content, enabled, sort_order)
                    values (:id, :hotelId, :type, :title, :content, :enabled, :sortOrder)
                    """, policyParams(id, hotelId, request, null));
        } catch (DataIntegrityViolationException ex) {
            throw badRequest("Policy type or order already exists for this hotel");
        }
        return queryPolicy(hotelId, id);
    }

    @Transactional
    public PolicyResponse updatePolicy(UUID hotelId, UUID policyId, PolicyMutationRequest request, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireCanManageHotel(hotelId, user);
        PolicyResponse current = queryPolicy(hotelId, policyId);
        try {
            jdbcTemplate.update("""
                    update hotel_policies
                    set type = :type,
                        title = :title,
                        content = :content,
                        enabled = :enabled,
                        sort_order = :sortOrder,
                        updated_at = now()
                    where id = :id and hotel_id = :hotelId
                    """, policyParams(policyId, hotelId, request, current));
        } catch (DataIntegrityViolationException ex) {
            throw badRequest("Policy type or order already exists for this hotel");
        }
        return queryPolicy(hotelId, policyId);
    }

    @Transactional
    public PolicyResponse deletePolicy(UUID hotelId, UUID policyId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireCanManageHotel(hotelId, user);
        jdbcTemplate.update("""
                update hotel_policies
                set deleted_at = now(), updated_at = now()
                where id = :id and hotel_id = :hotelId and deleted_at is null
                """, new MapSqlParameterSource("id", policyId).addValue("hotelId", hotelId));
        return queryPolicyIncludingDeleted(hotelId, policyId);
    }

    private ImageAssetResponse createImageAsset(MultipartFile file, UUID ownerId) {
        validateImageFile(file);
        UUID id = UUID.randomUUID();
        String provider = "CLOUDINARY".equalsIgnoreCase(uploadMode) ? "CLOUDINARY" : "LOCAL";
        String url = "LOCAL".equals(provider) ? "/api/v1/uploads/local/" + id : fileStorageService.uploadFile(file);
        String publicId = provider.toLowerCase(Locale.ROOT) + "/" + id;
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

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw badRequest("Image file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !Set.of("image/png", "image/jpeg", "image/webp", "image/gif").contains(contentType)) {
            throw badRequest("Only png, jpeg, webp, and gif images are supported");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw badRequest("Image file must be 10MB or smaller");
        }
    }

    private List<ImageAssetResponse> requireOwnedImages(List<UUID> imageIds, CurrentUser user) {
        if (imageIds == null) {
            return List.of();
        }
        if (imageIds.size() > Math.max(1, maxImageCount)) {
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

    private MapSqlParameterSource snapshotParams(String targetName, UUID targetId, ImageAssetResponse image, int sortOrder) {
        return new MapSqlParameterSource("id", UUID.randomUUID())
                .addValue(targetName, targetId)
                .addValue("imageAssetId", image.id())
                .addValue("url", normalizeImageUrl(image.secureUrl() == null ? image.url() : image.secureUrl()))
                .addValue("sortOrder", sortOrder);
    }

    private void insertReviewImages(UUID reviewId, List<UUID> imageIds, CurrentUser user) {
        List<ImageAssetResponse> images = requireOwnedImages(imageIds, user);
        int order = 0;
        for (ImageAssetResponse image : images) {
            jdbcTemplate.update("""
                    insert into review_images (id, review_id, image_asset_id, url, sort_order)
                    values (:id, :reviewId, :imageAssetId, :url, :sortOrder)
                    """, snapshotParams("reviewId", reviewId, image, order++));
        }
    }

    private void replaceNewsImages(UUID newsId, List<UUID> imageIds, CurrentUser user) {
        if (imageIds == null) {
            return;
        }
        List<ImageAssetResponse> images = requireOwnedImages(imageIds, user);
        jdbcTemplate.update("delete from news_images where news_id = :newsId", new MapSqlParameterSource("newsId", newsId));
        int order = 0;
        for (ImageAssetResponse image : images) {
            jdbcTemplate.update("""
                    insert into news_images (id, news_id, image_asset_id, url, sort_order)
                    values (:id, :newsId, :imageAssetId, :url, :sortOrder)
                    """, snapshotParams("newsId", newsId, image, order++));
        }
    }

    private void replaceBannerImages(UUID bannerId, List<BannerImageSource> images) {
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

    private List<BannerImageSource> resolveBannerImages(BannerMutationRequest request, CurrentUser user) {
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

    private BookingReviewSource requireCompletedBookingForReview(UUID hotelId, UUID bookingId, UUID accountId) {
        return jdbcTemplate.query("""
                select id, hotel_id
                from bookings
                where id = :bookingId
                  and hotel_id = :hotelId
                  and account_id = :accountId
                  and status = 'COMPLETED'
                """, new MapSqlParameterSource("bookingId", bookingId).addValue("hotelId", hotelId).addValue("accountId", accountId),
                rs -> {
                    if (!rs.next()) {
                        throw conflict("Only completed bookings can be reviewed");
                    }
                    return new BookingReviewSource(rs.getObject("id", UUID.class), rs.getObject("hotel_id", UUID.class));
                });
    }

    private ListResponse<ReviewResponse> listReviews(UUID hotelId, boolean visibleOnly, int page, int limit, UUID accountId) {
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
                """ + (visibleOnly ? " and r.visible\n" : "");
        long total = jdbcTemplate.queryForObject("select count(*) from reviews r " + where, params, Long.class);
        List<ReviewResponse> data = jdbcTemplate.query("""
                select r.*, a.email, a.first_name, a.last_name, a.avatar_url
                from reviews r
                join accounts a on a.id = r.account_id
                """ + where + """
                order by r.created_at desc
                limit :limit offset :offset
                """, params, (rs, rowNum) -> mapReview(rs));
        return paginated(data, boundedPage, boundedLimit, total);
    }

    private ReviewResponse reviewDetail(UUID reviewId, boolean visibleOnly) {
        return jdbcTemplate.query("""
                select r.*, a.email, a.first_name, a.last_name, a.avatar_url
                from reviews r
                join accounts a on a.id = r.account_id
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

    private ReviewResponse mapReview(ResultSet rs) throws SQLException {
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
                accountSummary(rs, "", "avatar_url")
        );
    }

    private List<ImageSnapshotResponse> listReviewImages(UUID reviewId) {
        return jdbcTemplate.query("""
                select id, image_asset_id, url, sort_order
                from review_images
                where review_id = :reviewId
                order by sort_order
                """, new MapSqlParameterSource("reviewId", reviewId), (rs, rowNum) -> mapSnapshot(rs));
    }

    private ListResponse<NewsResponse> listNews(String status, String q, int page, int limit, boolean publicOnly) {
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

    private NewsResponse queryNews(String where, MapSqlParameterSource params, boolean publicOnly) {
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

    private NewsResponse mapNews(ResultSet rs) throws SQLException {
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

    private List<NewsImageResponse> listNewsImages(UUID newsId) {
        return jdbcTemplate.query("""
                select id, news_id, url
                from news_images
                where news_id = :newsId
                order by sort_order
                """, new MapSqlParameterSource("newsId", newsId), (rs, rowNum) ->
                new NewsImageResponse(rs.getObject("id", UUID.class), rs.getObject("news_id", UUID.class), rs.getString("url")));
    }

    private BannerResponse bannerDetail(UUID id) {
        return jdbcTemplate.query("select * from banners where id = :id", new MapSqlParameterSource("id", id), rs -> {
            if (!rs.next()) {
                throw notFound("Banner not found");
            }
            return mapBanner(rs);
        });
    }

    private BannerResponse mapBanner(ResultSet rs) throws SQLException {
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

    private List<BannerImageResponse> listBannerImages(UUID bannerId, String fallbackUrl) {
        List<BannerImageResponse> images = jdbcTemplate.query("""
                select id, banner_id, url
                from banner_images
                where banner_id = :bannerId
                order by sort_order
                """, new MapSqlParameterSource("bannerId", bannerId), (rs, rowNum) ->
                new BannerImageResponse(rs.getObject("id", UUID.class), rs.getObject("banner_id", UUID.class), rs.getString("url")));
        if (images.isEmpty() && fallbackUrl != null) {
            return List.of(new BannerImageResponse(bannerId, bannerId, fallbackUrl));
        }
        return images;
    }

    private ContactResponse queryContact(UUID id) {
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

    private ContactResponse mapContact(ResultSet rs) throws SQLException {
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

    private NotificationResponse notificationDetail(UUID id, UUID accountId) {
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

    private NotificationResponse mapNotification(ResultSet rs) throws SQLException {
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

    private List<NewestBookingItemResponse> bookingItems(UUID bookingId) {
        return jdbcTemplate.query("""
                select room_type_name, quantity
                from booking_items
                where booking_id = :bookingId
                order by created_at
                """, new MapSqlParameterSource("bookingId", bookingId), (rs, rowNum) ->
                new NewestBookingItemResponse(new RoomTypeSummary(rs.getString("room_type_name")), rs.getInt("quantity")));
    }

    private CommissionPackageResponse queryCommissionPackage(UUID id) {
        return jdbcTemplate.query("select * from commission_packages where id = :id", new MapSqlParameterSource("id", id), rs -> {
            if (!rs.next()) {
                throw notFound("Commission package not found");
            }
            return mapCommissionPackage(rs);
        });
    }

    private CommissionPackageResponse mapCommissionPackage(ResultSet rs) throws SQLException {
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

    private CommissionAssignmentResponse commissionAssignment(UUID hotelId) {
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

    private PolicyResponse queryPolicy(UUID hotelId, UUID policyId) {
        return jdbcTemplate.query("""
                select *
                from hotel_policies
                where hotel_id = :hotelId and id = :id and deleted_at is null
                """, new MapSqlParameterSource("hotelId", hotelId).addValue("id", policyId), rs -> {
            if (!rs.next()) {
                throw notFound("Policy not found");
            }
            return mapPolicy(rs);
        });
    }

    private PolicyResponse queryPolicyIncludingDeleted(UUID hotelId, UUID policyId) {
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

    private PolicyResponse mapPolicy(ResultSet rs) throws SQLException {
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
                instantOrNull(rs, "deleted_at")
        );
    }

    private MapSqlParameterSource policyParams(UUID id, UUID hotelId, PolicyMutationRequest request, PolicyResponse current) {
        return new MapSqlParameterSource("id", id)
                .addValue("hotelId", hotelId)
                .addValue("type", normalizePolicyType(request.type() == null && current != null ? current.type() : request.type()))
                .addValue("title", request.title() == null && current != null ? current.title() : trimRequired(request.title(), "Policy title is required"))
                .addValue("content", request.content() == null && current != null ? current.content() : trimRequired(request.content(), "Policy content is required"))
                .addValue("enabled", request.enabled() == null ? current == null || current.enabled() : request.enabled())
                .addValue("sortOrder", request.sortOrder() == null ? current == null ? 0 : current.order() : request.sortOrder());
    }

    private ImageAssetResponse requireImageAsset(UUID id) {
        return jdbcTemplate.query("select * from image_assets where id = :id", new MapSqlParameterSource("id", id), rs -> {
            if (!rs.next()) {
                throw notFound("Image asset not found");
            }
            return mapImageAsset(rs);
        });
    }

    private ImageAssetResponse mapImageAsset(ResultSet rs) throws SQLException {
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

    private GalleryFolderResponse mapGalleryFolder(ResultSet rs) throws SQLException {
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

    private ImageSnapshotResponse mapSnapshot(ResultSet rs) throws SQLException {
        return new ImageSnapshotResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("image_asset_id", UUID.class),
                rs.getString("url"),
                rs.getInt("sort_order")
        );
    }

    private AccountSummary accountSummary(ResultSet rs, String prefix, String avatarColumn) throws SQLException {
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

    private AvatarSummary avatar(String secureUrl) {
        return secureUrl == null ? null : new AvatarSummary(secureUrl);
    }

    private CurrentUser requireUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AccountAuthUser account)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return new CurrentUser(account.getAccountId());
    }

    private CurrentUser currentUser(Authentication authentication) {
        return requireUser(authentication);
    }

    private void requireAdmin(Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        if (!isAdmin(user)) {
            throw forbidden("ADMIN role required");
        }
    }

    private boolean isAdmin(CurrentUser user) {
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

    private void requireCanManageHotel(UUID hotelId, CurrentUser user) {
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

    private ReportScope reportScope(UUID hotelId, CurrentUser user) {
        if (hotelId == null) {
            if (!isAdmin(user)) {
                throw forbidden("Hotel-scoped reports require a hotelId");
            }
            return ReportScope.global();
        }
        requireCanManageHotel(hotelId, user);
        return ReportScope.hotel(hotelId);
    }

    private void requireFolderOwner(UUID folderId, UUID accountId) {
        Boolean exists = jdbcTemplate.queryForObject("""
                select exists(select 1 from gallery_folders where id = :folderId and owner_account_id = :accountId)
                """, new MapSqlParameterSource("folderId", folderId).addValue("accountId", accountId), Boolean.class);
        if (!Boolean.TRUE.equals(exists)) {
            throw notFound("Gallery folder not found");
        }
    }

    private void requireAccountExists(UUID accountId) {
        Boolean exists = jdbcTemplate.queryForObject("select exists(select 1 from accounts where id = :id)",
                new MapSqlParameterSource("id", accountId), Boolean.class);
        if (!Boolean.TRUE.equals(exists)) {
            throw badRequest("Account does not exist");
        }
    }

    private void requireHotelExists(UUID hotelId) {
        Boolean exists = jdbcTemplate.queryForObject("select exists(select 1 from hotels where id = :id and deleted_at is null)",
                new MapSqlParameterSource("id", hotelId), Boolean.class);
        if (!Boolean.TRUE.equals(exists)) {
            throw notFound("Hotel not found");
        }
    }

    private void notifyAdmins(String type, String title, String body, String linkUrl) {
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

    private void notifyContactRecipients(String type, String title, String body, String linkUrl) {
        List<UUID> recipients = jdbcTemplate.query("""
                select distinct ar.account_id
                from account_roles ar
                join roles r on r.id = ar.role_id
                where r.name in ('ADMIN', 'OWNER', 'STAFF')
                """, (rs, rowNum) -> rs.getObject("account_id", UUID.class));
        for (UUID recipientId : recipients) {
            createNotification(recipientId, type, title, body, linkUrl);
        }
    }

    private void notifyHotelOperators(UUID hotelId, String type, String title, String body, String linkUrl) {
        List<UUID> recipients = jdbcTemplate.query("""
                select owner_id account_id from hotels where id = :hotelId
                union
                select account_id from hotel_members where hotel_id = :hotelId
                """, new MapSqlParameterSource("hotelId", hotelId), (rs, rowNum) -> rs.getObject("account_id", UUID.class));
        for (UUID recipient : recipients) {
            createNotification(recipient, type, title, body, linkUrl);
        }
    }

    private void createNotification(UUID recipientId, String type, String title, String body, String linkUrl) {
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

    private String uniqueSlug(String table, String base) {
        String slug = base;
        int suffix = 2;
        while (slugExists(table, slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private boolean slugExists(String table, String slug) {
        Boolean exists = jdbcTemplate.queryForObject("select exists(select 1 from " + table + " where slug = :slug)",
                new MapSqlParameterSource("slug", slug), Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    private int nextBannerPosition() {
        Integer next = jdbcTemplate.queryForObject("select coalesce(max(position), 0) + 1 from banners", new MapSqlParameterSource(), Integer.class);
        return next == null ? 1 : next;
    }

    private String normalizeImageUrl(String url) {
        String value = trimRequired(url, "Image URL is required");
        if (value.startsWith("/api/v1/uploads/local/") || value.startsWith("http://") || value.startsWith("https://") || value.startsWith("/")) {
            return value;
        }
        throw badRequest("Image URL must be absolute or app-relative");
    }

    private String normalizeLooseLink(String link) {
        String value = trimToNull(link);
        if (value == null) {
            return null;
        }
        if (value.startsWith("#") || value.startsWith("/") || value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        throw badRequest("Link URL is invalid");
    }

    private String normalizePolicyType(String type) {
        String normalized = trimRequired(type, "Policy type is required").toUpperCase(Locale.ROOT);
        if ("CHECKIN".equals(normalized)) {
            normalized = "CHECK_IN";
        }
        if (!POLICY_TYPES.contains(normalized)) {
            throw badRequest("Invalid policy type");
        }
        return normalized;
    }

    private String denormalizePolicyType(String type) {
        return "CHECK_IN".equals(type) ? "CHECKIN" : type;
    }

    private String normalizeOptionalStatus(String value, Set<String> allowed, String message) {
        return value == null || value.isBlank() ? null : normalizeStatus(value, null, allowed, message);
    }

    private String normalizeStatus(String value, String fallback, Set<String> allowed, String message) {
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

    private BigDecimal requireRate(BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw badRequest("Commission rate must be between 0 and 1");
        }
        return rate.setScale(4, RoundingMode.HALF_UP);
    }

    private String immutableCode(String code) {
        return trimRequired(code, "Code is required").toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_\\-]", "_");
    }

    private String slugify(String value) {
        String slug = trimRequired(value, "Slug source is required")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "item" : slug;
    }

    private String trimRequired(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw badRequest(message);
        }
        return trimmed;
    }

    private String trimOrDefault(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String like(String q) {
        String value = trimToNull(q);
        return value == null ? null : "%" + value.toLowerCase(Locale.ROOT) + "%";
    }

    private int boundedLimit(int limit) {
        if (limit <= 0) {
            return 10;
        }
        return Math.min(limit, 100);
    }

    private int boundedPage(int page) {
        return Math.max(page, 1);
    }

    private int offset(int page, int limit) {
        return (page - 1) * limit;
    }

    private int totalPages(long total, int limit) {
        return (int) Math.ceil(total / (double) Math.max(limit, 1));
    }

    private <T> ListResponse<T> paginated(List<T> data, int page, int limit, long total) {
        return new ListResponse<>(
                data,
                new PageMeta(limit, offset(page, limit), total),
                page,
                limit,
                total,
                totalPages(total, limit)
        );
    }

    private String formatPeriod(Instant period, String groupBy) {
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

    private Instant instantOrNull(ResultSet rs, String column) throws SQLException {
        var timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }

    private OffsetDateTime timestamptz(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private record CurrentUser(UUID accountId) {
    }

    private record BookingReviewSource(UUID id, UUID hotelId) {
    }

    private record BannerImageSource(UUID imageAssetId, String url) {
    }

    private record ReportScope(UUID hotelId) {
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
