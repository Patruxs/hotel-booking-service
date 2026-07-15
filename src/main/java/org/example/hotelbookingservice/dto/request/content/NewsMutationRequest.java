package org.example.hotelbookingservice.dto.request.content;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record NewsMutationRequest(
        @Size(max = 180) String title,
        @Size(max = 500) String summary,
        String content,
        String status,
        List<UUID> imageIds
) {
}
