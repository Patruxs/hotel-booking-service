package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.dto.request.hotel.HotelCreateRequest;
import org.example.hotelbookingservice.dto.request.hotel.HotelUpdateRequest;
import org.example.hotelbookingservice.dto.response.HotelResponse;
import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface IHotelService {
    HotelResponse addHotel(HotelCreateRequest hotelCreateRequest, List<MultipartFile> imageFile);


    HotelResponse updateHotel(Integer id, HotelUpdateRequest hotelUpdateRequest, List<MultipartFile> imageFile);


    HotelResponse getHotelById(Integer id);


    List<HotelResponse> getAllHotels();


    void deleteHotel(Integer id);


    List<HotelResponse> getMyHotels();

    List<HotelResponse> searchHotels(String location, LocalDate checkInDate, LocalDate checkOutDate, Integer capacity,Integer roomQuantity);

    List<RoomResponse> getRoomsByHotelId(Integer hotelId);
}

