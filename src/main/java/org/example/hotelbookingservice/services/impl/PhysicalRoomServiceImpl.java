package org.example.hotelbookingservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hotelbookingservice.dto.request.physicalroom.PhysicalRoomCreateRequest;
import org.example.hotelbookingservice.dto.request.physicalroom.PhysicalRoomUpdateStatusRequest;
import org.example.hotelbookingservice.dto.response.PhysicalRoomResponse;
import org.example.hotelbookingservice.entity.PhysicalRoom;
import org.example.hotelbookingservice.entity.Room;
import org.example.hotelbookingservice.enums.RoomCondition;
import org.example.hotelbookingservice.exception.AppException;
import org.example.hotelbookingservice.exception.ErrorCode;
import org.example.hotelbookingservice.repository.PhysicalRoomRepository;
import org.example.hotelbookingservice.repository.RoomRepository;
import org.example.hotelbookingservice.services.IPhysicalRoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PhysicalRoomServiceImpl implements IPhysicalRoomService {

    private final PhysicalRoomRepository physicalRoomRepository;
    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public PhysicalRoomResponse createPhysicalRoom(PhysicalRoomCreateRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_ROOM));

        if (physicalRoomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new AppException(ErrorCode.ROOM_ALREADY_EXISTS);
        }

        PhysicalRoom physicalRoom = new PhysicalRoom();
        physicalRoom.setRoomNumber(request.getRoomNumber());
        physicalRoom.setRoomCondition(request.getRoomCondition());
        physicalRoom.setRoom(room);

        PhysicalRoom saved = physicalRoomRepository.save(physicalRoom);
        log.info("Created physical room {} linked to room type {}", saved.getRoomNumber(), room.getName());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public PhysicalRoomResponse updatePhysicalRoom(Integer id, PhysicalRoomCreateRequest request) {
        PhysicalRoom existing = physicalRoomRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_ROOM));

        existing.setRoomNumber(request.getRoomNumber());
        existing.setRoomCondition(request.getRoomCondition());
        existing.setRoom(room);

        PhysicalRoom saved = physicalRoomRepository.save(existing);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public PhysicalRoomResponse updateStatus(Integer id, PhysicalRoomUpdateStatusRequest request) {
        PhysicalRoom existing = physicalRoomRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        existing.setRoomCondition(request.getRoomCondition());
        PhysicalRoom saved = physicalRoomRepository.save(existing);

        log.info("Physical room {} status updated to {}", saved.getRoomNumber(), saved.getRoomCondition());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deletePhysicalRoom(Integer id) {
        if (!physicalRoomRepository.existsById(id)) {
            throw new AppException(ErrorCode.NOT_FOUND_EXCEPTION);
        }
        physicalRoomRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PhysicalRoomResponse getById(Integer id) {
        PhysicalRoom physicalRoom = physicalRoomRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));
        return toResponse(physicalRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhysicalRoomResponse> getByRoomId(Integer roomId) {
        return physicalRoomRepository.findByRoomId(roomId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhysicalRoomResponse> getByCondition(RoomCondition condition) {
        return physicalRoomRepository.findByRoomCondition(condition).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhysicalRoomResponse> getAll() {
        return physicalRoomRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ---- Helper ----
    private PhysicalRoomResponse toResponse(PhysicalRoom entity) {
        return PhysicalRoomResponse.builder()
                .id(entity.getId())
                .roomNumber(entity.getRoomNumber())
                .roomCondition(entity.getRoomCondition())
                .roomId(entity.getRoom().getId())
                .roomName(entity.getRoom().getName())
                .build();
    }
}
