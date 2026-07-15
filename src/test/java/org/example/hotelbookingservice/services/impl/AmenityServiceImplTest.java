package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.entity.Amenity;
import org.example.hotelbookingservice.mapper.AmenityMapper;
import org.example.hotelbookingservice.mapper.RoomMapper;
import org.example.hotelbookingservice.repository.AmenityRepository;
import org.example.hotelbookingservice.repository.HotelRepository;
import org.example.hotelbookingservice.repository.HotelamenityRepository;
import org.example.hotelbookingservice.repository.RoomRepository;
import org.example.hotelbookingservice.repository.RoomamenityRepository;
import org.example.hotelbookingservice.services.IUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmenityServiceImplTest {

    @Mock private AmenityRepository amenityRepository;
    @Mock private AmenityMapper amenityMapper;
    @Mock private IUserService userService;
    @Mock private HotelRepository hotelRepository;
    @Mock private HotelamenityRepository hotelamenityRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomamenityRepository roomamenityRepository;
    @Mock private RoomMapper roomMapper;

    @InjectMocks private AmenityServiceImpl amenityService;

    @Test
    void deleteAmenity_whenAmenityExists_shouldDeactivateWithoutQueryingRetiredHotelRelation() {
        Amenity amenity = new Amenity();
        amenity.setId(UUID.fromString("b2000000-0000-4000-8000-000000000010"));
        amenity.setName("Owner amenity");
        amenity.setType("GENERAL");
        amenity.setActive(true);
        Instant originalUpdatedAt = Instant.parse("2026-07-01T00:00:00Z");
        amenity.setUpdatedAt(originalUpdatedAt);
        when(amenityRepository.findAll()).thenReturn(List.of(amenity));

        amenityService.deleteAmenity(amenity.getId());

        assertThat(amenity.getActive()).isFalse();
        assertThat(amenity.getUpdatedAt()).isAfter(originalUpdatedAt);
        verify(amenityRepository).save(amenity);
        verify(amenityRepository, never()).delete(amenity);
        verifyNoInteractions(hotelamenityRepository, roomamenityRepository);
    }
}
