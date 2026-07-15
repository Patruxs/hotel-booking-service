package org.example.hotelbookingservice.dto.response.booking.operations;

import java.math.BigDecimal;

public record CommissionSummary(
        String packageCode,
        BigDecimal rate,
        BigDecimal amount
) {
}
