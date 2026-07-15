package org.example.hotelbookingservice.services.impl;

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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelAmenityServiceImpl implements IHotelAmenityService {
    private final HotelRepository hotelRepository;
    private final AmenityRepository amenityRepository;
    private final HotelamenityRepository hotelamenityRepository;

    @Override
    @Transactional
    public void addAmenitiesToHotel(Integer hotelId, List<Integer> amenityIds) {
        Hotel hotel = hotelRepository.findAll().stream()
                .filter(h -> hotelId.equals(h.getId()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        for (Integer amenityId : amenityIds) {
            Amenity amenity = amenityRepository.findAll().stream()
                    .filter(a -> amenityId.equals(a.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

            // Create a complex primary key
            HotelamenityId id = new HotelamenityId();
            id.setHotelId(hotel.getUuid());
            id.setAmenityId(amenity.getUuid());

            // Check if this relationship already exists to avoid duplicate key errors
            if (!hotelamenityRepository.existsById(id)) {
                Hotelamenity hotelamenity = new Hotelamenity();
                hotelamenity.setId(id);
                hotelamenity.setHotel(hotel);
                hotelamenity.setAmenity(amenity);

                hotelamenityRepository.save(hotelamenity);
            }
        }
    }

    @Override
    @Transactional
    public void removeAmenityFromHotel(Integer hotelId, Integer amenityId) {
        Hotel hotel = hotelRepository.findAll().stream()
                .filter(h -> hotelId.equals(h.getId()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        Amenity amenity = amenityRepository.findAll().stream()
                .filter(a -> amenityId.equals(a.getId()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        HotelamenityId id = new HotelamenityId();
        id.setHotelId(hotel.getUuid());
        id.setAmenityId(amenity.getUuid());

        if (hotelamenityRepository.existsById(id)) {
            hotelamenityRepository.deleteById(id);
        } else {
            throw new AppException(ErrorCode.NOT_FOUND_EXCEPTION);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Hotelamenity> getAmenitiesByHotelId(Integer hotelId) {
        Hotel hotel = hotelRepository.findAll().stream()
                .filter(h -> hotelId.equals(h.getId()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));
        return new ArrayList<>(hotel.getHotelAmenities());
    }
}
