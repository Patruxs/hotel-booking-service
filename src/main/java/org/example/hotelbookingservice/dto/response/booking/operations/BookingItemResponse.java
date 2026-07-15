package org.example.hotelbookingservice.dto.response.booking.operations;

import java.math.BigDecimal;
import java.util.UUID;
public record BookingItemResponse(
        UUID id,
        UUID bookingId,
        UUID roomTypeId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        RoomTypeSummary roomType
) {
}
