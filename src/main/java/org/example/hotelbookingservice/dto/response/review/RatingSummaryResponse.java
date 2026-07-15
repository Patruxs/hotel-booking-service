package org.example.hotelbookingservice.dto.response.review;

import java.math.BigDecimal;

public record RatingSummaryResponse(
        BigDecimal averageRating,
        long reviewCount
) {
}
