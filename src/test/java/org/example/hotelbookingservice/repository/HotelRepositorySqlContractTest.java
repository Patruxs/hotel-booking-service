package org.example.hotelbookingservice.repository;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class HotelRepositorySqlContractTest {

    private static final Path HOTEL_REPOSITORY = Path.of(
            "src/main/java/org/example/hotelbookingservice/repository/HotelRepository.java"
    );

    @Test
    void hotelRepositoryNativeSqlUsesCurrentPostgresqlHotelAndRoomTypeSchema() throws Exception {
        String source = Files.readString(HOTEL_REPOSITORY);

        assertThat(source).contains("List<Hotel> findByUserId(@Param(\"userId\") UUID userId)");
        assertThat(source).contains("FROM hotels WHERE owner_id = :userId");
        assertThat(source).contains("WHERE name = :name AND city = :location");
        assertThat(source).contains("JOIN room_types r ON r.hotel_id = h.id");
        assertThat(source).contains("LOWER(h.city)");
        assertThat(source).contains("r.max_guests >= :capacity");
        assertThat(source).contains("FROM bookings b");
        assertThat(source).contains("JOIN booking_items br ON br.booking_id = b.id");
        assertThat(source).contains("br.room_type_id");
        assertThat(source).contains("b.check_out");
        assertThat(source).contains("b.check_in");

        assertThat(source).doesNotContain("FROM hotel WHERE");
        assertThat(source).doesNotContain("UserId = :userId");
        assertThat(source).doesNotContain("JOIN room r");
        assertThat(source).doesNotContain("LOWER(h.location)");
        assertThat(source).doesNotContain("r.capacity >= :capacity");
        assertThat(source).doesNotContain("FROM booking b");
        assertThat(source).doesNotContain("JOIN booking_room");
        assertThat(source).doesNotContain("br.room_id");
        assertThat(source).doesNotContain("b.checkoutDate");
        assertThat(source).doesNotContain("b.checkinDate");
    }
}
