package org.example.hotelbookingservice.dto.request.hotel.operations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AmenityRequest(
        @Size(max = 120) String key,
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 64) String type,
        Boolean active,
        @Size(max = 120) String iconKey
) {
    public AmenityRequest(String key, String name, String type, Boolean active) {
        this(key, name, type, active, null);
    }
}
