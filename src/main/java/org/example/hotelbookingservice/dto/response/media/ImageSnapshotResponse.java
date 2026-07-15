package org.example.hotelbookingservice.dto.response.media;

import java.util.UUID;

public record ImageSnapshotResponse(
        UUID id,
        UUID imageAssetId,
        String url,
        int sortOrder
) {
}
