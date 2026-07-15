package org.example.hotelbookingservice.dto.response.booking.operations;

import java.util.UUID;

public record HotelSummary(
        UUID id,
        String name,
        String address,
        String city,
        String country
) {
}
