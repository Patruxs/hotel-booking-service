package org.example.hotelbookingservice.dto.response.dashboard;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record NewestBookingResponse(
        UUID id,
        String guestName,
        LocalDate checkIn,
        LocalDate checkOut,
        Instant createdAt,
        List<NewestBookingItemResponse> items
) {
}
