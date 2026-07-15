package org.example.hotelbookingservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@Component
@Profile("legacy-demo-bootstrap")
@RequiredArgsConstructor
public class DemoBootstrapData implements CommandLineRunner {
    private static final UUID ADMIN_ID = UUID.fromString("30000000-0000-4000-8000-000000000001");
    private static final UUID CUSTOMER_ID = UUID.fromString("30000000-0000-4000-8000-000000000002");
    private static final UUID OPERATOR_ID = UUID.fromString("30000000-0000-4000-8000-000000000003");
    private static final UUID OWNER_ID = UUID.fromString("30000000-0000-4000-8000-000000000004");
    private static final UUID HOTEL_ID = UUID.fromString("30000000-0000-4000-8000-000000000101");
    private static final UUID ROOM_TYPE_ID = UUID.fromString("30000000-0000-4000-8000-000000000201");
    private static final UUID ROOM_ID = UUID.fromString("30000000-0000-4000-8000-000000000301");
    private static final UUID IMAGE_ID = UUID.fromString("30000000-0000-4000-8000-000000000401");
    private static final UUID BOOKING_ID = UUID.fromString("30000000-0000-4000-8000-000000000501");
    private static final UUID BASIC_PARTNER_PACKAGE_ID = UUID.fromString("30000000-0000-4000-8000-000000000a01");
    private static final UUID PREMIUM_GROWTH_PACKAGE_ID = UUID.fromString("30000000-0000-4000-8000-000000000a02");

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        upsertAccount(ADMIN_ID, "admin@gmail.com", "Admin", "User", "admin123");
        upsertAccount(CUSTOMER_ID, "customer@gmail.com", "Customer", "User", "customer123");
        upsertAccount(OPERATOR_ID, "operator@grand.test", "Demo", "Operator", "password123");
        upsertAccount(OWNER_ID, "owner@grand.test", "Demo", "Owner", "owner123");
        assignRole(ADMIN_ID, "ADMIN");
        assignRole(CUSTOMER_ID, "CUSTOMER");
        assignRole(OPERATOR_ID, "MANAGER");
        assignRole(OWNER_ID, "OWNER");
        seedCommissionPackages();
        upsertHotel();
        seedOperationsData();
    }

    private void upsertAccount(UUID id, String email, String firstName, String lastName, String password) {
        jdbcTemplate.update("""
                  insert into accounts (id, email, password_hash, first_name, last_name, email_verified)
                  values (:id, :email, :password, :firstName, :lastName, true)
                  on conflict (id)
                  do update set email = excluded.email,
                                password_hash = excluded.password_hash,
                                first_name = excluded.first_name,
                                last_name = excluded.last_name,
                                email_verified = true,
                                updated_at = now()
                  """, params()
                  .addValue("id", id)
                  .addValue("email", email)
                  .addValue("password", passwordEncoder.encode(password))
                  .addValue("firstName", firstName)
                  .addValue("lastName", lastName));
    }

    private void assignRole(UUID accountId, String roleName) {
        jdbcTemplate.update("""
                insert into account_roles (account_id, role_id)
                select :accountId, id
                from roles
                where name = :roleName
                on conflict do nothing
                """, params().addValue("accountId", accountId).addValue("roleName", roleName));
    }

    private void upsertHotel() {
        jdbcTemplate.update("""
                insert into hotels (id, owner_id, name, slug, description, address, city, country, email, phone, status, star_rating)
                  values (:id, :ownerId, 'Grand Demo Hotel', 'grand-demo-hotel', 'Demo hotel for local thesis flows.', '1 Demo Street', 'Ho Chi Minh City', 'Vietnam', 'hotel@grand.test', '0900000000', 'ACTIVE', 4.5)
                  on conflict (id)
                  do update set owner_id = excluded.owner_id, status = 'ACTIVE', updated_at = now()
                """, params().addValue("id", HOTEL_ID).addValue("ownerId", OWNER_ID));
        jdbcTemplate.update("""
                insert into hotel_commission_packages (hotel_id, commission_package_id)
                values (:hotelId, :packageId)
                on conflict (hotel_id)
                do update set commission_package_id = excluded.commission_package_id, assigned_at = now()
                """, params().addValue("hotelId", HOTEL_ID).addValue("packageId", BASIC_PARTNER_PACKAGE_ID));
        jdbcTemplate.update("""
                insert into hotel_members (hotel_id, account_id)
                values (:hotelId, :accountId)
                on conflict do nothing
                """, params().addValue("hotelId", HOTEL_ID).addValue("accountId", ADMIN_ID));
        jdbcTemplate.update("""
                insert into hotel_members (hotel_id, account_id)
                values (:hotelId, :accountId)
                on conflict do nothing
                """, params().addValue("hotelId", HOTEL_ID).addValue("accountId", OPERATOR_ID));
        jdbcTemplate.update("""
                insert into hotel_members (hotel_id, account_id)
                values (:hotelId, :accountId)
                on conflict do nothing
                """, params().addValue("hotelId", HOTEL_ID).addValue("accountId", OWNER_ID));
        jdbcTemplate.update("""
                insert into room_types (id, hotel_id, name, description, price_per_night, max_guests, number_of_bedrooms, active)
                values (:id, :hotelId, 'Demo Deluxe', 'Comfortable demo room type.', 1200000, 2, 1, true)
                on conflict (id)
                do update set active = true, updated_at = now()
                """, params().addValue("id", ROOM_TYPE_ID).addValue("hotelId", HOTEL_ID));
        jdbcTemplate.update("""
                insert into rooms (id, hotel_id, room_type_id, room_number, condition, active)
                values (:id, :hotelId, :roomTypeId, 'D101', 'CLEAN', true)
                on conflict (hotel_id, room_number)
                do update set active = true, updated_at = now()
                """, params().addValue("id", ROOM_ID).addValue("hotelId", HOTEL_ID).addValue("roomTypeId", ROOM_TYPE_ID));
        for (int i = 0; i < 14; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            jdbcTemplate.update("""
                    insert into inventories (id, hotel_id, room_type_id, stay_date, total_rooms, available_rooms)
                    values (:id, :hotelId, :roomTypeId, :date, 5, 5)
                    on conflict (room_type_id, stay_date)
                    do update set total_rooms = 5, available_rooms = 5, updated_at = now()
                    """, params().addValue("id", inventoryId(date))
                    .addValue("hotelId", HOTEL_ID)
                    .addValue("roomTypeId", ROOM_TYPE_ID)
                    .addValue("date", date));
        }
    }

    private void seedCommissionPackages() {
        upsertCommissionPackage(BASIC_PARTNER_PACKAGE_ID, "BASIC_PARTNER", "Basic Partner", "Default demo partner package.", new BigDecimal("0.080000"));
        upsertCommissionPackage(PREMIUM_GROWTH_PACKAGE_ID, "PREMIUM_GROWTH", "Premium Growth", "Higher-touch demo growth package.", new BigDecimal("0.120000"));
    }

    private void upsertCommissionPackage(UUID id, String code, String name, String description, BigDecimal rate) {
        jdbcTemplate.update("""
                insert into commission_packages (id, code, name, description, commission_rate, active)
                values (:id, :code, :name, :description, :rate, true)
                on conflict (code)
                do update set name = excluded.name,
                              description = excluded.description,
                              commission_rate = excluded.commission_rate,
                              active = true,
                              updated_at = now()
                """, params()
                .addValue("id", id)
                .addValue("code", code)
                .addValue("name", name)
                .addValue("description", description)
                .addValue("rate", rate));
    }

    private void seedOperationsData() {
        jdbcTemplate.update("""
                insert into image_assets (id, owner_account_id, provider, public_id, url, secure_url, width, height, bytes)
                values (:id, :ownerId, 'LOCAL', 'demo/hotel', '/api/v1/uploads/local/' || cast(:id as text), '/api/v1/uploads/local/' || cast(:id as text), 1200, 800, 0)
                on conflict (id) do nothing
                """, params().addValue("id", IMAGE_ID).addValue("ownerId", ADMIN_ID));
        jdbcTemplate.update("""
                insert into hotel_images (id, hotel_id, image_asset_id, url, sort_order)
                values (:id, :hotelId, :imageId, '/api/v1/uploads/local/' || cast(:imageId as text), 0)
                on conflict (hotel_id, sort_order) do nothing
                """, params().addValue("id", UUID.fromString("30000000-0000-4000-8000-000000000402"))
                .addValue("hotelId", HOTEL_ID).addValue("imageId", IMAGE_ID));
        jdbcTemplate.update("""
                insert into banners (id, title, subtitle, image_url, link_url, link_type, position, active)
                values (:id, 'Grand Summer Stays', 'Sample local banner', '/api/v1/uploads/local/' || cast(:imageId as text), '/hotels/' || cast(:hotelId as text), 'HOTEL', 1, true)
                on conflict (position) do nothing
                """, params().addValue("id", UUID.fromString("30000000-0000-4000-8000-000000000601"))
                .addValue("imageId", IMAGE_ID).addValue("hotelId", HOTEL_ID));
        jdbcTemplate.update("""
                insert into news (id, author_account_id, title, slug, summary, content, status, published_at)
                values (:id, :authorId, 'Demo travel update', 'demo-travel-update', 'Local demo content.', 'This is local demo news content.', 'PUBLISHED', now())
                on conflict (slug) do nothing
                """, params().addValue("id", UUID.fromString("30000000-0000-4000-8000-000000000701")).addValue("authorId", ADMIN_ID));
        jdbcTemplate.update("""
                insert into hotel_policies (id, hotel_id, type, title, content, enabled, sort_order)
                values (:id, :hotelId, 'CHECK_IN', 'Check-in', 'Check-in starts at 14:00.', true, 1)
                on conflict do nothing
                """, params().addValue("id", UUID.fromString("30000000-0000-4000-8000-000000000801")).addValue("hotelId", HOTEL_ID));
        jdbcTemplate.update("""
                insert into bookings (id, account_id, hotel_id, booking_reference, status, check_in, check_out, guest_name, guest_email, guest_phone, subtotal_amount, total_amount, commission_package_code, commission_rate, commission_amount, completed_at)
                values (:id, :accountId, :hotelId, 'DEMO-BOOKING-001', 'COMPLETED', current_date - 4, current_date - 2, 'Demo Customer', 'customer@grand.test', '0900000001', 2400000, 2400000, 'STANDARD', 0.1, 240000, now())
                on conflict (booking_reference) do nothing
                """, params().addValue("id", BOOKING_ID).addValue("accountId", CUSTOMER_ID).addValue("hotelId", HOTEL_ID));
        jdbcTemplate.update("""
                insert into booking_items (id, booking_id, room_type_id, room_type_name, quantity, unit_price, max_guests, line_total)
                values (:id, :bookingId, :roomTypeId, 'Demo Deluxe', 1, 1200000, 2, 2400000)
                on conflict (booking_id, room_type_id) do nothing
                """, params().addValue("id", UUID.fromString("30000000-0000-4000-8000-000000000502")).addValue("bookingId", BOOKING_ID).addValue("roomTypeId", ROOM_TYPE_ID));
        jdbcTemplate.update("""
                insert into payments (id, booking_id, provider, status, amount, merchant_txn_ref, paid_at)
                values (:id, :bookingId, 'VNPAY', 'SUCCEEDED', 2400000, 'DEMO-PAYMENT-001', now())
                on conflict (merchant_txn_ref) do nothing
                """, params().addValue("id", UUID.fromString("30000000-0000-4000-8000-000000000503")).addValue("bookingId", BOOKING_ID));
        jdbcTemplate.update("""
                insert into reviews (id, booking_id, hotel_id, account_id, rating, comment, visible)
                values (:id, :bookingId, :hotelId, :accountId, 5.0, 'Excellent demo stay.', true)
                on conflict (booking_id) do nothing
                """, params().addValue("id", UUID.fromString("30000000-0000-4000-8000-000000000901"))
                .addValue("bookingId", BOOKING_ID).addValue("hotelId", HOTEL_ID).addValue("accountId", CUSTOMER_ID));
    }

    private MapSqlParameterSource params() {
        return new MapSqlParameterSource();
    }

    private UUID inventoryId(LocalDate date) {
        return UUID.nameUUIDFromBytes(("inventory-" + ROOM_TYPE_ID + "-" + date).getBytes(StandardCharsets.UTF_8));
    }
}
