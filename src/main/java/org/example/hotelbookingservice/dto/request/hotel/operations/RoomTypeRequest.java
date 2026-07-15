package org.example.hotelbookingservice.dto.request.hotel.operations;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RoomTypeRequest(
        @Size(max = 160) String name,
        String description,
        @JsonProperty("price_per_night") BigDecimal pricePerNightSnake,
        BigDecimal pricePerNight,
        @JsonProperty("max_guests") Integer maxGuestsSnake,
        Integer maxGuests,
        Integer numberOfBedrooms,
        List<UUID> amenityIds
) {
    public BigDecimal resolvedPricePerNight() {
        return pricePerNightSnake != null ? pricePerNightSnake : pricePerNight;
    }

    public Integer resolvedMaxGuests() {
        return maxGuestsSnake != null ? maxGuestsSnake : maxGuests;
    }
}
