package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.dto.request.hotel.HotelCreateRequest;
import org.example.hotelbookingservice.dto.response.HotelResponse;
import org.example.hotelbookingservice.entity.*;
import org.example.hotelbookingservice.enums.UserRole;
import org.example.hotelbookingservice.exception.AppException;
import org.example.hotelbookingservice.exception.ErrorCode;
import org.example.hotelbookingservice.mapper.HotelMapper;
import org.example.hotelbookingservice.mapper.RoomMapper;
import org.example.hotelbookingservice.repository.HotelRepository;
import org.example.hotelbookingservice.repository.ImageRepository;
import org.example.hotelbookingservice.services.IFileStorageService;
import org.example.hotelbookingservice.services.IHotelAmenityService;
import org.example.hotelbookingservice.services.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private IUserService userService;

    @Mock
    private HotelMapper hotelMapper;

    @Mock
    private IFileStorageService fileStorageService;

    @Mock
    private IHotelAmenityService hotelAmenityService;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private HotelCreateRequest validRequest;
    private User adminUser;
    private User customerUser;
    private Hotel savedHotel;
    private List<MultipartFile> validImages;

    @BeforeEach
    void setUp() {
        validRequest = new HotelCreateRequest();
        validRequest.setName("Grand Saigon Hotel");
        validRequest.setLocation("Ho Chi Minh City");
        validRequest.setDescription("5 star hotel");
        validRequest.setStarRating(5);
        validRequest.setEmail("contact@grandsaigon.com");
        validRequest.setPhone("02839123456");
        validRequest.setContactName("Mr. Quan Ly");
        validRequest.setContactPhone("0909123456");
        validRequest.setAmenityIds(List.of(1, 2, 3));

        Role adminRole = new Role();
        adminRole.setId(1);
        adminRole.setName(UserRole.ADMIN.name());

        Userrole adminUserRole = new Userrole();
        adminUserRole.setRole(adminRole);

        adminUser = User.builder()
                .id(1)
                .fullName("Admin User")
                .userRoles(new HashSet<>(Collections.singletonList(adminUserRole)))
                .build();

        Role customerRole = new Role();
        customerRole.setId(2);
        customerRole.setName(UserRole.CUSTOMER.name());

        Userrole customerUserRole = new Userrole();
        customerUserRole.setRole(customerRole);

        customerUser = User.builder()
                .id(2)
                .fullName("Customer User")
                .userRoles(new HashSet<>(Collections.singletonList(customerUserRole)))
                .build();

        savedHotel = new Hotel();
        savedHotel.setId(100);
        savedHotel.setName("Grand Saigon Hotel");
        savedHotel.setLocation("Ho Chi Minh City");
        savedHotel.setImages(new HashSet<>());
        savedHotel.setHotelAmenities(new HashSet<>());

        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "hotel.jpg",
                "image/jpeg",
                "dummy image content".getBytes()
        );
        validImages = Collections.singletonList(imageFile);
    }

    @Test
    void addHotel_validRequest_success() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(adminUser);
        when(hotelRepository.existsByNameAndLocation(validRequest.getName(), validRequest.getLocation())).thenReturn(false);

        Hotel mappedHotel = new Hotel();
        mappedHotel.setName(validRequest.getName());
        when(hotelMapper.toHotel(validRequest)).thenReturn(mappedHotel);
        when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);

        when(fileStorageService.uploadFile(any(MultipartFile.class))).thenReturn("https://cloudinary.com/image.jpg");

        List<Hotelamenity> mockAddedAmenities = new ArrayList<>();
        Hotelamenity amenity1 = new Hotelamenity();
        mockAddedAmenities.add(amenity1);
        when(hotelAmenityService.getAmenitiesByHotelId(savedHotel.getId())).thenReturn(mockAddedAmenities);

        HotelResponse mockResponse = new HotelResponse();
        mockResponse.setId(100);
        when(hotelMapper.toHotelResponse(savedHotel)).thenReturn(mockResponse);

        // When
        HotelResponse response = hotelService.addHotel(validRequest, validImages);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100);

        verify(userService, times(1)).getCurrentLoggedInUser();
        verify(hotelRepository, times(1)).existsByNameAndLocation(validRequest.getName(), validRequest.getLocation());

        ArgumentCaptor<Hotel> hotelCaptor = ArgumentCaptor.forClass(Hotel.class);
        verify(hotelRepository, times(1)).save(hotelCaptor.capture());
        Hotel capturedHotel = hotelCaptor.getValue();
        assertThat(capturedHotel.getUser()).isEqualTo(adminUser);
        assertThat(capturedHotel.getIsActive()).isTrue();

        verify(fileStorageService, times(1)).uploadFile(any(MultipartFile.class));
        verify(imageRepository, times(1)).saveAll(anyList());

        verify(hotelAmenityService, times(1)).addAmenitiesToHotel(savedHotel.getId(), validRequest.getAmenityIds());
        verify(hotelAmenityService, times(1)).getAmenitiesByHotelId(savedHotel.getId());

        verify(hotelMapper, times(1)).toHotelResponse(savedHotel);
    }

    @Test
    void addHotel_missingImage_throwsImageRequiredException() {
        // Given
        List<MultipartFile> emptyImages = new ArrayList<>();

        // When & Then
        assertThatThrownBy(() -> hotelService.addHotel(validRequest, emptyImages))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_REQUIRED);

        assertThatThrownBy(() -> hotelService.addHotel(validRequest, null))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_REQUIRED);

        verifyNoInteractions(userService);
        verifyNoInteractions(hotelRepository);
    }

    @Test
    void addHotel_userNotAdmin_throwsUnauthorizedException() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(customerUser);

        // When & Then
        assertThatThrownBy(() -> hotelService.addHotel(validRequest, validImages))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);

        verify(userService, times(1)).getCurrentLoggedInUser();
        verifyNoInteractions(hotelRepository);
    }

    @Test
    void addHotel_hotelAlreadyExists_throwsHotelAlreadyExistsException() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(adminUser);
        when(hotelRepository.existsByNameAndLocation(validRequest.getName(), validRequest.getLocation())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> hotelService.addHotel(validRequest, validImages))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HOTEL_ALREADY_EXISTS);

        verify(hotelRepository, times(1)).existsByNameAndLocation(validRequest.getName(), validRequest.getLocation());
        verify(hotelRepository, never()).save(any(Hotel.class));
    }

    @Test
    void addHotel_invalidImageFormat_throwsInvalidFileFormatException() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(adminUser);
        when(hotelRepository.existsByNameAndLocation(validRequest.getName(), validRequest.getLocation())).thenReturn(false);

        Hotel mappedHotel = new Hotel();
        when(hotelMapper.toHotel(validRequest)).thenReturn(mappedHotel);
        when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);

        when(fileStorageService.uploadFile(any(MultipartFile.class)))
                .thenThrow(new AppException(ErrorCode.INVALID_FILE_FORMAT));

        MockMultipartFile invalidImage = new MockMultipartFile(
                "imageFile",
                "document.pdf",
                "application/pdf",
                "dummy pdf content".getBytes()
        );
        List<MultipartFile> images = Collections.singletonList(invalidImage);

        // When & Then
        assertThatThrownBy(() -> hotelService.addHotel(validRequest, images))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_FORMAT);

        verify(fileStorageService, times(1)).uploadFile(any(MultipartFile.class));
        verifyNoInteractions(imageRepository);
    }

    @Test
    void addHotel_fileUploadFails_throwsUncategorizedException() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(adminUser);
        when(hotelRepository.existsByNameAndLocation(validRequest.getName(), validRequest.getLocation())).thenReturn(false);

        Hotel mappedHotel = new Hotel();
        when(hotelMapper.toHotel(validRequest)).thenReturn(mappedHotel);
        when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);

        when(fileStorageService.uploadFile(any(MultipartFile.class)))
                .thenThrow(new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        // When & Then
        assertThatThrownBy(() -> hotelService.addHotel(validRequest, validImages))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNCATEGORIZED_EXCEPTION);

        verify(fileStorageService, times(1)).uploadFile(any(MultipartFile.class));
        verifyNoInteractions(imageRepository);
    }
}
