package org.example.hotelbookingservice.repository;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingStayJpaMappingTest {
    private static final UUID ACCOUNT_ID = UUID.fromString("60000000-0000-4000-8000-000000000001");
    private static final UUID HOTEL_ID = UUID.fromString("60000000-0000-4000-8000-000000000002");
    private static final UUID ROOM_TYPE_ID = UUID.fromString("60000000-0000-4000-8000-000000000003");
    private static final UUID INVENTORY_ID = UUID.fromString("60000000-0000-4000-8000-000000000004");
    private static final UUID BOOKING_ID = UUID.fromString("60000000-0000-4000-8000-000000000005");
    private static final UUID CHECK_IN_ID = UUID.fromString("60000000-0000-4000-8000-000000000006");
    private static final UUID GUEST_ID = UUID.fromString("60000000-0000-4000-8000-000000000007");
    private static final LocalDate STAY_DATE = LocalDate.of(2027, 7, 1);

    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManagerFactory entityManagerFactory;
    @Autowired InventoryRepository inventoryRepository;
    @Autowired CheckInRepository checkInRepository;
    @Autowired GuestDetailRepository guestDetailRepository;

    @Test
    void bookingStayEntitiesLoadAgainstFlywaySchemaWithLazyRelationships() {
        insertParents();
        insertInventory();
        insertBookingStay();

        var inventory = inventoryRepository
                .findByHotel_IdAndRoomType_IdAndDate(HOTEL_ID, ROOM_TYPE_ID, STAY_DATE)
                .orElseThrow();
        var checkIn = checkInRepository.findWithGuestDetailsByBooking_Id(BOOKING_ID).orElseThrow();
        var guests = guestDetailRepository.findByCheckIn_IdOrderByGuestOrder(CHECK_IN_ID);
        var persistenceUnit = entityManagerFactory.getPersistenceUnitUtil();

        assertThat(inventory.getId()).isEqualTo(INVENTORY_ID);
        assertThat(inventory.getHotelId()).isEqualTo(HOTEL_ID);
        assertThat(inventory.getRoomTypeId()).isEqualTo(ROOM_TYPE_ID);
        assertThat(inventory.getDate()).isEqualTo(STAY_DATE);
        assertThat(inventory.getTotalRooms()).isEqualTo(4);
        assertThat(inventory.getAvailableRooms()).isEqualTo(2);
        assertThat(inventory.getStopSell()).isFalse();
        assertThat(persistenceUnit.isLoaded(inventory, "hotel")).isFalse();
        assertThat(persistenceUnit.isLoaded(inventory, "roomType")).isFalse();

        assertThat(checkIn.getId()).isEqualTo(CHECK_IN_ID);
        assertThat(checkIn.getBookingId()).isEqualTo(BOOKING_ID);
        assertThat(checkIn.getCheckedInByAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(checkIn.getCheckedInAt()).isNotNull();
        assertThat(checkIn.getNote()).isEqualTo("Arrived early");
        assertThat(persistenceUnit.isLoaded(checkIn, "booking")).isFalse();
        assertThat(persistenceUnit.isLoaded(checkIn, "checkedInBy")).isFalse();
        assertThat(checkIn.getGuestDetails()).hasSize(1);

        assertThat(guests).hasSize(1);
        assertThat(guests.getFirst().getUuid()).isEqualTo(GUEST_ID);
        assertThat(guests.getFirst().getCheckIn().getId()).isEqualTo(CHECK_IN_ID);
        assertThat(guests.getFirst().getFullName()).isEqualTo("Primary Guest");
        assertThat(guests.getFirst().getPrimaryGuest()).isTrue();
    }

    @Test
    void inventoryRepositoryReservesAndReleasesAvailableRoomsThroughExistingTable() {
        insertParents();
        insertInventory();

        int reserved = inventoryRepository.reserveAvailableRooms(HOTEL_ID, ROOM_TYPE_ID, STAY_DATE, 2);
        int overReserved = inventoryRepository.reserveAvailableRooms(HOTEL_ID, ROOM_TYPE_ID, STAY_DATE, 1);
        int released = inventoryRepository.releaseAvailableRooms(ROOM_TYPE_ID, STAY_DATE, 5);

        var inventory = inventoryRepository.findById(INVENTORY_ID).orElseThrow();
        assertThat(reserved).isEqualTo(1);
        assertThat(overReserved).isZero();
        assertThat(released).isEqualTo(1);
        assertThat(inventory.getAvailableRooms()).isEqualTo(4);
    }

    private void insertParents() {
        jdbc.update("""
                insert into accounts (id, email, password_hash, first_name, last_name, email_verified)
                values (?, 'booking-stay-mapping@example.com', 'hash', 'Booking', 'Stay', true)
                on conflict (id) do nothing
                """, ACCOUNT_ID);
        jdbc.update("""
                insert into hotels (id, owner_id, name, slug, status)
                values (?, ?, 'Booking Stay Hotel', 'booking-stay-hotel', 'ACTIVE')
                on conflict (id) do nothing
                """, HOTEL_ID, ACCOUNT_ID);
        jdbc.update("""
                insert into room_types (id, hotel_id, name, price_per_night, max_guests)
                values (?, ?, 'Deluxe', ?, 2)
                on conflict (id) do nothing
                """, ROOM_TYPE_ID, HOTEL_ID, BigDecimal.valueOf(100));
    }

    private void insertInventory() {
        jdbc.update("""
                insert into inventories (id, hotel_id, room_type_id, stay_date, total_rooms, available_rooms)
                values (?, ?, ?, ?, 4, 2)
                on conflict (room_type_id, stay_date) do nothing
                """, INVENTORY_ID, HOTEL_ID, ROOM_TYPE_ID, STAY_DATE);
    }

    private void insertBookingStay() {
        jdbc.update("""
                insert into bookings (
                    id, account_id, hotel_id, booking_reference, status, check_in, check_out,
                    guest_name, guest_email, guest_phone, subtotal_amount, total_amount
                )
                values (?, ?, ?, 'BSTAY-001', 'CONFIRMED', ?, ?, 'Primary Guest',
                        'guest@example.com', '0900000000', 100.00, 100.00)
                on conflict (id) do nothing
                """, BOOKING_ID, ACCOUNT_ID, HOTEL_ID, STAY_DATE, STAY_DATE.plusDays(1));
        jdbc.update("""
                insert into check_ins (id, booking_id, checked_in_by_account_id, note)
                values (?, ?, ?, 'Arrived early')
                on conflict (id) do nothing
                """, CHECK_IN_ID, BOOKING_ID, ACCOUNT_ID);
        jdbc.update("""
                insert into booking_guests (id, check_in_id, full_name, identity_number, phone, is_primary, guest_order)
                values (?, ?, 'Primary Guest', 'ID-1', '0900000000', true, 0)
                on conflict (id) do nothing
                """, GUEST_ID, CHECK_IN_ID);
    }
}
