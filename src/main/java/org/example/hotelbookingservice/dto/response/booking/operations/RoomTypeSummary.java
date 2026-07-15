package org.example.hotelbookingservice.dto.response.booking.operations;

import java.util.UUID;

public record RoomTypeSummary(
        UUID id,
        String name,
        int maxGuests
) {
}
