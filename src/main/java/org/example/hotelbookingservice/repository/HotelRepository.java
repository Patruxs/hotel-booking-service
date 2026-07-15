package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HotelRepository extends JpaRepository<Hotel, UUID> {
    default Optional<Hotel> findById(Integer id) {
        return id == null ? Optional.empty() : findById(new UUID(0L, id.longValue()));
    }

    default boolean existsById(Integer id) {
        return id != null && existsById(new UUID(0L, id.longValue()));
    }

    @Query(value = "SELECT COUNT(*) > 0 FROM hotels WHERE name = :name AND city = :location", nativeQuery = true)
    boolean existsByNameAndLocation(@Param("name") String name, @Param("location") String location);

    @Query(value = "SELECT * FROM hotels WHERE owner_id = :userId", nativeQuery = true)
    List<Hotel> findByUserId(@Param("userId") UUID userId);

    // Find new hotel
    @Query(value = """
                SELECT h.*
                FROM hotels h
                JOIN room_types r ON r.hotel_id = h.id
                WHERE LOWER(h.city) LIKE LOWER(CONCAT('%', :location, '%'))
                AND (:capacity IS NULL OR r.max_guests >= :capacity)
                GROUP BY h.id
                HAVING MAX(
                    (SELECT COUNT(pr.id) FROM rooms pr WHERE pr.room_type_id = r.id AND pr.active = true) - (
                        SELECT COALESCE(SUM(br.quantity), 0)
                        FROM bookings b
                        JOIN booking_items br ON br.booking_id = b.id
                        WHERE br.room_type_id = r.id
                        AND b.status IN ('BOOKED', 'CONFIRMED', 'CHECKED_IN')
                        AND (:checkInDate < b.check_out AND :checkOutDate > b.check_in)
                    )
                ) >= :roomQuantity
            """, nativeQuery = true)
    List<Hotel> findAvailableHotels(
            @Param("location") String location,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("capacity") Integer capacity,
            @Param("roomQuantity") long roomQuantity
    );
}
