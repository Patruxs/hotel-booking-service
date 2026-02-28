package org.example.hotelbookingservice.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.booking.BookingCreateRequest;
import org.example.hotelbookingservice.dto.request.booking.BookingUpdateRequest;
import org.example.hotelbookingservice.dto.response.BookingResponse;
import org.example.hotelbookingservice.services.IBookingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.example.hotelbookingservice.api.BookingApi;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController implements BookingApi {

    IBookingService bookingService;

    @Override
    public ApiResponse<List<BookingResponse>> getAllBookings() {
        return ApiResponse.<List<BookingResponse>>builder().status(200).message("Success").data(bookingService.getAllBookings()).build();
    }

    @Override
    public ApiResponse<BookingResponse> createBooking(@RequestBody BookingCreateRequest bookingRequest) {
        return ApiResponse.<BookingResponse>builder().status(201).message("Booking successful").data(bookingService.createBooking(bookingRequest)).build();
    }

    @Override
    public ApiResponse<BookingResponse> getBookingByConfirmationCode(@PathVariable String confirmationCode) {
        return ApiResponse.<BookingResponse>builder().status(200).message("Success").data(bookingService.findBookingByReferenceNo(confirmationCode)).build();
    }

    @Override
    public ApiResponse<BookingResponse> updateBooking(@PathVariable Integer bookingId, @RequestBody BookingUpdateRequest bookingRequest) {
        return ApiResponse.<BookingResponse>builder().status(200).message("Booking Updated Successfully").data(bookingService.updateBooking(bookingId, bookingRequest)).build();
    }

    @Override
    public ApiResponse<Void> cancelBooking(@PathVariable Integer bookingId, @RequestParam(required = false) String reason) {
        bookingService.cancelBooking(bookingId, reason);
        return ApiResponse.<Void>builder().status(200).message("Booking cancelled successfully").build();
    }
}
