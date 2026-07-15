package org.example.hotelbookingservice.dto.response.hotel.operations;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InventoryResponse(
        UUID id,
        UUID hotelId,
        UUID roomTypeId,
        LocalDate date,
        int totalRooms,
        int availableRooms,
        boolean stopSell,
        Instant createdAt,
        Instant updatedAt
) {
}
