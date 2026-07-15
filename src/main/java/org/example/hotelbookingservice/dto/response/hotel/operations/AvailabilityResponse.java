package org.example.hotelbookingservice.dto.response.hotel.operations;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AvailabilityResponse(
        UUID id,
        UUID hotelId,
        String name,
        String description,
        @JsonProperty("price_per_night") BigDecimal pricePerNight,
        @JsonProperty("max_guests") int maxGuests,
        int numberOfBedrooms,
        int availableRooms,
        List<AmenityResponse> amenities
) {
}
