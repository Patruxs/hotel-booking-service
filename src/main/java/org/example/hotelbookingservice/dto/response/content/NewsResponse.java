package org.example.hotelbookingservice.dto.response.content;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record NewsResponse(
        UUID id,
        String title,
        String slug,
        String summary,
        String content,
        String status,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt,
        List<NewsImageResponse> images
) {
}
