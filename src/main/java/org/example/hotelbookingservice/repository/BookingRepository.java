package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Booking;
import org.example.hotelbookingservice.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    default Optional<Booking> findById(Integer id) {
        return findById(legacyId(id));
    }

    default Long countBookedRooms(Integer roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        return countBookedRooms(legacyId(roomId), checkInDate, checkOutDate);
    }

    private static UUID legacyId(Integer id) {
        return id == null ? null : new UUID(0L, id.longValue());
    }

    @Query(value = "SELECT * FROM bookings WHERE account_id = :userId", nativeQuery = true)
    List<Booking> findByUserId(@Param("userId") UUID userId);


    @Query(value = "SELECT * FROM booking WHERE \"bookingReference\" = :bookingReference", nativeQuery = true)
    Optional<Booking> findByBookingReference(@Param("bookingReference") String bookingReference);


    @Query(value = """
        SELECT COALESCE(SUM(br.quantity), 0)
        FROM bookings b
        JOIN booking_items br ON br.booking_id = b.id
        WHERE br.room_type_id = :roomId
          AND b.status IN ('BOOKED', 'CHECKED_IN')
          AND :checkInDate < b.check_out
          AND :checkOutDate > b.check_in
    """, nativeQuery = true)
    Long countBookedRooms(@Param("roomId") UUID roomId,
                          @Param("checkInDate") LocalDate checkInDate,
                          @Param("checkOutDate") LocalDate checkOutDate);

    @Query(value = """
        SELECT h.id, h.name, CAST(EXTRACT(MONTH FROM b.check_out) AS integer), COUNT(*), SUM(b.total_amount)
        FROM bookings b
        JOIN booking_items br ON br.booking_id = b.id
        JOIN room_types r ON r.id = br.room_type_id
        JOIN hotels h ON h.id = r.hotel_id
        WHERE b.status = 'COMPLETED'
        AND EXTRACT(YEAR FROM b.check_out) = :year
        GROUP BY h.id, h.name, EXTRACT(MONTH FROM b.check_out)
    """, nativeQuery = true)
    List<Object[]> getRevenueStatistics(@Param("year") int year);

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
        SELECT h.id, h.name, COUNT(*), SUM(b.total_amount)
        FROM bookings b
        JOIN booking_items br ON br.booking_id = b.id
        JOIN room_types r ON r.id = br.room_type_id
        JOIN hotels h ON h.id = r.hotel_id
        WHERE b.status = 'COMPLETED'
        AND b.check_out BETWEEN :startDate AND :endDate
        GROUP BY h.id, h.name
    """, nativeQuery = true)
    List<Object[]> getRevenueStatisticsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = """
        SELECT *
        FROM bookings
        WHERE status = :#{#status?.name()}
        AND check_out BETWEEN :startDate AND :endDate
    """, nativeQuery = true)
    List<Booking> findByStatusAndCheckoutDateBetween(@Param("status") BookingStatus status,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);
}
