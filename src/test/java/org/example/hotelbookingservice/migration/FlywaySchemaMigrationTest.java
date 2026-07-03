package org.example.hotelbookingservice.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class FlywaySchemaMigrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("hotel_booking_service_test")
            .withUsername("test")
            .withPassword("test");

    static JdbcTemplate jdbc;

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
    }

    @Test
    void flywayMigratesFreshPostgresqlSchema() {
        Integer migrations = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success",
                Integer.class
        );
        Integer applicationTables = jdbc.queryForObject(
                """
                select count(*)
                from information_schema.tables
                where table_schema = 'public'
                  and table_name in ('accounts', 'roles', 'api_actions', 'hotels', 'room_types', 'inventories', 'bookings', 'payments', 'payment_events')
                """,
                Integer.class
        );

        assertThat(migrations).isEqualTo(2);
        assertThat(applicationTables).isEqualTo(9);
    }

    @Test
    void flywaySeedsStableSystemAuthorizationCatalogRows() {
        UUID adminRoleId = jdbc.queryForObject(
                "select id from roles where name = 'ADMIN'",
                UUID.class
        );
        Boolean allRolesAreSystem = jdbc.queryForObject(
                "select bool_and(is_system) from roles where name in ('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'STAFF', 'CUSTOMER')",
                Boolean.class
        );
        Integer adminPermissionCount = jdbc.queryForObject(
                """
                select count(*)
                from role_permissions rp
                join roles r on r.id = rp.role_id
                where r.name = 'ADMIN'
                """,
                Integer.class
        );
        Integer actionPolicyCount = jdbc.queryForObject("select count(*) from action_policies", Integer.class);

        assertThat(adminRoleId).isEqualTo(UUID.fromString("00000000-0000-4000-8000-000000000001"));
        assertThat(allRolesAreSystem).isTrue();
        assertThat(adminPermissionCount).isEqualTo(8);
        assertThat(actionPolicyCount).isEqualTo(23);
    }

    @Test
    void schemaEnforcesCoreCheckConstraints() {
        UUID accountId = UUID.fromString("10000000-0000-4000-8001-000000000001");
        UUID hotelId = UUID.fromString("10000000-0000-4000-8001-000000000002");
        UUID roomTypeId = UUID.fromString("10000000-0000-4000-8001-000000000005");

        insertAccount(accountId, "constraint-owner@example.com");
        insertHotel(hotelId, accountId, "constraint-hotel");
        jdbc.update(
                """
                insert into room_types (id, hotel_id, name, price_per_night, max_guests)
                values (?, ?, 'Constraint Room Type', 100.00, 2)
                """,
                roomTypeId,
                hotelId
        );

        assertThatThrownBy(() -> jdbc.update(
                """
                insert into bookings (
                    id, account_id, hotel_id, booking_reference, status, check_in, check_out,
                    guest_name, guest_email, guest_phone, subtotal_amount, discount_amount, total_amount
                ) values (?, ?, ?, ?, 'BOOKED', date '2027-01-10', date '2027-01-12', ?, ?, ?, ?, ?, ?)
                """,
                UUID.fromString("10000000-0000-4000-8001-000000000003"),
                accountId,
                hotelId,
                "INVALID-STATUS",
                "Invalid Guest",
                "guest@example.com",
                "0900000000",
                BigDecimal.valueOf(100),
                BigDecimal.ZERO,
                BigDecimal.valueOf(100)
        )).hasMessageContaining("bookings_status_check");

        assertThatThrownBy(() -> jdbc.update(
                """
                insert into inventories (id, hotel_id, room_type_id, stay_date, total_rooms, available_rooms)
                values (?, ?, ?, date '2027-01-10', 2, 3)
                """,
                UUID.fromString("10000000-0000-4000-8001-000000000004"),
                hotelId,
                roomTypeId
        )).hasMessageContaining("inventories_counts_check");
    }

    @Test
    void schemaPreservesPostgresqlTypesForMoneyStayDatesMomentsAndAuditPayloads() {
        UUID accountId = UUID.fromString("10000000-0000-4000-8002-000000000001");
        UUID hotelId = UUID.fromString("10000000-0000-4000-8002-000000000002");
        UUID roomTypeId = UUID.fromString("10000000-0000-4000-8002-000000000003");
        UUID bookingId = UUID.fromString("10000000-0000-4000-8002-000000000004");
        UUID paymentId = UUID.fromString("10000000-0000-4000-8002-000000000005");
        UUID paymentEventId = UUID.fromString("10000000-0000-4000-8002-000000000006");

        insertAccount(accountId, "types-owner@example.com");
        insertHotel(hotelId, accountId, "types-hotel");
        jdbc.update(
                """
                insert into room_types (id, hotel_id, name, price_per_night, max_guests)
                values (?, ?, 'Deluxe', 1234567.89, 3)
                """,
                roomTypeId,
                hotelId
        );
        jdbc.update(
                """
                insert into inventories (id, hotel_id, room_type_id, stay_date, total_rooms, available_rooms)
                values (?, ?, ?, date '2027-02-03', 5, 4)
                """,
                UUID.fromString("10000000-0000-4000-8002-000000000007"),
                hotelId,
                roomTypeId
        );
        jdbc.update(
                """
                insert into bookings (
                    id, account_id, hotel_id, booking_reference, status, check_in, check_out,
                    guest_name, guest_email, guest_phone, subtotal_amount, discount_amount, total_amount,
                    commission_rate, commission_amount, pending_expires_at
                ) values (?, ?, ?, 'TYPE-BOOKING', 'PENDING', date '2027-02-03', date '2027-02-05',
                    'Type Guest', 'type-guest@example.com', '0900000001', 2469135.78, 100000.00, 2369135.78,
                    0.1000, 236914.00, timestamptz '2027-02-02 10:15:30+07')
                """,
                bookingId,
                accountId,
                hotelId
        );
        jdbc.update(
                """
                insert into payments (id, booking_id, status, amount, merchant_txn_ref, expires_at)
                values (?, ?, 'PENDING', 2369135.78, 'BK_TYPES_1', timestamptz '2027-02-02 10:15:30+07')
                """,
                paymentId,
                bookingId
        );
        jdbc.update(
                """
                insert into payment_events (id, payment_id, event_type, payload)
                values (?, ?, 'VNPAY_RETURN', '{"vnp_ResponseCode":"00","amount":236913578}'::jsonb)
                """,
                paymentEventId,
                paymentId
        );

        BigDecimal amount = jdbc.queryForObject("select amount from payments where id = ?", BigDecimal.class, paymentId);
        Boolean stayDateMatches = jdbc.queryForObject(
                "select stay_date = date '2027-02-03' from inventories where room_type_id = ?",
                Boolean.class,
                roomTypeId
        );
        String pendingExpiryType = jdbc.queryForObject(
                "select pg_typeof(pending_expires_at)::text from bookings where id = ?",
                String.class,
                bookingId
        );
        String payloadType = jdbc.queryForObject(
                "select pg_typeof(payload)::text from payment_events where id = ?",
                String.class,
                paymentEventId
        );
        String responseCode = jdbc.queryForObject(
                "select payload ->> 'vnp_ResponseCode' from payment_events where id = ?",
                String.class,
                paymentEventId
        );

        assertThat(amount).isEqualByComparingTo("2369135.78");
        assertThat(stayDateMatches).isTrue();
        assertThat(pendingExpiryType).isEqualTo("timestamp with time zone");
        assertThat(payloadType).isEqualTo("jsonb");
        assertThat(responseCode).isEqualTo("00");
    }

    private void insertAccount(UUID accountId, String email) {
        jdbc.update(
                """
                insert into accounts (id, email, password_hash, first_name, last_name, email_verified)
                values (?, ?, 'hash', 'Test', 'Owner', true)
                """,
                accountId,
                email
        );
    }

    private void insertHotel(UUID hotelId, UUID ownerId, String slug) {
        jdbc.update(
                """
                insert into hotels (id, owner_id, name, slug, status)
                values (?, ?, 'Test Hotel', ?, 'ACTIVE')
                """,
                hotelId,
                ownerId,
                slug
        );
    }
}
