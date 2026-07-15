package org.example.hotelbookingservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Optional, repeatable data set for local feature demonstrations.
 *
 * <p>The fixture runs in local and development environments, or explicitly with
 * {@code SPRING_PROFILES_ACTIVE=demo-seed}. It uses stable UUIDs and upserts, so
 * it is safe to run repeatedly. It is deliberately not enabled in production profiles.</p>
 */
@Component
@Profile("!legacy-demo-bootstrap & (local | dev | demo-seed)")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private UUID adminId = id("0001");
    private UUID ownerId = id("0002");
    private UUID managerId = id("0003");
    private UUID receptionistId = id("0004");
    private UUID customerId = id("0005");
    private UUID secondCustomerId = id("0006");

    private static final UUID CITY_HOTEL_ID = id("0101");
    private static final UUID BEACH_HOTEL_ID = id("0102");
    private static final UUID HERITAGE_HOTEL_ID = id("0103");
    private static final UUID GRAND_SAPPHIRE_HOTEL_ID = UUID.fromString("c0a8ae4a-cc3e-4af0-b336-c9bebc784f32");
    private static final UUID URBAN_EDGE_HOTEL_ID = UUID.fromString("c1b392be-30e5-4c11-ac3c-c552a0974d8b");
    private static final UUID MOUNTAIN_VIEW_LODGE_ID = UUID.fromString("6f3924c0-61c6-4c86-8bdc-b1f468e04468");
    private static final UUID DELUXE_ROOM_TYPE_ID = id("0201");
    private static final UUID SUITE_ROOM_TYPE_ID = id("0202");
    private static final UUID BEACH_ROOM_TYPE_ID = id("0203");
    private static final UUID ACTIVE_PROMOTION_ID = id("0301");
    private static final UUID EXPIRED_PROMOTION_ID = id("0302");
    private static final UUID STANDARD_PACKAGE_ID = UUID.fromString("00000000-0000-4004-8000-000000000001");
    private static final UUID PREMIUM_PACKAGE_ID = UUID.fromString("00000000-0000-4004-8000-000000000002");
    private static final List<String> ADDITIONAL_GALLERY_IMAGE_FILES = List.of(
            "small_mark-champs-Id2IIl1jOB0-unsplash.jpg",
            "small_mp-fV2dM2WvKvE-unsplash.jpg",
            "small_mr-junaid--3ohj90OT8o-unsplash.jpg",
            "small_natalia-gusakova-EYoK3eVKIiQ-unsplash.jpg",
            "small_orva-studio-YC8qqp50BdA-unsplash.jpg",
            "small_oswald-elsaboath-ym_EI-DTS1g-unsplash.jpg",
            "small_patrick-robert-doyle-AH8zKXqFITA-unsplash.jpg",
            "small_point3d-commercial-imaging-ltd-oxeCZrodz78-unsplash.jpg",
            "small_reagan-m-d-eWGvLCZfQ-unsplash.jpg",
            "small_rktkn-ssOtyGE8CyE-unsplash.jpg",
            "small_sara-dubler-Koei_7yYtIo-unsplash.jpg",
            "small_sasha-kaunas-67-sOi7mVIk-unsplash (1).jpg",
            "small_sasha-kaunas-67-sOi7mVIk-unsplash.jpg",
            "small_sasha-kaunas-TAgGZWz6Qg8-unsplash.jpg",
            "small_valeriia-bugaiova-_pPHgeHz1uk-unsplash.jpg",
            "small_vojtech-bruzek-Yrxr3bsPdS0-unsplash.jpg"
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Seeding comprehensive local demo data");
        seedAccountsAndRoles();
        seedCommercialData();
        seedHotelsAndInventory();
        seedPromotions();
        seedBookingsAndPayments();
        seedMediaAndContent();
        seedCustomerCommunication();
        log.info("Local demo data is ready. Accounts: admin@gmail.com/admin123, owner@demo.local/owner123, customer@gmail.com/customer123");
    }

    private void seedAccountsAndRoles() {
        adminId = upsertAccount(adminId, "admin@gmail.com", "Admin", "User", "0900000001", "admin123");
        ownerId = upsertAccount(ownerId, "owner@demo.local", "Linh", "Nguyen", "0900000002", "owner123");
        managerId = upsertAccount(managerId, "manager@demo.local", "Minh", "Tran", "0900000003", "staff123");
        receptionistId = upsertAccount(receptionistId, "receptionist@demo.local", "An", "Le", "0900000004", "staff123");
        customerId = upsertAccount(customerId, "customer@gmail.com", "Customer", "User", "0900000005", "customer123");
        secondCustomerId = upsertAccount(secondCustomerId, "traveler@demo.local", "Mai", "Pham", "0900000006", "customer123");

        assignRole(adminId, "ADMIN");
        // Older local snapshots used the manager account for the owner fixture.
        // Correct that deterministic fixture without changing unrelated accounts.
        removeRole(ownerId, "MANAGER");
        assignRole(ownerId, "OWNER");
        assignRole(managerId, "MANAGER");
        assignRole(receptionistId, "RECEPTIONIST");
        assignRole(customerId, "CUSTOMER");
        assignRole(secondCustomerId, "CUSTOMER");
    }

    private void seedCommercialData() {
        upsertCommissionPackage(STANDARD_PACKAGE_ID, "STANDARD", "Standard Commission",
                "Default commission package for demo hotels.", new BigDecimal("0.1000"));
        upsertCommissionPackage(PREMIUM_PACKAGE_ID, "PREMIUM", "Premium Commission",
                "Premium support and reporting package.", new BigDecimal("0.1500"));
    }

    private void seedHotelsAndInventory() {
        upsertHotel(CITY_HOTEL_ID, ownerId, "Grand City Hotel", "grand-city-hotel",
                "A modern city hotel with a rooftop pool and quick access to central Ho Chi Minh City.",
                "1 Nguyen Hue, District 1", "Ho Chi Minh City", new BigDecimal("4.5"));
        upsertHotel(BEACH_HOTEL_ID, ownerId, "Grand Beach Resort", "grand-beach-resort",
                "A relaxed beach resort for couples and families, steps from My Khe Beach.",
                "88 Vo Nguyen Giap", "Da Nang", new BigDecimal("4.8"));
        upsertHotel(HERITAGE_HOTEL_ID, ownerId, "Grand Heritage House", "grand-heritage-house",
                "A quiet boutique stay inspired by Hoi An's historic townhouses.",
                "22 Tran Phu", "Hoi An", new BigDecimal("4.2"));

        assignHotelPackage(CITY_HOTEL_ID, STANDARD_PACKAGE_ID);
        assignHotelPackage(BEACH_HOTEL_ID, PREMIUM_PACKAGE_ID);
        assignHotelPackage(HERITAGE_HOTEL_ID, STANDARD_PACKAGE_ID);
        for (UUID hotelId : List.of(CITY_HOTEL_ID, BEACH_HOTEL_ID, HERITAGE_HOTEL_ID)) {
            // NOTE: admin hotel membership is kept only for realistic demo data. Since
            // grant-admin-full-access, ADMIN bypasses the fine-grained permission gates,
            // so this membership row is no longer load-bearing for admin authorization.
            addHotelMember(hotelId, adminId);
            addHotelMember(hotelId, ownerId);
            addHotelMember(hotelId, managerId);
            addHotelMember(hotelId, receptionistId);
        }

        upsertAmenity("wifi", "Free Wi-Fi", "HOTEL_SERVICE");
        upsertAmenity("pool", "Swimming pool", "HOTEL_SERVICE");
        upsertAmenity("breakfast", "Breakfast included", "HOTEL_SERVICE");
        upsertAmenity("parking", "Private parking", "HOTEL_SERVICE");
        upsertAmenity("ocean-view", "Ocean view", "ROOM_FEATURE");
        upsertAmenity("bathtub", "Bathtub", "ROOM_FEATURE");
        upsertAmenity("balcony", "Private balcony", "ROOM_FEATURE");

        upsertRoomType(DELUXE_ROOM_TYPE_ID, CITY_HOTEL_ID, "Deluxe City View", 1_200_000, 2, 1,
                "A bright king room with city skyline views.");
        upsertRoomType(SUITE_ROOM_TYPE_ID, CITY_HOTEL_ID, "Family Suite", 2_400_000, 4, 2,
                "Two bedrooms, a living room, and space for the whole family.");
        upsertRoomType(BEACH_ROOM_TYPE_ID, BEACH_HOTEL_ID, "Oceanfront Bungalow", 2_800_000, 3, 1,
                "A private bungalow with a balcony overlooking the sea.");

        UUID STANDARD_ROOM_TYPE_ID = id("0204");
        upsertRoomType(STANDARD_ROOM_TYPE_ID, CITY_HOTEL_ID, "Standard Double Room", 800_000, 2, 1,
                "A cozy room for budget travelers.");
        UUID PENTHOUSE_ROOM_TYPE_ID = id("0205");
        upsertRoomType(PENTHOUSE_ROOM_TYPE_ID, CITY_HOTEL_ID, "Penthouse Suite", 5_000_000, 4, 2,
                "Luxury suite on the top floor with panoramic views.");
        UUID GARDEN_ROOM_TYPE_ID = id("0206");
        upsertRoomType(GARDEN_ROOM_TYPE_ID, HERITAGE_HOTEL_ID, "Garden View Room", 1_500_000, 2, 1,
                "Peaceful room overlooking the historic garden.");
        UUID PREMIUM_VILLA_TYPE_ID = id("0207");
        upsertRoomType(PREMIUM_VILLA_TYPE_ID, BEACH_HOTEL_ID, "Premium Beach Villa", 4_500_000, 6, 3,
                "A spacious beachfront villa with a private pool and direct beach access.");
        UUID EXECUTIVE_ROOM_TYPE_ID = id("0208");
        upsertRoomType(EXECUTIVE_ROOM_TYPE_ID, CITY_HOTEL_ID, "Executive Club Room", 1_800_000, 2, 1,
                "A high-floor executive room with club lounge access.");

        addRoomAmenities(DELUXE_ROOM_TYPE_ID, List.of("wifi", "bathtub", "balcony"));
        addRoomAmenities(SUITE_ROOM_TYPE_ID, List.of("wifi", "bathtub", "balcony"));
        addRoomAmenities(BEACH_ROOM_TYPE_ID, List.of("wifi", "ocean-view", "balcony"));
        addRoomAmenities(STANDARD_ROOM_TYPE_ID, List.of("wifi"));
        addRoomAmenities(PENTHOUSE_ROOM_TYPE_ID, List.of("wifi", "bathtub", "balcony"));
        addRoomAmenities(GARDEN_ROOM_TYPE_ID, List.of("wifi", "bathtub"));
        addRoomAmenities(PREMIUM_VILLA_TYPE_ID, List.of("wifi", "ocean-view", "bathtub", "balcony"));
        addRoomAmenities(EXECUTIVE_ROOM_TYPE_ID, List.of("wifi", "bathtub"));

        upsertPhysicalRoom(id("0501"), CITY_HOTEL_ID, DELUXE_ROOM_TYPE_ID, "C101", "CLEAN", true);
        upsertPhysicalRoom(id("0502"), CITY_HOTEL_ID, DELUXE_ROOM_TYPE_ID, "C102", "DIRTY", true);
        upsertPhysicalRoom(id("0503"), CITY_HOTEL_ID, SUITE_ROOM_TYPE_ID, "S201", "MAINTENANCE", false);
        upsertPhysicalRoom(id("0504"), BEACH_HOTEL_ID, BEACH_ROOM_TYPE_ID, "B01", "CLEAN", true);
        upsertPhysicalRoom(id("0505"), CITY_HOTEL_ID, STANDARD_ROOM_TYPE_ID, "S101", "CLEAN", true);
        upsertPhysicalRoom(id("0506"), CITY_HOTEL_ID, STANDARD_ROOM_TYPE_ID, "S102", "CLEAN", true);
        upsertPhysicalRoom(id("0507"), CITY_HOTEL_ID, PENTHOUSE_ROOM_TYPE_ID, "P901", "CLEAN", true);
        upsertPhysicalRoom(id("0508"), HERITAGE_HOTEL_ID, GARDEN_ROOM_TYPE_ID, "G01", "CLEAN", true);
        upsertPhysicalRoom(id("0509"), BEACH_HOTEL_ID, PREMIUM_VILLA_TYPE_ID, "B02", "CLEAN", true);
        upsertPhysicalRoom(id("0510"), CITY_HOTEL_ID, EXECUTIVE_ROOM_TYPE_ID, "E801", "CLEAN", true);

        seedInventory(CITY_HOTEL_ID, DELUXE_ROOM_TYPE_ID, 6, 5);
        seedInventory(CITY_HOTEL_ID, SUITE_ROOM_TYPE_ID, 3, 2);
        seedInventory(BEACH_HOTEL_ID, BEACH_ROOM_TYPE_ID, 4, 3);
        seedInventory(CITY_HOTEL_ID, STANDARD_ROOM_TYPE_ID, 2, 2);
        seedInventory(CITY_HOTEL_ID, PENTHOUSE_ROOM_TYPE_ID, 1, 1);
        seedInventory(HERITAGE_HOTEL_ID, GARDEN_ROOM_TYPE_ID, 1, 1);
        seedInventory(BEACH_HOTEL_ID, PREMIUM_VILLA_TYPE_ID, 2, 2);
        seedInventory(CITY_HOTEL_ID, EXECUTIVE_ROOM_TYPE_ID, 4, 3);
    }

    private void seedPromotions() {
        upsertPromotion(ACTIVE_PROMOTION_ID, CITY_HOTEL_ID, "WELCOME15", "Welcome 15% Off", "PERCENT",
                15, 500_000, 1_000_000, 100, 1, 8, true, "now() - interval '7 days'", "now() + interval '90 days'");
        upsertPromotion(EXPIRED_PROMOTION_ID, BEACH_HOTEL_ID, "SUMMER2025", "Summer 2025", "FIXED",
                300_000, null, 1_500_000, 50, 1, 50, false, "now() - interval '400 days'", "now() - interval '300 days'");
    }

    private void seedBookingsAndPayments() {
        List<DemoBooking> bookings = List.of(
                new DemoBooking("DEMO-PENDING-001", customerId, CITY_HOTEL_ID, DELUXE_ROOM_TYPE_ID, "PENDING", 3, 5, 1_200_000, 0, null),
                new DemoBooking("DEMO-CONFIRMED-001", secondCustomerId, CITY_HOTEL_ID, SUITE_ROOM_TYPE_ID, "CONFIRMED", 10, 13, 2_400_000, 360_000, ACTIVE_PROMOTION_ID),
                new DemoBooking("DEMO-CHECKIN-001", customerId, BEACH_HOTEL_ID, BEACH_ROOM_TYPE_ID, "CHECKED_IN", -1, 2, 2_800_000, 0, null),
                new DemoBooking("DEMO-COMPLETED-001", customerId, CITY_HOTEL_ID, DELUXE_ROOM_TYPE_ID, "COMPLETED", -10, -8, 1_200_000, 180_000, ACTIVE_PROMOTION_ID),
                new DemoBooking("DEMO-CANCELLED-001", secondCustomerId, BEACH_HOTEL_ID, BEACH_ROOM_TYPE_ID, "CANCELLED", 20, 23, 2_800_000, 0, null),
                new DemoBooking("DEMO-NOSHOW-001", secondCustomerId, CITY_HOTEL_ID, SUITE_ROOM_TYPE_ID, "NO_SHOW", -20, -18, 2_400_000, 0, null)
        );
        for (int index = 0; index < bookings.size(); index++) {
            DemoBooking booking = bookings.get(index);
            UUID bookingId = id("06" + String.format("%02d", index + 1));
            UUID paymentId = id("07" + String.format("%02d", index + 1));
            BigDecimal subtotal = BigDecimal.valueOf(booking.nightlyPrice()).multiply(BigDecimal.valueOf(booking.nights()));
            BigDecimal discount = BigDecimal.valueOf(booking.discount());
            BigDecimal total = subtotal.subtract(discount);
            upsertBooking(bookingId, booking, subtotal, discount, total);
            upsertBookingItem(id("08" + String.format("%02d", index + 1)), bookingId, booking.roomTypeId(), booking, subtotal);
            upsertPayment(paymentId, bookingId, booking.reference(), total, booking.status());
            if (booking.status().equals("CHECKED_IN") || booking.status().equals("COMPLETED")) {
                upsertCheckIn(id("09" + String.format("%02d", index + 1)), bookingId, booking.status().equals("COMPLETED"));
            }
        }
        upsertReview(id("1001"), id("0604"), CITY_HOTEL_ID, customerId, "4.8", "Excellent location, friendly staff, and a very comfortable room.");
        upsertReview(id("1002"), id("0601"), CITY_HOTEL_ID, customerId, "4.0", "Good stay, but breakfast could be better.");
        upsertReview(id("1003"), id("0602"), CITY_HOTEL_ID, secondCustomerId, "5.0", "Amazing experience! The suite was perfect for our family.");
        upsertReview(id("1004"), id("0603"), BEACH_HOTEL_ID, customerId, "4.5", "Beautiful ocean views and very relaxing.");
        upsertReview(id("1005"), id("0605"), BEACH_HOTEL_ID, secondCustomerId, "3.5", "Nice beach, but the room was a bit small.");
        upsertPaymentEvent(id("1101"), id("0704"), "PAYMENT_SUCCEEDED", "{\"source\":\"demo-seed\",\"message\":\"Payment completed successfully\"}");
    }

    private void seedMediaAndContent() {
        UUID cityHotelImage = id("1201");
        UUID beachHotelImage = id("1202");
        UUID heritageHotelImage = id("1203");
        UUID deluxeRoomImage = id("1204");
        UUID suiteRoomImage = id("1205");
        UUID beachRoomImage = id("1206");
        UUID standardRoomImage = id("1207");
        UUID penthouseRoomImage = id("1208");
        UUID gardenRoomImage = id("1209");
        UUID directBookingNewsImage = id("1210");
        UUID beachesNewsImage = id("1211");
        UUID culinaryNewsImage = id("1212");

        upsertImageAsset(cityHotelImage, ownerId, "demo/city-hotel", cloudinaryImage("small_andrew-neel-B4rEJ09-Puo-unsplash.jpg"));
        upsertImageAsset(beachHotelImage, ownerId, "demo/beach-resort", cloudinaryImage("small_the-anam-_twiIcIsp2s-unsplash.jpg"));
        upsertImageAsset(heritageHotelImage, ownerId, "demo/heritage-house", cloudinaryImage("small_visualsofdana-T5pL6ciEn-I-unsplash.jpg"));
        upsertImageAsset(deluxeRoomImage, ownerId, "demo/deluxe-room", cloudinaryImage("small_brett-campbell-k1OlQaEK2qI-unsplash.jpg"));
        upsertImageAsset(suiteRoomImage, ownerId, "demo/family-suite", cloudinaryImage("small_paolo-nicolello-2gOxKj594nM-unsplash.jpg"));
        upsertImageAsset(beachRoomImage, ownerId, "demo/oceanfront-bungalow", cloudinaryImage("small_reisetopia-pSDe7ePo0Tc-unsplash.jpg"));
        upsertImageAsset(standardRoomImage, ownerId, "demo/standard-room", cloudinaryImage("small_bilderboken-rlwE8f8anOc-unsplash.jpg"));
        upsertImageAsset(penthouseRoomImage, ownerId, "demo/penthouse-suite", cloudinaryImage("small_dad-hotel-FCSDBKliyTQ-unsplash.jpg"));
        upsertImageAsset(gardenRoomImage, ownerId, "demo/garden-room", cloudinaryImage("small_frames-for-your-heart-zSG-kd-L6vw-unsplash.jpg"));
        upsertImageAsset(directBookingNewsImage, adminId, "demo/direct-booking-news", cloudinaryImage("small_cory-bjork-D1yT791Nf9A-unsplash.jpg"));
        upsertImageAsset(beachesNewsImage, adminId, "demo/beaches-news", cloudinaryImage("small_jeffrey-francisco-_Ei9f33bQ1A-unsplash.jpg"));
        upsertImageAsset(culinaryNewsImage, adminId, "demo/culinary-news", cloudinaryImage("small_jorg-angeli-1tyuLfDOnG0-unsplash.jpg"));

        linkHotelImage(id("1301"), CITY_HOTEL_ID, cityHotelImage, 0);
        linkHotelImage(id("1332"), CITY_HOTEL_ID, deluxeRoomImage, 1);
        linkHotelImage(id("1333"), CITY_HOTEL_ID, standardRoomImage, 2);
        linkHotelImage(id("1334"), CITY_HOTEL_ID, penthouseRoomImage, 3);
        linkHotelImage(id("1302"), BEACH_HOTEL_ID, beachHotelImage, 0);
        linkHotelImage(id("1335"), BEACH_HOTEL_ID, suiteRoomImage, 1);
        linkHotelImage(id("1336"), BEACH_HOTEL_ID, beachRoomImage, 2);
        linkHotelImage(id("1337"), BEACH_HOTEL_ID, gardenRoomImage, 3);
        linkHotelImage(id("1303"), HERITAGE_HOTEL_ID, heritageHotelImage, 0);
        linkHotelImage(id("1338"), HERITAGE_HOTEL_ID, gardenRoomImage, 1);
        linkHotelImage(id("1339"), HERITAGE_HOTEL_ID, standardRoomImage, 2);
        linkHotelImage(id("1340"), HERITAGE_HOTEL_ID, culinaryNewsImage, 3);
        linkRoomTypeImage(id("1304"), DELUXE_ROOM_TYPE_ID, deluxeRoomImage, 0);
        linkRoomTypeImage(id("1305"), SUITE_ROOM_TYPE_ID, suiteRoomImage, 0);
        linkRoomTypeImage(id("1306"), BEACH_ROOM_TYPE_ID, beachRoomImage, 0);
        linkRoomTypeImage(id("1307"), id("0204"), standardRoomImage, 0);
        linkRoomTypeImage(id("1308"), id("0205"), penthouseRoomImage, 0);
        linkRoomTypeImage(id("1309"), id("0206"), gardenRoomImage, 0);

        linkHotelImage(id("1320"), GRAND_SAPPHIRE_HOTEL_ID, beachHotelImage, 0);
        linkHotelImage(id("1321"), GRAND_SAPPHIRE_HOTEL_ID, suiteRoomImage, 1);
        linkHotelImage(id("1322"), GRAND_SAPPHIRE_HOTEL_ID, beachRoomImage, 2);
        linkHotelImage(id("1323"), GRAND_SAPPHIRE_HOTEL_ID, penthouseRoomImage, 3);
        linkHotelImage(id("1324"), URBAN_EDGE_HOTEL_ID, cityHotelImage, 0);
        linkHotelImage(id("1325"), URBAN_EDGE_HOTEL_ID, deluxeRoomImage, 1);
        linkHotelImage(id("1326"), URBAN_EDGE_HOTEL_ID, standardRoomImage, 2);
        linkHotelImage(id("1327"), URBAN_EDGE_HOTEL_ID, directBookingNewsImage, 3);
        linkHotelImage(id("1328"), MOUNTAIN_VIEW_LODGE_ID, heritageHotelImage, 0);
        linkHotelImage(id("1329"), MOUNTAIN_VIEW_LODGE_ID, gardenRoomImage, 1);
        linkHotelImage(id("1330"), MOUNTAIN_VIEW_LODGE_ID, beachesNewsImage, 2);
        linkHotelImage(id("1331"), MOUNTAIN_VIEW_LODGE_ID, culinaryNewsImage, 3);

        UUID galleryFolderId = id("1310");
            jdbcTemplate.update("""
                    insert into gallery_folders (id, owner_account_id, folder_name)
                    values (:id, :ownerId, 'Demo media')
                    on conflict (id) do update set owner_account_id = excluded.owner_account_id,
                        folder_name = excluded.folder_name, updated_at = now()
                    """, params().addValue("id", galleryFolderId).addValue("ownerId", ownerId));
        jdbcTemplate.update("""
                insert into gallery_images (id, folder_id, image_asset_id)
                values (:id, :folderId, :imageId)
                on conflict (folder_id, image_asset_id) do nothing
                """, params().addValue("id", id("1311")).addValue("folderId", galleryFolderId).addValue("imageId", cityHotelImage));
        seedAdditionalGalleryImages(galleryFolderId);

        upsertNews(id("1401"), "why-book-direct-demo", "Why book direct for your next stay",
                "A demo article for the public news and admin content features.", "PUBLISHED");
        upsertNews(id("1402"), "draft-hotel-guide-demo", "A hotel manager's local guide",
                "A draft article to demonstrate content moderation.", "DRAFT");
        upsertNews(id("1404"), "top-10-beaches", "Top 10 Beaches in Vietnam",
                "Discover the most beautiful beaches for your next vacation.", "PUBLISHED");
        upsertNews(id("1405"), "culinary-journey", "A Culinary Journey Through Hoi An",
                "Explore the unique flavors of Central Vietnam.", "PUBLISHED");

        linkNewsImage(id("1403"), id("1401"), directBookingNewsImage, 0);
        linkNewsImage(id("1406"), id("1404"), beachesNewsImage, 0);
        linkNewsImage(id("1407"), id("1405"), culinaryNewsImage, 0);

        upsertBanner(id("1501"), "Plan your city escape", "Use WELCOME15 for an instant demo discount.",
                cloudinaryImage("small_andrew-neel-B4rEJ09-Puo-unsplash.jpg"), "/hotels/" + CITY_HOTEL_ID, "HOTEL", 1);
        upsertPolicy(id("1601"), CITY_HOTEL_ID, "CHECK_IN", "Check-in and check-out", "Check-in from 14:00. Check-out before 12:00.", 1);
        upsertPolicy(id("1602"), CITY_HOTEL_ID, "CANCELLATION", "Cancellation policy", "Free cancellation until 48 hours before check-in.", 2);
        upsertPolicy(id("1603"), BEACH_HOTEL_ID, "PAYMENT", "Payment policy", "A valid payment method is required to confirm the reservation.", 1);
    }

    private void seedCustomerCommunication() {
        jdbcTemplate.update("""
                insert into contact_messages (id, account_id, name, email, phone, subject, message, status, handled_by_account_id, note)
                values (:id, :accountId, 'Customer User', 'customer@gmail.com', '0900000005',
                        'Airport transfer enquiry', 'Can I arrange an airport transfer for my stay?', 'IN_PROGRESS', :managerId,
                        'Demo message assigned to the manager.')
                on conflict (id) do update set status = excluded.status, handled_by_account_id = excluded.handled_by_account_id, updated_at = now()
                """, params().addValue("id", id("1701")).addValue("accountId", customerId).addValue("managerId", managerId));
        upsertNotification(id("1702"), customerId, "BOOKING_CONFIRMED", "Your demo booking is confirmed",
                "Your Family Suite is ready for the upcoming stay.", "/my-bookings");
        upsertNotification(id("1703"), managerId, "NEW_BOOKING", "New demo booking received",
                "Review the confirmed Family Suite reservation.", "/admin/bookings");
    }

    private UUID upsertAccount(UUID id, String email, String firstName, String lastName, String phone, String password) {
        return jdbcTemplate.queryForObject("""
                insert into accounts (id, email, password_hash, first_name, last_name, phone, email_verified, auth_provider)
                values (:id, :email, :password, :firstName, :lastName, :phone, true, 'LOCAL')
                on conflict (email) do update set password_hash = excluded.password_hash,
                    first_name = excluded.first_name, last_name = excluded.last_name, phone = excluded.phone,
                    email_verified = true, updated_at = now()
                returning id
                """, params().addValue("id", id).addValue("email", email).addValue("password", passwordEncoder.encode(password))
                .addValue("firstName", firstName).addValue("lastName", lastName).addValue("phone", phone), UUID.class);
    }

    private void assignRole(UUID accountId, String roleName) {
        jdbcTemplate.update("""
                insert into account_roles (account_id, role_id)
                select :accountId, id from roles where name = :roleName on conflict do nothing
                """, params().addValue("accountId", accountId).addValue("roleName", roleName));
    }

    private void removeRole(UUID accountId, String roleName) {
        jdbcTemplate.update("""
                delete from account_roles
                where account_id = :accountId
                  and role_id = (select id from roles where name = :roleName)
                """, params().addValue("accountId", accountId).addValue("roleName", roleName));
    }

    private void upsertCommissionPackage(UUID id, String code, String name, String description, BigDecimal rate) {
        jdbcTemplate.update("""
                insert into commission_packages (id, code, name, description, commission_rate, active, is_system)
                values (:id, :code, :name, :description, :rate, true, false)
                on conflict (code) do update set name = excluded.name, description = excluded.description,
                    commission_rate = excluded.commission_rate, active = true, updated_at = now()
                """, params().addValue("id", id).addValue("code", code).addValue("name", name)
                .addValue("description", description).addValue("rate", rate));
    }

    private void upsertHotel(UUID id, UUID ownerId, String name, String slug, String description, String address, String city, BigDecimal rating) {
        jdbcTemplate.update("""
                insert into hotels (id, owner_id, name, slug, description, address, city, country, email, phone, status, star_rating)
                values (:id, :ownerId, :name, :slug, :description, :address, :city, 'Vietnam', :email, '0900000000', 'ACTIVE', :rating)
                  on conflict (id) do update set owner_id = excluded.owner_id, name = excluded.name,
                      description = excluded.description, address = excluded.address, city = excluded.city,
                      status = 'ACTIVE', star_rating = excluded.star_rating, updated_at = now()
                """, params().addValue("id", id).addValue("ownerId", ownerId).addValue("name", name).addValue("slug", slug)
                .addValue("description", description).addValue("address", address).addValue("city", city)
                .addValue("email", slug + "@demo.local").addValue("rating", rating));
    }

    private void assignHotelPackage(UUID hotelId, UUID packageId) {
        jdbcTemplate.update("""
                insert into hotel_commission_packages (hotel_id, commission_package_id)
                values (:hotelId, :packageId)
                on conflict (hotel_id) do update set commission_package_id = excluded.commission_package_id, assigned_at = now()
                """, params().addValue("hotelId", hotelId).addValue("packageId", packageId));
    }

    private void addHotelMember(UUID hotelId, UUID accountId) {
        jdbcTemplate.update("insert into hotel_members (hotel_id, account_id) values (:hotelId, :accountId) on conflict do nothing",
                params().addValue("hotelId", hotelId).addValue("accountId", accountId));
    }

    private void upsertAmenity(String key, String name, String type) {
        jdbcTemplate.update("""
                insert into amenities (id, key, name, type, active, is_system)
                values (:id, :key, :name, :type, true, false)
                on conflict (key) do update set name = excluded.name, type = excluded.type, active = true, updated_at = now()
                """, params().addValue("id", UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)))
                .addValue("key", key).addValue("name", name).addValue("type", type));
    }

    private void upsertRoomType(UUID id, UUID hotelId, String name, int price, int maxGuests, int bedrooms, String description) {
        jdbcTemplate.update("""
                insert into room_types (id, hotel_id, name, description, price_per_night, max_guests, number_of_bedrooms, active)
                values (:id, :hotelId, :name, :description, :price, :maxGuests, :bedrooms, true)
                on conflict (id) do update set name = excluded.name, description = excluded.description, price_per_night = excluded.price_per_night,
                    max_guests = excluded.max_guests, number_of_bedrooms = excluded.number_of_bedrooms, active = true, updated_at = now()
                """, params().addValue("id", id).addValue("hotelId", hotelId).addValue("name", name).addValue("description", description)
                .addValue("price", price).addValue("maxGuests", maxGuests).addValue("bedrooms", bedrooms));
    }

    private void addRoomAmenities(UUID roomTypeId, List<String> amenityKeys) {
        for (String key : amenityKeys) {
            jdbcTemplate.update("""
                    insert into room_type_amenities (room_type_id, amenity_id)
                    select :roomTypeId, id from amenities where key = :key on conflict do nothing
                    """, params().addValue("roomTypeId", roomTypeId).addValue("key", key));
        }
    }

    private void upsertPhysicalRoom(UUID id, UUID hotelId, UUID roomTypeId, String roomNumber, String condition, boolean active) {
        jdbcTemplate.update("""
                insert into rooms (id, hotel_id, room_type_id, room_number, condition, active)
                values (:id, :hotelId, :roomTypeId, :roomNumber, :condition, :active)
                on conflict (hotel_id, room_number) do update set room_type_id = excluded.room_type_id,
                    condition = excluded.condition, active = excluded.active, updated_at = now()
                """, params().addValue("id", id).addValue("hotelId", hotelId).addValue("roomTypeId", roomTypeId)
                .addValue("roomNumber", roomNumber).addValue("condition", condition).addValue("active", active));
    }

    private void seedInventory(UUID hotelId, UUID roomTypeId, int totalRooms, int availableRooms) {
        for (int offset = -7; offset <= 60; offset++) {
            LocalDate stayDate = LocalDate.now().plusDays(offset);
            jdbcTemplate.update("""
                    insert into inventories (id, hotel_id, room_type_id, stay_date, total_rooms, available_rooms, stop_sell)
                    values (:id, :hotelId, :roomTypeId, :stayDate, :totalRooms, :availableRooms, :stopSell)
                    on conflict (room_type_id, stay_date) do update set total_rooms = excluded.total_rooms,
                        available_rooms = excluded.available_rooms, stop_sell = excluded.stop_sell, updated_at = now()
                    """, params().addValue("id", inventoryId(roomTypeId, stayDate)).addValue("hotelId", hotelId)
                    .addValue("roomTypeId", roomTypeId).addValue("stayDate", stayDate).addValue("totalRooms", totalRooms)
                    .addValue("availableRooms", availableRooms).addValue("stopSell", offset == 30));
        }
    }

    private void upsertPromotion(UUID id, UUID hotelId, String code, String name, String type, int value, Integer maxDiscount,
                                 int minimumAmount, int totalLimit, int perUserLimit, int usedCount, boolean active, String startsAt, String endsAt) {
        jdbcTemplate.update("""
                insert into promotions (id, hotel_id, code, name, discount_type, discount_value, max_discount, min_booking_amount,
                    total_usage_limit, per_user_usage_limit, used_count, starts_at, ends_at, active)
                values (:id, :hotelId, :code, :name, :type, :value, :maxDiscount, :minimumAmount, :totalLimit, :perUserLimit,
                    :usedCount, %s, %s, :active)
                on conflict (code) do update set name = excluded.name, active = excluded.active, used_count = excluded.used_count, updated_at = now()
                """.formatted(startsAt, endsAt), params().addValue("id", id).addValue("hotelId", hotelId).addValue("code", code)
                .addValue("name", name).addValue("type", type).addValue("value", value).addValue("maxDiscount", maxDiscount)
                .addValue("minimumAmount", minimumAmount).addValue("totalLimit", totalLimit).addValue("perUserLimit", perUserLimit)
                .addValue("usedCount", usedCount).addValue("active", active));
    }

    private void upsertBooking(UUID id, DemoBooking booking, BigDecimal subtotal, BigDecimal discount, BigDecimal total) {
        String lifecycleColumn = switch (booking.status()) {
            case "COMPLETED" -> "completed_at";
            case "CANCELLED" -> "cancelled_at";
            case "NO_SHOW" -> "no_show_at";
            default -> null;
        };
        String lifecycleSql = lifecycleColumn == null ? "null" : "now() - interval '1 day'";
        jdbcTemplate.update("""
                insert into bookings (id, account_id, hotel_id, promotion_id, booking_reference, status, check_in, check_out,
                    guest_name, guest_email, guest_phone, note, subtotal_amount, discount_amount, total_amount,
                    commission_package_code, commission_rate, commission_amount, %s)
                values (:id, :accountId, :hotelId, :promotionId, :reference, :status, current_date + :checkInOffset,
                    current_date + :checkOutOffset, :guestName, :guestEmail, '0900000005', 'Seeded local demo booking.',
                    :subtotal, :discount, :total, 'STANDARD', 0.1000, :commissionAmount, %s)
                on conflict (booking_reference) do update set status = excluded.status, check_in = excluded.check_in,
                    check_out = excluded.check_out, subtotal_amount = excluded.subtotal_amount, discount_amount = excluded.discount_amount,
                    total_amount = excluded.total_amount, updated_at = now()
                """.formatted(lifecycleColumn == null ? "pending_expires_at" : lifecycleColumn, lifecycleSql),
                params().addValue("id", id).addValue("accountId", booking.accountId()).addValue("hotelId", booking.hotelId())
                        .addValue("promotionId", booking.promotionId()).addValue("reference", booking.reference()).addValue("status", booking.status())
                        .addValue("checkInOffset", booking.checkInOffset()).addValue("checkOutOffset", booking.checkOutOffset())
                        .addValue("guestName", booking.accountId().equals(customerId) ? "Customer User" : "Mai Pham")
                        .addValue("guestEmail", booking.accountId().equals(customerId) ? "customer@gmail.com" : "traveler@demo.local")
                        .addValue("subtotal", subtotal).addValue("discount", discount).addValue("total", total)
                        .addValue("commissionAmount", total.multiply(new BigDecimal("0.10"))));
    }

    private void upsertBookingItem(UUID id, UUID bookingId, UUID roomTypeId, DemoBooking booking, BigDecimal subtotal) {
        jdbcTemplate.update("""
                insert into booking_items (id, booking_id, room_type_id, room_type_name, quantity, unit_price, max_guests, line_total)
                select :id, :bookingId, :roomTypeId, name, 1, :unitPrice, max_guests, :lineTotal from room_types where id = :roomTypeId
                on conflict (booking_id, room_type_id) do update set unit_price = excluded.unit_price, line_total = excluded.line_total
                """, params().addValue("id", id).addValue("bookingId", bookingId).addValue("roomTypeId", roomTypeId)
                .addValue("unitPrice", booking.nightlyPrice()).addValue("lineTotal", subtotal));
    }

    private void upsertPayment(UUID id, UUID bookingId, String bookingReference, BigDecimal amount, String bookingStatus) {
        String status = bookingStatus.equals("CANCELLED") ? "CANCELED" : bookingStatus.equals("PENDING") ? "PENDING" : "SUCCEEDED";
        jdbcTemplate.update("""
                insert into payments (id, booking_id, provider, status, amount, merchant_txn_ref, paid_at)
                values (:id, :bookingId, 'VNPAY', :status, :amount, :reference, case when :status = 'SUCCEEDED' then now() else null end)
                on conflict (merchant_txn_ref) do update set status = excluded.status, amount = excluded.amount, paid_at = excluded.paid_at, updated_at = now()
                """, params().addValue("id", id).addValue("bookingId", bookingId).addValue("status", status)
                .addValue("amount", amount).addValue("reference", "PAY-" + bookingReference));
    }

    private void upsertCheckIn(UUID id, UUID bookingId, boolean checkedOut) {
        jdbcTemplate.update("""
                insert into check_ins (id, booking_id, checked_in_by_account_id, checked_in_at, checked_out_at, note)
                values (:id, :bookingId, :receptionistId, now() - interval '1 day', %s, 'Demo front-desk check-in.')
                on conflict (booking_id) do update set checked_out_at = excluded.checked_out_at, note = excluded.note
                """.formatted(checkedOut ? "now() - interval '1 hour'" : "null"), params().addValue("id", id)
                .addValue("bookingId", bookingId).addValue("receptionistId", receptionistId));
    }

    private void upsertReview(UUID id, UUID bookingId, UUID hotelId, UUID accountId, String rating, String comment) {
        jdbcTemplate.update("""
                insert into reviews (id, booking_id, hotel_id, account_id, rating, comment, visible)
                values (:id, :bookingId, :hotelId, :accountId, :rating, :comment, true)
                on conflict (booking_id) do update set rating = excluded.rating, comment = excluded.comment, visible = true, updated_at = now()
                """, params().addValue("id", id).addValue("bookingId", bookingId).addValue("hotelId", hotelId)
                .addValue("accountId", accountId).addValue("rating", new BigDecimal(rating)).addValue("comment", comment));
    }

    private void upsertPaymentEvent(UUID id, UUID paymentId, String eventType, String payload) {
        jdbcTemplate.update("insert into payment_events (id, payment_id, event_type, payload) values (:id, :paymentId, :eventType, cast(:payload as jsonb)) on conflict (id) do nothing",
                params().addValue("id", id).addValue("paymentId", paymentId).addValue("eventType", eventType).addValue("payload", payload));
    }

    private void upsertImageAsset(UUID id, UUID ownerId, String publicId, String url) {
        jdbcTemplate.update("""
                insert into image_assets (id, owner_account_id, provider, public_id, url, secure_url, width, height, bytes)
                values (:id, :ownerId, 'CLOUDINARY', :publicId, :url, :url, 1600, 900, 0)
                on conflict (id) do update set provider = excluded.provider, public_id = excluded.public_id,
                    url = excluded.url, secure_url = excluded.secure_url
                """, params().addValue("id", id).addValue("ownerId", ownerId).addValue("publicId", publicId).addValue("url", url));
    }

    private void seedAdditionalGalleryImages(UUID galleryFolderId) {
        for (int index = 0; index < ADDITIONAL_GALLERY_IMAGE_FILES.size(); index++) {
            UUID imageId = id("18" + String.format("%02d", index + 1));
            upsertImageAsset(imageId, ownerId, "demo/gallery/" + (index + 1),
                    cloudinaryImage(ADDITIONAL_GALLERY_IMAGE_FILES.get(index)));
            linkGalleryImage(id("19" + String.format("%02d", index + 1)), galleryFolderId, imageId);
        }
    }

    private void linkGalleryImage(UUID id, UUID galleryFolderId, UUID imageId) {
        jdbcTemplate.update("""
                insert into gallery_images (id, folder_id, image_asset_id)
                values (:id, :folderId, :imageId)
                on conflict (folder_id, image_asset_id) do nothing
                """, params().addValue("id", id).addValue("folderId", galleryFolderId).addValue("imageId", imageId));
    }

    private void linkHotelImage(UUID id, UUID hotelId, UUID imageId, int sortOrder) {
        jdbcTemplate.update("""
                insert into hotel_images (id, hotel_id, image_asset_id, url, sort_order)
                select :id, :hotelId, :imageId, url, :sortOrder from image_assets where id = :imageId
                on conflict (hotel_id, sort_order) do update set image_asset_id = excluded.image_asset_id, url = excluded.url
                """, params().addValue("id", id).addValue("hotelId", hotelId).addValue("imageId", imageId).addValue("sortOrder", sortOrder));
    }

    private void linkRoomTypeImage(UUID id, UUID roomTypeId, UUID imageId, int sortOrder) {
        jdbcTemplate.update("""
                insert into room_type_images (id, room_type_id, image_asset_id, url, sort_order)
                select :id, :roomTypeId, :imageId, url, :sortOrder from image_assets where id = :imageId
                on conflict (room_type_id, sort_order) do update set image_asset_id = excluded.image_asset_id, url = excluded.url
                """, params().addValue("id", id).addValue("roomTypeId", roomTypeId).addValue("imageId", imageId).addValue("sortOrder", sortOrder));
    }

    private void upsertNews(UUID id, String slug, String title, String summary, String status) {
        jdbcTemplate.update("""
                insert into news (id, author_account_id, title, slug, summary, content, status, published_at)
                values (:id, :authorId, :title, :slug, :summary, :content, :status, case when :status = 'PUBLISHED' then now() else null end)
                on conflict (slug) do update set title = excluded.title, summary = excluded.summary, content = excluded.content,
                    status = excluded.status, published_at = excluded.published_at, updated_at = now()
                """, params().addValue("id", id).addValue("authorId", adminId).addValue("title", title).addValue("slug", slug)
                .addValue("summary", summary).addValue("content", summary + " This seeded content is safe to edit during a local demo.").addValue("status", status));
    }

    private void linkNewsImage(UUID id, UUID newsId, UUID imageId, int sortOrder) {
        jdbcTemplate.update("""
                insert into news_images (id, news_id, image_asset_id, url, sort_order)
                select :id, :newsId, :imageId, url, :sortOrder from image_assets where id = :imageId
                on conflict (news_id, sort_order) do update set image_asset_id = excluded.image_asset_id, url = excluded.url
                """, params().addValue("id", id).addValue("newsId", newsId).addValue("imageId", imageId).addValue("sortOrder", sortOrder));
    }

    private void upsertBanner(UUID id, String title, String subtitle, String imageUrl, String linkUrl, String linkType, int position) {
        jdbcTemplate.update("""
                insert into banners (id, title, subtitle, image_url, link_url, link_type, position, active, starts_at, ends_at)
                values (:id, :title, :subtitle, :imageUrl, :linkUrl, :linkType, :position, true, now() - interval '1 day', now() + interval '60 days')
                on conflict (position) do update set title = excluded.title, subtitle = excluded.subtitle, image_url = excluded.image_url,
                    link_url = excluded.link_url, link_type = excluded.link_type, active = true, updated_at = now()
                """, params().addValue("id", id).addValue("title", title).addValue("subtitle", subtitle).addValue("imageUrl", imageUrl)
                .addValue("linkUrl", linkUrl).addValue("linkType", linkType).addValue("position", position));
    }

    private void upsertPolicy(UUID id, UUID hotelId, String type, String title, String content, int sortOrder) {
        jdbcTemplate.update("""
                insert into hotel_policies (id, hotel_id, type, title, content, enabled, sort_order)
                values (:id, :hotelId, :type, :title, :content, true, :sortOrder)
                on conflict (hotel_id, type) do update set title = excluded.title, content = excluded.content,
                    enabled = true, sort_order = excluded.sort_order, updated_at = now()
                """, params().addValue("id", id).addValue("hotelId", hotelId).addValue("type", type).addValue("title", title)
                .addValue("content", content).addValue("sortOrder", sortOrder));
    }

    private void upsertNotification(UUID id, UUID recipientId, String type, String title, String body, String linkUrl) {
        jdbcTemplate.update("""
                insert into notifications (id, recipient_account_id, type, title, body, link_url)
                values (:id, :recipientId, :type, :title, :body, :linkUrl)
                on conflict (id) do update set title = excluded.title, body = excluded.body, link_url = excluded.link_url
                """, params().addValue("id", id).addValue("recipientId", recipientId).addValue("type", type)
                .addValue("title", title).addValue("body", body).addValue("linkUrl", linkUrl));
    }

    private MapSqlParameterSource params() {
        return new MapSqlParameterSource();
    }

    private UUID inventoryId(UUID roomTypeId, LocalDate stayDate) {
        return UUID.nameUUIDFromBytes((roomTypeId + ":" + stayDate).getBytes(StandardCharsets.UTF_8));
    }

    private static UUID id(String suffix) {
        return UUID.fromString("40000000-0000-4000-8000-00000000" + suffix);
    }

    private static String cloudinaryImage(String fileName) {
        return switch (fileName) {
            case "small_andrew-neel-B4rEJ09-Puo-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699214/small_andrew-neel-B4rEJ09-Puo-unsplash_ilcr8p.jpg";
            case "small_bilderboken-rlwE8f8anOc-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699214/small_bilderboken-rlwE8f8anOc-unsplash_njofaz.jpg";
            case "small_brett-campbell-k1OlQaEK2qI-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699214/small_brett-campbell-k1OlQaEK2qI-unsplash_iafggu.jpg";
            case "small_cory-bjork-D1yT791Nf9A-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699214/small_cory-bjork-D1yT791Nf9A-unsplash_otawcw.jpg";
            case "small_dad-hotel-FCSDBKliyTQ-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699215/small_dad-hotel-FCSDBKliyTQ-unsplash_fhpsmt.jpg";
            case "small_frames-for-your-heart-zSG-kd-L6vw-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699215/small_frames-for-your-heart-zSG-kd-L6vw-unsplash_moi4cv.jpg";
            case "small_jeffrey-francisco-_Ei9f33bQ1A-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699209/jeffrey-francisco-_Ei9f33bQ1A-unsplash_kb0ifz.jpg";
            case "small_jorg-angeli-1tyuLfDOnG0-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699215/small_jorg-angeli-1tyuLfDOnG0-unsplash_pizjfd.jpg";
            case "small_mark-champs-Id2IIl1jOB0-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699215/small_mark-champs-Id2IIl1jOB0-unsplash_e1phl6.jpg";
            case "small_mp-fV2dM2WvKvE-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699215/small_mp-fV2dM2WvKvE-unsplash_f86j3v.jpg";
            case "small_mr-junaid--3ohj90OT8o-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699215/small_mr-junaid--3ohj90OT8o-unsplash_vqxcuj.jpg";
            case "small_natalia-gusakova-EYoK3eVKIiQ-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699215/small_natalia-gusakova-EYoK3eVKIiQ-unsplash_gwoybm.jpg";
            case "small_orva-studio-YC8qqp50BdA-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699216/small_orva-studio-YC8qqp50BdA-unsplash_kr7gnz.jpg";
            case "small_oswald-elsaboath-ym_EI-DTS1g-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699216/small_oswald-elsaboath-ym_EI-DTS1g-unsplash_pihkzy.jpg";
            case "small_paolo-nicolello-2gOxKj594nM-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699216/small_paolo-nicolello-2gOxKj594nM-unsplash_cccvwq.jpg";
            case "small_patrick-robert-doyle-AH8zKXqFITA-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699216/small_patrick-robert-doyle-AH8zKXqFITA-unsplash_wswjdr.jpg";
            case "small_point3d-commercial-imaging-ltd-oxeCZrodz78-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699216/small_point3d-commercial-imaging-ltd-oxeCZrodz78-unsplash_bvnzw2.jpg";
            case "small_reagan-m-d-eWGvLCZfQ-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699216/small_reagan-m-d-eWGvLCZfQ-unsplash_jpzghm.jpg";
            case "small_reisetopia-pSDe7ePo0Tc-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699216/small_reisetopia-pSDe7ePo0Tc-unsplash_ztop2g.jpg";
            case "small_rktkn-ssOtyGE8CyE-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699216/small_rktkn-ssOtyGE8CyE-unsplash_l5uuli.jpg";
            case "small_sara-dubler-Koei_7yYtIo-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699219/small_sara-dubler-Koei_7yYtIo-unsplash_y2jare.jpg";
            case "small_sasha-kaunas-67-sOi7mVIk-unsplash (1).jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699217/small_sasha-kaunas-67-sOi7mVIk-unsplash_1_wgsg94.jpg";
            case "small_sasha-kaunas-67-sOi7mVIk-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699217/small_sasha-kaunas-67-sOi7mVIk-unsplash_v02zb3.jpg";
            case "small_sasha-kaunas-TAgGZWz6Qg8-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699217/small_sasha-kaunas-TAgGZWz6Qg8-unsplash_li8kcx.jpg";
            case "small_the-anam-_twiIcIsp2s-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699217/small_the-anam-_twiIcIsp2s-unsplash_wlcji8.jpg";
            case "small_valeriia-bugaiova-_pPHgeHz1uk-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699218/small_valeriia-bugaiova-_pPHgeHz1uk-unsplash_i0vbzz.jpg";
            case "small_visualsofdana-T5pL6ciEn-I-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699218/small_visualsofdana-T5pL6ciEn-I-unsplash_cmvyfo.jpg";
            case "small_vojtech-bruzek-Yrxr3bsPdS0-unsplash.jpg" -> "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699218/small_vojtech-bruzek-Yrxr3bsPdS0-unsplash_ciknsd.jpg";
            default -> throw new IllegalArgumentException("Missing Cloudinary URL for demo image: " + fileName);
        };
    }

    private record DemoBooking(String reference, UUID accountId, UUID hotelId, UUID roomTypeId, String status,
                               int checkInOffset, int checkOutOffset, int nightlyPrice, int discount, UUID promotionId) {
        private int nights() {
            return checkOutOffset - checkInOffset;
        }
    }
}
