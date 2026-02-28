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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomAmenityServiceImpl implements IRoomAmenityService {
    private final RoomamenityRepository roomAmenityRepository;
    private final RoomRepository roomRepository;
    private final AmenityRepository amenityRepository;

    @Override
    public void addAmenitiesToRoom(Integer roomId, List<Integer> amenityIds) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new AppException(ErrorCode.NOT_FOUND_ROOM));

        // 1. Tìm tất cả các Amenity có trong danh sách ID gửi lên
        List<Amenity> foundAmenities = amenityRepository.findAllById(amenityIds);

        // 2. Lấy ra danh sách các ID thực sự tồn tại trong DB
        List<Integer> foundIds = foundAmenities.stream()
                .map(Amenity::getId)
                .collect(Collectors.toList());

        // 3. Tìm các ID bị thiếu (Có trong request nhưng không có trong DB)
        List<Integer> missingIds = amenityIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());

        // 4. Nếu có ID thiếu -> Báo lỗi ngay lập tức
        if (!missingIds.isEmpty()) {
            throw new NotFoundException("Could not add successfully. The following Amenity IDs do not exist: " + missingIds);
        }

        // 5. Lấy danh sách các Roomamenity đã tồn tại để tránh lỗi trùng lặp
        List<Roomamenity> existingRoomAmenities = roomAmenityRepository.findByIdRoomIdAndIdAmenityIdIn(roomId, foundIds);
        List<Integer> existingAmenityIds = existingRoomAmenities.stream()
                .map(ra -> ra.getId().getAmenityId())
                .collect(Collectors.toList());

        // 6. Lọc ra các Amenity chưa được thêm vào phòng
        List<Roomamenity> newRoomAmenities = foundAmenities.stream()
                .filter(amenity -> !existingAmenityIds.contains(amenity.getId()))
                .map(amenity -> {
                    RoomamenityId id = new RoomamenityId();
                    id.setRoomId(roomId);
                    id.setAmenityId(amenity.getId());

                    Roomamenity roomAmenity = new Roomamenity();
                    roomAmenity.setId(id);
                    roomAmenity.setRoom(room);
                    roomAmenity.setAmenity(amenity);
                    return roomAmenity;
                })
                .collect(Collectors.toList());

        // 7. Nếu có Amenity mới, tiến hành lưu hàng loạt
        if (!newRoomAmenities.isEmpty()) {
            roomAmenityRepository.saveAll(newRoomAmenities);
        }

    }

    @Override
    public void removeAmenityFromRoom(Integer roomId, Integer amenityId) {
        RoomamenityId id = new RoomamenityId();
        id.setRoomId(roomId);
        id.setAmenityId(amenityId);

        if (roomAmenityRepository.existsById(id)) {
            roomAmenityRepository.deleteById(id);
        } else {
            throw new AppException(ErrorCode.NOT_FOUND_EXCEPTION);
        }
    }

    @Override
    public List<Roomamenity> getAmenitiesByRoomId(Integer roomId) {
        return roomAmenityRepository.findByIdRoomId(roomId);
    }
}
