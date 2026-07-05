package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Room;
import org.example.hotelbookingservice.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    @Query(value = """
                SELECT r.* FROM room r
                WHERE (:#{#roomType == null} = true OR r.type = :#{#roomType?.name()})
                AND r.id NOT IN (
                    SELECT br.room_id
                    FROM booking b
                    JOIN booking_room br ON br.booking_id = b.id
                    WHERE b.status IN ('BOOKED', 'CHECKED_IN')
                    AND (:checkInDate < b.checkoutDate AND :checkOutDate > b.checkinDate)
                    GROUP BY br.room_id
                    HAVING COUNT(*) >= r.amount
                )
            """, nativeQuery = true)
    List<Room> findAvailableRooms(@Param("checkInDate") LocalDate checkinDate, @Param("checkOutDate") LocalDate checkoutDate, @Param("roomType") RoomType roomType);


    @Query(value = """
               SELECT r.* FROM room r
               WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :searchParam, '%'))
                  OR LOWER(CAST(r.type AS text)) LIKE LOWER(CONCAT('%', :searchParam, '%'))
                  OR CAST(r.price AS text) LIKE CONCAT('%', :searchParam, '%')
                  OR CAST(r.capacity AS text) LIKE CONCAT('%', :searchParam, '%')
                  OR LOWER(r.description) LIKE LOWER(CONCAT('%', :searchParam, '%'))
            """, nativeQuery = true)
    List<Room> searchRooms(@Param("searchParam") String searchParam);

    @Query(value = """
            SELECT r.* FROM room r
            WHERE r.hotel_id = :hotelId
            AND r.id NOT IN (
                SELECT br.room_id
                FROM booking b
                JOIN booking_room br ON br.booking_id = b.id
                WHERE b.status IN ('BOOKED', 'CHECKED_IN')
                AND (:checkInDate <= b.checkoutDate AND :checkOutDate >= b.checkinDate)
            )
            """, nativeQuery = true)
    List<Room> findAvailableRoomsByHotelId(
            @Param("hotelId") Integer hotelId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );


}
