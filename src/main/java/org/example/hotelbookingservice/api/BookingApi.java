package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.booking.BookingCreateRequest;
import org.example.hotelbookingservice.dto.request.booking.BookingUpdateRequest;
import org.example.hotelbookingservice.dto.response.BookingResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/api/v1/bookings")
@Tag(name = "Booking Management", description = "Manage bookings (Create, cancel, update status, view history)")
public interface BookingApi {

    @Operation(summary = "Get all bookings (ADMIN, RECEPTIONIST)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied", content = @Content) })
    @GetMapping({"", "/all"})
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('RECEPTIONIST')")
    ApiResponse<List<BookingResponse>> getAllBookings();

    @Operation(summary = "Create a new booking", description = "Customer creates booking.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Booking created successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Logic error (Check-in date > check-out date, Room fully booked...)", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room or user not found", content = @Content) })
    @PostMapping({"", "/create"})
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CUSTOMER') or hasAuthority('RECEPTIONIST')")
    ApiResponse<BookingResponse> createBooking(@RequestBody @Valid BookingCreateRequest bookingRequest);

    @Operation(summary = "Find booking by confirmation code")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking code does not exist", content = @Content) })
    @GetMapping({"/confirmation-codes/{confirmationCode}", "/get-by-confirmation-code/{confirmationCode}"})
    ApiResponse<BookingResponse> getBookingByConfirmationCode(@PathVariable String confirmationCode);

    @Operation(summary = "Update booking status (ADMIN, RECEPTIONIST)", description = "Check-in, Check-out, Cancel.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updated successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking does not exist", content = @Content) })
    @PutMapping({"/{bookingId:\\d+}", "/update/{bookingId}"})
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('RECEPTIONIST')")
    ApiResponse<BookingResponse> updateBooking(@PathVariable Integer bookingId, @RequestBody BookingUpdateRequest bookingRequest);

    @Operation(summary = "Cancel booking", description = "Customer or Admin cancels the booking.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cancelled successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot cancel (Already checked out or previously cancelled)", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized to cancel (Not the owner)", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking does not exist", content = @Content) })
    @DeleteMapping({"/{bookingId:\\d+}", "/cancel/{bookingId}"})
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CUSTOMER') or hasAuthority('RECEPTIONIST')")
    ApiResponse<Void> cancelBooking(@PathVariable Integer bookingId, @RequestParam(required = false) String reason);
}
