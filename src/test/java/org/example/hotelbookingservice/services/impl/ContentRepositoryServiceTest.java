package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.config.UploadProperties;
import org.example.hotelbookingservice.dto.request.content.ContactCreateRequest;
import org.example.hotelbookingservice.dto.request.content.ContactUpdateRequest;
import org.example.hotelbookingservice.dto.request.content.NewsMutationRequest;
import org.example.hotelbookingservice.entity.Banner;
import org.example.hotelbookingservice.entity.ContactMessage;
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
import org.example.hotelbookingservice.security.AccountAuthUser;
import org.example.hotelbookingservice.services.IFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentRepositoryServiceTest {
    private static final UUID ADMIN_ID = UUID.fromString("74000000-0000-4000-8000-000000000001");
    private static final UUID NEWS_ID = UUID.fromString("74000000-0000-4000-8000-000000000002");
    private static final UUID BANNER_ID = UUID.fromString("74000000-0000-4000-8000-000000000003");
    private static final UUID CONTACT_ID = UUID.fromString("74000000-0000-4000-8000-000000000004");
    private static final UUID NOTIFICATION_ID = UUID.fromString("74000000-0000-4000-8000-000000000005");

    @Mock NamedParameterJdbcTemplate jdbcTemplate;
    @Mock IFileStorageService fileStorageService;
    @Mock UploadProperties uploadProperties;
    @Mock CommissionPackageRepository commissionPackageRepository;
      @Mock HotelCommissionPackageRepository hotelCommissionPackageRepository;
      @Mock NewsRepository newsRepository;
      @Mock BannerRepository bannerRepository;
      @Mock NewsImageRepository newsImageRepository;
      @Mock BannerImageRepository bannerImageRepository;
      @Mock ContactMessageRepository contactMessageRepository;
      @Mock NotificationRepository notificationRepository;

    ContentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ContentServiceImpl(
                jdbcTemplate,
                fileStorageService,
                uploadProperties,
                commissionPackageRepository,
                  hotelCommissionPackageRepository,
                  newsRepository,
                  bannerRepository,
                  newsImageRepository,
                  bannerImageRepository,
                  contactMessageRepository,
                  notificationRepository
          );
        lenient().when(jdbcTemplate.queryForObject(anyString(), any(SqlParameterSource.class), eq(Boolean.class))).thenReturn(true);
        lenient().when(jdbcTemplate.query(anyString(), any(SqlParameterSource.class), any(RowMapper.class))).thenReturn(List.of());
    }

    @Test
    void newsListsUseRepositoriesAndPreservePaginationMetadata() {
        News news = news();
        when(newsRepository.findForAdmin(eq("PUBLISHED"), eq("%launch%"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(news), PageRequest.of(1, 5), 7));
        when(newsRepository.findForPublic(eq("%launch%"), any(Instant.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(news), PageRequest.of(0, 10), 1));

        var admin = service.listNewsAdmin("published", "Launch", 2, 5, adminAuthentication());
        var published = service.listNewsPublic("Launch", 1, 10);

        assertThat(admin.data()).extracting(item -> item.id()).containsExactly(NEWS_ID);
        assertThat(admin.page()).isEqualTo(2);
        assertThat(admin.limit()).isEqualTo(5);
        assertThat(admin.total()).isEqualTo(6);
        assertThat(admin.meta().offset()).isEqualTo(5);
        assertThat(published.data()).extracting(item -> item.slug()).containsExactly("launch-news");
    }

    @Test
    void createNewsTreatsGeneratedIdentifierAsNewEntity() {
        AtomicReference<News> savedNews = new AtomicReference<>();
        when(newsRepository.existsBySlug(anyString())).thenReturn(false);
        when(newsRepository.saveAndFlush(any(News.class))).thenAnswer(invocation -> {
            News candidate = invocation.getArgument(0);
            assertThat(candidate.getId()).isNull();
            candidate.setId(NEWS_ID);
            savedNews.set(candidate);
            return candidate;
        });
        when(newsRepository.findById(any(UUID.class))).thenAnswer(invocation -> Optional.of(savedNews.get()));
        when(newsImageRepository.findByNews_IdOrderBySortOrderAsc(any(UUID.class))).thenReturn(List.of());

        var created = service.createNews(
                new NewsMutationRequest("Generated ID News", "Summary", "Body", "DRAFT", List.of()),
                adminAuthentication()
        );

        assertThat(created.id()).isEqualTo(NEWS_ID);
        assertThat(savedNews.get().getSlug()).isEqualTo("generated-id-news");
    }

    @Test
    void bannersUseRepositoriesForAdminAndPublicLists() {
        Banner banner = banner();
        when(bannerRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(banner));
        when(bannerRepository.findActiveForPublic(any(Instant.class))).thenReturn(List.of(banner));

        var admin = service.listAdminBanners(adminAuthentication());
        var published = service.listPublicBanners();

        assertThat(admin).extracting(item -> item.id()).containsExactly(BANNER_ID);
        assertThat(published.getFirst().linkType()).isEqualTo("URL");
    }

    @Test
    void contactsCreateUpdateAndListThroughRepositories() {
        ContactMessage contact = contact();
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(List.of(ADMIN_ID));
        when(contactMessageRepository.saveAndFlush(any(ContactMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contactMessageRepository.findForAdmin(eq("IN_PROGRESS"), eq("%guest%"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(contact), PageRequest.of(0, 10), 1));
        when(contactMessageRepository.findById(CONTACT_ID)).thenReturn(Optional.of(contact));
        when(contactMessageRepository.findDetailById(CONTACT_ID)).thenReturn(Optional.of(contact));
        ArgumentCaptor<ContactMessage> savedContact = ArgumentCaptor.forClass(ContactMessage.class);

        var created = service.createContact(
                new ContactCreateRequest("Guest", "guest@example.com", null, "Help", "Need support"),
                "127.0.0.1",
                "JUnit",
                null
        );
        var listed = service.listContacts("in_progress", "Guest", 1, 10, adminAuthentication());
        var updated = service.updateContact(CONTACT_ID, new ContactUpdateRequest("RESOLVED", ADMIN_ID, "Done"), adminAuthentication());

        verify(contactMessageRepository, times(2)).saveAndFlush(savedContact.capture());
        verify(notificationRepository).save(any(Notification.class));
        assertThat(created.ok()).isTrue();
        assertThat(savedContact.getAllValues().getFirst().getName()).isEqualTo("Guest");
        assertThat(listed.data()).extracting(item -> item.id()).containsExactly(CONTACT_ID);
        assertThat(updated.status()).isEqualTo("RESOLVED");
        assertThat(updated.handledById()).isEqualTo(ADMIN_ID);
    }

    @Test
    void notificationsUseRecipientScopedRepositories() {
        Notification notification = notification();
        when(notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(eq(ADMIN_ID), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(notification), PageRequest.of(0, 10), 1));
        when(notificationRepository.countByRecipient_IdAndReadAtIsNull(ADMIN_ID)).thenReturn(3L);
        when(notificationRepository.findByIdAndRecipient_Id(NOTIFICATION_ID, ADMIN_ID)).thenReturn(Optional.of(notification));
        when(notificationRepository.saveAndFlush(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var listed = service.listNotifications(1, 10, adminAuthentication());
        long unread = service.unreadCount(adminAuthentication());
        var read = service.markRead(NOTIFICATION_ID, adminAuthentication());
        service.markAllRead(adminAuthentication());
        service.deleteNotification(NOTIFICATION_ID, adminAuthentication());

        verify(notificationRepository).markAllRead(eq(ADMIN_ID), any(Instant.class));
        verify(notificationRepository).deleteByIdAndRecipient_Id(NOTIFICATION_ID, ADMIN_ID);
        assertThat(listed.data()).extracting(item -> item.id()).containsExactly(NOTIFICATION_ID);
        assertThat(unread).isEqualTo(3);
        assertThat(read.read()).isTrue();
    }

    private UsernamePasswordAuthenticationToken adminAuthentication() {
        AccountAuthUser user = AccountAuthUser.builder()
                .accountId(ADMIN_ID)
                .email("admin@example.com")
                .passwordHash("hash")
                .emailVerified(true)
                .authorities(List.of())
                .build();
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private News news() {
        News news = new News();
        news.setId(NEWS_ID);
        news.setTitle("Launch News");
        news.setSlug("launch-news");
        news.setSummary("Summary");
        news.setContent("Body");
        news.setStatus("PUBLISHED");
        news.setPublishedAt(Instant.parse("2026-07-09T00:00:00Z"));
        news.setCreatedAt(Instant.parse("2026-07-08T00:00:00Z"));
        news.setUpdatedAt(Instant.parse("2026-07-09T00:00:00Z"));
        return news;
    }

    private Banner banner() {
        Banner banner = new Banner();
        banner.setId(BANNER_ID);
        banner.setTitle("Hero");
        banner.setSubtitle("Subtitle");
        banner.setImageUrl("/hero.png");
        banner.setLinkUrl("/news/launch-news");
        banner.setLinkType("URL");
        banner.setPosition(1);
        banner.setActive(true);
        banner.setCreatedAt(Instant.parse("2026-07-08T00:00:00Z"));
        banner.setUpdatedAt(Instant.parse("2026-07-09T00:00:00Z"));
        return banner;
    }

    private ContactMessage contact() {
        User handler = new User();
        handler.setId(ADMIN_ID);
        handler.setEmail("admin@example.com");
        handler.setFirstName("Admin");
        handler.setLastName("User");
        ContactMessage contact = new ContactMessage();
        contact.setId(CONTACT_ID);
        contact.setName("Guest");
        contact.setEmail("guest@example.com");
        contact.setSubject("Help");
        contact.setMessage("Need support");
        contact.setStatus("IN_PROGRESS");
        contact.setHandledBy(handler);
        contact.setNote("Handling");
        contact.setIpAddress("127.0.0.1");
        contact.setUserAgent("JUnit");
        contact.setCreatedAt(Instant.parse("2026-07-08T00:00:00Z"));
        contact.setUpdatedAt(Instant.parse("2026-07-09T00:00:00Z"));
        return contact;
    }

    private Notification notification() {
        Notification notification = new Notification();
        notification.setId(NOTIFICATION_ID);
        notification.setRecipient(new User());
        notification.getRecipient().setId(ADMIN_ID);
        notification.setType("SYSTEM");
        notification.setTitle("Hello");
        notification.setBody("Body");
        notification.setLinkUrl("/notifications");
        notification.setCreatedAt(Instant.parse("2026-07-09T00:00:00Z"));
        return notification;
    }
}
