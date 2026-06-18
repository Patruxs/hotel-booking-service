package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.dto.request.booking.BookingCreateRequest;

import org.example.hotelbookingservice.dto.request.booking.BookingUpdateRequest;
import org.example.hotelbookingservice.dto.response.BookingResponse;

import java.util.List;

public interface IBookingService {

    List<BookingResponse> getAllBookings();
    BookingResponse createBooking(BookingCreateRequest bookingRequest);
    BookingResponse findBookingByReferenceNo(String  bookingReference);
    BookingResponse updateBooking(Integer bookingId, BookingUpdateRequest bookingRequest);
    void cancelBooking(Integer bookingId, String cancelReason);
}
