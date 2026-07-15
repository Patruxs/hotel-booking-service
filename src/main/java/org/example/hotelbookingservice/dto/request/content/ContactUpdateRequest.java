package org.example.hotelbookingservice.dto.request.content;

import java.util.UUID;

public record ContactUpdateRequest(
        String status,
        UUID handledById,
        String note
) {
}
