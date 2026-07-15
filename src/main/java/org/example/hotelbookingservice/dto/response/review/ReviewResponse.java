package org.example.hotelbookingservice.dto.response.review;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.example.hotelbookingservice.dto.response.common.AccountSummary;
import org.example.hotelbookingservice.dto.response.booking.operations.HotelSummary;
import org.example.hotelbookingservice.dto.response.media.ImageSnapshotResponse;

public record ReviewResponse(
        UUID id,
        UUID bookingId,
        UUID hotelId,
        UUID accountId,
        BigDecimal rating,
        String comment,
        boolean visible,
        Instant createdAt,
        Instant updatedAt,
        List<ImageSnapshotResponse> images,
        AccountSummary user,
        HotelSummary hotel
) {
}
