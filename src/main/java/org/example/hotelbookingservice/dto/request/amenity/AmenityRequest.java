package org.example.hotelbookingservice.dto.request.amenity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AmenityRequest {
    @NotBlank(message = "Amenity name is required")
    @Schema(description = "Amenity name", example = "Free Wi-Fi")
    private String name;

    @NotBlank(message = "Amenity type is required")
    @Schema (description = "Amenity type (Room, Hotel, Pool...)", example = "Hotel Service")
    private String type;
}
