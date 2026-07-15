package org.example.hotelbookingservice.dto.response.content;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CommissionPackageResponse(
        UUID id,
        String code,
        String name,
        String description,
        BigDecimal commissionRate,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
