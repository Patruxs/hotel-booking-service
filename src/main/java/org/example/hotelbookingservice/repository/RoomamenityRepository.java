package org.example.hotelbookingservice.repository;

import jakarta.transaction.Transactional;
import org.example.hotelbookingservice.entity.Roomamenity;
import org.example.hotelbookingservice.entity.RoomamenityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomamenityRepository extends JpaRepository<Roomamenity, RoomamenityId> {
    List<Roomamenity> findByIdRoomId(Integer roomId);

    boolean existsByIdAmenityId(Integer amenityId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Roomamenity ra WHERE ra.id.roomId = :roomId")
    void deleteByRoomId(Integer roomId);
}
