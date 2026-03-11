package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.dto.request.hotel.HotelCreateRequest;
import org.example.hotelbookingservice.entity.Hotel;
import org.example.hotelbookingservice.entity.Role;
import org.example.hotelbookingservice.entity.User;
import org.example.hotelbookingservice.entity.Userrole;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplPerformanceTest {

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

    private User adminUser;
    private HotelCreateRequest createRequest;
    private Hotel hotelEntity;

    @BeforeEach
    void setUp() {
        Role adminRole = new Role();
        adminRole.setName(org.example.hotelbookingservice.enums.UserRole.ADMIN.name());

        Userrole userRole = new Userrole();
        userRole.setRole(adminRole);
        userRole.setUser(adminUser);

        adminUser = new User();
        adminUser.setId(1);
        adminUser.setUserRoles(Set.of(userRole));

        createRequest = new HotelCreateRequest();
        createRequest.setName("Test Hotel");
        createRequest.setLocation("Test Location");

        hotelEntity = new Hotel();
        hotelEntity.setId(1);
        hotelEntity.setName("Test Hotel");
        hotelEntity.setLocation("Test Location");
        hotelEntity.setUser(adminUser);
    }

    @Test
    void addHotel_PerformanceTest() throws Exception {
        // Given
        int imageCount = 5;
        List<MultipartFile> imageFiles = new ArrayList<>();
        for (int i = 0; i < imageCount; i++) {
            imageFiles.add(new MockMultipartFile("file" + i, "image" + i + ".jpg", "image/jpeg", new byte[]{1, 2, 3}));
        }

        when(userService.getCurrentLoggedInUser()).thenReturn(adminUser);
        when(hotelRepository.existsByNameAndLocation(anyString(), anyString())).thenReturn(false);
        when(hotelMapper.toHotel(any(HotelCreateRequest.class))).thenReturn(hotelEntity);
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotelEntity);

        // Simulate 1 second network delay for each upload
        when(fileStorageService.uploadFile(any(MultipartFile.class))).thenAnswer(invocation -> {
            Thread.sleep(1000);
            return "url_for_" + invocation.getArgument(0).toString();
        });

        // When
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        hotelService.addHotel(createRequest, imageFiles);
        stopWatch.stop();

        // Then
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        System.out.println("Time taken to add hotel with " + imageCount + " images: " + totalTimeMillis + " ms");

        // Before optimization, it will take at least 5000 ms
        // After optimization, it should take ~1000 ms

        verify(fileStorageService, times(imageCount)).uploadFile(any(MultipartFile.class));
    }
}
