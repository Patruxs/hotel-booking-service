package org.example.hotelbookingservice.dto.request.amenity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RoomAmenityRemoveRequest {

    @NotNull(message = "Amenity list cannot be null")
    @NotEmpty(message = "Amenity list to be removed cannot be empty")
    @Schema(description = "List of amenity IDs to be removed from the room", example = "[3, 4]")
    private List<Integer> amenityIds;
}
