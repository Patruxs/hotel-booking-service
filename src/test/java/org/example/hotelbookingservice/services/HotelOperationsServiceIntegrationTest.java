package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.dto.request.hotel.operations.AmenityRequest;
import org.example.hotelbookingservice.dto.response.hotel.operations.AvailabilityResponse;
import org.example.hotelbookingservice.dto.request.hotel.operations.HotelCreateRequest;
import org.example.hotelbookingservice.dto.request.hotel.operations.HotelUpdateRequest;
import org.example.hotelbookingservice.dto.response.hotel.operations.HotelResponse;
import org.example.hotelbookingservice.dto.request.hotel.operations.InventoryRequest;
import org.example.hotelbookingservice.dto.request.hotel.operations.BulkInventoryRequest;
import org.example.hotelbookingservice.dto.response.hotel.operations.PaginatedResponse;
import org.example.hotelbookingservice.dto.request.hotel.operations.RoomRequest;
import org.example.hotelbookingservice.dto.request.hotel.operations.RoomConditionRequest;
import org.example.hotelbookingservice.dto.request.hotel.operations.RoomTypeRequest;
import org.example.hotelbookingservice.dto.response.hotel.operations.RoomTypeResponse;
import org.example.hotelbookingservice.repository.operations.HotelOperationsRepository;
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
class HotelOperationsServiceImplIntegrationTest {
    private static final UUID OWNER_ID = UUID.fromString("20000000-0000-4000-8000-000000000001");
    private static final UUID ADMIN_ID = UUID.fromString("20000000-0000-4000-8000-000000000002");
    private static final UUID MEMBER_ID = UUID.fromString("20000000-0000-4000-8000-000000000003");
    private static final UUID OUTSIDER_ID = UUID.fromString("20000000-0000-4000-8000-000000000004");
    private static final UUID RECEPTIONIST_ID = UUID.fromString("20000000-0000-4000-8000-000000000005");
    private static final UUID HOTEL_IMAGE_ASSET_ID = UUID.fromString("20000000-0000-4000-8000-000000000101");
    private static final UUID HOTEL_IMAGE_ID = UUID.fromString("20000000-0000-4000-8000-000000000102");
    private static final String HOTEL_IMAGE_URL = "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699217/small_the-anam-_twiIcIsp2s-unsplash_wlcji8.jpg";

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("hotel_booking_service_ops_test")
            .withUsername("test")
            .withPassword("test");

    static JdbcTemplate jdbc;
    static HotelOperationsServiceImpl service;

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
        service = new HotelOperationsServiceImpl(new HotelOperationsRepository(new NamedParameterJdbcTemplate(dataSource)));
    }

    @BeforeEach
    void resetData() {
        jdbc.update("delete from review_images");
        jdbc.update("delete from reviews");
        jdbc.update("delete from payment_events");
        jdbc.update("delete from payments");
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
        insertAccount(RECEPTIONIST_ID, "receptionist@example.com");
        assignRole(OWNER_ID, "OWNER");
        assignRole(ADMIN_ID, "ADMIN");
        assignRole(MEMBER_ID, "MANAGER");
        assignRole(OUTSIDER_ID, "CUSTOMER");
        assignRole(RECEPTIONIST_ID, "RECEPTIONIST");
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
        assertThat(service.getRoom(visible.id(), room.id(), ownerAuth())).isEqualTo(room);
        assertThatThrownBy(() -> service.publicHotelDetail(emptyActive.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
    }

    @Test
    void ownerManageableHotelsAreScopedToOwnershipAndMembership() {
        HotelResponse owned = createHotel("Owned Management Hotel");
        service.changeHotelStatus(owned.id(), "ACTIVE", ownerAuth());

        HotelResponse foreign = service.createHotel(new HotelCreateRequest(
                OUTSIDER_ID,
                "Foreign Management Hotel",
                "Description",
                "456 Other Street",
                "Da Nang",
                "Vietnam",
                "foreign@example.com",
                "0900000001",
                BigDecimal.valueOf(4.0)
        ), adminAuth());
        service.changeHotelStatus(foreign.id(), "ACTIVE", adminAuth());

        PaginatedResponse<HotelResponse> manageable = service.listManageableHotels(10, 0, ownerAuth());

        assertThat(manageable.data()).extracting(HotelResponse::id).containsExactly(owned.id());
        assertThat(manageable.data().getFirst().allowedActions())
                .contains("hotels.manage", "hotel.members.manage", "reports.hotel.view");
    }

    @Test
    void managerUpdatesHotelDetailsWhenSubmittedStatusIsUnchanged() {
        HotelResponse hotel = createHotel("Manager Editable Hotel");
        service.addMembers(hotel.id(), List.of(MEMBER_ID), ownerAuth());

        HotelResponse updated = service.updateHotel(hotel.id(), new HotelUpdateRequest(
                hotel.name(),
                "Updated by manager",
                hotel.address(),
                hotel.city(),
                hotel.country(),
                hotel.email(),
                hotel.phone(),
                hotel.starRating(),
                hotel.status()
        ), memberAuth());

        assertThat(updated.description()).isEqualTo("Updated by manager");
        assertThat(updated.status()).isEqualTo(hotel.status());
    }

    @Test
    void managerCannotChangeHotelStatus() {
        HotelResponse hotel = createHotel("Manager Status Guarded Hotel");
        service.addMembers(hotel.id(), List.of(MEMBER_ID), ownerAuth());

        assertThatThrownBy(() -> service.updateHotel(hotel.id(), new HotelUpdateRequest(
                hotel.name(),
                hotel.description(),
                hotel.address(),
                hotel.city(),
                hotel.country(),
                hotel.email(),
                hotel.phone(),
                hotel.starRating(),
                "ACTIVE"
        ), memberAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Action not allowed: hotels.status.update");
    }

    @Test
    void publicHotelDetail_includesOrderedHotelImages() {
        HotelResponse hotel = createHotel("Hotel With Cloudinary Image");
        RoomTypeResponse roomType = service.createRoomType(hotel.id(), new RoomTypeRequest(
                "Deluxe", "Public room type", BigDecimal.valueOf(150), null, 2, null, 1, List.of()), ownerAuth());
        service.createRoom(hotel.id(), new RoomRequest(roomType.id(), "301", "CLEAN", true), ownerAuth());
        service.changeHotelStatus(hotel.id(), "ACTIVE", ownerAuth());
        insertHotelImage(hotel.id());

        assertThat(service.publicHotelDetail(hotel.id()).images())
                .extracting(image -> image.url())
                .containsExactly(HOTEL_IMAGE_URL);
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
    void managerCanCreateAmenitiesThatReuseTheSameIcon() {
        var first = service.createAmenity(
                new AmenityRequest("wifi-lobby", "Lobby WiFi", "GENERAL", true, "Wifi"),
                memberAuth()
        );
        var second = service.createAmenity(
                new AmenityRequest("wifi-rooms", "Room WiFi", "GENERAL", true, "Wifi"),
                memberAuth()
        );

        assertThat(first.key()).isEqualTo("wifi_lobby");
        assertThat(second.key()).isEqualTo("wifi_rooms");
        assertThat(first.iconKey()).isEqualTo("Wifi");
        assertThat(second.iconKey()).isEqualTo("Wifi");
    }

    @Test
    void amenityDisablePreservesManagementVisibilityAndHidesPublicRoomTypeAmenity() {
        HotelResponse hotel = createHotel("Amenity Hotel");
        UUID amenityId = service.createAmenity(new AmenityRequest("pool", "Pool", "GENERAL", true), ownerAuth()).id();
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

        assertThat(service.disableAmenity(amenityId, ownerAuth()).active()).isFalse();

        RoomTypeResponse management = service.roomTypeDetail(hotel.id(), roomType.id(), false);
        RoomTypeResponse publicRoomType = service.roomTypeDetail(hotel.id(), roomType.id(), true);

        assertThat(management.amenities()).hasSize(1);
        assertThat(management.amenities().getFirst().active()).isFalse();
        assertThat(publicRoomType.amenities()).isEmpty();
    }

    @Test
    void listAmenitiesFiltersByActiveStateWithoutBreakingQueryParameters() {
        UUID activeAmenityId = service.createAmenity(
                new AmenityRequest("active-pool", "Active Pool", "GENERAL", true),
                adminAuth()
        ).id();
        UUID disabledAmenityId = service.createAmenity(
                new AmenityRequest("disabled-pool", "Disabled Pool", "GENERAL", true),
                adminAuth()
        ).id();

        service.disableAmenity(disabledAmenityId, adminAuth());

        assertThat(service.listAmenities(true))
                .extracting(amenity -> amenity.id())
                .containsExactly(activeAmenityId);
        assertThat(service.listAmenities(false))
                .extracting(amenity -> amenity.id())
                .containsExactly(disabledAmenityId);
        assertThat(service.listAmenities(null))
                .extracting(amenity -> amenity.id())
                .containsExactlyInAnyOrder(activeAmenityId, disabledAmenityId);
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

    @Test
    void ownerBulkSetsInventoryAndDeletesOnlyEligibleFutureRecords() {
        HotelResponse hotel = createHotel("Bulk Inventory Hotel");
        RoomTypeResponse roomType = service.createRoomType(hotel.id(), new RoomTypeRequest(
                "Bulk Deluxe",
                null,
                BigDecimal.valueOf(180),
                null,
                2,
                null,
                1,
                List.of()
        ), ownerAuth());

        List<?> saved = service.bulkSetInventory(
                hotel.id(),
                roomType.id(),
                new BulkInventoryRequest(LocalDate.of(2027, 8, 1), LocalDate.of(2027, 8, 3), 5, 5, false),
                ownerAuth()
        );
        assertThat(saved).hasSize(3);

        var inventory = service.listInventory(
                hotel.id(),
                roomType.id(),
                LocalDate.of(2027, 8, 1),
                LocalDate.of(2027, 8, 4),
                ownerAuth()
        );
        assertThat(inventory).hasSize(3);
        service.deleteInventory(hotel.id(), roomType.id(), inventory.getFirst().id(), ownerAuth());
        assertThat(service.listInventory(hotel.id(), roomType.id(), null, null, ownerAuth())).hasSize(2);

        service.upsertInventory(
                hotel.id(),
                roomType.id(),
                new InventoryRequest(LocalDate.of(2027, 8, 4), 5, 4, false),
                ownerAuth()
        );
        assertThatThrownBy(() -> service.deleteInventory(
                hotel.id(),
                roomType.id(),
                service.listInventory(hotel.id(), roomType.id(), LocalDate.of(2027, 8, 4), LocalDate.of(2027, 8, 5), ownerAuth()).getFirst().id(),
                ownerAuth()
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("reserved or consumed capacity");
    }

    @Test
    void publicRoomTypeListBuildsSqlWithClauseBoundaries() {
        HotelResponse hotel = createHotel("Public Room Type Hotel");
        RoomTypeResponse roomType = service.createRoomType(hotel.id(), new RoomTypeRequest(
                "Visible Deluxe",
                null,
                BigDecimal.valueOf(180),
                null,
                2,
                null,
                1,
                List.of()
        ), ownerAuth());
        service.changeHotelStatus(hotel.id(), "ACTIVE", ownerAuth());

        assertThat(service.listRoomTypes(hotel.id(), false))
                .extracting(RoomTypeResponse::id)
                .containsExactly(roomType.id());
    }

    @Test
    void ownerUpdatesAndDeletesRoomsAndRoomTypesWithinHotelScope() {
        HotelResponse hotel = createHotel("Room Lifecycle Hotel");
        RoomTypeResponse roomType = service.createRoomType(hotel.id(), new RoomTypeRequest(
                "Standard",
                "Initial description",
                BigDecimal.valueOf(120),
                null,
                2,
                null,
                1,
                List.of()
        ), ownerAuth());
        var room = service.createRoom(hotel.id(), new RoomRequest(roomType.id(), "101", "CLEAN", true), ownerAuth());
        assertThat(service.getRoom(hotel.id(), room.id(), ownerAuth()).roomNumber()).isEqualTo("101");

        assertThat(service.updateRoomType(hotel.id(), roomType.id(), new RoomTypeRequest(
                null,
                "Updated description",
                null,
                BigDecimal.valueOf(140),
                null,
                3,
                null,
                null
        ), ownerAuth()).description()).isEqualTo("Updated description");
        assertThat(service.updateRoom(hotel.id(), room.id(), new RoomRequest(null, "102", "DIRTY", false), ownerAuth()).roomNumber())
                .isEqualTo("102");
        assertThat(service.listRooms(hotel.id(), ownerAuth())).extracting(item -> item.roomNumber()).containsExactly("102");

        service.deleteRoom(hotel.id(), room.id(), ownerAuth());
        service.deleteRoomType(hotel.id(), roomType.id(), ownerAuth());
        assertThat(service.listRooms(hotel.id(), ownerAuth())).isEmpty();
        assertThat(service.listRoomTypes(hotel.id(), true)).isEmpty();
    }

    @Test
    void receptionistViewsRoomsOnlyForAssignedHotel() {
        HotelResponse assignedHotel = createHotel("Receptionist Assigned Hotel");
        RoomTypeResponse assignedType = service.createRoomType(assignedHotel.id(), new RoomTypeRequest(
                "Front Desk Room", null, BigDecimal.valueOf(120), null, 2, null, 1, List.of()), ownerAuth());
        var assignedRoom = service.createRoom(
                assignedHotel.id(), new RoomRequest(assignedType.id(), "101", "CLEAN", true), ownerAuth());
        service.addMembers(assignedHotel.id(), List.of(RECEPTIONIST_ID), ownerAuth());

        HotelResponse unassignedHotel = createHotel("Receptionist Unassigned Hotel");
        RoomTypeResponse unassignedType = service.createRoomType(unassignedHotel.id(), new RoomTypeRequest(
                "Private Room", null, BigDecimal.valueOf(140), null, 2, null, 1, List.of()), ownerAuth());
        service.createRoom(unassignedHotel.id(), new RoomRequest(unassignedType.id(), "201", "CLEAN", true), ownerAuth());

        assertThat(service.listRooms(assignedHotel.id(), receptionistAuth()))
                .extracting(item -> item.id())
                .containsExactly(assignedRoom.id());
        assertThat(service.updateRoomCondition(
                assignedHotel.id(),
                assignedRoom.id(),
                new RoomConditionRequest("DIRTY"),
                receptionistAuth()
        ).condition()).isEqualTo("DIRTY");
        assertThatThrownBy(() -> service.listRooms(unassignedHotel.id(), receptionistAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Action not allowed: rooms.view");
        assertThatThrownBy(() -> service.updateRoomCondition(
                unassignedHotel.id(),
                assignedRoom.id(),
                new RoomConditionRequest("MAINTENANCE"),
                receptionistAuth()
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Action not allowed: rooms.condition.update");
    }

    @Test
    void adminManagesForeignHotelViaRoleOverrideWithoutAnyGrantedPermissions() {
        HotelResponse hotel = createHotel("Foreign Hotel"); // owned by OWNER_ID; admin is neither owner nor member
        UUID adminRoleId = jdbc.queryForObject("select id from roles where name = 'ADMIN'", UUID.class);
        // Remove every permission from the ADMIN role so only the role-based override can grant access.
        jdbc.update("delete from role_permissions where role_id = ?", adminRoleId);
        try {
            assertThat(ownerMemberExists(hotel.id(), ADMIN_ID)).isFalse();

            // hotel.members.manage (GLOBAL security.manage / HOTEL_OWNER) — admin has neither now.
            service.addMembers(hotel.id(), List.of(MEMBER_ID), adminAuth());
            assertThat(ownerMemberExists(hotel.id(), MEMBER_ID)).isTrue();

            // hotels.status.update (GLOBAL security.manage / HOTEL_OWNER) — still allowed via the override.
            assertThat(service.changeHotelStatus(hotel.id(), "ACTIVE", adminAuth()).status()).isEqualTo("ACTIVE");
        } finally {
            jdbc.update("insert into role_permissions (role_id, permission_id) select ?, id from permissions", adminRoleId);
        }
    }

    @Test
    void nonAdminOutsiderIsDeniedOwnerScopedActionWhileOwnerIsAllowed() {
        HotelResponse hotel = createHotel("Guarded Hotel"); // owned by OWNER_ID

        // Non-admin, non-member outsider stays denied — scope semantics are unchanged for non-admins.
        assertThatThrownBy(() -> service.addMembers(hotel.id(), List.of(MEMBER_ID), outsiderAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Action not allowed");

        // Owner retains access exactly as before.
        service.addMembers(hotel.id(), List.of(MEMBER_ID), ownerAuth());
        assertThat(ownerMemberExists(hotel.id(), MEMBER_ID)).isTrue();
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

    private void insertHotelImage(UUID hotelId) {
        jdbc.update("""
                insert into image_assets (id, owner_account_id, provider, public_id, url, secure_url, width, height, bytes)
                values (?, ?, 'CLOUDINARY', 'test/hotel-image', ?, ?, 1600, 900, 0)
                on conflict (id) do update set provider = excluded.provider, url = excluded.url, secure_url = excluded.secure_url
                """, HOTEL_IMAGE_ASSET_ID, OWNER_ID, HOTEL_IMAGE_URL, HOTEL_IMAGE_URL);
        jdbc.update("""
                insert into hotel_images (id, hotel_id, image_asset_id, url, sort_order)
                values (?, ?, ?, ?, 0)
                on conflict (hotel_id, sort_order) do update set image_asset_id = excluded.image_asset_id, url = excluded.url
                """, HOTEL_IMAGE_ID, hotelId, HOTEL_IMAGE_ASSET_ID, HOTEL_IMAGE_URL);
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

    private Authentication receptionistAuth() {
        return auth(RECEPTIONIST_ID, "RECEPTIONIST");
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
