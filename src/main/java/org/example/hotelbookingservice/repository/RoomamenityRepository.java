package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Roomamenity;
import org.example.hotelbookingservice.entity.RoomamenityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface RoomamenityRepository extends JpaRepository<Roomamenity, RoomamenityId> {
    @Query(value = "SELECT * FROM room_type_amenities WHERE room_type_id = :roomId", nativeQuery = true)
    List<Roomamenity> findByIdRoomId(@Param("roomId") UUID roomId);

    @Query(value = "SELECT COUNT(*) > 0 FROM room_type_amenities WHERE amenity_id = :amenityId", nativeQuery = true)
    boolean existsByIdAmenityId(@Param("amenityId") UUID amenityId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM room_type_amenities WHERE room_type_id = :roomId", nativeQuery = true)
    void deleteByRoomId(@Param("roomId") UUID roomId);
}
