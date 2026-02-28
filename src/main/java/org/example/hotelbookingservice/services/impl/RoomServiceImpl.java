package org.example.hotelbookingservice.services.impl;



import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.dto.request.room.RoomCreateRequest;
import org.example.hotelbookingservice.dto.response.AmenityResponse;
import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.example.hotelbookingservice.mapper.AmenityMapper;
import org.example.hotelbookingservice.mapper.RoomMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.hotelbookingservice.entity.Hotel;
import org.example.hotelbookingservice.entity.Image;
import org.example.hotelbookingservice.entity.Room;
import org.example.hotelbookingservice.entity.User;
import org.example.hotelbookingservice.enums.RoomType;
import org.example.hotelbookingservice.exception.AppException;
import org.example.hotelbookingservice.exception.ErrorCode;
import org.example.hotelbookingservice.exception.InvalidBookingStateAndDateException;
import org.example.hotelbookingservice.repository.ImageRepository;
import org.example.hotelbookingservice.repository.RoomRepository;
import org.example.hotelbookingservice.services.IRoomAmenityService;
import org.example.hotelbookingservice.services.IRoomService;
import org.example.hotelbookingservice.services.IUserService;
import org.example.hotelbookingservice.services.IFileStorageService;
import org.example.hotelbookingservice.utils.DateValidationUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements IRoomService {
    private final RoomRepository roomRepository;
    private final ImageRepository imageRepository;
    private final RoomMapper roomMapper;
    private final IUserService userService;
    private final IRoomAmenityService roomAmenityService;
    private final IFileStorageService fileStorageService;
    private final AmenityMapper amenityMapper;

    @Override
    @Transactional
    public RoomResponse addRoom(RoomCreateRequest roomCreateRequest, MultipartFile imageFile) {

        //Get current user (admin)
        User currentUser = userService.getCurrentLoggedInUser();

        //Check admin has hotel
        if (currentUser.getHotels() == null || currentUser.getHotels().isEmpty()) {
            throw new AppException(ErrorCode.NOT_FOUND_EXCEPTION);
        }
        //Find hotel by ID posted in Admin's hotel list
        Hotel targetHotel = currentUser.getHotels().stream()
                .filter(hotel -> hotel.getId().equals(roomCreateRequest.getHotelId()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        //Map DTO to Entity
        Room roomToSave = roomMapper.toRoom(roomCreateRequest);

        //Assign Hotel to room
        roomToSave.setHotel(targetHotel);


        Room savedRoom = roomRepository.save(roomToSave);

        RoomResponse response = roomMapper.toRoomResponse(savedRoom);

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileStorageService.uploadFile(imageFile);

            Image image = new Image();
            image.setPath(imageUrl);
            image.setRoom(savedRoom);
            image.setHotel(savedRoom.getHotel());
            imageRepository.save(image);

            if (response.getRoomImages() == null) {
                response.setRoomImages(new java.util.ArrayList<>());
            }
            response.getRoomImages().add(imageUrl);
        }


        // Amenity logic
        if (roomCreateRequest.getAmenityIds() != null && !roomCreateRequest.getAmenityIds().isEmpty()) {
            //Call service to save to DB
            roomAmenityService.addAmenitiesToRoom(savedRoom.getId(), roomCreateRequest.getAmenityIds());
            // Get the list just saved from DB
            var roomAmenities = roomAmenityService.getAmenitiesByRoomId(savedRoom.getId());

            List<AmenityResponse> amenityResponses = roomAmenities.stream()
                    .map(ra -> amenityMapper.toAmenityResponse(ra.getAmenity()))
                    .collect(Collectors.toList());

            response.setAmenities(amenityResponses);
        }
        return response;
    }

    @Override
    public RoomResponse updateRoom(RoomCreateRequest roomCreateRequest, MultipartFile imageFile) {
        Room existingRoom = roomRepository.findById(roomCreateRequest.getId())
                .orElseThrow(()-> new AppException(ErrorCode.NOT_FOUND_ROOM));

        if (imageFile != null && !imageFile.isEmpty()){
            String imagePath = fileStorageService.uploadFile(imageFile);

            Image image = new Image();
            image.setPath(imagePath);
            image.setRoom(existingRoom);
            image.setHotel(existingRoom.getHotel());

            imageRepository.save(image);
        }

        roomMapper.updateRoomFromRequest(roomCreateRequest, existingRoom);

        Room savedRoom = roomRepository.save(existingRoom);

        return roomMapper.toRoomResponse(existingRoom);
    }

    @Override
    public List<RoomResponse> getAllRooms() {
        List<Room> roomList = roomRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        return roomMapper.toRoomResponseList(roomList);
    }




    @Override
    public RoomResponse getRoomById(Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.NOT_FOUND_ROOM));

        return roomMapper.toRoomResponse(room);
    }




    @Override
    public void deleteRoom(Integer id) {
        if (!roomRepository.existsById(id)){
            throw new AppException(ErrorCode.NOT_FOUND_ROOM);
        }
        roomRepository.deleteById(id);
    }

    @Override
    public List<RoomResponse> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, RoomType roomType) {
        DateValidationUtils.validateCheckInAndCheckOutDates(checkInDate, checkOutDate);

        //Get the list of available rooms from DB
        List<Room> roomList = roomRepository.findAvailableRooms(checkInDate, checkOutDate, roomType);

        return roomMapper.toRoomResponseList(roomList);
    }

    @Override
    public List<RoomType> getAllRoomTypes() {
        return Arrays.asList(RoomType.values());
    }

    @Override
    public List<RoomResponse> searchRoom(String input) {
        List<Room> roomList = roomRepository.searchRooms(input);

        return roomMapper.toRoomResponseList(roomList);
    }

    @Override
    public List<RoomResponse> getAvailableRoomsByHotelId(Integer hotelId, LocalDate checkInDate, LocalDate checkOutDate) {
        DateValidationUtils.validateCheckInAndCheckOutDates(checkInDate, checkOutDate);

        List<Room> availableRooms = roomRepository.findAvailableRoomsByHotelId(hotelId, checkInDate, checkOutDate);

        return roomMapper.toRoomResponseList(availableRooms);
    }
}
