package org.example.hotelbookingservice.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.hotelbookingservice.config.UploadProperties;
import org.example.hotelbookingservice.dto.request.content.BannerMutationRequest;
import org.example.hotelbookingservice.dto.request.content.CommissionPackageRequest;
import org.example.hotelbookingservice.dto.request.content.ContactCreateRequest;
import org.example.hotelbookingservice.dto.request.content.ContactUpdateRequest;
import org.example.hotelbookingservice.dto.request.content.NewsMutationRequest;
import org.example.hotelbookingservice.dto.request.content.PolicyMutationRequest;
import org.example.hotelbookingservice.dto.response.common.ListResponse;
import org.example.hotelbookingservice.dto.response.content.BannerResponse;
import org.example.hotelbookingservice.dto.response.content.CommissionAssignmentResponse;
import org.example.hotelbookingservice.dto.response.content.CommissionPackageResponse;
import org.example.hotelbookingservice.dto.response.content.ContactCreateResponse;
import org.example.hotelbookingservice.dto.response.content.ContactResponse;
import org.example.hotelbookingservice.dto.response.content.NewsResponse;
import org.example.hotelbookingservice.dto.response.content.NotificationResponse;
import org.example.hotelbookingservice.dto.response.content.PolicyResponse;
import org.example.hotelbookingservice.entity.Banner;
import org.example.hotelbookingservice.entity.CommissionPackage;
import org.example.hotelbookingservice.entity.ContactMessage;
import org.example.hotelbookingservice.entity.HotelCommissionPackage;
import org.example.hotelbookingservice.entity.News;
import org.example.hotelbookingservice.entity.Notification;
import org.example.hotelbookingservice.entity.User;
import org.example.hotelbookingservice.repository.BannerRepository;
import org.example.hotelbookingservice.repository.BannerImageRepository;
import org.example.hotelbookingservice.repository.CommissionPackageRepository;
import org.example.hotelbookingservice.repository.ContactMessageRepository;
import org.example.hotelbookingservice.repository.HotelCommissionPackageRepository;
import org.example.hotelbookingservice.repository.NewsImageRepository;
import org.example.hotelbookingservice.repository.NewsRepository;
import org.example.hotelbookingservice.repository.NotificationRepository;
import org.example.hotelbookingservice.services.IContentService;
import org.example.hotelbookingservice.services.IFileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ContentServiceImpl extends Milestone6ServiceSupport implements IContentService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final CommissionPackageRepository commissionPackageRepository;
    private final HotelCommissionPackageRepository hotelCommissionPackageRepository;
    private final NewsRepository newsRepository;
    private final BannerRepository bannerRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final NotificationRepository notificationRepository;
    private final Clock clock;

    public ContentServiceImpl(NamedParameterJdbcTemplate jdbcTemplate, IFileStorageService fileStorageService, UploadProperties uploadProperties) {
        this(jdbcTemplate, fileStorageService, uploadProperties, null, null, null, null, null, null, null, null);
    }

    public ContentServiceImpl(NamedParameterJdbcTemplate jdbcTemplate,
                              IFileStorageService fileStorageService,
                              UploadProperties uploadProperties,
                              CommissionPackageRepository commissionPackageRepository,
                              HotelCommissionPackageRepository hotelCommissionPackageRepository) {
        this(jdbcTemplate, fileStorageService, uploadProperties, commissionPackageRepository, hotelCommissionPackageRepository, null, null, null, null, null, null);
    }

    public ContentServiceImpl(NamedParameterJdbcTemplate jdbcTemplate,
                              IFileStorageService fileStorageService,
                              UploadProperties uploadProperties,
                              CommissionPackageRepository commissionPackageRepository,
                              HotelCommissionPackageRepository hotelCommissionPackageRepository,
                              NewsRepository newsRepository,
                              BannerRepository bannerRepository,
                                NewsImageRepository newsImageRepository,
                                BannerImageRepository bannerImageRepository,
                                ContactMessageRepository contactMessageRepository,
                                NotificationRepository notificationRepository) {
        this(jdbcTemplate, fileStorageService, uploadProperties, commissionPackageRepository,
                hotelCommissionPackageRepository, newsRepository, bannerRepository, newsImageRepository,
                bannerImageRepository, contactMessageRepository, notificationRepository, Clock.systemUTC());
    }

    @Autowired
    public ContentServiceImpl(NamedParameterJdbcTemplate jdbcTemplate,
                              IFileStorageService fileStorageService,
                              UploadProperties uploadProperties,
                              CommissionPackageRepository commissionPackageRepository,
                              HotelCommissionPackageRepository hotelCommissionPackageRepository,
                              NewsRepository newsRepository,
                              BannerRepository bannerRepository,
                              NewsImageRepository newsImageRepository,
                              BannerImageRepository bannerImageRepository,
                              ContactMessageRepository contactMessageRepository,
                              NotificationRepository notificationRepository,
                              Clock clock) {
        super(jdbcTemplate, fileStorageService, uploadProperties, null, null, null, newsImageRepository, bannerImageRepository);
        this.commissionPackageRepository = commissionPackageRepository;
        this.hotelCommissionPackageRepository = hotelCommissionPackageRepository;
        this.newsRepository = newsRepository;
        this.bannerRepository = bannerRepository;
        this.contactMessageRepository = contactMessageRepository;
        this.notificationRepository = notificationRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public NewsResponse createNews(NewsMutationRequest request, Authentication authentication) {
        requireAction(currentUser(authentication), "content.manage", null);
        String title = trimRequired(request.title(), "Title is required");
        String status = normalizeStatus(request.status(), "DRAFT", NEWS_STATUSES, "Invalid news status");
        if (newsRepository != null) {
            News news = new News();
            news.setAuthor(accountReference(currentUser(authentication).accountId()));
            news.setTitle(title);
            news.setSlug(uniqueNewsSlug(slugify(title)));
            news.setSummary(trimToNull(request.summary()));
            news.setContent(trimOrDefault(request.content(), ""));
            news.setStatus(status);
            news.setPublishedAt("PUBLISHED".equals(status) ? Instant.now() : null);
            News saved = newsRepository.saveAndFlush(news);
            UUID id = saved.getId();
            replaceNewsImages(id, request.imageIds(), currentUser(authentication));
            return toNewsResponse(newsRepository.findById(id).orElseThrow(() -> notFound("News not found")));
        }
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

    @Override
    @Transactional
    public NewsResponse updateNews(UUID id, NewsMutationRequest request, Authentication authentication) {
        requireAction(currentUser(authentication), "content.manage", null);
        News current = newsRepository == null ? null : newsRepository.findById(id).orElseThrow(() -> notFound("News not found"));
        NewsResponse currentResponse = current == null ? newsDetailAdmin(id, authentication) : toNewsResponse(current);
        String nextStatus = request.status() == null ? currentResponse.status() : normalizeStatus(request.status(), currentResponse.status(), NEWS_STATUSES, "Invalid news status");
        Instant publishedAt = currentResponse.publishedAt();
        if ("PUBLISHED".equals(nextStatus) && publishedAt == null) {
            publishedAt = Instant.now();
        }
        if (!"PUBLISHED".equals(nextStatus)) {
            publishedAt = null;
        }
        String title = request.title() == null ? currentResponse.title() : trimRequired(request.title(), "Title is required");
        if (current != null) {
            current.setTitle(title);
            current.setSummary(request.summary() == null ? currentResponse.summary() : trimToNull(request.summary()));
            current.setContent(request.content() == null ? currentResponse.content() : trimOrDefault(request.content(), ""));
            current.setStatus(nextStatus);
            current.setPublishedAt(publishedAt);
            current.setUpdatedAt(Instant.now());
            newsRepository.saveAndFlush(current);
            if (request.imageIds() != null) {
                replaceNewsImages(id, request.imageIds(), currentUser(authentication));
            }
            return toNewsResponse(newsRepository.findById(id).orElseThrow(() -> notFound("News not found")));
        }
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
                .addValue("summary", request.summary() == null ? currentResponse.summary() : trimToNull(request.summary()))
                .addValue("content", request.content() == null ? currentResponse.content() : trimOrDefault(request.content(), ""))
                .addValue("status", nextStatus)
                .addValue("publishedAt", timestamptz(publishedAt), Types.TIMESTAMP_WITH_TIMEZONE));
        if (request.imageIds() != null) {
            replaceNewsImages(id, request.imageIds(), currentUser(authentication));
        }
        return newsDetailAdmin(id, authentication);
    }

    @Override
    @Transactional
    public void deleteNews(UUID id, Authentication authentication) {
        requireAction(currentUser(authentication), "content.manage", null);
        if (newsRepository != null) {
            if (!newsRepository.existsById(id)) {
                throw notFound("News not found");
            }
            newsRepository.deleteById(id);
            return;
        }
        int updated = jdbcTemplate.update("delete from news where id = :id", new MapSqlParameterSource("id", id));
        if (updated == 0) {
            throw notFound("News not found");
        }
    }

    @Override
    public ListResponse<NewsResponse> listNewsAdmin(String status, String q, int page, int limit, Authentication authentication) {
        requireAction(currentUser(authentication), "content.manage", null);
        if (newsRepository != null) {
            int boundedLimit = boundedLimit(limit);
            int boundedPage = boundedPage(page);
            var news = newsRepository.findForAdmin(
                    normalizeOptionalStatus(status, NEWS_STATUSES, "Invalid news status"),
                    like(q),
                    PageRequest.of(boundedPage - 1, boundedLimit)
            );
            return paginated(news.getContent().stream().map(this::toNewsResponse).toList(), boundedPage, boundedLimit, news.getTotalElements());
        }
        return listNews(status, q, page, limit, false);
    }

    @Override
    public ListResponse<NewsResponse> listNewsPublic(String q, int page, int limit) {
        if (newsRepository != null) {
            int boundedLimit = boundedLimit(limit);
            int boundedPage = boundedPage(page);
            var news = newsRepository.findForPublic(like(q), Instant.now(), PageRequest.of(boundedPage - 1, boundedLimit));
            return paginated(news.getContent().stream().map(this::toNewsResponse).toList(), boundedPage, boundedLimit, news.getTotalElements());
        }
        return listNews("PUBLISHED", q, page, limit, true);
    }

    @Override
    public NewsResponse newsDetailAdmin(UUID id, Authentication authentication) {
        requireAction(currentUser(authentication), "content.manage", null);
        if (newsRepository != null) {
            return newsRepository.findById(id).map(this::toNewsResponse).orElseThrow(() -> notFound("News not found"));
        }
        return queryNews("where n.id = :id", new MapSqlParameterSource("id", id), false);
    }

    @Override
    public NewsResponse newsDetailPublic(String slug) {
        if (newsRepository != null) {
            return newsRepository.findBySlugAndStatusAndPublishedAtLessThanEqual(slug, "PUBLISHED", Instant.now())
                    .map(this::toNewsResponse)
                    .orElseThrow(() -> notFound("News not found"));
        }
        return queryNews("where n.slug = :slug and n.status = 'PUBLISHED' and n.published_at <= now()",
                new MapSqlParameterSource("slug", slug), true);
    }

    private void validateBannerStartDate(Instant startsAt) {
        if (startsAt == null) {
            return;
        }
        LocalDate startDate = startsAt.atZone(BUSINESS_ZONE).toLocalDate();
        LocalDate today = LocalDate.now(clock.withZone(BUSINESS_ZONE));
        if (startDate.isBefore(today)) {
            throw badRequest("Banner start date cannot be before today");
        }
    }

    @Override
    @Transactional
    public BannerResponse createBanner(BannerMutationRequest request, Authentication authentication) {
        requireAdmin(authentication);
        validateBannerStartDate(request.startsAt());
        List<BannerImageSource> images = resolveBannerImages(request, currentUser(authentication));
          if (images.isEmpty()) {
              throw badRequest("At least one banner image is required");
          }
          if (bannerRepository != null) {
              UUID id;
              try {
                  Banner banner = new Banner();
                  banner.setTitle(trimOrDefault(request.title(), "Banner"));
                banner.setSubtitle(trimToNull(request.subtitle()));
                banner.setImageUrl(images.getFirst().url());
                banner.setLinkUrl(normalizeLooseLink(request.linkUrl()));
                banner.setLinkType(normalizeStatus(request.linkType(), "URL", LINK_TYPES, "Invalid banner link type"));
                  banner.setPosition(request.position() == null ? bannerRepository.nextPosition() : request.position());
                  banner.setActive(request.active() == null || request.active());
                  banner.setStartsAt(request.startsAt());
                  banner.setEndsAt(request.endsAt());
                  Banner savedBanner = bannerRepository.saveAndFlush(banner);
                  id = savedBanner.getId();
              } catch (DataIntegrityViolationException ex) {
                  throw conflict("Banner position already exists");
              }
              replaceBannerImages(id, images);
              return bannerRepository.findById(id).map(this::toBannerResponse).orElseThrow(() -> notFound("Banner not found"));
          }
          UUID id = UUID.randomUUID();
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

    @Override
    @Transactional
    public BannerResponse updateBanner(UUID id, BannerMutationRequest request, Authentication authentication) {
        requireAdmin(authentication);
        Banner current = bannerRepository == null ? null : bannerRepository.findById(id).orElseThrow(() -> notFound("Banner not found"));
        BannerResponse currentResponse = current == null ? bannerDetail(id) : toBannerResponse(current);
        if (request.startsAt() != null && !request.startsAt().equals(currentResponse.startAt())) {
            validateBannerStartDate(request.startsAt());
        }
        List<BannerImageSource> images = request.imageIds() == null && request.images() == null
                ? List.of(new BannerImageSource(null, currentResponse.images().getFirst().url()))
                : resolveBannerImages(request, currentUser(authentication));
        if (images.isEmpty()) {
            throw badRequest("At least one banner image is required");
        }
        if (current != null) {
            try {
                current.setTitle(request.title() == null ? currentResponse.title() : trimOrDefault(request.title(), "Banner"));
                current.setSubtitle(request.subtitle() == null ? currentResponse.subtitle() : trimToNull(request.subtitle()));
                current.setImageUrl(images.getFirst().url());
                current.setLinkUrl(request.linkUrl() == null ? currentResponse.link() : normalizeLooseLink(request.linkUrl()));
                current.setLinkType(request.linkType() == null ? currentResponse.linkType() : normalizeStatus(request.linkType(), currentResponse.linkType(), LINK_TYPES, "Invalid banner link type"));
                current.setPosition(request.position() == null ? currentResponse.position() : request.position());
                current.setActive(request.active() == null ? currentResponse.isActive() : request.active());
                current.setStartsAt(request.startsAt() == null ? currentResponse.startAt() : request.startsAt());
                current.setEndsAt(request.endsAt() == null ? currentResponse.endAt() : request.endsAt());
                current.setUpdatedAt(Instant.now());
                bannerRepository.saveAndFlush(current);
            } catch (DataIntegrityViolationException ex) {
                throw conflict("Banner position already exists");
            }
            if (request.imageIds() != null || request.images() != null) {
                replaceBannerImages(id, images);
            }
            return bannerRepository.findById(id).map(this::toBannerResponse).orElseThrow(() -> notFound("Banner not found"));
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
                    .addValue("title", request.title() == null ? currentResponse.title() : trimOrDefault(request.title(), "Banner"))
                    .addValue("subtitle", request.subtitle() == null ? currentResponse.subtitle() : trimToNull(request.subtitle()))
                    .addValue("imageUrl", images.getFirst().url())
                    .addValue("linkUrl", request.linkUrl() == null ? currentResponse.link() : normalizeLooseLink(request.linkUrl()))
                    .addValue("linkType", request.linkType() == null ? currentResponse.linkType() : normalizeStatus(request.linkType(), currentResponse.linkType(), LINK_TYPES, "Invalid banner link type"))
                    .addValue("position", request.position() == null ? currentResponse.position() : request.position())
                    .addValue("active", request.active() == null ? currentResponse.isActive() : request.active())
                    .addValue("startsAt", timestamptz(request.startsAt() == null ? currentResponse.startAt() : request.startsAt()), Types.TIMESTAMP_WITH_TIMEZONE)
                    .addValue("endsAt", timestamptz(request.endsAt() == null ? currentResponse.endAt() : request.endsAt()), Types.TIMESTAMP_WITH_TIMEZONE));
        } catch (DuplicateKeyException ex) {
            throw conflict("Banner position already exists");
        }
        if (request.imageIds() != null || request.images() != null) {
            replaceBannerImages(id, images);
        }
        return bannerDetail(id);
    }

    @Override
    @Transactional
    public void deleteBanner(UUID id, Authentication authentication) {
        requireAdmin(authentication);
        if (bannerRepository != null) {
            if (!bannerRepository.existsById(id)) {
                throw notFound("Banner not found");
            }
            bannerRepository.deleteById(id);
            return;
        }
        int updated = jdbcTemplate.update("delete from banners where id = :id", new MapSqlParameterSource("id", id));
        if (updated == 0) {
            throw notFound("Banner not found");
        }
    }

    @Override
    public List<BannerResponse> listAdminBanners(Authentication authentication) {
        requireAdmin(authentication);
        if (bannerRepository != null) {
            return bannerRepository.findAllByOrderByPositionAsc().stream().map(this::toBannerResponse).toList();
        }
        return jdbcTemplate.query("select * from banners order by position", (rs, rowNum) -> mapBanner(rs));
    }

    @Override
    public List<BannerResponse> listPublicBanners() {
        if (bannerRepository != null) {
            return bannerRepository.findActiveForPublic(Instant.now()).stream().map(this::toBannerResponse).toList();
        }
        return jdbcTemplate.query("""
                select *
                from banners
                where active
                  and (starts_at is null or starts_at <= now())
                  and (ends_at is null or ends_at >= now())
                order by position
                """, (rs, rowNum) -> mapBanner(rs));
    }

    @Override
    @Transactional
    public ContactCreateResponse createContact(ContactCreateRequest request, String ipAddress, String userAgent, Authentication authentication) {
        UUID id = UUID.randomUUID();
        UUID accountId = null;
        if (authentication != null && authentication.getPrincipal() instanceof org.example.hotelbookingservice.security.AccountAuthUser account) {
            accountId = account.getAccountId();
        }
        if (contactMessageRepository != null) {
            ContactMessage contact = new ContactMessage();
            contact.setId(id);
            contact.setAccount(accountReference(accountId));
            contact.setName(trimRequired(request.name(), "Name is required"));
            contact.setEmail(trimToNull(request.email()));
            contact.setPhone(trimToNull(request.phone()));
            contact.setSubject(trimToNull(request.subject()));
            contact.setMessage(trimRequired(request.message(), "Message is required"));
            contact.setIpAddress(trimToNull(ipAddress));
            contact.setUserAgent(trimToNull(userAgent));
            contactMessageRepository.saveAndFlush(contact);
            try {
                log.info("Contact notification email requested for {}", trimToNull(request.email()));
                notifyContactRecipients("SYSTEM", "New contact message", "A visitor submitted a contact message.", "/admin/contacts/" + id);
            } catch (RuntimeException ex) {
                log.warn("Contact notification failed for {}, public submission preserved", id, ex);
            }
            return new ContactCreateResponse(id, true);
        }
        jdbcTemplate.update("""
                insert into contact_messages (id, account_id, name, email, phone, subject, message, ip_address, user_agent)
                values (:id, :accountId, :name, :email, :phone, :subject, :message, :ip, :userAgent)
                """, new MapSqlParameterSource("id", id)
                .addValue("accountId", accountId)
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

    @Override
    public ListResponse<ContactResponse> listContacts(String status, String q, int page, int limit, Authentication authentication) {
        requireAdmin(authentication);
        if (contactMessageRepository != null) {
            int boundedLimit = boundedLimit(limit);
            int boundedPage = boundedPage(page);
            var contacts = contactMessageRepository.findForAdmin(
                    normalizeOptionalStatus(status, CONTACT_STATUSES, "Invalid contact status"),
                    like(q),
                    PageRequest.of(boundedPage - 1, boundedLimit)
            );
            return paginated(contacts.getContent().stream().map(this::toContactResponse).toList(), boundedPage, boundedLimit, contacts.getTotalElements());
        }
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

    @Override
    public ContactResponse contactDetail(UUID id, Authentication authentication) {
        requireAdmin(authentication);
        if (contactMessageRepository != null) {
            return contactMessageRepository.findDetailById(id)
                    .map(this::toContactResponse)
                    .orElseThrow(() -> notFound("Contact not found"));
        }
        return queryContact(id);
    }

    @Override
    @Transactional
    public ContactResponse updateContact(UUID id, ContactUpdateRequest request, Authentication authentication) {
        requireAdmin(authentication);
        ContactMessage current = contactMessageRepository == null ? null : contactMessageRepository.findById(id).orElseThrow(() -> notFound("Contact not found"));
        String status = request.status() == null
                ? (current == null ? queryContact(id).status() : current.getStatus())
                : normalizeStatus(request.status(), "NEW", CONTACT_STATUSES, "Invalid contact status");
        if (request.handledById() != null) {
            requireAccountExists(request.handledById());
        }
        if (current != null) {
            current.setStatus(status);
            current.setHandledBy(accountReference(request.handledById()));
            current.setNote(trimToNull(request.note()));
            current.setUpdatedAt(Instant.now());
            contactMessageRepository.saveAndFlush(current);
            return contactMessageRepository.findDetailById(id).map(this::toContactResponse).orElseThrow(() -> notFound("Contact not found"));
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

    @Override
    public ListResponse<NotificationResponse> listNotifications(int page, int limit, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        if (notificationRepository != null) {
            int boundedLimit = boundedLimit(limit);
            int boundedPage = boundedPage(page);
            var notifications = notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(user.accountId(), PageRequest.of(boundedPage - 1, boundedLimit));
            return paginated(notifications.getContent().stream().map(this::toNotificationResponse).toList(), boundedPage, boundedLimit, notifications.getTotalElements());
        }
        int boundedLimit = boundedLimit(limit);
        int boundedPage = boundedPage(page);
        MapSqlParameterSource params = new MapSqlParameterSource("accountId", user.accountId())
                .addValue("limit", boundedLimit)
                .addValue("offset", offset(boundedPage, boundedLimit));
        long total = jdbcTemplate.queryForObject("select count(*) from notifications where recipient_account_id = :accountId", params, Long.class);
        List<NotificationResponse> data = jdbcTemplate.query("""
                select *
                from notifications
                where recipient_account_id = :accountId
                order by created_at desc
                limit :limit offset :offset
                """, params, (rs, rowNum) -> mapNotification(rs));
        return paginated(data, boundedPage, boundedLimit, total);
    }

    @Override
    public long unreadCount(Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        if (notificationRepository != null) {
            return notificationRepository.countByRecipient_IdAndReadAtIsNull(user.accountId());
        }
        return jdbcTemplate.queryForObject("""
                select count(*)
                from notifications
                where recipient_account_id = :accountId and read_at is null
                """, new MapSqlParameterSource("accountId", user.accountId()), Long.class);
    }

    @Override
    @Transactional
    public NotificationResponse markRead(UUID id, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        if (notificationRepository != null) {
            Notification notification = notificationRepository.findByIdAndRecipient_Id(id, user.accountId())
                    .orElseThrow(() -> notFound("Notification not found"));
            if (notification.getReadAt() == null) {
                notification.setReadAt(Instant.now());
                notificationRepository.saveAndFlush(notification);
            }
            return toNotificationResponse(notification);
        }
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

    @Override
    @Transactional
    public void markAllRead(Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        if (notificationRepository != null) {
            notificationRepository.markAllRead(user.accountId(), Instant.now());
            return;
        }
        jdbcTemplate.update("""
                update notifications
                set read_at = coalesce(read_at, now())
                where recipient_account_id = :accountId and read_at is null
                """, new MapSqlParameterSource("accountId", user.accountId()));
    }

    @Override
    @Transactional
    public void deleteNotification(UUID id, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        if (notificationRepository != null) {
            notificationRepository.deleteByIdAndRecipient_Id(id, user.accountId());
            return;
        }
        jdbcTemplate.update("delete from notifications where id = :id and recipient_account_id = :accountId",
                new MapSqlParameterSource("id", id).addValue("accountId", user.accountId()));
    }

    @Override
    public List<CommissionPackageResponse> listCommissionPackages(Authentication authentication) {
        requireAdmin(authentication);
        if (commissionPackageRepository != null) {
            return commissionPackageRepository.findAllByOrderByActiveDescCodeAsc().stream()
                    .map(this::toCommissionPackageResponse)
                    .toList();
        }
        return jdbcTemplate.query("""
                select *
                from commission_packages
                order by active desc, code
                """, (rs, rowNum) -> mapCommissionPackage(rs));
    }

    @Override
    public CommissionPackageResponse commissionPackageDetail(UUID id, Authentication authentication) {
        requireAdmin(authentication);
        if (commissionPackageRepository != null) {
            return commissionPackageRepository.findById(id)
                    .map(this::toCommissionPackageResponse)
                    .orElseThrow(() -> notFound("Commission package not found"));
        }
        return queryCommissionPackage(id);
    }

    @Override
    @Transactional
    public CommissionPackageResponse createCommissionPackage(CommissionPackageRequest request, Authentication authentication) {
        requireAdmin(authentication);
        UUID id = UUID.randomUUID();
        try {
            if (commissionPackageRepository != null) {
                CommissionPackage commissionPackage = new CommissionPackage();
                commissionPackage.setId(id);
                commissionPackage.setCode(immutableCode(request.code()));
                commissionPackage.setName(trimRequired(request.name(), "Name is required"));
                commissionPackage.setDescription(trimToNull(request.description()));
                commissionPackage.setCommissionRate(requireRate(request.commissionRate()));
                commissionPackage.setActive(request.active() == null || request.active());
                commissionPackage = commissionPackageRepository.saveAndFlush(commissionPackage);
                return toCommissionPackageResponse(commissionPackage);
            }
            jdbcTemplate.update("""
                    insert into commission_packages (id, code, name, description, commission_rate, active)
                    values (:id, :code, :name, :description, :rate, :active)
                    """, new MapSqlParameterSource("id", id)
                    .addValue("code", immutableCode(request.code()))
                    .addValue("name", trimRequired(request.name(), "Name is required"))
                    .addValue("description", trimToNull(request.description()))
                    .addValue("rate", requireRate(request.commissionRate()))
                    .addValue("active", request.active() == null || request.active()));
        } catch (DataIntegrityViolationException ex) {
            throw conflict("Commission package code already exists");
        }
        return queryCommissionPackage(id);
    }

    @Override
    @Transactional
    public CommissionPackageResponse updateCommissionPackage(UUID id, CommissionPackageRequest request, Authentication authentication) {
        requireAdmin(authentication);
        CommissionPackageResponse current = commissionPackageDetail(id, authentication);
        if (request.code() != null && !immutableCode(request.code()).equals(current.code())) {
            throw badRequest("Commission package code is immutable");
        }
        if (commissionPackageRepository != null) {
            CommissionPackage commissionPackage = commissionPackageRepository.findById(id)
                    .orElseThrow(() -> notFound("Commission package not found"));
            commissionPackage.setName(request.name() == null ? current.name() : trimRequired(request.name(), "Name is required"));
            commissionPackage.setDescription(request.description() == null ? current.description() : trimToNull(request.description()));
            commissionPackage.setCommissionRate(request.commissionRate() == null ? current.commissionRate() : requireRate(request.commissionRate()));
            commissionPackage.setActive(request.active() == null ? current.isActive() : request.active());
            commissionPackage.setUpdatedAt(Instant.now());
            return toCommissionPackageResponse(commissionPackageRepository.saveAndFlush(commissionPackage));
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

    @Override
    @Transactional
    public CommissionPackageResponse deactivateCommissionPackage(UUID id, Authentication authentication) {
        requireAdmin(authentication);
        if (commissionPackageRepository != null) {
            CommissionPackage commissionPackage = commissionPackageRepository.findById(id)
                    .orElseThrow(() -> notFound("Commission package not found"));
            commissionPackage.setActive(false);
            commissionPackage.setUpdatedAt(Instant.now());
            return toCommissionPackageResponse(commissionPackageRepository.saveAndFlush(commissionPackage));
        }
        jdbcTemplate.update("update commission_packages set active = false, updated_at = now() where id = :id", new MapSqlParameterSource("id", id));
        return queryCommissionPackage(id);
    }

    @Override
    @Transactional
    public CommissionAssignmentResponse assignCommissionPackage(UUID hotelId, UUID packageId, Authentication authentication) {
        requireAdmin(authentication);
        CommissionPackageResponse pack = queryCommissionPackage(packageId);
        if (!pack.isActive()) {
            throw conflict("Cannot assign an inactive commission package");
        }
        requireHotelExists(hotelId);
        if (hotelCommissionPackageRepository != null) {
            hotelCommissionPackageRepository.upsertAssignment(hotelId, packageId);
            return hotelCommissionPackageRepository.findWithPackageByHotelId(hotelId)
                    .map(this::toCommissionAssignmentResponse)
                    .orElseThrow(() -> notFound("Commission assignment not found"));
        }
        jdbcTemplate.update("""
                insert into hotel_commission_packages (hotel_id, commission_package_id)
                values (:hotelId, :packageId)
                on conflict (hotel_id)
                do update set commission_package_id = excluded.commission_package_id, assigned_at = now()
                """, new MapSqlParameterSource("hotelId", hotelId).addValue("packageId", packageId));
        return commissionAssignment(hotelId);
    }

    private CommissionPackageResponse toCommissionPackageResponse(CommissionPackage commissionPackage) {
        return new CommissionPackageResponse(
                commissionPackage.getId(),
                commissionPackage.getCode(),
                commissionPackage.getName(),
                commissionPackage.getDescription(),
                commissionPackage.getCommissionRate(),
                commissionPackage.isActive(),
                commissionPackage.getCreatedAt(),
                commissionPackage.getUpdatedAt()
        );
    }

    private CommissionAssignmentResponse toCommissionAssignmentResponse(HotelCommissionPackage assignment) {
        CommissionPackage commissionPackage = assignment.getCommissionPackage();
        return new CommissionAssignmentResponse(
                assignment.getHotelId(),
                commissionPackage.getId(),
                commissionPackage.getCode(),
                commissionPackage.getCommissionRate(),
                assignment.getAssignedAt()
        );
    }

    @Override
    protected void createNotification(UUID recipientId, String type, String title, String body, String linkUrl) {
        if (notificationRepository == null) {
            super.createNotification(recipientId, type, title, body, linkUrl);
            return;
          }
          Notification notification = new Notification();
          notification.setRecipient(accountReference(recipientId));
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setLinkUrl(linkUrl);
        notificationRepository.save(notification);
    }

    private NewsResponse toNewsResponse(News news) {
        return new NewsResponse(
                news.getId(),
                news.getTitle(),
                news.getSlug(),
                news.getSummary(),
                news.getContent(),
                news.getStatus(),
                news.getPublishedAt(),
                news.getCreatedAt(),
                news.getUpdatedAt(),
                listNewsImages(news.getId())
        );
    }

    private BannerResponse toBannerResponse(Banner banner) {
        return new BannerResponse(
                banner.getId(),
                banner.getTitle(),
                banner.getSubtitle(),
                banner.getLinkUrl(),
                banner.getLinkType(),
                banner.getPosition(),
                banner.isActive(),
                banner.getStartsAt(),
                banner.getEndsAt(),
                listBannerImages(banner.getId(), banner.getImageUrl()),
                banner.getCreatedAt(),
                banner.getUpdatedAt()
        );
    }

    private ContactResponse toContactResponse(ContactMessage contact) {
        User handledBy = contact.getHandledBy();
        UUID handledById = contact.getHandledByAccountId();
        if (handledById == null && handledBy != null) {
            handledById = handledBy.getId();
        }
        return new ContactResponse(
                contact.getId(),
                contact.getName(),
                contact.getEmail(),
                contact.getPhone(),
                contact.getSubject(),
                contact.getMessage(),
                contact.getStatus(),
                contact.getAccountId(),
                handledById,
                handledBy == null ? null : new org.example.hotelbookingservice.dto.response.common.AccountSummary(
                        handledBy.getId(),
                        handledBy.getEmail(),
                        handledBy.getFirstName(),
                        handledBy.getLastName(),
                        avatar(handledBy.getAvatarUrl())
                ),
                contact.getNote(),
                contact.getIpAddress(),
                contact.getUserAgent(),
                contact.getCreatedAt(),
                contact.getUpdatedAt()
        );
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
        Instant readAt = notification.getReadAt();
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getLinkUrl(),
                readAt != null,
                readAt,
                notification.getCreatedAt()
        );
    }

    private User accountReference(UUID accountId) {
        if (accountId == null) {
            return null;
        }
        User user = new User();
        user.setId(accountId);
        return user;
    }

    private String uniqueNewsSlug(String base) {
        if (newsRepository == null) {
            return uniqueSlug("news", base);
        }
        String slug = base;
        int suffix = 2;
        while (newsRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    @Override
    public List<PolicyResponse> listPoliciesPublic(UUID hotelId) {
        return jdbcTemplate.query("""
                select *
                from hotel_policies hp
                where hp.hotel_id = :hotelId
                  and hp.enabled
                  and exists (
                      select 1
                      from hotels h
                      where h.id = hp.hotel_id and h.status = 'ACTIVE' and h.deleted_at is null
                  )
                order by sort_order
                """, new MapSqlParameterSource("hotelId", hotelId), (rs, rowNum) -> mapPolicy(rs));
    }

    @Override
    public List<PolicyResponse> listPoliciesAdmin(UUID hotelId, Authentication authentication) {
        requireAdmin(authentication);
        return jdbcTemplate.query("""
                select *
                from hotel_policies
                where hotel_id = :hotelId
                order by sort_order
                """, new MapSqlParameterSource("hotelId", hotelId), (rs, rowNum) -> mapPolicy(rs));
    }

    @Override
    public PolicyResponse policyDetail(UUID hotelId, UUID policyId, Authentication authentication) {
        requireAdmin(authentication);
        return queryPolicy(hotelId, policyId);
    }

    @Override
    @Transactional
    public PolicyResponse createPolicy(UUID hotelId, PolicyMutationRequest request, Authentication authentication) {
        requireAdmin(authentication);
        UUID id = UUID.randomUUID();
        try {
            jdbcTemplate.update("""
                    insert into hotel_policies (id, hotel_id, type, title, content, enabled, sort_order)
                    values (:id, :hotelId, :type, :title, :content, :enabled, :sortOrder)
                    """, policyParams(id, hotelId, request, null));
        } catch (DataIntegrityViolationException ex) {
            throw policyCollision(ex, request.sortOrder());
        }
        return queryPolicy(hotelId, id);
    }

    @Override
    @Transactional
    public PolicyResponse updatePolicy(UUID hotelId, UUID policyId, PolicyMutationRequest request, Authentication authentication) {
        requireAdmin(authentication);
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
            throw policyCollision(ex, request.sortOrder() == null ? current.order() : request.sortOrder());
        }
        return queryPolicy(hotelId, policyId);
    }

    @Override
    @Transactional
    public PolicyResponse deletePolicy(UUID hotelId, UUID policyId, Authentication authentication) {
        requireAdmin(authentication);
        PolicyResponse current = queryPolicy(hotelId, policyId);
        jdbcTemplate.update("""
                delete from hotel_policies
                where id = :id and hotel_id = :hotelId
                """, new MapSqlParameterSource("id", policyId).addValue("hotelId", hotelId));
        return current;
    }
}
