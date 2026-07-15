package org.example.hotelbookingservice.dto.response.content;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String title,
        String body,
        String linkUrl,
        boolean read,
        Instant readAt,
        Instant createdAt
) {
}
