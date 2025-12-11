package org.example.hotelbookingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/api/v1/amenities")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Amenity Management", description = "Quản lý danh mục tiện ích (Wifi, Bể bơi, Gym, Spa...)")
public class AmenityController {
    IAmenityService amenityService;

    @Operation(summary = "Lấy tiện ích cấp Khách sạn theo Hotel ID (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/hotel/{hotelId}/hotel-amenities")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<List<AmenityResponse>> getHotelAmenities(@PathVariable Integer hotelId) {
        return ApiResponse.<List<AmenityResponse>>builder()
                .status(200)
                .message("Success")
                .data(amenityService.getHotelAmenitiesByHotelId(hotelId))
                .build();
    }


    @Operation(summary = "Lấy tiện ích cấp Phòng theo Hotel ID (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/hotel/{hotelId}/room-amenities")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<List<RoomResponse>> getRoomAmenities(@PathVariable Integer hotelId) {
        return ApiResponse.<List<RoomResponse>>builder()
                .status(200)
                .message("Success")
                .data(amenityService.getRoomAmenitiesByHotelId(hotelId))
                .build();
    }

    @Operation(summary = "Xem chi tiết tiện ích")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tiện ích không tồn tại", content = @Content)
    })
    @GetMapping("/{id}")
    public ApiResponse<AmenityResponse> getAmenityById(@PathVariable Integer id) {
        return ApiResponse.<AmenityResponse>builder()
                .status(200)
                .message("Success")
                .data(amenityService.getAmenityById(id))
                .build();
    }

    @Operation(summary = "Tạo tiện ích mới (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Tên tiện ích đã tồn tại", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền Admin", content = @Content)
    })
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<AmenityResponse> createAmenity(@RequestBody @Valid AmenityRequest request) {
        return ApiResponse.<AmenityResponse>builder()
                .status(201)
                .message("Amenity created successfully")
                .data(amenityService.createAmenity(request))
                .build();
    }

    @Operation(summary = "Cập nhật tiện ích (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Tên trùng lặp", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy tiện ích", content = @Content)
    })
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<AmenityResponse> updateAmenity(
            @PathVariable Integer id,
            @RequestBody @Valid AmenityRequest request) {
        return ApiResponse.<AmenityResponse>builder()
                .status(200)
                .message("Amenity updated successfully")
                .data(amenityService.updateAmenity(id, request))
                .build();
    }

    @Operation(summary = "Xóa tiện ích (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy tiện ích", content = @Content)
    })
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteAmenity(@PathVariable Integer id) {
        amenityService.deleteAmenity(id);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Amenity deleted successfully")
                .build();
    }

    @Operation(summary = "Xóa danh sách tiện ích khỏi Khách sạn (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/hotel/{hotelId}/remove")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> removeHotelAmenities(
            @PathVariable Integer hotelId,
            @RequestBody @Valid HotelAmenityRemoveRequest request
    ) {
        amenityService.removeAmenitiesFromHotel(hotelId, request.getAmenityIds());

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Amenities removed from hotel successfully")
                .build();
    }

    @Operation(summary = "Xóa danh sách tiện ích khỏi Phòng (ADMIN)",
            description = "Yêu cầu cung cấp chính xác Hotel ID và Room ID để đảm bảo tính toàn vẹn dữ liệu.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/hotel/{hotelId}/room/{roomId}/remove")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> removeRoomAmenities(
            @PathVariable Integer hotelId,
            @PathVariable Integer roomId,
            @RequestBody @Valid RoomAmenityRemoveRequest request
    ) {
        amenityService.removeAmenitiesFromRoom(hotelId, roomId, request.getAmenityIds());

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Amenities removed from room successfully")
                .build();
    }

    @Operation(summary = "Lấy danh sách tiện ích của một phòng cụ thể thuộc khách sạn")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy phòng", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Phòng không thuộc khách sạn này", content = @Content)
    })
    @GetMapping("/hotel/{hotelId}/room/{roomId}")
    public ApiResponse<List<AmenityResponse>> getAmenitiesByRoom(
            @PathVariable Integer hotelId,
            @PathVariable Integer roomId
    ) {
        return ApiResponse.<List<AmenityResponse>>builder()
                .status(200)
                .message("Success")
                .data(amenityService.getAmenitiesByRoomId(hotelId, roomId))
                .build();
    }

}
