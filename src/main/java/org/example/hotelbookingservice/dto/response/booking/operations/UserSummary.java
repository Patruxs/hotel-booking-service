package org.example.hotelbookingservice.dto.response.booking.operations;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String email,
        String firstName,
        String lastName
) {
}
