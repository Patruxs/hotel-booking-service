package org.example.hotelbookingservice.repository;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ImageRelationJpaMappingTest {
    private static final UUID ACCOUNT_ID = UUID.fromString("76000000-0000-4000-8000-000000000001");
    private static final UUID HOTEL_ID = UUID.fromString("76000000-0000-4000-8000-000000000002");
    private static final UUID ROOM_TYPE_ID = UUID.fromString("76000000-0000-4000-8000-000000000003");
    private static final UUID BOOKING_ID = UUID.fromString("76000000-0000-4000-8000-000000000004");
    private static final UUID REVIEW_ID = UUID.fromString("76000000-0000-4000-8000-000000000005");
    private static final UUID NEWS_ID = UUID.fromString("76000000-0000-4000-8000-000000000006");
    private static final UUID BANNER_ID = UUID.fromString("76000000-0000-4000-8000-000000000007");
    private static final UUID IMAGE_ASSET_ID = UUID.fromString("76000000-0000-4000-8000-000000000008");
    private static final UUID HOTEL_IMAGE_ID = UUID.fromString("76000000-0000-4000-8000-000000000009");
    private static final UUID ROOM_TYPE_IMAGE_ID = UUID.fromString("76000000-0000-4000-8000-000000000010");
    private static final UUID REVIEW_IMAGE_ID = UUID.fromString("76000000-0000-4000-8000-000000000011");
    private static final UUID NEWS_IMAGE_ID = UUID.fromString("76000000-0000-4000-8000-000000000012");
    private static final UUID BANNER_IMAGE_ID = UUID.fromString("76000000-0000-4000-8000-000000000013");

    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManagerFactory entityManagerFactory;
    @Autowired HotelImageRepository hotelImageRepository;
    @Autowired RoomTypeImageRepository roomTypeImageRepository;
    @Autowired ReviewImageRepository reviewImageRepository;
    @Autowired NewsImageRepository newsImageRepository;
    @Autowired BannerImageRepository bannerImageRepository;

    @Test
    void imageRelationEntitiesLoadAgainstFlywaySchemaWithLazyImageAssets() {
        insertParents();
        insertImageRows();
        var persistenceUnit = entityManagerFactory.getPersistenceUnitUtil();

        var hotelImage = hotelImageRepository.findByHotel_IdOrderBySortOrderAsc(HOTEL_ID).getFirst();
        var roomTypeImage = roomTypeImageRepository.findByRoomType_IdOrderBySortOrderAsc(ROOM_TYPE_ID).getFirst();
        var reviewImage = reviewImageRepository.findByReview_IdOrderBySortOrderAsc(REVIEW_ID).getFirst();
        var newsImage = newsImageRepository.findByNews_IdOrderBySortOrderAsc(NEWS_ID).getFirst();
        var bannerImage = bannerImageRepository.findByBanner_IdOrderBySortOrderAsc(BANNER_ID).getFirst();

        assertThat(hotelImage.getId()).isEqualTo(HOTEL_IMAGE_ID);
        assertThat(hotelImage.getHotelId()).isEqualTo(HOTEL_ID);
        assertThat(hotelImage.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(hotelImage.getUrl()).isEqualTo("/hotel.png");
        assertThat(hotelImage.getSortOrder()).isZero();
        assertThat(persistenceUnit.isLoaded(hotelImage, "hotel")).isFalse();
        assertThat(persistenceUnit.isLoaded(hotelImage, "imageAsset")).isFalse();

        assertThat(roomTypeImage.getRoomTypeId()).isEqualTo(ROOM_TYPE_ID);
        assertThat(roomTypeImage.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(roomTypeImage.getUrl()).isEqualTo("/room.png");
        assertThat(persistenceUnit.isLoaded(roomTypeImage, "roomType")).isFalse();
        assertThat(persistenceUnit.isLoaded(roomTypeImage, "imageAsset")).isFalse();

        assertThat(reviewImage.getReviewId()).isEqualTo(REVIEW_ID);
        assertThat(reviewImage.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(reviewImage.getUrl()).isEqualTo("/review.png");
        assertThat(persistenceUnit.isLoaded(reviewImage, "review")).isFalse();
        assertThat(persistenceUnit.isLoaded(reviewImage, "imageAsset")).isFalse();

        assertThat(newsImage.getNewsId()).isEqualTo(NEWS_ID);
        assertThat(newsImage.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(newsImage.getUrl()).isEqualTo("/news.png");
        assertThat(persistenceUnit.isLoaded(newsImage, "news")).isFalse();
        assertThat(persistenceUnit.isLoaded(newsImage, "imageAsset")).isFalse();

        assertThat(bannerImage.getBannerId()).isEqualTo(BANNER_ID);
        assertThat(bannerImage.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(bannerImage.getUrl()).isEqualTo("/banner.png");
        assertThat(persistenceUnit.isLoaded(bannerImage, "banner")).isFalse();
        assertThat(persistenceUnit.isLoaded(bannerImage, "imageAsset")).isFalse();
    }

    private void insertParents() {
        jdbc.update("""
                insert into accounts (id, email, password_hash, first_name, last_name, email_verified)
                values (?, 'image-relations@example.com', 'hash', 'Image', 'Relations', true)
                on conflict (id) do nothing
                """, ACCOUNT_ID);
        jdbc.update("""
                insert into hotels (id, owner_id, name, slug, country, status)
                values (?, ?, 'Image Relation Hotel', 'image-relation-hotel', 'Vietnam', 'ACTIVE')
                on conflict (id) do nothing
                """, HOTEL_ID, ACCOUNT_ID);
        jdbc.update("""
                insert into room_types (id, hotel_id, name, price_per_night, max_guests)
                values (?, ?, 'Image Relation Room', 100000, 2)
                on conflict (id) do nothing
                """, ROOM_TYPE_ID, HOTEL_ID);
        jdbc.update("""
                insert into bookings (
                    id, account_id, hotel_id, booking_reference, status, check_in, check_out, guest_name,
                    guest_email, guest_phone, subtotal_amount, total_amount
                )
                values (?, ?, ?, 'IMG-REL-001', 'COMPLETED', current_date, current_date + 1,
                    'Image Guest', 'guest@example.com', '0900000000', 100000, 100000)
                on conflict (id) do nothing
                """, BOOKING_ID, ACCOUNT_ID, HOTEL_ID);
        jdbc.update("""
                insert into reviews (id, booking_id, hotel_id, account_id, rating, comment, visible)
                values (?, ?, ?, ?, 4.5, 'Great', true)
                on conflict (id) do nothing
                """, REVIEW_ID, BOOKING_ID, HOTEL_ID, ACCOUNT_ID);
        jdbc.update("""
                insert into news (id, author_account_id, title, slug, summary, content, status, published_at)
                values (?, ?, 'Image Relation News', 'image-relation-news', 'Summary', 'Body', 'PUBLISHED', now())
                on conflict (id) do nothing
                """, NEWS_ID, ACCOUNT_ID);
        jdbc.update("""
                insert into banners (id, title, subtitle, image_url, link_url, link_type, position, active)
                values (?, 'Image Relation Banner', 'Subtitle', '/banner.png', '/news/image-relation-news', 'URL', 76, true)
                on conflict (id) do nothing
                """, BANNER_ID);
        jdbc.update("""
                insert into image_assets (id, owner_account_id, provider, public_id, url, secure_url, width, height, bytes)
                values (?, ?, 'LOCAL', 'local/image-relation', '/asset.png', '/asset.png', 1200, 800, 1024)
                on conflict (id) do nothing
                """, IMAGE_ASSET_ID, ACCOUNT_ID);
    }

    private void insertImageRows() {
        jdbc.update("""
                insert into hotel_images (id, hotel_id, image_asset_id, url, sort_order)
                values (?, ?, ?, '/hotel.png', 0)
                on conflict (id) do nothing
                """, HOTEL_IMAGE_ID, HOTEL_ID, IMAGE_ASSET_ID);
        jdbc.update("""
                insert into room_type_images (id, room_type_id, image_asset_id, url, sort_order)
                values (?, ?, ?, '/room.png', 0)
                on conflict (id) do nothing
                """, ROOM_TYPE_IMAGE_ID, ROOM_TYPE_ID, IMAGE_ASSET_ID);
        jdbc.update("""
                insert into review_images (id, review_id, image_asset_id, url, sort_order)
                values (?, ?, ?, '/review.png', 0)
                on conflict (id) do nothing
                """, REVIEW_IMAGE_ID, REVIEW_ID, IMAGE_ASSET_ID);
        jdbc.update("""
                insert into news_images (id, news_id, image_asset_id, url, sort_order)
                values (?, ?, ?, '/news.png', 0)
                on conflict (id) do nothing
                """, NEWS_IMAGE_ID, NEWS_ID, IMAGE_ASSET_ID);
        jdbc.update("""
                insert into banner_images (id, banner_id, image_asset_id, url, sort_order)
                values (?, ?, ?, '/banner.png', 0)
                on conflict (id) do nothing
                """, BANNER_IMAGE_ID, BANNER_ID, IMAGE_ASSET_ID);
    }
}
