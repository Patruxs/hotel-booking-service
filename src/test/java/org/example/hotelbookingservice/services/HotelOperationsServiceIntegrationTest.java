package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.AmenityRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.AvailabilityResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.HotelCreateRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.HotelResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.InventoryRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.PaginatedResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.RoomRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.RoomTypeRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.RoomTypeResponse;
import org.example.hotelbookingservice.security.AccountAuthUser;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;
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
class HotelOperationsServiceIntegrationTest {
    private static final UUID OWNER_ID = UUID.fromString("20000000-0000-4000-8000-000000000001");
    private static final UUID ADMIN_ID = UUID.fromString("20000000-0000-4000-8000-000000000002");
    private static final UUID MEMBER_ID = UUID.fromString("20000000-0000-4000-8000-000000000003");
    private static final UUID OUTSIDER_ID = UUID.fromString("20000000-0000-4000-8000-000000000004");

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("hotel_booking_service_ops_test")
            .withUsername("test")
            .withPassword("test");

    static JdbcTemplate jdbc;
    static HotelOperationsService service;

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
        service = new HotelOperationsService(new NamedParameterJdbcTemplate(dataSource));
    }

    @BeforeEach
    void resetData() {
        jdbc.update("delete from inventories");
        jdbc.update("delete from rooms");
        jdbc.update("delete from room_type_amenities");
        jdbc.update("delete from booking_items");
        jdbc.update("delete from bookings");
        jdbc.update("delete from room_types");
        jdbc.update("delete from hotel_members");
        jdbc.update("delete from hotels");
        jdbc.update("delete from account_roles");
        jdbc.update("delete from accounts");
        jdbc.update("delete from amenities");

        insertAccount(OWNER_ID, "owner@example.com");
        insertAccount(ADMIN_ID, "admin@example.com");
        insertAccount(MEMBER_ID, "member@example.com");
        insertAccount(OUTSIDER_ID, "outsider@example.com");
        assignRole(OWNER_ID, "OWNER");
        assignRole(ADMIN_ID, "ADMIN");
        assignRole(MEMBER_ID, "MANAGER");
        assignRole(OUTSIDER_ID, "CUSTOMER");
    }

    @Test
    void hotelCreationCreatesDraftOwnerScopeAndManagementAccess() {
        HotelResponse hotel = createHotel("Owner Draft Hotel");

        assertThat(hotel.status()).isEqualTo("DRAFT");
        assertThat(hotel.ownerId()).isEqualTo(OWNER_ID);
        assertThat(ownerMemberExists(hotel.id(), OWNER_ID)).isTrue();
        assertThatThrownBy(() -> service.publicHotelDetail(hotel.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");

        service.addMembers(hotel.id(), List.of(MEMBER_ID), ownerAuth());

        assertThat(service.managementHotelDetail(hotel.id(), ownerAuth()).allowedActions())
                .contains("hotel.members.manage", "inventory.manage");
        assertThat(service.managementHotelDetail(hotel.id(), memberAuth()).allowedActions())
                .contains("hotels.manage")
                .doesNotContain("hotel.members.manage");
        assertThatThrownBy(() -> service.managementHotelDetail(hotel.id(), outsiderAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403 FORBIDDEN");
    }

    @Test
    void publicDiscoveryReturnsOnlyActiveHotelsWithRoomTypes() {
        HotelResponse emptyActive = createHotel("Empty Active Hotel");
        service.changeHotelStatus(emptyActive.id(), "ACTIVE", ownerAuth());

        HotelResponse visible = createHotel("Visible Active Hotel");
        UUID amenityId = service.createAmenity(new AmenityRequest("wifi", "Wifi", "GENERAL", true), adminAuth()).id();
        RoomTypeResponse roomType = service.createRoomType(visible.id(), new RoomTypeRequest(
                "Deluxe",
                "Public room type",
                BigDecimal.valueOf(150),
                null,
                2,
                null,
                1,
                List.of(amenityId)
        ), ownerAuth());
        var room = service.createRoom(visible.id(), new RoomRequest(
                roomType.id(),
                "201",
                "CLEAN",
                true
        ), ownerAuth());
        service.changeHotelStatus(visible.id(), "ACTIVE", ownerAuth());

        PaginatedResponse<HotelResponse> hotels = service.listPublicHotels(10, 0);

        assertThat(hotels.meta().total()).isEqualTo(1);
        assertThat(hotels.data()).extracting(HotelResponse::id).containsExactly(visible.id());
        assertThat(service.publicHotelDetail(visible.id()).name()).isEqualTo("Visible Active Hotel");
        assertThat(room.roomTypeId()).isEqualTo(roomType.id());
        assertThat(room.id()).isNotEqualTo(roomType.id());
        assertThat(service.listRooms(visible.id(), ownerAuth())).extracting(item -> item.id()).containsExactly(room.id());
        assertThatThrownBy(() -> service.publicHotelDetail(emptyActive.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
    }

    @Test
    void hotelStatusTransitionsAreRestrictedAndArchiveBlocksNonTerminalBookings() {
        HotelResponse hotel = createHotel("Lifecycle Hotel");

        assertThatThrownBy(() -> service.changeHotelStatus(hotel.id(), "SUSPENDED", ownerAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409 CONFLICT");

        HotelResponse active = service.changeHotelStatus(hotel.id(), "ACTIVE", ownerAuth());
        assertThat(active.status()).isEqualTo("ACTIVE");
        insertPendingBooking(hotel.id(), OWNER_ID);

        assertThatThrownBy(() -> service.archiveHotel(hotel.id(), ownerAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cannot archive a hotel with non-terminal bookings");

        jdbc.update("update bookings set status = 'CANCELLED'");
        HotelResponse archived = service.archiveHotel(hotel.id(), ownerAuth());
        assertThat(archived.status()).isEqualTo("ARCHIVED");
        assertThat(archived.deletedAt()).isNotNull();
    }

    @Test
    void memberApisRejectRemovingOwnerMembership() {
        HotelResponse hotel = createHotel("Member Hotel");
        service.addMembers(hotel.id(), List.of(MEMBER_ID), ownerAuth());

        assertThat(service.listMembers(hotel.id(), ownerAuth()))
                .extracting(member -> member.accountId())
                .contains(OWNER_ID, MEMBER_ID);
        assertThatThrownBy(() -> service.removeMember(hotel.id(), OWNER_ID, ownerAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cannot remove the hotel owner");

        service.removeMember(hotel.id(), MEMBER_ID, ownerAuth());
        assertThat(service.listMembers(hotel.id(), ownerAuth()))
                .extracting(member -> member.accountId())
                .containsExactly(OWNER_ID);
    }

    @Test
    void amenityDisablePreservesManagementVisibilityAndHidesPublicRoomTypeAmenity() {
        HotelResponse hotel = createHotel("Amenity Hotel");
        UUID amenityId = service.createAmenity(new AmenityRequest("pool", "Pool", "GENERAL", true), adminAuth()).id();
        RoomTypeResponse roomType = service.createRoomType(hotel.id(), new RoomTypeRequest(
                "Suite",
                "Suite with amenity",
                BigDecimal.valueOf(250),
                null,
                4,
                null,
                2,
                List.of(amenityId)
        ), ownerAuth());
        service.changeHotelStatus(hotel.id(), "ACTIVE", ownerAuth());

        assertThat(service.disableAmenity(amenityId, adminAuth()).active()).isFalse();

        RoomTypeResponse management = service.roomTypeDetail(hotel.id(), roomType.id(), false);
        RoomTypeResponse publicRoomType = service.roomTypeDetail(hotel.id(), roomType.id(), true);

        assertThat(management.amenities()).hasSize(1);
        assertThat(management.amenities().getFirst().active()).isFalse();
        assertThat(publicRoomType.amenities()).isEmpty();
    }

    @Test
    void availabilityUsesCheckoutExclusiveDatesMinimumAvailabilityAndCheapestOrdering() {
        HotelResponse hotel = createHotel("Availability Hotel");
        UUID amenityId = service.createAmenity(new AmenityRequest("breakfast", "Breakfast", "GENERAL", true), adminAuth()).id();
        RoomTypeResponse expensive = service.createRoomType(hotel.id(), new RoomTypeRequest(
                "Expensive Complete",
                null,
                BigDecimal.valueOf(200),
                null,
                2,
                null,
                1,
                List.of(amenityId)
        ), ownerAuth());
        RoomTypeResponse cheapIncomplete = service.createRoomType(hotel.id(), new RoomTypeRequest(
                "Cheap Missing Night",
                null,
                BigDecimal.valueOf(100),
                null,
                2,
                null,
                1,
                List.of()
        ), ownerAuth());
        service.changeHotelStatus(hotel.id(), "ACTIVE", ownerAuth());

        LocalDate july1 = LocalDate.of(2027, 7, 1);
        service.upsertInventory(hotel.id(), expensive.id(), new InventoryRequest(july1, 5, 3, false), ownerAuth());
        service.upsertInventory(hotel.id(), expensive.id(), new InventoryRequest(july1.plusDays(1), 5, 1, false), ownerAuth());
        service.upsertInventory(hotel.id(), expensive.id(), new InventoryRequest(july1.plusDays(2), 5, 0, true), ownerAuth());
        service.upsertInventory(hotel.id(), cheapIncomplete.id(), new InventoryRequest(july1, 5, 5, false), ownerAuth());

        PaginatedResponse<AvailabilityResponse> availability = service.publicAvailability(
                hotel.id(),
                july1,
                july1.plusDays(2),
                10,
                0
        );

        assertThat(availability.meta().total()).isEqualTo(1);
        assertThat(availability.data()).extracting(AvailabilityResponse::id).containsExactly(expensive.id());
        assertThat(availability.data().getFirst().availableRooms()).isEqualTo(1);
    }

    private HotelResponse createHotel(String name) {
        return service.createHotel(new HotelCreateRequest(
                null,
                name,
                "Description",
                "123 Main",
                "Da Nang",
                "Vietnam",
                "hotel@example.com",
                "0900000000",
                BigDecimal.valueOf(4.5)
        ), ownerAuth());
    }

    private void insertAccount(UUID accountId, String email) {
        jdbc.update("""
                insert into accounts (id, email, password_hash, first_name, last_name, email_verified)
                values (?, ?, 'hash', 'Test', 'Account', true)
                """, accountId, email);
    }

    private void assignRole(UUID accountId, String roleName) {
        jdbc.update("""
                insert into account_roles (account_id, role_id)
                select ?, id
                from roles
                where name = ?
                """, accountId, roleName);
    }

    private void insertPendingBooking(UUID hotelId, UUID accountId) {
        jdbc.update("""
                insert into bookings (
                    id, account_id, hotel_id, booking_reference, status, check_in, check_out,
                    guest_name, guest_email, guest_phone, subtotal_amount, discount_amount, total_amount
                ) values (?, ?, ?, ?, 'PENDING', date '2027-08-01', date '2027-08-03',
                    'Guest', 'guest@example.com', '0900000001', 100.00, 0.00, 100.00)
                """, UUID.randomUUID(), accountId, hotelId, "BK-" + UUID.randomUUID());
    }

    private boolean ownerMemberExists(UUID hotelId, UUID accountId) {
        Boolean exists = jdbc.queryForObject("""
                select exists (
                    select 1 from hotel_members where hotel_id = ? and account_id = ?
                )
                """, Boolean.class, hotelId, accountId);
        return Boolean.TRUE.equals(exists);
    }

    private Authentication ownerAuth() {
        return auth(OWNER_ID, "OWNER");
    }

    private Authentication adminAuth() {
        return auth(ADMIN_ID, "ADMIN");
    }

    private Authentication memberAuth() {
        return auth(MEMBER_ID, "MANAGER");
    }

    private Authentication outsiderAuth() {
        return auth(OUTSIDER_ID, "CUSTOMER");
    }

    private Authentication auth(UUID accountId, String role) {
        AccountAuthUser principal = AccountAuthUser.builder()
                .accountId(accountId)
                .email(accountId + "@example.com")
                .passwordHash("hash")
                .emailVerified(true)
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
