package org.example.hotelbookingservice.dto.request.hotel.operations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RoomConditionRequest(
        @NotBlank
        @Pattern(regexp = "CLEAN|DIRTY|MAINTENANCE")
        String condition
) {
}
