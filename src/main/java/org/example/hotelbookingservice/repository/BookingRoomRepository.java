package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Bookingroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookingRoomRepository extends JpaRepository<Bookingroom, UUID> {
    Optional<Bookingroom> findByBooking_BookingReference(String bookingReference);
}
