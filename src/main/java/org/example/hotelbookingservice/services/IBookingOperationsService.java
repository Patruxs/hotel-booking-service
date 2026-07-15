package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.dto.request.booking.operations.*;
import org.example.hotelbookingservice.dto.response.booking.operations.*;
import org.example.hotelbookingservice.dto.response.hotel.operations.PaginatedResponse;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.UUID;

public interface IBookingOperationsService {
    BookingResponse createBooking(UUID hotelId, BookingCreateRequest request, Authentication authentication);
    PaginatedResponse<BookingResponse> listMine(int limit, int offset, String status, Authentication authentication);
    BookingResponse mineDetail(UUID bookingId, Authentication authentication);
    PaginatedResponse<BookingResponse> listHotelBookings(UUID hotelId, int limit, int offset, String status, String q, Authentication authentication);
    BookingResponse hotelBookingDetail(UUID hotelId, UUID bookingId, Authentication authentication);
    BookingResponse cancelMine(UUID bookingId, Authentication authentication);
    BookingResponse cancelHotelBooking(UUID hotelId, UUID bookingId, Authentication authentication);
    BookingResponse updateStatus(UUID hotelId, UUID bookingId, String requestedStatus, Authentication authentication);
    BookingResponse checkIn(UUID hotelId, UUID bookingId, CheckInRequest request, Authentication authentication);
    CheckInDetailResponse checkInDetail(UUID hotelId, UUID bookingId, Authentication authentication);
    PaymentStartResponse startPayment(UUID bookingId, PaymentStartRequest request, Authentication authentication);
    String handleVnpayReturn(Map<String, String> params);
    Map<String, String> handleVnpayIpn(Map<String, String> params);
    void expireDuePendingBookings();
    void expireBookingIfDue(UUID bookingId);
}
