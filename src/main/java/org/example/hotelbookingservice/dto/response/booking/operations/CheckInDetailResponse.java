package org.example.hotelbookingservice.dto.response.booking.operations;

import java.util.List;

public record CheckInDetailResponse(
        CheckInSummary checkIn,
        List<BookingGuestResponse> guests
) {
}
