package org.example.hotelbookingservice.dto.response.review;

import java.util.UUID;

public record ReviewEligibilityResponse(
        boolean canReview,
        UUID bookingId,
        String reason
) {
}
