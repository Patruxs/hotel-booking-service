package org.example.hotelbookingservice.dto.response.hotel.operations;

import java.time.Instant;
import java.util.UUID;

public record AmenityResponse(
        UUID id,
        String key,
        String name,
        String type,
        boolean active,
        boolean system,
        Instant createdAt,
        Instant updatedAt,
        String iconKey
) {
}
