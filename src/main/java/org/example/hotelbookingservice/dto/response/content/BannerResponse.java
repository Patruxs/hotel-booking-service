package org.example.hotelbookingservice.dto.response.content;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BannerResponse(
        UUID id,
        String title,
        String subtitle,
        String link,
        String linkType,
        int position,
        boolean isActive,
        Instant startAt,
        Instant endAt,
        List<BannerImageResponse> images,
        Instant createdAt,
        Instant updatedAt
) {
}
