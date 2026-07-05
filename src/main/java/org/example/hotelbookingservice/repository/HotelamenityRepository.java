package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Hotelamenity;
import org.example.hotelbookingservice.entity.HotelamenityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface HotelamenityRepository extends JpaRepository<Hotelamenity, HotelamenityId> {
    @Query(value = "SELECT * FROM hotel_amenity WHERE hotel_id = :hotelId", nativeQuery = true)
    List<Hotelamenity> findByIdHotelId(@Param("hotelId") Integer hotelId);

    @Query(value = "SELECT COUNT(*) > 0 FROM hotel_amenity WHERE amenity_id = :amenityId", nativeQuery = true)
    boolean existsByIdAmenityId(@Param("amenityId") Integer amenityId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM hotel_amenity WHERE hotel_id = :hotelId", nativeQuery = true)
    void deleteByHotelId(@Param("hotelId") Integer hotelId);

}
