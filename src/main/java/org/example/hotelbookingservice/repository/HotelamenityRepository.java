package org.example.hotelbookingservice.repository;

import jakarta.transaction.Transactional;
import org.example.hotelbookingservice.entity.Hotelamenity;
import org.example.hotelbookingservice.entity.HotelamenityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import org.springframework.data.repository.query.Param;
import java.util.Collection;

public interface HotelamenityRepository extends JpaRepository<Hotelamenity, HotelamenityId> {
    List<Hotelamenity> findByIdHotelId(Integer hotelId);

    boolean existsByIdAmenityId(Integer amenityId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Hotelamenity ha WHERE ha.id.hotelId = :hotelId")
    void deleteByHotelId(Integer hotelId);

    int countById_HotelIdAndId_AmenityIdIn(Integer hotelId, Collection<Integer> amenityIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM Hotelamenity h WHERE h.id.hotelId = :hotelId AND h.id.amenityId IN :amenityIds")
    void deleteAllByHotelIdAndAmenityIds(@Param("hotelId") Integer hotelId, @Param("amenityIds") Collection<Integer> amenityIds);

}
