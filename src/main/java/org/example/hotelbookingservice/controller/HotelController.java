package org.example.hotelbookingservice.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.hotel.HotelCreateRequest;
import org.example.hotelbookingservice.dto.request.hotel.HotelUpdateRequest;
import org.example.hotelbookingservice.dto.response.HotelResponse;
import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.example.hotelbookingservice.services.IHotelService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;
import org.example.hotelbookingservice.api.HotelApi;

@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HotelController implements HotelApi {

    IHotelService hotelService;

    @Override
    public ApiResponse<HotelResponse> addHotel(@RequestParam(value = "image", required = false) List<MultipartFile> image, @ModelAttribute HotelCreateRequest hotelCreateRequest) {
        return ApiResponse.<HotelResponse>builder().status(201).message("Hotel added successfully").data(hotelService.addHotel(hotelCreateRequest, image)).build();
    }

    @Override
    public ApiResponse<HotelResponse> updateHotel(@PathVariable Integer hotelId, @RequestParam(value = "image", required = false) List<MultipartFile> image, @ModelAttribute HotelUpdateRequest hotelUpdateRequest) {
        return ApiResponse.<HotelResponse>builder().status(200).message(//
        "Hotel updated successfully").data(//
        hotelService.updateHotel(hotelId, hotelUpdateRequest, image)).build();
    }

    @Override
    public ApiResponse<Void> deleteHotel(@PathVariable Integer hotelId) {
        //
        hotelService.deleteHotel(hotelId);
        return ApiResponse.<Void>builder().status(200).message("Hotel deleted successfully").build();
    }

    @Override
    public ApiResponse<HotelResponse> getHotelById(@PathVariable Integer hotelId) {
        return ApiResponse.<HotelResponse>builder().status(200).message("Success").data(//
        hotelService.getHotelById(hotelId)).build();
    }

    @Override
    public ApiResponse<List<HotelResponse>> getMyHotels() {
        return ApiResponse.<List<HotelResponse>>builder().status(200).message("Success").data(//
        hotelService.getMyHotels()).build();
    }

    @Override
    public ApiResponse<List<HotelResponse>> getAllHotels() {
        return ApiResponse.<List<HotelResponse>>builder().status(200).message("Success").data(//
        hotelService.getAllHotels()).build();
    }

    @Override
    public ApiResponse<List<HotelResponse>> searchHotels(@RequestParam String location, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate, @RequestParam(required = false) Integer capacity, @RequestParam(required = false, defaultValue = "1") Integer roomQuantity) {
        return ApiResponse.<List<HotelResponse>>builder().status(200).message("Search results").data(hotelService.searchHotels(location, checkInDate, checkOutDate, capacity, roomQuantity)).build();
    }

    @Override
    public ApiResponse<List<RoomResponse>> getRoomsByHotelId(@PathVariable Integer hotelId) {
        return ApiResponse.<List<RoomResponse>>builder().status(200).message("Success").data(hotelService.getRoomsByHotelId(hotelId)).build();
    }
}
