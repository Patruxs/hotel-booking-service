package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.PhysicalRoom;
import org.example.hotelbookingservice.enums.RoomCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhysicalRoomRepository extends JpaRepository<PhysicalRoom, UUID> {
    List<PhysicalRoom> findByRoom_Id(UUID roomId);
    long countByRoom_IdAndActiveTrue(UUID roomId);
    Optional<PhysicalRoom> findByRoomNumber(String roomNumber);
    List<PhysicalRoom> findByRoomCondition(RoomCondition roomCondition);
    List<PhysicalRoom> findByRoom_IdAndRoomCondition(UUID roomId, RoomCondition roomCondition);
    boolean existsByRoomNumber(String roomNumber);
}
