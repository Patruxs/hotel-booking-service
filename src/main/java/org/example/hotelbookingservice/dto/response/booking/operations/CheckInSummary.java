package org.example.hotelbookingservice.dto.response.booking.operations;

import java.time.Instant;
import java.util.UUID;

public record CheckInSummary(
        UUID id,
        UUID bookingId,
        UUID checkedInBy,
        Instant checkedInAt,
        Instant checkedOutAt,
        String note,
        int guestCount
) {
}
