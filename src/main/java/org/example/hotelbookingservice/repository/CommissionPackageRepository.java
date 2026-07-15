package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.CommissionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommissionPackageRepository extends JpaRepository<CommissionPackage, UUID> {
    List<CommissionPackage> findAllByOrderByActiveDescCodeAsc();

    Optional<CommissionPackage> findByCode(String code);

    @Query(value = """
            select cp.code as code, cp.commission_rate as commissionRate, cp.active as active
            from hotel_commission_packages hcp
            join commission_packages cp on cp.id = hcp.commission_package_id
            where hcp.hotel_id = :hotelId
            union all
            select cp.code as code, cp.commission_rate as commissionRate, cp.active as active
            from commission_packages cp
            where cp.code = 'STANDARD'
              and not exists (select 1 from hotel_commission_packages where hotel_id = :hotelId)
            limit 1
            """, nativeQuery = true)
    Optional<CommissionRateView> findCommissionForHotel(@Param("hotelId") UUID hotelId);

    interface CommissionRateView {
        String getCode();

        BigDecimal getCommissionRate();

        Boolean getActive();
    }
}
