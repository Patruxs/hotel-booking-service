package org.example.hotelbookingservice.dto.request.booking.operations;

public record PaymentStartRequest(
        String locale,
        String bankCode
) {
}
