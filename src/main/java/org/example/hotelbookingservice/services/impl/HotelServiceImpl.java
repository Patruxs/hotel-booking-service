package org.example.hotelbookingservice.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hotelbookingservice.dto.request.hotel.HotelCreateRequest;
import org.example.hotelbookingservice.dto.request.hotel.HotelUpdateRequest;
import org.example.hotelbookingservice.dto.response.HotelResponse;
import org.example.hotelbookingservice.entity.Hotel;
import org.example.hotelbookingservice.entity.Hotelamenity;
import org.example.hotelbookingservice.entity.Image;
import org.example.hotelbookingservice.entity.User;
import org.example.hotelbookingservice.exception.AppException;
import org.example.hotelbookingservice.exception.ErrorCode;
import org.example.hotelbookingservice.exception.InvalidBookingStateAndDateException;
import org.example.hotelbookingservice.mapper.HotelMapper;
import org.example.hotelbookingservice.repository.HotelRepository;
import org.example.hotelbookingservice.repository.ImageRepository;
import org.example.hotelbookingservice.services.IHotelAmenityService;
import org.example.hotelbookingservice.services.IHotelService;
import org.example.hotelbookingservice.services.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelServiceImpl implements IHotelService {

    private final HotelRepository hotelRepository;
    private final ImageRepository imageRepository;
    private final IUserService userService;
    private final HotelMapper hotelMapper;
    private final Cloudinary cloudinary;
    private final IHotelAmenityService hotelAmenityService;

    @Override
    @Transactional
    public HotelResponse addHotel(HotelCreateRequest hotelCreateRequest, List<MultipartFile> imageFile) {

        if (imageFile == null || imageFile.isEmpty()) {
            throw new AppException(ErrorCode.IMAGE_REQUIRED);
        }

        User currentUser = userService.getCurrentLoggedInUser();

        boolean isAdmin = currentUser.getUserRoles().stream()
                .anyMatch(role -> role.getRole().getName().equals("ADMIN"));

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

        if (imageFile != null && !imageFile.isEmpty()) {
            List<Image> imagesToSave = new ArrayList<>();
            for (MultipartFile file : imageFile ) {
                validateImageFile(file);

                String imageUrl = saveImageToCloud(file);
                Image image = new Image();
                image.setPath(imageUrl);
                image.setHotel(savedHotel);
                imagesToSave.add(image);
            }
            imageRepository.saveAll(imagesToSave);
            savedHotel.setImages(new HashSet<>(imagesToSave));
        }

        if (hotelCreateRequest.getAmenityIds() != null && !hotelCreateRequest.getAmenityIds().isEmpty()) {
            hotelAmenityService.addAmenitiesToHotel(savedHotel.getId(), hotelCreateRequest.getAmenityIds());
            List<Hotelamenity> addedAmenities = hotelAmenityService.getAmenitiesByHotelId(savedHotel.getId());
            savedHotel.setHotelAmenities(new HashSet<>(addedAmenities));
        }

        return hotelMapper.toHotelResponse(savedHotel);

    }

    @Override
    @Transactional
    public HotelResponse updateHotel(Integer id, HotelUpdateRequest hotelUpdateRequest, List<MultipartFile> imageFiles) {
        Hotel existingHotel = hotelRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        User currentUser = userService.getCurrentLoggedInUser();
        if (!existingHotel.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        hotelMapper.updateHotelFromRequest(hotelUpdateRequest, existingHotel);

        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<Image> imagesToSave = new ArrayList<>();
            for (MultipartFile file : imageFiles ) {
                validateImageFile(file);

                String imageUrl = saveImageToCloud(file);
                Image image = new Image();
                image.setPath(imageUrl);
                image.setHotel(existingHotel);
                imagesToSave.add(image);
            }
            imageRepository.saveAll(imagesToSave);
            existingHotel.setImages(new HashSet<>(imagesToSave));
        }

        Hotel savedHotel = hotelRepository.save(existingHotel);
        return hotelMapper.toHotelResponse(existingHotel);
    }

    @Override
    @Transactional
    public HotelResponse getHotelById(Integer id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.NOT_FOUND_EXCEPTION));
        return hotelMapper.toHotelResponse(hotel);
    }

    @Override
    public List<HotelResponse> getAllHotels() {
       List<Hotel> hotelList = hotelRepository.findAll();
       return hotelMapper.toHotelResponseList(hotelList);
    }

    @Override
    public void deleteHotel(Integer id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        User currentUser = userService.getCurrentLoggedInUser();
        if (!hotel.getUser().getId().equals(currentUser.getId())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        hotelRepository.delete(hotel);
    }

    @Override
    public List<HotelResponse> getMyHotels() {
       User currentUser = userService.getCurrentLoggedInUser();

       List<Hotel> hotels = hotelRepository.findByUserId(currentUser.getId());
       return hotelMapper.toHotelResponseList(hotels);
    }

    @Override
    public List<HotelResponse> searchHotels(String location, LocalDate checkInDate, LocalDate checkOutDate, Integer capacity, Integer roomQuantity) {
        if (checkInDate != null && checkOutDate != null) {
            if (checkInDate.isBefore(LocalDate.now())) {
                throw new InvalidBookingStateAndDateException("Check-in date cannot be in the past");
            }
            if (checkOutDate.isBefore(checkInDate)) {
                throw new InvalidBookingStateAndDateException("Check-out date cannot be before check-in date");
            }
            if (checkInDate.isEqual(checkOutDate)) {
                throw new InvalidBookingStateAndDateException("Check-in and Check-out dates cannot be the same");
            }
        } else {
            if (location == null || location.isBlank()) {
                throw new AppException(ErrorCode.NAME_VALUE_REQUIRED_EXCEPTION);
            }
        }

        //If null or <= 0 then consider finding at least 1 room
        long quantityParam = (roomQuantity == null || roomQuantity < 1) ? 1L : roomQuantity.longValue();

        List<Hotel> availableHotels = hotelRepository.findAvailableHotels(
                location,
                checkInDate,
                checkOutDate,
                capacity,
                quantityParam
        );

        return hotelMapper.toHotelResponseList(availableHotels);
    }

    private String saveImageToCloud(MultipartFile imageFile) {
        try {
            Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
            throw new AppException(ErrorCode.INVALID_FILE_FORMAT);
        }
    }
}
