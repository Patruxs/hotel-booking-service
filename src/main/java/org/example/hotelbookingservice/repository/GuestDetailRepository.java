package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.GuestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GuestDetailRepository extends JpaRepository<GuestDetail, UUID> {
    List<GuestDetail> findByCheckIn_IdOrderByGuestOrder(UUID checkInId);

    List<GuestDetail> findByCheckIn_Booking_Id(UUID bookingId);

    long countByCheckIn_Id(UUID checkInId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "delete from booking_guests where check_in_id = :checkInId", nativeQuery = true)
    int deleteByCheckInId(@Param("checkInId") UUID checkInId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            insert into booking_guests (id, check_in_id, full_name, identity_number, phone, is_primary, guest_order)
            values (:id, :checkInId, :fullName, :identityNumber, :phone, :primaryGuest, :guestOrder)
            """, nativeQuery = true)
    int insertGuest(@Param("id") UUID id,
                    @Param("checkInId") UUID checkInId,
                    @Param("fullName") String fullName,
                    @Param("identityNumber") String identityNumber,
                    @Param("phone") String phone,
                    @Param("primaryGuest") boolean primaryGuest,
                    @Param("guestOrder") int guestOrder);
}
