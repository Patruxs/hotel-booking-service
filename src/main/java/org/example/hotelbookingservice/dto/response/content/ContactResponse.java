package org.example.hotelbookingservice.dto.response.content;

import java.time.Instant;
import java.util.UUID;
import org.example.hotelbookingservice.dto.response.common.AccountSummary;

public record ContactResponse(
        UUID id,
        String name,
        String email,
        String phone,
        String subject,
        String message,
        String status,
        UUID userId,
        UUID handledById,
        AccountSummary handledBy,
        String note,
        String ip,
        String userAgent,
        Instant createdAt,
        Instant updatedAt
) {
}
