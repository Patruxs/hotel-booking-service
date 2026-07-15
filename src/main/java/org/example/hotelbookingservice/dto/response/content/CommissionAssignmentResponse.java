package org.example.hotelbookingservice.dto.response.content;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CommissionAssignmentResponse(
        UUID hotelId,
        UUID commissionPackageId,
        String packageCode,
        BigDecimal commissionRate,
        Instant assignedAt
) {
}
