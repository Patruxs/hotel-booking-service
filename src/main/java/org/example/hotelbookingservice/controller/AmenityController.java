package org.example.hotelbookingservice.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.amenity.AmenityRequest;
import org.example.hotelbookingservice.dto.request.amenity.HotelAmenityRemoveRequest;
import org.example.hotelbookingservice.dto.request.amenity.RoomAmenityRemoveRequest;
import org.example.hotelbookingservice.dto.response.AmenityResponse;
import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.example.hotelbookingservice.services.IAmenityService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.example.hotelbookingservice.api.AmenityApi;

@RestController
@RequestMapping("/api/v1/amenities")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AmenityController implements AmenityApi {

    IAmenityService amenityService;

    @Override
    public ApiResponse<List<AmenityResponse>> getAllAmenities() {
        return ApiResponse.<List<AmenityResponse>>builder().status(200).message("Success").data(amenityService.getAllAmenities()).build();
    }

    @Override
    public ApiResponse<List<AmenityResponse>> getHotelAmenities(@PathVariable Integer hotelId) {
        return ApiResponse.<List<AmenityResponse>>builder().status(200).message("Success").data(amenityService.getHotelAmenitiesByHotelId(hotelId)).build();
    }

    @Override
    public ApiResponse<List<RoomResponse>> getRoomAmenitiesByHotel(@PathVariable Integer hotelId) {
        return ApiResponse.<List<RoomResponse>>builder().status(200).message("Success").data(amenityService.getRoomAmenitiesByHotelId(hotelId)).build();
    }

    @Override
    public ApiResponse<AmenityResponse> getAmenityById(@PathVariable Integer id) {
        return ApiResponse.<AmenityResponse>builder().status(200).message("Success").data(amenityService.getAmenityById(id)).build();
    }

    @Override
    public ApiResponse<AmenityResponse> createAmenity(@RequestBody AmenityRequest request) {
        return ApiResponse.<AmenityResponse>builder().status(201).message("Amenity created successfully").data(amenityService.createAmenity(request)).build();
    }

    @Override
    public ApiResponse<AmenityResponse> updateAmenity(@PathVariable Integer id, @RequestBody AmenityRequest request) {
        return ApiResponse.<AmenityResponse>builder().status(200).message("Amenity updated successfully").data(amenityService.updateAmenity(id, request)).build();
    }

    @Override
    public ApiResponse<Void> deleteAmenity(@PathVariable Integer id) {
        amenityService.deleteAmenity(id);
        return ApiResponse.<Void>builder().status(200).message("Amenity deleted successfully").build();
    }

    @Override
    public ApiResponse<Void> removeHotelAmenities(@PathVariable Integer hotelId, @RequestBody HotelAmenityRemoveRequest request) {
        amenityService.removeAmenitiesFromHotel(hotelId, request.getAmenityIds());
        return ApiResponse.<Void>builder().status(200).message("Amenities removed from hotel successfully").build();
    }

    @Override
    public ApiResponse<Void> removeRoomAmenities(@PathVariable Integer hotelId, @PathVariable Integer roomId, @RequestBody RoomAmenityRemoveRequest request) {
        amenityService.removeAmenitiesFromRoom(hotelId, roomId, request.getAmenityIds());
        return ApiResponse.<Void>builder().status(200).message("Amenities removed from room successfully").build();
    }

    @Override
    public ApiResponse<List<AmenityResponse>> getAmenitiesByRoom(@PathVariable Integer hotelId, @PathVariable Integer roomId) {
        return ApiResponse.<List<AmenityResponse>>builder().status(200).message("Success").data(amenityService.getAmenitiesByRoomId(hotelId, roomId)).build();
    }
}
