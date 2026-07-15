package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.HotelImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HotelImageRepository extends JpaRepository<HotelImage, UUID> {
    List<HotelImage> findByHotel_IdOrderBySortOrderAsc(UUID hotelId);

    void deleteByHotel_Id(UUID hotelId);
}
