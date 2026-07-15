package org.example.hotelbookingservice.dto.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ReviewUpdateRequest(
        @Min(1) @Max(5) BigDecimal rating,
        @Size(max = 5000) String comment
) {
}
