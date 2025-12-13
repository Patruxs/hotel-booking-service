package org.example.hotelbookingservice.services.impl;

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
            throw new AppException(ErrorCode.NOT_FOUND_EXCEPTION);
        }

        amenityRepository.deleteById(id);
    }

    @Override
    public void removeAmenitiesFromHotel(Integer hotelId, List<Integer> amenityIds) {
        Hotel hotel = getHotelIfOwnedByCurrentUser(hotelId);

        //Generate a list of intermediate table IDs to delete
        List<HotelamenityId> idsToDelete = amenityIds.stream()
                .map(amenityId -> {
                    HotelamenityId id = new HotelamenityId();
                    id.setHotelId(hotel.getId());
                    id.setAmenityId(amenityId);
                    return id;
                })
                .collect(Collectors.toList());

        hotelamenityRepository.deleteAllById(idsToDelete);
    }

    @Override
    public void removeAmenitiesFromRoom(Integer hotelId, Integer roomId, List<Integer> amenityIds) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_ROOM));

        // Check room belong to hotel
        if (!room.getHotel().getId().equals(hotelId)) {
            throw new AppException(ErrorCode.ROOM_NOT_BELONG_TO_HOTEL);
        }

        //Room belongs to Hotel -> Hotel belongs to User. User must be the one logged in
        User currentUser = userService.getCurrentLoggedInUser();
        if (!room.getHotel().getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<RoomamenityId> idsToDelete = amenityIds.stream()
                .map(amenityId -> {
                    RoomamenityId id = new RoomamenityId();
                    id.setRoomId(room.getId());
                    id.setAmenityId(amenityId);
                    return id;
                })
                .collect(Collectors.toList());

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
