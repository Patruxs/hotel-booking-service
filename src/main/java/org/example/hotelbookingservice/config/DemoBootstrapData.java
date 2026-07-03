package org.example.hotelbookingservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class DemoBootstrapData implements CommandLineRunner {
    private static final UUID ADMIN_ID = UUID.fromString("30000000-0000-4000-8000-000000000001");
    private static final UUID CUSTOMER_ID = UUID.fromString("30000000-0000-4000-8000-000000000002");
    private static final UUID OPERATOR_ID = UUID.fromString("30000000-0000-4000-8000-000000000003");
    private static final UUID HOTEL_ID = UUID.fromString("30000000-0000-4000-8000-000000000101");
    private static final UUID ROOM_TYPE_ID = UUID.fromString("30000000-0000-4000-8000-000000000201");
    private static final UUID ROOM_ID = UUID.fromString("30000000-0000-4000-8000-000000000301");
    private static final UUID IMAGE_ID = UUID.fromString("30000000-0000-4000-8000-000000000401");
    private static final UUID BOOKING_ID = UUID.fromString("30000000-0000-4000-8000-000000000501");

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        upsertAccount(ADMIN_ID, "admin@kinyias.test", "Demo", "Admin");
        upsertAccount(CUSTOMER_ID, "customer@kinyias.test", "Demo", "Customer");
        upsertAccount(OPERATOR_ID, "operator@kinyias.test", "Demo", "Operator");
        assignRole(ADMIN_ID, "ADMIN");
        assignRole(CUSTOMER_ID, "CUSTOMER");
        assignRole(OPERATOR_ID, "MANAGER");
        upsertHotel();
        seedOperationsData();
    }

    private void upsertAccount(UUID id, String email, String firstName, String lastName) {
        jdbcTemplate.update("""
                insert into accounts (id, email, password_hash, first_name, last_name, email_verified)
                values (:id, :email, :password, :firstName, :lastName, true)
                on conflict (email)
                do update set first_name = excluded.first_name, last_name = excluded.last_name, updated_at = now()
                """, params()
                .addValue("id", id)
                .addValue("email", email)
                .addValue("password", passwordEncoder.encode("password123"))
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
                values (:id, :ownerId, 'Kinyias Demo Hotel', 'kinyias-demo-hotel', 'Demo hotel for local thesis flows.', '1 Demo Street', 'Ho Chi Minh City', 'Vietnam', 'hotel@kinyias.test', '0900000000', 'ACTIVE', 4.5)
                on conflict (id)
                do update set status = 'ACTIVE', updated_at = now()
                """, params().addValue("id", HOTEL_ID).addValue("ownerId", ADMIN_ID));
        jdbcTemplate.update("""
                insert into hotel_members (hotel_id, account_id)
                values (:hotelId, :accountId)
                on conflict do nothing
                """, params().addValue("hotelId", HOTEL_ID).addValue("accountId", OPERATOR_ID));
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
                    """, params().addValue("id", UUID.nameUUIDFromBytes(("inventory-" + i).getBytes()))
                    .addValue("hotelId", HOTEL_ID)
                    .addValue("roomTypeId", ROOM_TYPE_ID)
                    .addValue("date", date));
        }
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
                values (:id, 'Kinyias Summer Stays', 'Sample local banner', '/api/v1/uploads/local/' || cast(:imageId as text), '/hotels/' || cast(:hotelId as text), 'HOTEL', 1, true)
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
                values (:id, :accountId, :hotelId, 'DEMO-BOOKING-001', 'COMPLETED', current_date - 4, current_date - 2, 'Demo Customer', 'customer@kinyias.test', '0900000001', 2400000, 2400000, 'STANDARD', 0.1, 240000, now())
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
}
