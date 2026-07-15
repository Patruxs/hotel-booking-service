package org.example.hotelbookingservice.repository;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ContentJpaMappingTest {
    private static final UUID AUTHOR_ID = UUID.fromString("73000000-0000-4000-8000-000000000001");
    private static final UUID HANDLER_ID = UUID.fromString("73000000-0000-4000-8000-000000000002");
    private static final UUID NEWS_ID = UUID.fromString("73000000-0000-4000-8000-000000000003");
    private static final UUID BANNER_ID = UUID.fromString("73000000-0000-4000-8000-000000000004");
    private static final UUID CONTACT_ID = UUID.fromString("73000000-0000-4000-8000-000000000005");
    private static final UUID NOTIFICATION_ID = UUID.fromString("73000000-0000-4000-8000-000000000006");

    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManagerFactory entityManagerFactory;
    @Autowired NewsRepository newsRepository;
    @Autowired BannerRepository bannerRepository;
    @Autowired ContactMessageRepository contactMessageRepository;
    @Autowired NotificationRepository notificationRepository;

    @Test
    void contentEntitiesLoadAgainstFlywaySchemaWithLazyRelationships() {
        insertAccounts();
        insertContentRows();

        var news = newsRepository.findById(NEWS_ID).orElseThrow();
        var banner = bannerRepository.findById(BANNER_ID).orElseThrow();
        var contact = contactMessageRepository.findDetailById(CONTACT_ID).orElseThrow();
        var notification = notificationRepository.findByIdAndRecipient_Id(NOTIFICATION_ID, AUTHOR_ID).orElseThrow();
        var persistenceUnit = entityManagerFactory.getPersistenceUnitUtil();

        assertThat(news.getTitle()).isEqualTo("Mapping News");
        assertThat(news.getAuthorAccountId()).isEqualTo(AUTHOR_ID);
        assertThat(news.getStatus()).isEqualTo("PUBLISHED");
        assertThat(persistenceUnit.isLoaded(news, "author")).isFalse();

        assertThat(banner.getImageUrl()).isEqualTo("/banner.png");
        assertThat(banner.getLinkType()).isEqualTo("URL");
        assertThat(banner.isActive()).isTrue();

        assertThat(contact.getHandledByAccountId()).isEqualTo(HANDLER_ID);
        assertThat(contact.getHandledBy().getEmail()).isEqualTo("content-handler@example.com");
        assertThat(contact.getIpAddress()).isEqualTo("127.0.0.1");

        assertThat(notification.getRecipientAccountId()).isEqualTo(AUTHOR_ID);
        assertThat(notification.getTitle()).isEqualTo("Hello");
        assertThat(notification.getReadAt()).isNull();
        assertThat(persistenceUnit.isLoaded(notification, "recipient")).isFalse();
    }

    @Test
    void contentRepositoriesPreserveFrontendFilteringAndPaginationContracts() {
        insertAccounts();
        insertContentRows();

        var publicNews = newsRepository.findForPublic("%mapping%", Instant.now(), PageRequest.of(0, 10));
        var adminContacts = contactMessageRepository.findForAdmin("IN_PROGRESS", "%guest%", PageRequest.of(0, 10));
        var publicBanners = bannerRepository.findActiveForPublic(Instant.now());
        long unreadCount = notificationRepository.countByRecipient_IdAndReadAtIsNull(AUTHOR_ID);
        int markedRead = notificationRepository.markAllRead(AUTHOR_ID, Instant.now());

        assertThat(publicNews.getTotalElements()).isEqualTo(1);
        assertThat(adminContacts.getTotalElements()).isEqualTo(1);
        assertThat(publicBanners).extracting("id").contains(BANNER_ID);
        assertThat(unreadCount).isEqualTo(1);
        assertThat(markedRead).isEqualTo(1);
        assertThat(notificationRepository.countByRecipient_IdAndReadAtIsNull(AUTHOR_ID)).isZero();
    }

    private void insertAccounts() {
        jdbc.update("""
                insert into accounts (id, email, password_hash, first_name, last_name, email_verified)
                values (?, 'content-author@example.com', 'hash', 'Content', 'Author', true)
                on conflict (id) do nothing
                """, AUTHOR_ID);
        jdbc.update("""
                insert into accounts (id, email, password_hash, first_name, last_name, email_verified)
                values (?, 'content-handler@example.com', 'hash', 'Content', 'Handler', true)
                on conflict (id) do nothing
                """, HANDLER_ID);
    }

    private void insertContentRows() {
        jdbc.update("""
                insert into news (id, author_account_id, title, slug, summary, content, status, published_at)
                values (?, ?, 'Mapping News', 'mapping-news', 'Mapping summary', 'Mapping body', 'PUBLISHED', now())
                on conflict (id) do nothing
                """, NEWS_ID, AUTHOR_ID);
        jdbc.update("""
                insert into banners (id, title, subtitle, image_url, link_url, link_type, position, active)
                values (?, 'Mapping Banner', 'Subtitle', '/banner.png', '/news/mapping-news', 'URL', 73, true)
                on conflict (id) do nothing
                """, BANNER_ID);
        jdbc.update("""
                insert into contact_messages (
                    id, account_id, name, email, subject, message, status, handled_by_account_id, note, ip_address, user_agent
                )
                values (?, ?, 'Guest User', 'guest@example.com', 'Help', 'Need help', 'IN_PROGRESS', ?, 'Handling', '127.0.0.1', 'JUnit')
                on conflict (id) do nothing
                """, CONTACT_ID, AUTHOR_ID, HANDLER_ID);
        jdbc.update("""
                insert into notifications (id, recipient_account_id, type, title, body, link_url)
                values (?, ?, 'SYSTEM', 'Hello', 'Body', '/notifications')
                on conflict (id) do nothing
                """, NOTIFICATION_ID, AUTHOR_ID);
    }
}
