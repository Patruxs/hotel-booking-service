package org.example.hotelbookingservice.dto.response.booking.operations;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID hotelId,
        UUID userId,
        String bookingReference,
        String status,
        LocalDate checkIn,
        LocalDate checkOut,
        String guestName,
        String guestEmail,
        String guestPhone,
        String note,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        PromotionSummary promotion,
        HotelSummary hotel,
        UserSummary user,
        CommissionSummary commission,
        List<BookingItemResponse> items,
        List<PaymentSummary> payments,
        CheckInSummary checkInDetail,
        Instant pendingExpiresAt,
        Instant cancelledAt,
        Instant completedAt,
        Instant noShowAt,
        Instant createdAt,
        Instant updatedAt
) {
}
