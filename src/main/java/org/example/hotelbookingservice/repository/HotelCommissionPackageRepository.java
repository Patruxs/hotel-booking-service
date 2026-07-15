package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.HotelCommissionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface HotelCommissionPackageRepository extends JpaRepository<HotelCommissionPackage, UUID> {
    @Query("""
            select assignment
            from HotelCommissionPackage assignment
            join fetch assignment.commissionPackage
            where assignment.hotelId = :hotelId
            """)
    Optional<HotelCommissionPackage> findWithPackageByHotelId(@Param("hotelId") UUID hotelId);

    @Modifying
    @Query(value = """
            insert into hotel_commission_packages (hotel_id, commission_package_id)
            values (:hotelId, :packageId)
            on conflict (hotel_id)
            do update set commission_package_id = excluded.commission_package_id, assigned_at = now()
            """, nativeQuery = true)
    void upsertAssignment(@Param("hotelId") UUID hotelId, @Param("packageId") UUID packageId);
}
