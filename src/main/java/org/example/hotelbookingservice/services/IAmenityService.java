package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.dto.request.amenity.AmenityRequest;
import org.example.hotelbookingservice.dto.response.AmenityResponse;
import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.example.hotelbookingservice.entity.Amenity;

import java.util.List;

public interface IAmenityService {
    List<AmenityResponse> getHotelAmenitiesByHotelId(Integer hotelId);
    List<RoomResponse> getRoomAmenitiesByHotelId(Integer hotelId);
    List<AmenityResponse> getAllAmenities();
    AmenityResponse getAmenityById(Integer id);
    AmenityResponse createAmenity(AmenityRequest request);
    AmenityResponse updateAmenity(Integer id, AmenityRequest request);
    void deleteAmenity(Integer id);
    void removeAmenitiesFromHotel(Integer hotelId, List<Integer> amenityIds);
    void removeAmenitiesFromRoom(Integer hotelId,Integer roomId, List<Integer> amenityIds);
    List<AmenityResponse> getAmenitiesByRoomId(Integer hotelId, Integer roomId);

}