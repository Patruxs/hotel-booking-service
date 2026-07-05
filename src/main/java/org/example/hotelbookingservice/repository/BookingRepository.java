package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Booking;
import org.example.hotelbookingservice.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    @Query(value = "SELECT * FROM booking WHERE user_id = :userId", nativeQuery = true)
    List<Booking> findByUserId(@Param("userId") Long userId); // Fetch all bookings for a specific user


    @Query(value = "SELECT * FROM booking WHERE \"bookingReference\" = :bookingReference", nativeQuery = true)
    Optional<Booking> findByBookingReference(@Param("bookingReference") String bookingReference);


    @Query(value = """
               SELECT COUNT(*) = 0
                FROM booking b
                JOIN booking_room br ON br.booking_id = b.id
                WHERE br.room_id = :roomId
                  AND :checkInDate <= b.checkoutDate
                  AND :checkOutDate >= b.checkinDate
                  AND b.status IN ('BOOKED', 'CHECKED_IN')
            """, nativeQuery = true)
    boolean isRoomAvailable(@Param("roomId") Long roomId,
                            @Param("checkInDate") LocalDate checkinDate,
                            @Param("checkOutDate") LocalDate checkoutDate);

    @Query(value = """
        SELECT h.id, h.name, CAST(EXTRACT(MONTH FROM b.checkoutDate) AS integer), COUNT(*), SUM(b.totalPrice)
        FROM booking b
        JOIN booking_room br ON br.booking_id = b.id
        JOIN room r ON r.id = br.room_id
        JOIN hotel h ON h.id = r.hotel_id
        WHERE b.status = 'CHECKED_OUT'
        AND EXTRACT(YEAR FROM b.checkoutDate) = :year
        GROUP BY h.id, h.name, EXTRACT(MONTH FROM b.checkoutDate)
    """, nativeQuery = true)
    List<Object[]> getRevenueStatistics(@Param("year") int year);

    @Query(value = """
        SELECT COUNT(*)
        FROM booking b
        JOIN booking_room br ON br.booking_id = b.id
        WHERE br.room_id = :roomId
          AND b.status IN ('BOOKED', 'CHECKED_IN')
          AND :checkInDate < b.checkoutDate
          AND :checkOutDate > b.checkinDate
    """, nativeQuery = true)
    Long countBookedRooms(@Param("roomId") Integer roomId,
                          @Param("checkInDate") LocalDate checkInDate,
                          @Param("checkOutDate") LocalDate checkOutDate);

    @Query(value = """
        SELECT COUNT(*) > 0
        FROM booking b
        WHERE b.room_number = :roomNumber
          AND b.status = 'CHECKED_IN'
          AND b.id <> :currentBookingId
    """, nativeQuery = true)
    boolean isRoomOccupied(@Param("roomNumber") String roomNumber,
                           @Param("currentBookingId") Integer currentBookingId);

    @Query(value = """
        SELECT h.id, h.name, COUNT(*), SUM(b.totalPrice)
        FROM booking b
        JOIN booking_room br ON br.booking_id = b.id
        JOIN room r ON r.id = br.room_id
        JOIN hotel h ON h.id = r.hotel_id
        WHERE b.status = 'CHECKED_OUT'
        AND b.checkoutDate BETWEEN :startDate AND :endDate
        GROUP BY h.id, h.name
    """, nativeQuery = true)
    List<Object[]> getRevenueStatisticsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = """
        SELECT *
        FROM booking
        WHERE status = :#{#status?.name()}
        AND checkoutDate BETWEEN :startDate AND :endDate
    """, nativeQuery = true)
    List<Booking> findByStatusAndCheckoutDateBetween(@Param("status") BookingStatus status,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);
}
