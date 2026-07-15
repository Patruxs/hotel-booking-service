package org.example.hotelbookingservice.dto.response.media;

import java.time.Instant;
import java.util.UUID;

public record GalleryFolderResponse(
        UUID id,
        UUID ownerAccountId,
        String name,
        String folderName,
        Instant createdAt,
        Instant updatedAt
) {
}
