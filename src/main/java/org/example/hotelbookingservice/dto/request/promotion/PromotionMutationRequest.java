package org.example.hotelbookingservice.dto.request.promotion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PromotionMutationRequest(
        UUID hotelId,
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 160) String name,
        @Size(max = 1000) String description,
        @NotBlank @Size(max = 16) String discountType,
        @NotNull @DecimalMin("0.00") BigDecimal discountValue,
        @DecimalMin("0.00") BigDecimal maxDiscountAmount,
        @DecimalMin("0.00") BigDecimal minBookingAmount,
        Integer totalUsageLimit,
        Integer perUserLimit,
        Instant startAt,
        Instant endAt,
        Boolean isActive
) {
}
