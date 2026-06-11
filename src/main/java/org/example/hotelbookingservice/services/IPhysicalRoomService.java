package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.dto.request.physicalroom.PhysicalRoomCreateRequest;
import org.example.hotelbookingservice.dto.request.physicalroom.PhysicalRoomUpdateStatusRequest;
import org.example.hotelbookingservice.dto.response.PhysicalRoomResponse;
import org.example.hotelbookingservice.enums.RoomCondition;

import java.util.List;

public interface IPhysicalRoomService {
    PhysicalRoomResponse createPhysicalRoom(PhysicalRoomCreateRequest request);
    PhysicalRoomResponse updatePhysicalRoom(Integer id, PhysicalRoomCreateRequest request);
    PhysicalRoomResponse updateStatus(Integer id, PhysicalRoomUpdateStatusRequest request);
    void deletePhysicalRoom(Integer id);
    PhysicalRoomResponse getById(Integer id);
    List<PhysicalRoomResponse> getByRoomId(Integer roomId);
    List<PhysicalRoomResponse> getByCondition(RoomCondition condition);
    List<PhysicalRoomResponse> getAll();
}
