package org.example.hotelbookingservice.dto.response.common;

import java.util.UUID;

public record AccountSummary(
        UUID id,
        String email,
        String firstName,
        String lastName,
        AvatarSummary avatar
) {
}
