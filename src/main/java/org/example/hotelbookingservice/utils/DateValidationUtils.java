package org.example.hotelbookingservice.utils;

import org.example.hotelbookingservice.exception.InvalidBookingStateAndDateException;

import java.time.LocalDate;

public class DateValidationUtils {

    private DateValidationUtils() {
        // Private constructor to hide the implicit public one
    }

    public static void validateCheckInAndCheckOutDates(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            throw new InvalidBookingStateAndDateException("Check-in and Check-out dates are required");
        }
        if (checkInDate.isBefore(LocalDate.now())) {
            throw new InvalidBookingStateAndDateException("Check-in date cannot be in the past");
        }
        if (checkOutDate.isBefore(checkInDate)) {
            throw new InvalidBookingStateAndDateException("Check-out date cannot be before check-in date");
        }
        if (checkInDate.isEqual(checkOutDate)) {
            throw new InvalidBookingStateAndDateException("Check-in and Check-out dates cannot be the same");
        }
    }
}
