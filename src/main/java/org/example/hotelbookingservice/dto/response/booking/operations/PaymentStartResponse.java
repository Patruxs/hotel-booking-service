package org.example.hotelbookingservice.dto.response.booking.operations;

import java.util.UUID;

public record PaymentStartResponse(
        UUID paymentId,
        String merchantTxnRef,
        String paymentUrl
) {
}
