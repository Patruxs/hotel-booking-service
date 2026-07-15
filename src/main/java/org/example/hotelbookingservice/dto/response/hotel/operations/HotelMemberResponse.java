package org.example.hotelbookingservice.dto.response.hotel.operations;

import java.time.Instant;
import java.util.UUID;

public record HotelMemberResponse(
        UUID hotelId,
        UUID accountId,
        String email,
        String firstName,
        String lastName,
        Instant createdAt,
        boolean owner
) {
}
