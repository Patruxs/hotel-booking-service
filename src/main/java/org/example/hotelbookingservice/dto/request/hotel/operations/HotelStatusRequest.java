package org.example.hotelbookingservice.dto.request.hotel.operations;

import jakarta.validation.constraints.NotBlank;

public record HotelStatusRequest(
        @NotBlank String status
) {
}
