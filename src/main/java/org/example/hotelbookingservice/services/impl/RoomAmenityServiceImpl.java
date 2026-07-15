package org.example.hotelbookingservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.entity.Amenity;
import org.example.hotelbookingservice.entity.Room;
import org.example.hotelbookingservice.entity.Roomamenity;
import org.example.hotelbookingservice.entity.RoomamenityId;
import org.example.hotelbookingservice.exception.AppException;
import org.example.hotelbookingservice.exception.ErrorCode;
import org.example.hotelbookingservice.exception.NotFoundException;
import org.example.hotelbookingservice.repository.AmenityRepository;
import org.example.hotelbookingservice.repository.RoomRepository;
import org.example.hotelbookingservice.repository.RoomamenityRepository;
import org.example.hotelbookingservice.services.IRoomAmenityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomAmenityServiceImpl implements IRoomAmenityService {
    private final RoomamenityRepository roomAmenityRepository;
    private final RoomRepository roomRepository;
    private final AmenityRepository amenityRepository;

    @Override
    @Transactional
    public void addAmenitiesToRoom(Integer roomId, List<Integer> amenityIds) {
        Room room = roomRepository.findById(new UUID(0L, roomId.longValue()))
                .orElseThrow(()-> new AppException(ErrorCode.NOT_FOUND_ROOM));

        // 1. Find all amenities present in the list of sent IDs
        List<UUID> uuids = amenityIds.stream()
                .map(id -> new UUID(0L, id.longValue()))
                .collect(Collectors.toList());
        List<Amenity> foundAmenities = amenityRepository.findAllById(uuids);

        // 2. Get the list of IDs that actually exist in the DB
        List<Integer> foundIds = foundAmenities.stream()
                .map(Amenity::getId)
                .collect(Collectors.toList());

        // 3. Find the missing IDs (present in the request but not in the DB)
        List<Integer> missingIds = amenityIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());

        // 4. If there are missing IDs -> Throw an error immediately
        if (!missingIds.isEmpty()) {
            throw new NotFoundException("Could not add successfully. The following Amenity IDs do not exist: " + missingIds);
        }

        // 5. If all are valid, proceed to save
        for (Amenity amenity : foundAmenities) {
            RoomamenityId id = new RoomamenityId();
            id.setRoomId(room.getUuid());
            id.setAmenityId(amenity.getUuid());

            if (!roomAmenityRepository.existsById(id)) {
                Roomamenity roomAmenity = new Roomamenity();
                roomAmenity.setId(id);
                roomAmenity.setRoom(room);
                roomAmenity.setAmenity(amenity);

                roomAmenityRepository.save(roomAmenity);
            }
        }
    }

    @Override
    @Transactional
    public void removeAmenityFromRoom(Integer roomId, Integer amenityId) {
        RoomamenityId id = new RoomamenityId();
        id.setRoomId(new UUID(0L, roomId.longValue()));
        id.setAmenityId(new UUID(0L, amenityId.longValue()));

        if (roomAmenityRepository.existsById(id)) {
            roomAmenityRepository.deleteById(id);
        } else {
            throw new AppException(ErrorCode.NOT_FOUND_EXCEPTION);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Roomamenity> getAmenitiesByRoomId(Integer roomId) {
        return roomAmenityRepository.findByIdRoomId(new UUID(0L, roomId.longValue()));
    }
}
