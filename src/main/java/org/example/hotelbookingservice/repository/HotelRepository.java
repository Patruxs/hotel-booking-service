package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Integer> {
    @Query(value = "SELECT COUNT(*) > 0 FROM hotel WHERE name = :name AND location = :location", nativeQuery = true)
    boolean existsByNameAndLocation(@Param("name") String name, @Param("location") String location);

    @Query(value = "SELECT * FROM hotel WHERE UserId = :userId", nativeQuery = true)
    List<Hotel> findByUserId(@Param("userId") Integer userId);

    // Find new hotel
    @Query(value = """
                SELECT h.*
                FROM hotel h
                JOIN room r ON r.hotel_id = h.id
                WHERE LOWER(h.location) LIKE LOWER(CONCAT('%', :location, '%'))
                AND (:capacity IS NULL OR r.capacity >= :capacity)
                AND r.id NOT IN (
                    SELECT br.room_id
                    FROM booking b
                    JOIN booking_room br ON br.booking_id = b.id
                    WHERE b.status IN ('BOOKED', 'CHECKED_IN')
                    AND (:checkInDate < b.checkoutDate AND :checkOutDate > b.checkinDate)
                )
                GROUP BY h.id
                HAVING COUNT(DISTINCT r.id) >= :roomQuantity
            """, nativeQuery = true)
    List<Hotel> findAvailableHotels(
            @Param("location") String location,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("capacity") Integer capacity,
            @Param("roomQuantity") long roomQuantity
    );
}
