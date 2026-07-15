package org.example.hotelbookingservice.dto.response.hotel.operations;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RoomTypeResponse(
        UUID id,
        UUID hotelId,
        String name,
        String description,
        @JsonProperty("price_per_night") BigDecimal pricePerNight,
        @JsonProperty("max_guests") int maxGuests,
        int numberOfBedrooms,
        boolean active,
        Instant deletedAt,
        Instant createdAt,
        Instant updatedAt,
        List<AmenityResponse> amenities
) {
}
