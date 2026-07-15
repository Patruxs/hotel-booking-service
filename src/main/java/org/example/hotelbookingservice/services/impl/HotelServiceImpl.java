package org.example.hotelbookingservice.services.impl;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hotelbookingservice.dto.request.hotel.HotelCreateRequest;
import org.example.hotelbookingservice.dto.request.hotel.HotelUpdateRequest;
import org.example.hotelbookingservice.dto.response.HotelResponse;
import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.example.hotelbookingservice.entity.*;
import org.example.hotelbookingservice.exception.AppException;
import org.example.hotelbookingservice.exception.ErrorCode;
import org.example.hotelbookingservice.exception.InvalidBookingStateAndDateException;
import org.example.hotelbookingservice.mapper.HotelMapper;
import org.example.hotelbookingservice.mapper.RoomMapper;
import org.example.hotelbookingservice.repository.HotelRepository;
import org.example.hotelbookingservice.repository.ImageRepository;
import org.example.hotelbookingservice.services.IHotelAmenityService;
import org.example.hotelbookingservice.services.IHotelService;
import org.example.hotelbookingservice.services.IUserService;
import org.example.hotelbookingservice.services.IFileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelServiceImpl implements IHotelService {

    private final HotelRepository hotelRepository;
    private final ImageRepository imageRepository;
    private final IUserService userService;
    private final HotelMapper hotelMapper;
    private final IFileStorageService fileStorageService;
    private final IHotelAmenityService hotelAmenityService;
    private final RoomMapper roomMapper;
    private final TransactionTemplate transactionTemplate;


    @Override
    public HotelResponse addHotel(HotelCreateRequest hotelCreateRequest, List<MultipartFile> imageFile) {

        if (imageFile == null || imageFile.isEmpty()) {
            throw new AppException(ErrorCode.IMAGE_REQUIRED);
        }

        List<String> imageUrls = uploadImages(imageFile);

        return transactionTemplate.execute(status -> addHotelInTransaction(hotelCreateRequest, imageUrls));
    }

    private HotelResponse addHotelInTransaction(HotelCreateRequest hotelCreateRequest, List<String> imageUrls) {

        User currentUser = userService.getCurrentLoggedInUser();

        boolean isAdmin = currentUser.getUserRoles().stream()
                .anyMatch(role -> role.getRole().getName().equals(org.example.hotelbookingservice.enums.UserRole.ADMIN.name()));

        if (!isAdmin) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (hotelRepository.existsByNameAndLocation(hotelCreateRequest.getName(),hotelCreateRequest.getLocation())) {
            throw new AppException(ErrorCode.HOTEL_ALREADY_EXISTS);
        }

        Hotel hotel = hotelMapper.toHotel(hotelCreateRequest);
        hotel.setUser(currentUser);

        hotel.setIsActive(true);

        Hotel savedHotel = hotelRepository.save(hotel);

        if (!imageUrls.isEmpty()) {
            List<Image> imagesToSave = new ArrayList<>();
            for (String imageUrl : imageUrls) {
                Image image = new Image();
                image.setPath(imageUrl);
                image.setHotel(savedHotel);
                imagesToSave.add(image);
            }
            imageRepository.saveAll(imagesToSave);
            savedHotel.getImages().addAll(imagesToSave);
        }

        if (hotelCreateRequest.getAmenityIds() != null && !hotelCreateRequest.getAmenityIds().isEmpty()) {
            hotelAmenityService.addAmenitiesToHotel(savedHotel.getId(), hotelCreateRequest.getAmenityIds());
            List<Hotelamenity> addedAmenities = hotelAmenityService.getAmenitiesByHotelId(savedHotel.getId());
            savedHotel.getHotelAmenities().addAll(addedAmenities);
        }

        return hotelMapper.toHotelResponse(savedHotel);

    }

    @Override
    public HotelResponse updateHotel(Integer id, HotelUpdateRequest hotelUpdateRequest, List<MultipartFile> imageFiles) {
        List<String> imageUrls = uploadImages(imageFiles);
        return transactionTemplate.execute(status -> updateHotelInTransaction(id, hotelUpdateRequest, imageUrls));
    }

    private HotelResponse updateHotelInTransaction(Integer id, HotelUpdateRequest hotelUpdateRequest, List<String> imageUrls) {
        Hotel existingHotel = hotelRepository.findById(legacyId(id))
                .orElseThrow(()->new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        User currentUser = userService.getCurrentLoggedInUser();
        if (!existingHotel.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        hotelMapper.updateHotelFromRequest(hotelUpdateRequest, existingHotel);

        if (!imageUrls.isEmpty()) {
            List<Image> imagesToSave = new ArrayList<>();
            for (String imageUrl : imageUrls) {
                Image image = new Image();
                image.setPath(imageUrl);
                image.setHotel(existingHotel);
                imagesToSave.add(image);
            }
            imageRepository.saveAll(imagesToSave);
            existingHotel.getImages().clear();
            existingHotel.getImages().addAll(imagesToSave);
        }

        Hotel savedHotel = hotelRepository.save(existingHotel);
        return hotelMapper.toHotelResponse(existingHotel);
    }

    private List<String> uploadImages(List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return List.of();
        }
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : imageFiles) {
            imageUrls.add(fileStorageService.uploadFile(file));
        }
        return imageUrls;
    }

    @Override
    @Transactional(readOnly = true)
    public HotelResponse getHotelById(Integer id) {
        Hotel hotel = hotelRepository.findById(legacyId(id))
                .orElseThrow(()->new AppException(ErrorCode.NOT_FOUND_EXCEPTION));
        return hotelMapper.toHotelResponse(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponse> getAllHotels() {
       List<Hotel> hotelList = hotelRepository.findAll();
       return hotelMapper.toHotelResponseList(hotelList);
    }

    @Override
    @Transactional
    public void deleteHotel(Integer id) {
        Hotel hotel = hotelRepository.findById(legacyId(id))
                .orElseThrow(()->new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        User currentUser = userService.getCurrentLoggedInUser();
        if (!hotel.getUser().getId().equals(currentUser.getId())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        hotelRepository.delete(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponse> getMyHotels() {
       User currentUser = userService.getCurrentLoggedInUser();

       List<Hotel> hotels = hotelRepository.findByUserId(currentUser.getUuid());
       return hotelMapper.toHotelResponseList(hotels);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponse> searchHotels(String location, LocalDate checkInDate, LocalDate checkOutDate, Integer capacity, Integer roomQuantity) {
        String searchLocation = (location == null) ? "" : location.trim();

        if (checkInDate != null && checkOutDate != null) {
            if (checkInDate.isBefore(LocalDate.now())) {
                return new ArrayList<>(); // Return empty list instead of throwing
            }
            if (checkOutDate.isBefore(checkInDate)) {
                return new ArrayList<>();
            }
            if (checkInDate.isEqual(checkOutDate)) {
                return new ArrayList<>();
            }
        }

        //If null or <= 0 then consider finding at least 1 room
        long quantityParam = (roomQuantity == null || roomQuantity < 1) ? 1L : roomQuantity.longValue();

        List<Hotel> availableHotels = hotelRepository.findAvailableHotels(
                searchLocation,
                checkInDate,
                checkOutDate,
                capacity,
                quantityParam
        );

        return hotelMapper.toHotelResponseList(availableHotels);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomsByHotelId(Integer hotelId) {
        Hotel hotel = hotelRepository.findById(legacyId(hotelId))
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        List<Room> roomList = new ArrayList<>(hotel.getRooms());

        return roomMapper.toRoomResponseList(roomList);
    }

    private UUID legacyId(Integer id) {
        return id == null ? null : new UUID(0L, id.longValue());
    }
}
