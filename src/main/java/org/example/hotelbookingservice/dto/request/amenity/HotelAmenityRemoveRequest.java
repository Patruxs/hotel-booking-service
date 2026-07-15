package org.example.hotelbookingservice.dto.request.amenity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class HotelAmenityRemoveRequest {

    @NotNull(message = "Amenity list cannot be null")
    @NotEmpty(message = "Amenity list to be removed cannot be empty")
    @Schema(description = "List of amenity IDs to be removed from the hotel", example = "[1, 2, 5]")
    private List<Integer> amenityIds;
}