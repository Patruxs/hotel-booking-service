package org.example.hotelbookingservice.dto.request.media;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ImageSnapshotRequest(
        @NotNull List<UUID> imageIds
) {
}
