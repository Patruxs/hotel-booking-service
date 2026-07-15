package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.RoomTypeImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomTypeImageRepository extends JpaRepository<RoomTypeImage, UUID> {
    List<RoomTypeImage> findByRoomType_IdOrderBySortOrderAsc(UUID roomTypeId);

    void deleteByRoomType_Id(UUID roomTypeId);
}
