package org.example.hotelbookingservice.dto.response.dashboard;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.example.hotelbookingservice.dto.response.common.AccountSummary;

public record LatestReviewResponse(
        UUID id,
        BigDecimal rating,
        String content,
        Instant createdAt,
        AccountSummary user
) {
}
