package org.example.hotelbookingservice.utils;

import org.example.hotelbookingservice.exception.InvalidBookingStateAndDateException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DateValidationUtilsTest {

    @Test
    void validateCheckInAndCheckOutDates_validDates_noException() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(2);
        assertDoesNotThrow(() -> DateValidationUtils.validateCheckInAndCheckOutDates(checkIn, checkOut));
    }

    @Test
    void validateCheckInAndCheckOutDates_nullCheckIn_throwsException() {
        LocalDate checkOut = LocalDate.now().plusDays(2);
        assertThatThrownBy(() -> DateValidationUtils.validateCheckInAndCheckOutDates(null, checkOut))
                .isInstanceOf(InvalidBookingStateAndDateException.class)
                .hasMessage("Check-in and Check-out dates are required");
    }

    @Test
    void validateCheckInAndCheckOutDates_nullCheckOut_throwsException() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> DateValidationUtils.validateCheckInAndCheckOutDates(checkIn, null))
                .isInstanceOf(InvalidBookingStateAndDateException.class)
                .hasMessage("Check-in and Check-out dates are required");
    }

    @Test
    void validateCheckInAndCheckOutDates_checkInPast_throwsException() {
        LocalDate checkIn = LocalDate.now().minusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(2);
        assertThatThrownBy(() -> DateValidationUtils.validateCheckInAndCheckOutDates(checkIn, checkOut))
                .isInstanceOf(InvalidBookingStateAndDateException.class)
                .hasMessage("Check-in date cannot be in the past");
    }

    @Test
    void validateCheckInAndCheckOutDates_checkOutBeforeCheckIn_throwsException() {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> DateValidationUtils.validateCheckInAndCheckOutDates(checkIn, checkOut))
                .isInstanceOf(InvalidBookingStateAndDateException.class)
                .hasMessage("Check-out date cannot be before check-in date");
    }

    @Test
    void validateCheckInAndCheckOutDates_sameDates_throwsException() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> DateValidationUtils.validateCheckInAndCheckOutDates(checkIn, checkOut))
                .isInstanceOf(InvalidBookingStateAndDateException.class)
                .hasMessage("Check-in and Check-out dates cannot be the same");
    }
}
