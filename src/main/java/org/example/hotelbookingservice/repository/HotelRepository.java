package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Integer> {
    boolean existsByNameAndLocation(String name,String location);

    List<Hotel> findByUserId(Integer userId);

    // Find new hotel
    @Query("""
                SELECT h 
                FROM Hotel h 
                JOIN h.rooms r 
                WHERE LOWER(h.location) LIKE LOWER(CONCAT('%', :location, '%')) 
                AND (:capacity IS NULL OR r.capacity >= :capacity)
                AND r.id NOT IN (
                    SELECT br.room.id 
                    FROM Booking b 
                    JOIN b.bookingrooms br 
                    WHERE b.status IN ('BOOKED', 'CHECKED_IN') 
                    AND (:checkInDate < b.checkoutDate AND :checkOutDate > b.checkinDate)
                )
                GROUP BY h.id
                HAVING COUNT(DISTINCT r.id) >= :roomQuantity 
            """)
    List<Hotel> findAvailableHotels(
            @Param("location") String location,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("capacity") Integer capacity,
            @Param("roomQuantity") long roomQuantity
    );
}
