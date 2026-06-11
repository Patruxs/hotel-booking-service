package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.PhysicalRoom;
import org.example.hotelbookingservice.enums.RoomCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PhysicalRoomRepository extends JpaRepository<PhysicalRoom, Integer> {
    List<PhysicalRoom> findByRoomId(Integer roomId);
    Optional<PhysicalRoom> findByRoomNumber(Integer roomNumber);
    List<PhysicalRoom> findByRoomCondition(RoomCondition roomCondition);
    List<PhysicalRoom> findByRoomIdAndRoomCondition(Integer roomId, RoomCondition roomCondition);
    boolean existsByRoomNumber(Integer roomNumber);
}
