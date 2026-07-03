package org.example.hotelbookingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.BookingCreateRequest;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.BookingResponse;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.BookingStatusRequest;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.CheckInDetailResponse;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.CheckInRequest;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.PaymentStartResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.PaginatedResponse;
import org.example.hotelbookingservice.services.BookingOperationsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BookingOperationsController {
    private final BookingOperationsService bookingOperationsService;

    @PostMapping("/hotels/{hotelId}/bookings")
    public ApiResponse<BookingResponse> createBooking(
            @PathVariable UUID hotelId,
            @RequestBody @Valid BookingCreateRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<BookingResponse>builder()
                .status(201)
                .message("Booking created successfully")
                .data(bookingOperationsService.createBooking(hotelId, request, authentication))
                .build();
    }

    @GetMapping("/bookings/me")
    public ApiResponse<PaginatedResponse<BookingResponse>> listMine(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String status,
            Authentication authentication
    ) {
        return ApiResponse.<PaginatedResponse<BookingResponse>>builder()
                .status(200)
                .message("Success")
                .data(bookingOperationsService.listMine(limit, offset, status, authentication))
                .build();
    }

    @GetMapping("/bookings/me/{bookingId}")
    public ApiResponse<BookingResponse> mineDetail(
            @PathVariable UUID bookingId,
            Authentication authentication
    ) {
        return ApiResponse.<BookingResponse>builder()
                .status(200)
                .message("Success")
                .data(bookingOperationsService.mineDetail(bookingId, authentication))
                .build();
    }

    @PatchMapping("/bookings/me/{bookingId}/cancel")
    public ApiResponse<BookingResponse> cancelMine(
            @PathVariable UUID bookingId,
            Authentication authentication
    ) {
        return ApiResponse.<BookingResponse>builder()
                .status(200)
                .message("Booking cancelled successfully")
                .data(bookingOperationsService.cancelMine(bookingId, authentication))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/bookings")
    public ApiResponse<PaginatedResponse<BookingResponse>> listHotelBookings(
            @PathVariable UUID hotelId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            Authentication authentication
    ) {
        return ApiResponse.<PaginatedResponse<BookingResponse>>builder()
                .status(200)
                .message("Success")
                .data(bookingOperationsService.listHotelBookings(hotelId, limit, offset, status, q, authentication))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/bookings/{bookingId}")
    public ApiResponse<BookingResponse> hotelBookingDetail(
            @PathVariable UUID hotelId,
            @PathVariable UUID bookingId,
            Authentication authentication
    ) {
        return ApiResponse.<BookingResponse>builder()
                .status(200)
                .message("Success")
                .data(bookingOperationsService.hotelBookingDetail(hotelId, bookingId, authentication))
                .build();
    }

    @PatchMapping("/hotels/{hotelId}/bookings/{bookingId}/cancel")
    public ApiResponse<BookingResponse> cancelHotelBooking(
            @PathVariable UUID hotelId,
            @PathVariable UUID bookingId,
            Authentication authentication
    ) {
        return ApiResponse.<BookingResponse>builder()
                .status(200)
                .message("Booking cancelled successfully")
                .data(bookingOperationsService.cancelHotelBooking(hotelId, bookingId, authentication))
                .build();
    }

    @PatchMapping("/hotels/{hotelId}/bookings/{bookingId}/status")
    public ApiResponse<BookingResponse> updateStatus(
            @PathVariable UUID hotelId,
            @PathVariable UUID bookingId,
            @RequestBody @Valid BookingStatusRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<BookingResponse>builder()
                .status(200)
                .message("Booking status updated successfully")
                .data(bookingOperationsService.updateStatus(hotelId, bookingId, request.status(), authentication))
                .build();
    }

    @PostMapping("/hotels/{hotelId}/bookings/{bookingId}/check-in")
    public ApiResponse<BookingResponse> checkIn(
            @PathVariable UUID hotelId,
            @PathVariable UUID bookingId,
            @RequestBody @Valid CheckInRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<BookingResponse>builder()
                .status(200)
                .message("Booking checked in successfully")
                .data(bookingOperationsService.checkIn(hotelId, bookingId, request, authentication))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/bookings/{bookingId}/check-in")
    public ApiResponse<CheckInDetailResponse> checkInDetail(
            @PathVariable UUID hotelId,
            @PathVariable UUID bookingId,
            Authentication authentication
    ) {
        return ApiResponse.<CheckInDetailResponse>builder()
                .status(200)
                .message("Success")
                .data(bookingOperationsService.checkInDetail(hotelId, bookingId, authentication))
                .build();
    }

    @PostMapping("/bookings/{bookingId}/payments/vnpay")
    public ApiResponse<PaymentStartResponse> startPayment(
            @PathVariable UUID bookingId,
            Authentication authentication
    ) {
        return ApiResponse.<PaymentStartResponse>builder()
                .status(201)
                .message("Payment started successfully")
                .data(bookingOperationsService.startPayment(bookingId, authentication))
                .build();
    }
}
