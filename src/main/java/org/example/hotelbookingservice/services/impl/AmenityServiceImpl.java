package org.example.hotelbookingservice.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.dto.request.amenity.AmenityRequest;
import org.example.hotelbookingservice.dto.response.AmenityResponse;
import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.example.hotelbookingservice.entity.*;
import org.example.hotelbookingservice.exception.AppException;
import org.example.hotelbookingservice.exception.ErrorCode;
import org.example.hotelbookingservice.mapper.AmenityMapper;
import org.example.hotelbookingservice.mapper.RoomMapper;
import org.example.hotelbookingservice.repository.*;
import org.example.hotelbookingservice.services.IAmenityService;
import org.example.hotelbookingservice.services.IUserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AmenityServiceImpl implements IAmenityService {
    private final AmenityRepository amenityRepository;
    private final AmenityMapper amenityMapper;
    private final IUserService userService;
    private final HotelRepository hotelRepository;
    private final HotelamenityRepository hotelamenityRepository;
    private final RoomRepository roomRepository;
    private final RoomamenityRepository roomamenityRepository;
    private final RoomMapper roomMapper;

    private Hotel getHotelIfOwnedByCurrentUser(Integer hotelId) {
        User currentUser = userService.getCurrentLoggedInUser();

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        if (!hotel.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return hotel;
    }


    @Override
    public List<AmenityResponse> getHotelAmenitiesByHotelId(Integer hotelId) {
        Hotel targetHotel = getHotelIfOwnedByCurrentUser(hotelId);

        if (targetHotel.getHotelAmenities() == null) return new ArrayList<>();

        return targetHotel.getHotelAmenities().stream()
                .map(ha -> amenityMapper.toAmenityResponse(ha.getAmenity()))
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomResponse> getRoomAmenitiesByHotelId(Integer hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        List<Room> rooms = new ArrayList<>(hotel.getRooms());

        return roomMapper.toRoomAmenityOnlyList(rooms);
    }

    @Override
    public List<AmenityResponse> getAllAmenities() {
        List<Amenity> amenities = amenityRepository.findAll();
        return amenityMapper.toAmenityResponseList(amenities);
    }

    @Override
    public AmenityResponse getAmenityById(Integer id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));
        return amenityMapper.toAmenityResponse(amenity);
    }

    @Override
    public AmenityResponse createAmenity(AmenityRequest request) {
        if (amenityRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.AMENITY_EXISTED);
        }

        Amenity amenity = amenityMapper.toAmenity(request);
        Amenity savedAmenity = amenityRepository.save(amenity);

        return amenityMapper.toAmenityResponse(savedAmenity);
    }

    @Override
    public AmenityResponse updateAmenity(Integer id, AmenityRequest request) {
        Amenity existingAmenity = amenityRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        // Check for duplicate names if the user changes their name
        // (except if the name is the same as the old name)
        if (!existingAmenity.getName().equalsIgnoreCase(request.getName())
                && amenityRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        amenityMapper.updateAmenityFromRequest(request, existingAmenity);

        return amenityMapper.toAmenityResponse(amenityRepository.save(existingAmenity));
    }

    @Override
    public void deleteAmenity(Integer id) {
        if (!amenityRepository.existsById(id)) {
            throw new AppException(ErrorCode.NOT_FOUND_AMENITY);
        }
        // Validate: Check if Amenity is currently being used by any hotel.
        if (hotelamenityRepository.existsByIdAmenityId(id)) {
            throw new AppException(ErrorCode.AMENITY_IN_USE);
        }
        //Validate: Check if the Amenity is currently being used by any Room.
        if (roomamenityRepository.existsByIdAmenityId(id)) {
            throw new AppException(ErrorCode.AMENITY_IN_USE);
        }

        amenityRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void removeAmenitiesFromHotel(Integer hotelId, List<Integer> amenityIds) {
        // Validate Hotel & Ownership (This logic is already in the getHotelIfOwnedByCurrentUser helper function)
        Hotel hotel = getHotelIfOwnedByCurrentUser(hotelId);

        if (amenityIds == null || amenityIds.isEmpty()) return;

        List<HotelamenityId> idsToDelete = new ArrayList<>();

        for (Integer amenityId : amenityIds) {
            // Validate: Does the Amenity ID exist in the system?
            if (!amenityRepository.existsById(amenityId)) {
                throw new AppException(ErrorCode.NOT_FOUND_AMENITY);
            }

            // 3. Validate: Does this amenity actually belong to this Hotel?
            HotelamenityId id = new HotelamenityId();
            id.setHotelId(hotel.getId());
            id.setAmenityId(amenityId);

            if (!hotelamenityRepository.existsById(id)) {
                throw new AppException(ErrorCode.NOT_FOUND_AMENITY);
            }
            idsToDelete.add(id);
        }

        hotelamenityRepository.deleteAllById(idsToDelete);
    }

    @Override
    public void removeAmenitiesFromRoom(Integer hotelId, Integer roomId, List<Integer> amenityIds) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_ROOM));

        if (!room.getHotel().getId().equals(hotelId)) {
            throw new AppException(ErrorCode.ROOM_NOT_BELONG_TO_HOTEL);
        }

        // Validate: Is the current user the owner of this hotel?
        User currentUser = userService.getCurrentLoggedInUser();
        if (!room.getHotel().getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (amenityIds == null || amenityIds.isEmpty()) return;

        List<RoomamenityId> idsToDelete = new ArrayList<>();

        for (Integer amenityId : amenityIds) {
            if (!amenityRepository.existsById(amenityId)) {
                throw new AppException(ErrorCode.NOT_FOUND_AMENITY);
            }

            //Validate: Is this amenity actually present in this room?
            RoomamenityId id = new RoomamenityId();
            id.setRoomId(roomId);
            id.setAmenityId(amenityId);

            if (!roomamenityRepository.existsById(id)) {
                throw new AppException(ErrorCode.NOT_FOUND_AMENITY);
            }
            idsToDelete.add(id);
        }
        roomamenityRepository.deleteAllById(idsToDelete);
    }

    @Override
    public List<AmenityResponse> getAmenitiesByRoomId(Integer hotelId, Integer roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_ROOM));

        if (!room.getHotel().getId().equals(hotelId)) {
            throw new AppException(ErrorCode.ROOM_NOT_BELONG_TO_HOTEL);
        }

        if (room.getRoomAmenities() == null || room.getRoomAmenities().isEmpty()) {
            return new ArrayList<>();
        }

        return room.getRoomAmenities().stream()
                .map(roomAmenity -> amenityMapper.toAmenityResponse(roomAmenity.getAmenity()))
                .collect(Collectors.toList());
    }


}
