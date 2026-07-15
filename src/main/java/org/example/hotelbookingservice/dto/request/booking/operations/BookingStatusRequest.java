package org.example.hotelbookingservice.dto.request.booking.operations;

import jakarta.validation.constraints.NotBlank;

public record BookingStatusRequest(
        @NotBlank String status
) {
}
