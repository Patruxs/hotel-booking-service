package org.example.hotelbookingservice.dto.response.media;

import java.time.Instant;
import java.util.UUID;

public record ImageAssetResponse(
        UUID id,
        UUID ownerAccountId,
        String provider,
        String publicId,
        String url,
        String secureUrl,
        Integer width,
        Integer height,
        Long bytes,
        Instant createdAt
) {
}
