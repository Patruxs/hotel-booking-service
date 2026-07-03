package org.example.hotelbookingservice.dto.operations;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class BookingOperationsDtos {
    private BookingOperationsDtos() {
    }

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

    public record BookingItemRequest(
            @NotNull UUID roomTypeId,
            @NotNull @Min(1) Integer quantity
    ) {
    }

    public record BookingStatusRequest(
            @NotBlank String status
    ) {
    }

    public record CheckInRequest(
            @Size(max = 1000) String note,
            @NotNull @Valid CheckInGuestRequest primary,
            List<@Valid CheckInGuestRequest> companions
    ) {
    }

    public record CheckInGuestRequest(
            UUID userId,
            @NotBlank @Size(max = 120) String fullName,
            @JsonAlias({"idNumber", "identityNumber"})
            @Size(max = 40) String identityNumber,
            @Size(max = 32) String phone,
            @Email @Size(max = 320) String email,
            @Size(max = 80) String nationality,
            @Size(max = 24) String gender,
            LocalDate dateOfBirth
    ) {
    }

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

    public record RoomTypeSummary(
            UUID id,
            String name,
            int maxGuests
    ) {
    }

    public record HotelSummary(
            UUID id,
            String name,
            String address,
            String city,
            String country
    ) {
    }

    public record UserSummary(
            UUID id,
            String email,
            String firstName,
            String lastName
    ) {
    }

    public record PromotionSummary(
            UUID id,
            String code
    ) {
    }

    public record CommissionSummary(
            String packageCode,
            BigDecimal rate,
            BigDecimal amount
    ) {
    }

    public record PaymentSummary(
            UUID id,
            UUID bookingId,
            String provider,
            String status,
            BigDecimal amount,
            String currency,
            String merchantTxnRef,
            Instant paidAt,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

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

    public record CheckInDetailResponse(
            CheckInSummary checkIn,
            List<BookingGuestResponse> guests
    ) {
    }

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

    public record PaymentStartResponse(
            UUID paymentId,
            String merchantTxnRef,
            String paymentUrl
    ) {
    }
}
