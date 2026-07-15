package org.example.hotelbookingservice.dto.response.content;

import java.util.UUID;

public record BannerImageResponse(UUID id, UUID bannerId, String url) {
}
