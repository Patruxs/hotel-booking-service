package org.example.hotelbookingservice.dto.response.booking.operations;

import java.util.UUID;

public record PromotionSummary(
        UUID id,
        String code
) {
}
