package org.example.hotelbookingservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmenityResponse {
    @Schema(description = "ID of the amenity")
    private Integer id;

    @Schema(description = "Amenity name", example = "Swimming Pool")
    private String name;

    @Schema(description = "Amenity type", example = "Hotel Service")
    private String type;
}
