package org.example.hotelbookingservice.services.impl;



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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
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
    private final TransactionTemplate transactionTemplate;

    @Override
    public RoomResponse addRoom(RoomCreateRequest roomCreateRequest, MultipartFile imageFile) {
        String imageUrl = uploadImage(imageFile);
        return transactionTemplate.execute(status -> addRoomInTransaction(roomCreateRequest, imageUrl));
    }

    private RoomResponse addRoomInTransaction(RoomCreateRequest roomCreateRequest, String imageUrl) {

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

        if (imageUrl != null) {
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
        String imagePath = uploadImage(imageFile);
        return transactionTemplate.execute(status -> updateRoomInTransaction(roomCreateRequest, imagePath));
    }

    private RoomResponse updateRoomInTransaction(RoomCreateRequest roomCreateRequest, String imagePath) {
        Room existingRoom = roomRepository.findById(roomCreateRequest.getId())
                .orElseThrow(()-> new AppException(ErrorCode.NOT_FOUND_ROOM));

        if (imagePath != null){
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

    private String uploadImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }
        return fileStorageService.uploadFile(imageFile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        List<Room> roomList = roomRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        return roomMapper.toRoomResponseList(roomList);
    }




    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.NOT_FOUND_ROOM));

        return roomMapper.toRoomResponse(room);
    }




    @Override
    @Transactional
    public void deleteRoom(Integer id) {
        if (!roomRepository.existsById(id)){
            throw new AppException(ErrorCode.NOT_FOUND_ROOM);
        }
        roomRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, RoomType roomType) {
        //validation: Ensure the check-in date is not before today
        if (checkInDate.isBefore(LocalDate.now())){
            throw new InvalidBookingStateAndDateException("check in date cannot be before today ");
        }

        //validation: Ensure the check-out date is not before check in date
        if (checkOutDate.isBefore(checkInDate)){
            throw new InvalidBookingStateAndDateException("check out date cannot be before check in date ");
        }

        //validation: Ensure the check-in date is not same as check out date
        if (checkInDate.isEqual(checkOutDate)){
            throw new InvalidBookingStateAndDateException("check in date cannot be equal to check out date ");
        }

        //Get the list of available rooms from DB
        List<Room> roomList = roomRepository.findAvailableRooms(checkInDate, checkOutDate, roomType);

        return roomMapper.toRoomResponseList(roomList);
    }

    @Override
    public List<RoomType> getAllRoomTypes() {
        return Arrays.asList(RoomType.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> searchRoom(String input) {
        List<Room> roomList = roomRepository.searchRooms(input);

        return roomMapper.toRoomResponseList(roomList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRoomsByHotelId(Integer hotelId, LocalDate checkInDate, LocalDate checkOutDate) {
        //validate
        if (checkInDate == null || checkOutDate == null) {
            throw new InvalidBookingStateAndDateException("Check-in and Check-out dates are required");
        }
        if (checkInDate.isBefore(LocalDate.now())) {
            throw new InvalidBookingStateAndDateException("Check-in date cannot be in the past");
        }
        if (checkOutDate.isBefore(checkInDate)) {
            throw new InvalidBookingStateAndDateException("Check-out date cannot be before check-in date");
        }
        if (checkInDate.isEqual(checkOutDate)) {
            throw new InvalidBookingStateAndDateException("Check-in and Check-out dates cannot be the same");
        }

        List<Room> availableRooms = roomRepository.findAvailableRoomsByHotelId(hotelId, checkInDate, checkOutDate);

        return roomMapper.toRoomResponseList(availableRooms);
    }
}
