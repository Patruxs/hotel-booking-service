package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Room;
import org.example.hotelbookingservice.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    @Query("""
                SELECT r FROM Room r
                WHERE (:roomType IS NULL OR r.type = :roomType)
                AND r.id NOT IN (
                    SELECT br.room.id
                    FROM Booking b
                    JOIN b.bookingrooms br
                    WHERE b.status IN ('BOOKED', 'CHECKED_IN')
                    AND (:checkInDate < b.checkoutDate AND :checkOutDate > b.checkinDate)
                    GROUP BY br.room.id
                    HAVING COUNT(b) >= r.amount
                )
            """)
    List<Room> findAvailableRooms(@Param("checkInDate") LocalDate checkinDate, @Param("checkOutDate") LocalDate checkoutDate, @Param("roomType") RoomType roomType);


    @Query("""
               SELECT r FROM Room r
               WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :searchParam, '%'))
                  OR LOWER(CAST(r.type AS string)) LIKE LOWER(CONCAT('%', :searchParam, '%')) 
                  OR CAST(r.price AS string) LIKE CONCAT('%', :searchParam, '%')
                  OR CAST(r.capacity AS string) LIKE CONCAT('%', :searchParam, '%')
                  OR LOWER(r.description) LIKE LOWER(CONCAT('%', :searchParam, '%'))
            """)
    List<Room> searchRooms(@Param("searchParam") String searchParam);

    @Query("""
            SELECT r FROM Room r
            WHERE r.hotel.id = :hotelId
            AND r.id NOT IN (
                SELECT br.room.id
                FROM Booking b
                JOIN b.bookingrooms br
                WHERE b.status IN ('BOOKED', 'CHECKED_IN')
                AND (:checkInDate <= b.checkoutDate AND :checkOutDate >= b.checkinDate)
            )
            """)
    List<Room> findAvailableRoomsByHotelId(
            @Param("hotelId") Integer hotelId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );


}