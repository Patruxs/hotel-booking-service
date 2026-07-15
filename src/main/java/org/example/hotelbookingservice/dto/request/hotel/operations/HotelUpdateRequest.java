package org.example.hotelbookingservice.dto.request.hotel.operations;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record HotelUpdateRequest(
        @Size(max = 160) String name,
        String description,
        @Size(max = 1000) String address,
        @Size(max = 120) String city,
        @Size(max = 120) String country,
        @Email @Size(max = 320) String email,
        @Size(max = 32) String phone,
        @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal starRating,
        @Size(max = 32) String status
) {
}
