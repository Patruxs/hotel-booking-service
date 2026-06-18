package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.entity.Hotelamenity;

import java.util.List;

public interface IHotelAmenityService {

    void addAmenitiesToHotel(Integer hotelId, List<Integer> amenityIds);


    void removeAmenityFromHotel(Integer hotelId, Integer amenityId);


    List<Hotelamenity> getAmenitiesByHotelId(Integer hotelId);
}
