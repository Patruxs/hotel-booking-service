package org.example.hotelbookingservice.dto.response.content;

import java.time.Instant;
import java.util.UUID;

public record PolicyResponse(
        UUID id,
        UUID hotelId,
        String type,
        String title,
        String content,
        boolean enabled,
        int order,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
}
