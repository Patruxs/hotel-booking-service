package org.example.hotelbookingservice.dto.request.room;

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
public class RoomUpdateRequest {

    @NotNull(message = "Room type is required")
    private RoomType type;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1 person")
    private Integer capacity;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Room name is required")
    private String name;

    @Min(value = 1, message = "Amount of rooms must be at least 1")
    private Integer amount;

    private List<String> amenities;
}
