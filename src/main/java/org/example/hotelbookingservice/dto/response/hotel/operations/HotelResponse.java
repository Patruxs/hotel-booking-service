package org.example.hotelbookingservice.dto.response.hotel.operations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record HotelResponse(
        UUID id,
        UUID ownerId,
        String name,
        String slug,
        String description,
        String address,
        String city,
        String country,
        String email,
        String phone,
        String status,
        BigDecimal starRating,
        Instant deletedAt,
        Instant createdAt,
        Instant updatedAt,
        List<HotelImageResponse> images,
        List<String> allowedActions,
        BigDecimal minPrice
) {
}
