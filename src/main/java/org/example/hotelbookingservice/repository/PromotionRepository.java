package org.example.hotelbookingservice.repository;

import jakarta.persistence.LockModeType;
import org.example.hotelbookingservice.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    @Query(value = """
            select promotion
            from Promotion promotion
            left join fetch promotion.hotel hotel
            where (:search is null
                   or upper(promotion.code) like concat('%', :search, '%')
                   or upper(promotion.name) like concat('%', :search, '%'))
              and (:hotelId is null or hotel.id = :hotelId)
              and (:active is null or promotion.active = :active)
            """,
            countQuery = """
                    select count(promotion)
                    from Promotion promotion
                    left join promotion.hotel hotel
                    where (:search is null
                           or upper(promotion.code) like concat('%', :search, '%')
                           or upper(promotion.name) like concat('%', :search, '%'))
                      and (:hotelId is null or hotel.id = :hotelId)
                      and (:active is null or promotion.active = :active)
                    """)
    Page<Promotion> searchAdmin(@Param("search") String search,
                                @Param("hotelId") UUID hotelId,
                                @Param("active") Boolean active,
                                Pageable pageable);

    @Query("""
            select promotion
            from Promotion promotion
            left join fetch promotion.hotel hotel
            where promotion.active = true
              and (:search is null
                   or upper(promotion.code) like concat('%', :search, '%')
                   or upper(promotion.name) like concat('%', :search, '%'))
              and ((:hotelId is null and hotel is null)
                   or (:hotelId is not null and (hotel is null or hotel.id = :hotelId)))
              and (promotion.startsAt is null or promotion.startsAt <= :now)
              and (promotion.endsAt is null or promotion.endsAt >= :now)
              and (:subtotal is null or promotion.minBookingAmount is null or promotion.minBookingAmount <= :subtotal)
              and (promotion.totalUsageLimit is null or promotion.usedCount < promotion.totalUsageLimit)
            """)
    List<Promotion> searchPublic(@Param("search") String search,
                                 @Param("hotelId") UUID hotelId,
                                 @Param("subtotal") BigDecimal subtotal,
                                 @Param("now") Instant now,
                                 Pageable pageable);

    @Query("""
            select promotion
            from Promotion promotion
            left join fetch promotion.hotel hotel
            where upper(promotion.code) = upper(:code)
              and promotion.active = true
              and ((:hotelId is null and hotel is null)
                   or (:hotelId is not null and (hotel is null or hotel.id = :hotelId)))
              and (promotion.startsAt is null or promotion.startsAt <= :now)
              and (promotion.endsAt is null or promotion.endsAt >= :now)
              and (:subtotal is null or promotion.minBookingAmount is null or promotion.minBookingAmount <= :subtotal)
              and (promotion.totalUsageLimit is null or promotion.usedCount < promotion.totalUsageLimit)
            """)
    Optional<Promotion> findPublicEligible(@Param("code") String code,
                                           @Param("hotelId") UUID hotelId,
                                           @Param("subtotal") BigDecimal subtotal,
                                           @Param("now") Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select promotion
            from Promotion promotion
            where upper(promotion.code) = upper(:code)
              and promotion.active = true
              and (promotion.hotel is null or promotion.hotel.id = :hotelId)
              and (promotion.startsAt is null or promotion.startsAt <= :now)
              and (promotion.endsAt is null or promotion.endsAt >= :now)
              and (promotion.minBookingAmount is null or promotion.minBookingAmount <= :subtotal)
              and (promotion.totalUsageLimit is null or promotion.usedCount < promotion.totalUsageLimit)
            """)
    Optional<Promotion> findActiveForBooking(@Param("hotelId") UUID hotelId,
                                             @Param("code") String code,
                                             @Param("subtotal") BigDecimal subtotal,
                                             @Param("now") Instant now);

    @Query("""
            select count(booking)
            from Booking booking
            where booking.user.id = :accountId
              and booking.promotionId = :promotionId
              and booking.status <> org.example.hotelbookingservice.enums.BookingStatus.CANCELLED
            """)
    long countNonCancelledUsesByAccount(@Param("accountId") UUID accountId, @Param("promotionId") UUID promotionId);

    @Modifying
    @Query("update Promotion promotion set promotion.usedCount = promotion.usedCount + 1, promotion.updatedAt = :now where promotion.id = :promotionId")
    int incrementUsedCount(@Param("promotionId") UUID promotionId, @Param("now") Instant now);

    @Modifying
    @Query("""
            update Promotion promotion
            set promotion.usedCount = case when promotion.usedCount > 0 then promotion.usedCount - 1 else 0 end,
                promotion.updatedAt = :now
            where promotion.id = :promotionId
            """)
    int decrementUsedCount(@Param("promotionId") UUID promotionId, @Param("now") Instant now);
}
