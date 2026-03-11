package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.entity.Amenity;
import org.example.hotelbookingservice.entity.Room;
import org.example.hotelbookingservice.entity.Roomamenity;
import org.example.hotelbookingservice.entity.RoomamenityId;
import org.example.hotelbookingservice.exception.NotFoundException;
import org.example.hotelbookingservice.repository.AmenityRepository;
import org.example.hotelbookingservice.repository.RoomRepository;
import org.example.hotelbookingservice.repository.RoomamenityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomAmenityServiceImplTest {

    @Mock
    private RoomamenityRepository roomAmenityRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private AmenityRepository amenityRepository;

    @InjectMocks
    private RoomAmenityServiceImpl roomAmenityService;

    @Captor
    private ArgumentCaptor<List<Roomamenity>> roomAmenityListCaptor;

    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room();
        room.setId(1);
    }

    @Test
    void addAmenitiesToRoom_AllNewAmenities_SavesAll() {
        // Given
        List<Integer> amenityIds = Arrays.asList(10, 20);

        Amenity amenity1 = new Amenity();
        amenity1.setId(10);
        Amenity amenity2 = new Amenity();
        amenity2.setId(20);
        List<Amenity> foundAmenities = Arrays.asList(amenity1, amenity2);

        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(amenityRepository.findAllById(amenityIds)).thenReturn(foundAmenities);
        when(roomAmenityRepository.findByIdRoomIdAndIdAmenityIdIn(eq(1), anyList())).thenReturn(new ArrayList<>());

        // When
        roomAmenityService.addAmenitiesToRoom(1, amenityIds);

        // Then
        verify(roomAmenityRepository, times(1)).saveAll(roomAmenityListCaptor.capture());
        List<Roomamenity> savedAmenities = roomAmenityListCaptor.getValue();

        assertThat(savedAmenities).hasSize(2);
        assertThat(savedAmenities.get(0).getAmenity().getId()).isEqualTo(10);
        assertThat(savedAmenities.get(1).getAmenity().getId()).isEqualTo(20);
    }

    @Test
    void addAmenitiesToRoom_SomeExistingAmenities_SavesOnlyNew() {
        // Given
        List<Integer> amenityIds = Arrays.asList(10, 20);

        Amenity amenity1 = new Amenity();
        amenity1.setId(10);
        Amenity amenity2 = new Amenity();
        amenity2.setId(20);
        List<Amenity> foundAmenities = Arrays.asList(amenity1, amenity2);

        Roomamenity existingRoomAmenity = new Roomamenity();
        RoomamenityId id = new RoomamenityId();
        id.setRoomId(1);
        id.setAmenityId(10); // Amenity 10 is already in room 1
        existingRoomAmenity.setId(id);

        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(amenityRepository.findAllById(amenityIds)).thenReturn(foundAmenities);
        when(roomAmenityRepository.findByIdRoomIdAndIdAmenityIdIn(eq(1), anyList())).thenReturn(Arrays.asList(existingRoomAmenity));

        // When
        roomAmenityService.addAmenitiesToRoom(1, amenityIds);

        // Then
        verify(roomAmenityRepository, times(1)).saveAll(roomAmenityListCaptor.capture());
        List<Roomamenity> savedAmenities = roomAmenityListCaptor.getValue();

        assertThat(savedAmenities).hasSize(1);
        assertThat(savedAmenities.get(0).getAmenity().getId()).isEqualTo(20); // Only 20 should be saved
    }

    @Test
    void addAmenitiesToRoom_MissingAmenities_ThrowsNotFoundException() {
        // Given
        List<Integer> amenityIds = Arrays.asList(10, 20);

        Amenity amenity1 = new Amenity();
        amenity1.setId(10);
        // Amenity 20 is not found in DB
        List<Amenity> foundAmenities = Arrays.asList(amenity1);

        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(amenityRepository.findAllById(amenityIds)).thenReturn(foundAmenities);

        // When / Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            roomAmenityService.addAmenitiesToRoom(1, amenityIds);
        });

        assertThat(exception.getMessage()).contains("Could not add successfully");
        assertThat(exception.getMessage()).contains("[20]");

        // Ensure nothing was saved
        verify(roomAmenityRepository, never()).saveAll(any());
    }

    @Test
    void addAmenitiesToRoom_PerformanceBaseline() {
        // Given
        int numAmenities = 1000;
        List<Integer> amenityIds = new ArrayList<>();
        List<Amenity> foundAmenities = new ArrayList<>();

        for (int i = 1; i <= numAmenities; i++) {
            amenityIds.add(i);
            Amenity amenity = new Amenity();
            amenity.setId(i);
            foundAmenities.add(amenity);
        }

        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(amenityRepository.findAllById(amenityIds)).thenReturn(foundAmenities);
        when(roomAmenityRepository.findByIdRoomIdAndIdAmenityIdIn(eq(1), anyList())).thenReturn(new ArrayList<>());

        // When
        long startTime = System.currentTimeMillis();
        roomAmenityService.addAmenitiesToRoom(1, amenityIds);
        long endTime = System.currentTimeMillis();

        // Then
        System.out.println("Optimized time for adding " + numAmenities + " amenities: " + (endTime - startTime) + " ms");

        // Verify that saveAll was called exactly once
        verify(roomAmenityRepository, times(1)).saveAll(anyList());
    }
}
