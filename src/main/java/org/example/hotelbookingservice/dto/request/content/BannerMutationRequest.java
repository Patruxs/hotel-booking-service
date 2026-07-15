package org.example.hotelbookingservice.dto.request.content;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BannerMutationRequest(
        String title,
        String subtitle,
        @JsonAlias("link") String linkUrl,
        String linkType,
        Integer position,
        @JsonAlias("isActive") Boolean active,
        @JsonAlias("startAt") Instant startsAt,
        @JsonAlias("endAt") Instant endsAt,
        List<UUID> imageIds,
        List<String> images
) {
}
