package org.example.hotelbookingservice.dto.request.booking.operations;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BookingItemRequest(
        @NotNull UUID roomTypeId,
        @NotNull @Min(1) Integer quantity
) {
}
