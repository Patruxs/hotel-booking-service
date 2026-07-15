package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.CheckIn;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {
    Optional<CheckIn> findByBooking_Id(UUID bookingId);

    @EntityGraph(attributePaths = "guestDetails")
    @Query("select c from CheckIn c where c.booking.id = :bookingId")
    Optional<CheckIn> findWithGuestDetailsByBooking_Id(@Param("bookingId") UUID bookingId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            insert into check_ins (id, booking_id, checked_in_by_account_id, checked_in_at, note)
            values (:id, :bookingId, :checkedInBy, now(), :note)
            """, nativeQuery = true)
    int insertCheckIn(@Param("id") UUID id,
                      @Param("bookingId") UUID bookingId,
                      @Param("checkedInBy") UUID checkedInBy,
                      @Param("note") String note);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update check_ins
            set note = :note
            where id = :id
            """, nativeQuery = true)
    int updateNote(@Param("id") UUID id, @Param("note") String note);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update check_ins
            set checked_out_at = coalesce(checked_out_at, now())
            where id = :id
            """, nativeQuery = true)
    int markCheckedOut(@Param("id") UUID id);
}
