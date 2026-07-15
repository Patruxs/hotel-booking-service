package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.enums.RoomCondition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PhysicalRoomRepositoryIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PhysicalRoomRepository physicalRoomRepository;

    @Test
    void findByRoomNumber_whenDatabaseStoresAlphanumericValue_shouldHydratePhysicalRoom() {
        UUID accountId = UUID.fromString("20000000-0000-4000-8001-000000000001");
        UUID hotelId = UUID.fromString("20000000-0000-4000-8001-000000000002");
        UUID roomTypeId = UUID.fromString("20000000-0000-4000-8001-000000000003");
        UUID physicalRoomId = UUID.fromString("20000000-0000-4000-8001-000000000004");

        insertAccount(accountId);
        insertHotel(hotelId, accountId);
        insertRoomType(roomTypeId, hotelId);
        jdbc.update(
                """
                insert into rooms (id, hotel_id, room_type_id, room_number, condition, active)
                values (?, ?, ?, 'D101', 'CLEAN', true)
                """,
                physicalRoomId,
                hotelId,
                roomTypeId
        );

        var physicalRoom = physicalRoomRepository.findByRoomNumber("D101");

        assertThat(physicalRoom).isPresent();
        assertThat(physicalRoom.orElseThrow().getRoomNumber()).isEqualTo("D101");
        assertThat(physicalRoom.orElseThrow().getRoomCondition()).isEqualTo(RoomCondition.CLEAN);
    }

    private void insertAccount(UUID accountId) {
        jdbc.update(
                """
                insert into accounts (id, email, password_hash, first_name, last_name, email_verified)
                values (?, 'physical-room-owner@example.com', 'hash', 'Physical', 'Owner', true)
                """,
                accountId
        );
    }

    private void insertHotel(UUID hotelId, UUID ownerId) {
        jdbc.update(
                """
                insert into hotels (id, owner_id, name, slug, status)
                values (?, ?, 'Physical Room Hotel', 'physical-room-hotel', 'ACTIVE')
                """,
                hotelId,
                ownerId
        );
    }

    private void insertRoomType(UUID roomTypeId, UUID hotelId) {
        jdbc.update(
                """
                insert into room_types (id, hotel_id, name, price_per_night, max_guests)
                values (?, ?, 'Deluxe', ?, 2)
                """,
                roomTypeId,
                hotelId,
                BigDecimal.valueOf(100)
        );
    }
}
