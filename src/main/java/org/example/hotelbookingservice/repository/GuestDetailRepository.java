package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.GuestDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestDetailRepository extends JpaRepository<GuestDetail, Integer> {
    List<GuestDetail> findByBookingId(Integer bookingId);
}
