package org.example.hotelbookingservice.dto.response.hotel.operations;

import java.util.UUID;

public record HotelMemberCandidateResponse(
        UUID accountId,
        String email,
        String firstName,
        String lastName
) {
}
