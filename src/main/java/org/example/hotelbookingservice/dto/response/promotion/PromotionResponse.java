package org.example.hotelbookingservice.dto.response.promotion;

import org.example.hotelbookingservice.entity.Hotel;
import org.example.hotelbookingservice.entity.Promotion;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PromotionResponse(
        UUID id,
        String code,
        String name,
        String description,
        String discountType,
        BigDecimal discountValue,
        BigDecimal maxDiscountAmount,
        BigDecimal minBookingAmount,
        Integer totalUsageLimit,
        Integer usedCount,
        Integer perUserLimit,
        Instant startAt,
        Instant endAt,
        Boolean isActive,
        UUID hotelId,
        PromotionHotelSummary hotel,
        Instant createdAt,
        Instant updatedAt
) {
    public static PromotionResponse from(Promotion promotion) {
        Hotel hotel = promotion.getHotel();
        PromotionHotelSummary hotelSummary = hotel == null
                ? null
                : new PromotionHotelSummary(hotel.getUuid(), hotel.getName());

        return new PromotionResponse(
                promotion.getId(),
                promotion.getCode(),
                promotion.getName(),
                null,
                promotion.getDiscountType(),
                promotion.getDiscountValue(),
                promotion.getMaxDiscount(),
                promotion.getMinBookingAmount(),
                promotion.getTotalUsageLimit(),
                promotion.getUsedCount(),
                promotion.getPerUserUsageLimit(),
                promotion.getStartsAt(),
                promotion.getEndsAt(),
                promotion.isActive(),
                promotion.getHotelId(),
                hotelSummary,
                promotion.getCreatedAt(),
                promotion.getUpdatedAt()
        );
    }
}
