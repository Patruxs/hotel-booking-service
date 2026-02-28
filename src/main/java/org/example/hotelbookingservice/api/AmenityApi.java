package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.amenity.AmenityRequest;
import org.example.hotelbookingservice.dto.request.amenity.HotelAmenityRemoveRequest;
import org.example.hotelbookingservice.dto.request.amenity.RoomAmenityRemoveRequest;
import org.example.hotelbookingservice.dto.response.AmenityResponse;
import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/api/v1/amenities")
@Tag(name = "Amenity Management", description = "Quản lý danh mục tiện ích (Wifi, Bể bơi, Gym, Spa...)")
public interface AmenityApi {

    @Operation(summary = "Lấy danh sách tất cả tiện ích trong hệ thống", description = "Dùng cho Admin khi muốn xem danh sách để chọn tiện ích thêm vào phòng/khách sạn.")
    @GetMapping("/all")
    ApiResponse<List<AmenityResponse>> getAllAmenities();

    @Operation(summary = "Lấy tiện ích cấp Khách sạn theo Hotel ID (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/hotel/{hotelId}/hotel-amenities")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<List<AmenityResponse>> getHotelAmenities(@PathVariable Integer hotelId);

    @Operation(summary = "Lấy danh sách amenity của tất cả các phòng thuộc Hotel A")
    @GetMapping("/hotel/{hotelId}/room-amenities")
    ApiResponse<List<RoomResponse>> getRoomAmenitiesByHotel(@PathVariable Integer hotelId);

    @Operation(summary = "Xem chi tiết tiện ích")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tiện ích không tồn tại", content = @Content) })
    @GetMapping("/{id}")
    ApiResponse<AmenityResponse> getAmenityById(@PathVariable Integer id);

    @Operation(summary = "Tạo tiện ích mới (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Tên tiện ích đã tồn tại", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền Admin", content = @Content) })
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<AmenityResponse> createAmenity(@RequestBody @Valid AmenityRequest request);

    @Operation(summary = "Cập nhật tiện ích (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Tên trùng lặp", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy tiện ích", content = @Content) })
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<AmenityResponse> updateAmenity(@PathVariable Integer id, @RequestBody @Valid AmenityRequest request);

    @Operation(summary = "Xóa tiện ích (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy tiện ích", content = @Content) })
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> deleteAmenity(@PathVariable Integer id);

    @Operation(summary = "Xóa danh sách tiện ích khỏi Khách sạn (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/hotel/{hotelId}/remove")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> removeHotelAmenities(@PathVariable Integer hotelId, @RequestBody @Valid HotelAmenityRemoveRequest request);

    @Operation(summary = "Xóa danh sách tiện ích khỏi Phòng (ADMIN)", description = "Yêu cầu cung cấp chính xác Hotel ID và Room ID để đảm bảo tính toàn vẹn dữ liệu.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/hotel/{hotelId}/room/{roomId}/remove")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> removeRoomAmenities(@PathVariable Integer hotelId, @PathVariable Integer roomId, @RequestBody @Valid RoomAmenityRemoveRequest request);

    @Operation(summary = "Lấy danh sách tiện ích của một phòng cụ thể thuộc khách sạn")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy phòng", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Phòng không thuộc khách sạn này", content = @Content) })
    @GetMapping("/hotel/{hotelId}/room/{roomId}")
    ApiResponse<List<AmenityResponse>> getAmenitiesByRoom(@PathVariable Integer hotelId, @PathVariable Integer roomId);
}
