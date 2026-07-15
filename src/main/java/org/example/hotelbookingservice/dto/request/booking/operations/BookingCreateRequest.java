package org.example.hotelbookingservice.dto.request.booking.operations;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BookingCreateRequest(
        LocalDate checkIn,
        LocalDate checkOut,
        @NotBlank @Size(max = 120) String guestName,
        @NotBlank @Email @Size(max = 320) String guestEmail,
        @NotBlank @Size(max = 32) String guestPhone,
        @Size(max = 1000) String note,
        @Size(max = 64) String promotionCode,
        BigDecimal totalAmount,
        @NotNull @Size(min = 1) List<@Valid BookingItemRequest> items
) {
}
