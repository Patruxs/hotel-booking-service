package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.dto.request.room.RoomCreateRequest;
import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.example.hotelbookingservice.enums.RoomType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface IRoomService {
    RoomResponse addRoom(RoomCreateRequest roomCreateRequest, MultipartFile imageFile);
    RoomResponse updateRoom(RoomCreateRequest roomCreateRequest, MultipartFile imageFile);
    List<RoomResponse> getAllRooms();
    RoomResponse getRoomById(Integer id);
    void deleteRoom (Integer id);
    List<RoomResponse> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, RoomType roomType);
    List<RoomType> getAllRoomTypes();
    List<RoomResponse> searchRoom(String input);
    List<RoomResponse> getAvailableRoomsByHotelId(Integer hotelId, LocalDate checkInDate, LocalDate checkOutDate);
}
