package org.example.hotelbookingservice.dto.request.hotel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HotelSearchRequest {
    private String location;
    @Schema(description = "Viết theo format YYYY-MM-DD", example = "2025-12-12")
    private LocalDate checkInDate;
    @Schema(description = "Viết theo format YYYY-MM-DD", example = "2025-12-12")
    private LocalDate checkOutDate;
    private Integer adultAmount;
    private Integer childrenAmount;
    private Integer roomQuantity;
    private Double minPrice;
    private Double maxPrice;
}
