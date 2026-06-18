package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Bookingroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRoomRepository extends JpaRepository<Bookingroom, Integer> {
    Optional<Bookingroom> findByBooking_BookingReference(String bookingReference);
}
