package org.example.hotelbookingservice.dto.request.hotel.operations;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BulkInventoryRequest(
        @NotNull LocalDate from,
        @NotNull LocalDate to,
        @NotNull @Min(0) Integer totalRooms,
        @NotNull @Min(0) Integer availableRooms,
        Boolean stopSell
) {
}
