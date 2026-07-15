package org.example.hotelbookingservice.dto.response.booking.operations;

import java.util.UUID;

public record BookingGuestResponse(
        UUID id,
        UUID bookingId,
        UUID checkInId,
        String fullName,
        String identityNumber,
        String phone,
        boolean primary,
        int guestOrder
) {
}
