package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.dto.request.room.RoomCreateRequest;
import org.example.hotelbookingservice.dto.response.AmenityResponse;
import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.example.hotelbookingservice.entity.Amenity;
import org.example.hotelbookingservice.entity.Hotel;
import org.example.hotelbookingservice.entity.Room;
import org.example.hotelbookingservice.entity.Roomamenity;
import org.example.hotelbookingservice.entity.User;
import org.example.hotelbookingservice.enums.RoomType;
import org.example.hotelbookingservice.exception.AppException;
import org.example.hotelbookingservice.exception.ErrorCode;
import org.example.hotelbookingservice.mapper.AmenityMapper;
import org.example.hotelbookingservice.mapper.RoomMapper;
import org.example.hotelbookingservice.repository.ImageRepository;
import org.example.hotelbookingservice.repository.RoomRepository;
import org.example.hotelbookingservice.services.IFileStorageService;
import org.example.hotelbookingservice.services.IRoomAmenityService;
import org.example.hotelbookingservice.services.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private IUserService userService;

    @Mock
    private IRoomAmenityService roomAmenityService;

    @Mock
    private IFileStorageService fileStorageService;

    @Mock
    private AmenityMapper amenityMapper;

    @InjectMocks
    private RoomServiceImpl roomService;

    private User mockUser;
    private Hotel mockHotel;
    private Room mockRoom;
    private RoomCreateRequest mockCreateRequest;
    private RoomResponse mockRoomResponse;
    private MultipartFile mockMultipartFile;

    @BeforeEach
    void setUp() {
        mockHotel = new Hotel();
        mockHotel.setId(1);
        mockHotel.setName("Test Hotel");

        mockUser = new User();
        mockUser.setId(1);
        mockUser.setFullName("Admin User");
        mockUser.setHotels(Set.of(mockHotel));

        mockCreateRequest = new RoomCreateRequest();
        mockCreateRequest.setHotelId(1);
        mockCreateRequest.setName("Deluxe Room");
        mockCreateRequest.setPrice(BigDecimal.valueOf(500000));
        mockCreateRequest.setCapacity(2);
        mockCreateRequest.setType(RoomType.SINGLE);
        mockCreateRequest.setDescription("A nice room");
        mockCreateRequest.setAmount(5);
        mockCreateRequest.setAmenityIds(List.of(1, 2));

        mockRoom = new Room();
        mockRoom.setId(1);
        mockRoom.setHotel(mockHotel);
        mockRoom.setName("Deluxe Room");

        mockRoomResponse = new RoomResponse();
        mockRoomResponse.setId(1);
        mockRoomResponse.setName("Deluxe Room");

        mockMultipartFile = new MockMultipartFile(
                "image", "test-image.jpg", "image/jpeg", "test image content".getBytes()
        );
    }

    @Test
    void addRoom_ValidRequestWithImageAndAmenities_ReturnsRoomResponse() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);
        when(roomMapper.toRoom(mockCreateRequest)).thenReturn(mockRoom);
        when(roomRepository.save(any(Room.class))).thenReturn(mockRoom);
        when(roomMapper.toRoomResponse(mockRoom)).thenReturn(mockRoomResponse);

        String imageUrl = "http://cloudinary.com/test-image.jpg";
        when(fileStorageService.uploadFile(any(MultipartFile.class))).thenReturn(imageUrl);

        Roomamenity roomAmenity1 = new Roomamenity();
        Amenity amenity1 = new Amenity();
        amenity1.setId(1);
        amenity1.setName("WiFi");
        roomAmenity1.setAmenity(amenity1);

        Roomamenity roomAmenity2 = new Roomamenity();
        Amenity amenity2 = new Amenity();
        amenity2.setId(2);
        amenity2.setName("TV");
        roomAmenity2.setAmenity(amenity2);

        List<Roomamenity> roomAmenities = Arrays.asList(roomAmenity1, roomAmenity2);
        when(roomAmenityService.getAmenitiesByRoomId(mockRoom.getId())).thenReturn(roomAmenities);

        AmenityResponse amenityResponse1 = new AmenityResponse();
        amenityResponse1.setId(1);
        amenityResponse1.setName("WiFi");

        AmenityResponse amenityResponse2 = new AmenityResponse();
        amenityResponse2.setId(2);
        amenityResponse2.setName("TV");

        when(amenityMapper.toAmenityResponse(amenity1)).thenReturn(amenityResponse1);
        when(amenityMapper.toAmenityResponse(amenity2)).thenReturn(amenityResponse2);

        // When
        RoomResponse result = roomService.addRoom(mockCreateRequest, mockMultipartFile);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Deluxe Room");
        assertThat(result.getRoomImages()).containsExactly(imageUrl);
        assertThat(result.getAmenities()).hasSize(2);
        assertThat(result.getAmenities().get(0).getName()).isEqualTo("WiFi");

        verify(roomRepository, times(1)).save(any(Room.class));
        verify(fileStorageService, times(1)).uploadFile(any(MultipartFile.class));
        verify(imageRepository, times(1)).save(any(org.example.hotelbookingservice.entity.Image.class));
        verify(roomAmenityService, times(1)).addAmenitiesToRoom(eq(mockRoom.getId()), eq(mockCreateRequest.getAmenityIds()));
        verify(roomAmenityService, times(1)).getAmenitiesByRoomId(mockRoom.getId());
    }

    @Test
    void addRoom_ValidRequestWithoutImageAndAmenities_ReturnsRoomResponse() {
        // Given
        mockCreateRequest.setAmenityIds(null); // No amenities
        MultipartFile emptyImageFile = new MockMultipartFile("image", "", "image/jpeg", new byte[0]);

        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);
        when(roomMapper.toRoom(mockCreateRequest)).thenReturn(mockRoom);
        when(roomRepository.save(any(Room.class))).thenReturn(mockRoom);
        when(roomMapper.toRoomResponse(mockRoom)).thenReturn(mockRoomResponse);

        // When
        RoomResponse result = roomService.addRoom(mockCreateRequest, emptyImageFile);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Deluxe Room");
        assertThat(result.getRoomImages()).isNull();
        assertThat(result.getAmenities()).isNull();

        verify(roomRepository, times(1)).save(any(Room.class));
        verify(fileStorageService, never()).uploadFile(any(MultipartFile.class));
        verify(imageRepository, never()).save(any(org.example.hotelbookingservice.entity.Image.class));
        verify(roomAmenityService, never()).addAmenitiesToRoom(anyInt(), anyList());
        verify(roomAmenityService, never()).getAmenitiesByRoomId(anyInt());
    }

    @Test
    void addRoom_UserHasNoHotels_ThrowsNotFoundException() {
        // Given
        mockUser.setHotels(null);
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);

        // When & Then
        assertThatThrownBy(() -> roomService.addRoom(mockCreateRequest, mockMultipartFile))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_EXCEPTION);

        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void addRoom_UserHotelsListEmpty_ThrowsNotFoundException() {
        // Given
        mockUser.setHotels(new HashSet<>());
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);

        // When & Then
        assertThatThrownBy(() -> roomService.addRoom(mockCreateRequest, mockMultipartFile))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_EXCEPTION);

        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void addRoom_TargetHotelNotFoundInUserHotels_ThrowsUnauthorizedException() {
        // Given
        mockCreateRequest.setHotelId(999); // Hotel ID not in user's hotels
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);

        // When & Then
        assertThatThrownBy(() -> roomService.addRoom(mockCreateRequest, mockMultipartFile))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);

        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void addRoom_ImageUploadThrowsInvalidFileFormat_ThrowsAppException() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);
        when(roomMapper.toRoom(mockCreateRequest)).thenReturn(mockRoom);
        when(roomRepository.save(any(Room.class))).thenReturn(mockRoom);
        when(roomMapper.toRoomResponse(mockRoom)).thenReturn(mockRoomResponse);

        // Simulate fileStorageService throwing an exception for invalid format
        when(fileStorageService.uploadFile(any(MultipartFile.class)))
                .thenThrow(new AppException(ErrorCode.INVALID_FILE_FORMAT));

        // When & Then
        assertThatThrownBy(() -> roomService.addRoom(mockCreateRequest, mockMultipartFile))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_FORMAT);

        // Verify room was saved but image wasn't, and amenity process was aborted
        verify(roomRepository, times(1)).save(any(Room.class));
        verify(fileStorageService, times(1)).uploadFile(any(MultipartFile.class));
        verify(imageRepository, never()).save(any(org.example.hotelbookingservice.entity.Image.class));
        verify(roomAmenityService, never()).addAmenitiesToRoom(anyInt(), anyList());
    }

    @Test
    void addRoom_ImageUploadThrowsUncategorizedException_ThrowsAppException() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);
        when(roomMapper.toRoom(mockCreateRequest)).thenReturn(mockRoom);
        when(roomRepository.save(any(Room.class))).thenReturn(mockRoom);
        when(roomMapper.toRoomResponse(mockRoom)).thenReturn(mockRoomResponse);

        // Simulate fileStorageService throwing an exception for general upload failure (like IOException from Cloudinary)
        when(fileStorageService.uploadFile(any(MultipartFile.class)))
                .thenThrow(new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        // When & Then
        assertThatThrownBy(() -> roomService.addRoom(mockCreateRequest, mockMultipartFile))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNCATEGORIZED_EXCEPTION);

        // Verify room was saved but image wasn't, and amenity process was aborted
        verify(roomRepository, times(1)).save(any(Room.class));
        verify(fileStorageService, times(1)).uploadFile(any(MultipartFile.class));
        verify(imageRepository, never()).save(any(org.example.hotelbookingservice.entity.Image.class));
        verify(roomAmenityService, never()).addAmenitiesToRoom(anyInt(), anyList());
    }
}
