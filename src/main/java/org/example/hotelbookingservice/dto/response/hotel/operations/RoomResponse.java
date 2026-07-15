package org.example.hotelbookingservice.dto.response.hotel.operations;

import java.time.Instant;
import java.util.UUID;

public record RoomResponse(
        UUID id,
        UUID hotelId,
        UUID roomTypeId,
        String roomNumber,
        String condition,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
