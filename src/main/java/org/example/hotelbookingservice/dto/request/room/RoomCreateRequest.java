package org.example.hotelbookingservice.dto.request.room;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.hotelbookingservice.enums.RoomType;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomCreateRequest {
    @Schema(hidden = true)
    private Integer id;

    @NotNull(message = "Hotel ID is required")
    @Schema(description = "ID of the hotel to add rooms to", example = "1")
    private Integer hotelId;


    @NotNull(message = "Room type is required")
    @Schema(description = "Room type", example = "SINGLE")
    private RoomType type;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Schema(description = "Room price per night", example = "500000")
    private BigDecimal price;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1 person")
    @Schema(description = "Maximum capacity (people)", example = "2")
    private Integer capacity;

    @Schema(description = "Number of bedrooms", example = "1")
    private Integer numberOfBedrooms;

    @NotBlank(message = "Description is required")
    @Schema(description = "Detailed description of amenities", example = "Room with ocean view, two-way air conditioning...")
    private String description;

    @NotBlank(message = "Room name is required")
    @Schema(description = "Display name of the room", example = "Deluxe Ocean View")
    private String name;

    @Min(value = 1, message = "Amount of rooms must be at least 1")
    @Schema(description = "Number of rooms of this type", example = "5")
    private Integer amount;

    @Schema(description = "List of accompanying amenities", example = "[1,2]")
    private List<Integer> amenityIds;
}
