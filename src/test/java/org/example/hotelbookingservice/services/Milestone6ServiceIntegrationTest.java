package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.config.UploadProperties;
import org.example.hotelbookingservice.services.IFileStorageService;

import org.example.hotelbookingservice.dto.request.content.BannerMutationRequest;
import org.example.hotelbookingservice.dto.request.content.CommissionPackageRequest;
import org.example.hotelbookingservice.dto.request.content.ContactCreateRequest;
import org.example.hotelbookingservice.dto.request.content.ContactUpdateRequest;
import org.example.hotelbookingservice.dto.request.content.NewsMutationRequest;
import org.example.hotelbookingservice.dto.request.content.PolicyMutationRequest;
import org.example.hotelbookingservice.dto.request.review.ReviewModerationRequest;
import org.example.hotelbookingservice.dto.request.review.ReviewRequest;
import org.example.hotelbookingservice.security.AccountAuthUser;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class Milestone6SplitServicesIntegrationTest {
    private static final UUID ADMIN_ID = UUID.fromString("40000000-0000-4000-8000-000000000001");
    private static final UUID OWNER_ID = UUID.fromString("40000000-0000-4000-8000-000000000002");
    private static final UUID CUSTOMER_ID = UUID.fromString("40000000-0000-4000-8000-000000000003");
    private static final UUID OUTSIDER_ID = UUID.fromString("40000000-0000-4000-8000-000000000004");
    private static final UUID HOTEL_ID = UUID.fromString("40000000-0000-4000-8000-000000000101");
    private static final UUID ROOM_TYPE_ID = UUID.fromString("40000000-0000-4000-8000-000000000201");
    private static final UUID BOOKING_ID = UUID.fromString("40000000-0000-4000-8000-000000000301");

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("hotel_booking_service_m6_test")
            .withUsername("test")
            .withPassword("test");

    static JdbcTemplate jdbc;
    static MediaServiceImpl mediaService;
    static ReviewOperationsServiceImpl reviewService;
    static ContentServiceImpl contentService;
    static DashboardServiceImpl dashboardService;

    @BeforeAll
    static void migrate() {
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();

        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );
        jdbc = new JdbcTemplate(dataSource);
        NamedParameterJdbcTemplate namedJdbc = new NamedParameterJdbcTemplate(dataSource);
        StubFileStorageService storageService = new StubFileStorageService();
        UploadProperties uploadProperties = new UploadProperties(null, 12);
        mediaService = new MediaServiceImpl(namedJdbc, storageService, uploadProperties);
        reviewService = new ReviewOperationsServiceImpl(namedJdbc, storageService, uploadProperties);
        contentService = new ContentServiceImpl(namedJdbc, storageService, uploadProperties);
        dashboardService = new DashboardServiceImpl(namedJdbc, storageService, uploadProperties);
    }

    @BeforeEach
    void resetData() {
        jdbc.update("delete from notifications");
        jdbc.update("delete from contact_messages");
        jdbc.update("delete from review_images");
        jdbc.update("delete from reviews");
        jdbc.update("delete from payments");
        jdbc.update("delete from booking_items");
        jdbc.update("delete from bookings");
        jdbc.update("delete from hotel_policies");
        jdbc.update("delete from banner_images");
        jdbc.update("delete from banners");
        jdbc.update("delete from news_images");
        jdbc.update("delete from news");
        jdbc.update("delete from room_type_images");
        jdbc.update("delete from hotel_images");
        jdbc.update("delete from gallery_images");
        jdbc.update("delete from gallery_folders");
        jdbc.update("delete from image_assets");
        jdbc.update("delete from rooms");
        jdbc.update("delete from room_types");
        jdbc.update("delete from hotel_members");
        jdbc.update("delete from hotel_commission_packages");
        jdbc.update("delete from hotels");
        jdbc.update("delete from account_roles");
        jdbc.update("delete from accounts");
        jdbc.update("delete from commission_packages where not is_system");

        insertAccount(ADMIN_ID, "admin@example.com");
        insertAccount(OWNER_ID, "owner@example.com");
        insertAccount(CUSTOMER_ID, "customer@example.com");
        insertAccount(OUTSIDER_ID, "outsider@example.com");
        assignRole(ADMIN_ID, "ADMIN");
        assignRole(OWNER_ID, "OWNER");
        assignRole(CUSTOMER_ID, "CUSTOMER");
        assignRole(OUTSIDER_ID, "CUSTOMER");
        insertHotel();
        insertCompletedBooking();
    }

      @Test
      void reviewEligibility_whenCompletedStayIsUnreviewed_returnsBookingUntilReviewCreated() {
          var eligible = reviewService.reviewEligibility(HOTEL_ID, customerAuth());

          assertThat(eligible.canReview()).isTrue();
          assertThat(eligible.bookingId()).isEqualTo(BOOKING_ID);
          assertThat(eligible.reason()).isEqualTo("ELIGIBLE");

          reviewService.createReview(
                  HOTEL_ID,
                  new ReviewRequest(BOOKING_ID, BigDecimal.valueOf(5), "Excellent stay", List.of()),
                  customerAuth()
          );

          var reviewed = reviewService.reviewEligibility(HOTEL_ID, customerAuth());
          assertThat(reviewed.canReview()).isFalse();
          assertThat(reviewed.bookingId()).isNull();
          assertThat(reviewed.reason()).isEqualTo("ALL_STAYS_REVIEWED");
      }

      @Test
      void uploadsGallerySnapshotsAndReviewsEnforceOwnershipAndVisibility() {
        var image = mediaService.upload(imageFile("hotel.png"), ownerAuth());
        var folder = mediaService.createGalleryFolder("hotel-gallery", ownerAuth());
        var uploaded = mediaService.uploadGalleryImages(folder.folderName(), List.of(imageFile("gallery.png")), ownerAuth());

        assertThat(image.provider()).isEqualTo("LOCAL");
        assertThat(image.url()).startsWith("/api/v1/uploads/local/");
        assertThat(mediaService.listGalleryImages(folder.id(), ownerAuth())).hasSize(1);

        assertThatThrownBy(() -> mediaService.replaceHotelImages(HOTEL_ID, List.of(uploaded.getFirst().id()), outsiderAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403 FORBIDDEN");

        assertThat(mediaService.replaceHotelImages(HOTEL_ID, List.of(uploaded.getFirst().id()), ownerAuth()))
                .extracting(snapshot -> snapshot.sortOrder())
                .containsExactly(0);
        assertThat(mediaService.replaceRoomTypeImages(HOTEL_ID, ROOM_TYPE_ID, List.of(uploaded.getFirst().id()), ownerAuth()))
                .hasSize(1);

        var customerImage = mediaService.upload(imageFile("review.png"), customerAuth());
        var review = reviewService.createReview(HOTEL_ID, new ReviewRequest(BOOKING_ID, BigDecimal.valueOf(4.5), "Great stay", List.of(customerImage.id())), customerAuth());
        assertThat(review.images()).hasSize(1);
        assertThat(reviewService.visibleRatingSummary(HOTEL_ID).reviewCount()).isEqualTo(1);
        assertThatThrownBy(() -> reviewService.createReview(HOTEL_ID, new ReviewRequest(BOOKING_ID, BigDecimal.valueOf(5), "Again", List.of()), customerAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("This booking has already been reviewed");

          reviewService.moderateReview(HOTEL_ID, review.id(), new ReviewModerationRequest(false), ownerAuth());
          assertThat(reviewService.listPublicReviews(HOTEL_ID, 1, 10).data()).isEmpty();
          assertThat(reviewService.listModerationReviews(HOTEL_ID, 1, 10, ownerAuth()).data()).hasSize(1);
          reviewService.deleteReview(HOTEL_ID, review.id(), ownerAuth());
          assertThat(reviewService.listModerationReviews(HOTEL_ID, 1, 10, ownerAuth()).data()).isEmpty();
      }

    @Test
    void contentContactsNotificationsReportsCommissionsAndPoliciesWorkTogether() {
        var image = mediaService.upload(imageFile("content.png"), adminAuth());

        var news = contentService.createNews(new NewsMutationRequest("Launch News", "Summary", "Body", "PUBLISHED", List.of(image.id())), adminAuth());
        assertThat(news.slug()).isEqualTo("launch-news");
        assertThat(contentService.listNewsPublic(null, 1, 10).data()).extracting(item -> item.id()).contains(news.id());

        var ownerNews = contentService.createNews(new NewsMutationRequest("Owner News", "Summary", "Body", "DRAFT", List.of()), ownerAuth());
        assertThat(contentService.listNewsAdmin("DRAFT", null, 1, 10, ownerAuth()).data())
                .extracting(item -> item.id()).contains(ownerNews.id());
        assertThat(contentService.updateNews(ownerNews.id(), new NewsMutationRequest(null, "Updated", null, "PUBLISHED", null), ownerAuth()).status())
                .isEqualTo("PUBLISHED");
        contentService.deleteNews(ownerNews.id(), ownerAuth());
        assertThatThrownBy(() -> contentService.newsDetailAdmin(ownerNews.id(), ownerAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("News not found");

        var banner = contentService.createBanner(new BannerMutationRequest(
                "Hero", "Subtitle", "/hotels/" + HOTEL_ID, "HOTEL", 1, true, null, null, List.of(image.id()), null
        ), adminAuth());
        assertThat(contentService.listPublicBanners()).extracting(item -> item.id()).contains(banner.id());
        assertThatThrownBy(() -> contentService.createBanner(new BannerMutationRequest(
                "Duplicate", null, "#hotels", "URL", 1, true, null, null, List.of(image.id()), null
        ), adminAuth())).isInstanceOf(ResponseStatusException.class).hasMessageContaining("Banner position already exists");

        var contact = contentService.createContact(new ContactCreateRequest("Guest", "guest@example.com", null, "Help", "Need support"), "127.0.0.1", "JUnit", null);
        assertThat(contact.ok()).isTrue();
        assertThat(contentService.listNotifications(1, 10, adminAuth()).data()).hasSize(1);
        contentService.updateContact(contact.id(), new ContactUpdateRequest("IN_PROGRESS", ADMIN_ID, "Handling"), adminAuth());

        assertThat(dashboardService.dashboardStats(null, adminAuth()).revenue()).isEqualByComparingTo("1000.00");

        var commission = contentService.createCommissionPackage(new CommissionPackageRequest("demo", "Demo", "Demo package", BigDecimal.valueOf(0.12), true), adminAuth());
        assertThatThrownBy(() -> contentService.updateCommissionPackage(commission.id(), new CommissionPackageRequest("other", "Demo", null, null, true), adminAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("immutable");
        assertThat(contentService.assignCommissionPackage(HOTEL_ID, commission.id(), adminAuth()).packageCode()).isEqualTo("DEMO");

          assertThatThrownBy(() -> contentService.listPoliciesAdmin(HOTEL_ID, ownerAuth()))
                  .isInstanceOf(ResponseStatusException.class)
                  .hasMessageContaining("403 FORBIDDEN");
          var policy = contentService.createPolicy(HOTEL_ID, new PolicyMutationRequest("CHECKIN", "Check-in", "From 14:00", true, 1), adminAuth());
          assertThat(policy.type()).isEqualTo("CHECKIN");
          assertThatThrownBy(() -> contentService.createPolicy(HOTEL_ID, new PolicyMutationRequest("PAYMENT", "Payment", "Pay now", true, 1), adminAuth()))
                  .isInstanceOf(ResponseStatusException.class)
                  .hasMessageContaining("Order 1 is already taken in this hotel");
          assertThat(contentService.deletePolicy(HOTEL_ID, policy.id(), adminAuth()).deletedAt()).isNull();
          assertThat(contentService.createPolicy(HOTEL_ID, new PolicyMutationRequest("CHECKIN", "Check-in again", "From 15:00", true, 1), adminAuth()).id())
                  .isNotEqualTo(policy.id());
    }

    private void insertAccount(UUID id, String email) {
        jdbc.update("""
                insert into accounts (id, email, first_name, last_name, email_verified, auth_provider)
                values (?, ?, 'Test', 'User', true, 'LOCAL')
                """, id, email);
    }

    private void assignRole(UUID accountId, String roleName) {
        jdbc.update("""
                insert into account_roles (account_id, role_id)
                select ?, id from roles where name = ?
                """, accountId, roleName);
    }

    private void insertHotel() {
        jdbc.update("""
                insert into hotels (id, owner_id, name, slug, city, country, status)
                values (?, ?, 'Review Hotel', 'review-hotel', 'HCMC', 'Vietnam', 'ACTIVE')
                """, HOTEL_ID, OWNER_ID);
        jdbc.update("insert into hotel_members (hotel_id, account_id) values (?, ?)", HOTEL_ID, OWNER_ID);
        jdbc.update("""
                insert into room_types (id, hotel_id, name, price_per_night, max_guests, number_of_bedrooms, active)
                values (?, ?, 'Suite', 1000, 2, 1, true)
                """, ROOM_TYPE_ID, HOTEL_ID);
    }

    private void insertCompletedBooking() {
        jdbc.update("""
                insert into bookings (id, account_id, hotel_id, booking_reference, status, check_in, check_out, guest_name, guest_email, guest_phone, subtotal_amount, total_amount, commission_rate, commission_amount, completed_at)
                values (?, ?, ?, 'M6-BOOKING', 'COMPLETED', current_date - 3, current_date - 2, 'Customer', 'customer@example.com', '090', 1000, 1000, 0, 0, now())
                """, BOOKING_ID, CUSTOMER_ID, HOTEL_ID);
        jdbc.update("""
                insert into booking_items (id, booking_id, room_type_id, room_type_name, quantity, unit_price, max_guests, line_total)
                values (?, ?, ?, 'Suite', 1, 1000, 2, 1000)
                """, UUID.randomUUID(), BOOKING_ID, ROOM_TYPE_ID);
        jdbc.update("""
                insert into payments (id, booking_id, provider, status, amount, merchant_txn_ref, paid_at)
                values (?, ?, 'VNPAY', 'SUCCEEDED', 1000, 'M6-PAYMENT', now())
                """, UUID.randomUUID(), BOOKING_ID);
    }

    private MockMultipartFile imageFile(String name) {
        return new MockMultipartFile("file", name, "image/png", new byte[]{1, 2, 3});
    }

    private Authentication adminAuth() {
        return auth(ADMIN_ID, "ADMIN");
    }

    private Authentication ownerAuth() {
        return auth(OWNER_ID, "OWNER");
    }

    private Authentication customerAuth() {
        return auth(CUSTOMER_ID, "CUSTOMER");
    }

    private Authentication outsiderAuth() {
        return auth(OUTSIDER_ID, "CUSTOMER");
    }

    private Authentication auth(UUID accountId, String role) {
        AccountAuthUser user = AccountAuthUser.builder()
                .accountId(accountId)
                .email(accountId + "@example.com")
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private static final class StubFileStorageService implements IFileStorageService {
        @Override
        public String uploadFile(MultipartFile file) {
            return "https://cdn.example.test/" + file.getOriginalFilename();
        }

        @Override
        public List<String> uploadFiles(List<MultipartFile> files) {
            return files.stream().map(this::uploadFile).toList();
        }

        @Override
        public void validateImageFile(MultipartFile file) {
        }

        @Override
        public void validateImageFiles(List<MultipartFile> files) {
        }
    }
}
