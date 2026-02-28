package org.example.hotelbookingservice.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.entity.Amenity;
import org.example.hotelbookingservice.entity.Hotel;
import org.example.hotelbookingservice.entity.Hotelamenity;
import org.example.hotelbookingservice.entity.HotelamenityId;
import org.example.hotelbookingservice.exception.AppException;
import org.example.hotelbookingservice.exception.ErrorCode;
import org.example.hotelbookingservice.repository.AmenityRepository;
import org.example.hotelbookingservice.repository.HotelRepository;
import org.example.hotelbookingservice.repository.HotelamenityRepository;
import org.example.hotelbookingservice.services.IHotelAmenityService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelAmenityServiceImpl implements IHotelAmenityService {
    private final HotelRepository hotelRepository;
    private final AmenityRepository amenityRepository;
    private final HotelamenityRepository hotelamenityRepository;

    @Override
    @Transactional
    public void addAmenitiesToHotel(Integer hotelId, List<Integer> amenityIds) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        if (amenityIds == null || amenityIds.isEmpty()) {
            return;
        }

        Set<Integer> uniqueAmenityIds = new HashSet<>(amenityIds);

        List<Amenity> foundAmenities = amenityRepository.findAllById(uniqueAmenityIds);

        if (foundAmenities.size() != uniqueAmenityIds.size()) {
            throw new AppException(ErrorCode.NOT_FOUND_EXCEPTION);
        }

        // Fetch existing hotel amenities to filter out duplicates in-memory
        List<Hotelamenity> existingHotelAmenities = hotelamenityRepository.findByIdHotelId(hotelId);
        Set<Integer> existingAmenityIds = existingHotelAmenities.stream()
                .map(ha -> ha.getId().getAmenityId())
                .collect(Collectors.toSet());

        List<Hotelamenity> newHotelAmenities = new ArrayList<>();

        for (Amenity amenity : foundAmenities) {
            if (!existingAmenityIds.contains(amenity.getId())) {
                HotelamenityId id = new HotelamenityId();
                id.setHotelId(hotelId);
                id.setAmenityId(amenity.getId());

                Hotelamenity hotelamenity = new Hotelamenity();
                hotelamenity.setId(id);
                hotelamenity.setHotel(hotel);
                hotelamenity.setAmenity(amenity);

                newHotelAmenities.add(hotelamenity);
            }
        }

        if (!newHotelAmenities.isEmpty()) {
            hotelamenityRepository.saveAll(newHotelAmenities);
        }
    }

    @Override
    public void removeAmenityFromHotel(Integer hotelId, Integer amenityId) {
        HotelamenityId id = new HotelamenityId();
        id.setAmenityId(hotelId);
        id.setHotelId(hotelId);

        if (hotelamenityRepository.existsById(id)) {
            hotelamenityRepository.deleteById(id);
        }else{
            throw new AppException(ErrorCode.NOT_FOUND_EXCEPTION);
        }
    }

    @Override
    public List<Hotelamenity> getAmenitiesByHotelId(Integer hotelId) {
        return hotelamenityRepository.findByIdHotelId(hotelId);
    }
}
