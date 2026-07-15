package org.example.hotelbookingservice.dto.response.booking.operations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentSummary(
        UUID id,
        UUID bookingId,
        String provider,
        String status,
        BigDecimal amount,
        String currency,
        String merchantTxnRef,
        Instant paidAt,
        Instant expiresAt,
        Instant createdAt,
        Instant updatedAt
) {
}
