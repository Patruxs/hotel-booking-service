package org.example.hotelbookingservice.dto.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ReviewRequest(
        @NotNull UUID bookingId,
        @NotNull @Min(1) @Max(5) BigDecimal rating,
        @Size(max = 5000) String comment,
        List<UUID> imageIds
) {
}
