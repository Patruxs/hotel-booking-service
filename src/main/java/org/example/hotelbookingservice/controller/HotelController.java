package org.example.hotelbookingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Hotel Management", description = "Quản lý thông tin khách sạn")
public class HotelController  {
    IHotelService hotelService;

    @Operation(summary = "Thêm khách sạn mới", description = "Dành cho ADMIN. Yêu cầu upload ảnh.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo khách sạn thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Khách sạn đã tồn tại hoặc dữ liệu không hợp lệ", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền Admin", content = @Content)
    })
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<HotelResponse> addHotel(
            @RequestParam(value = "image", required = false) List<MultipartFile> image,
            @ParameterObject @ModelAttribute @Valid HotelCreateRequest hotelCreateRequest
    ) {
        return ApiResponse.<HotelResponse>builder()
                .status(201)
                .message("Hotel added successfully")
                .data(hotelService.addHotel(hotelCreateRequest, image))
                .build();
    }



    @Operation(summary = "Cập nhật khách sạn", description = "Cập nhật thông tin khách sạn.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền sở hữu khách sạn này", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Khách sạn không tồn tại", content = @Content)
    })
    @PutMapping(value = "/update/{hotelId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<HotelResponse> updateHotel(
            @PathVariable Integer hotelId,
            @RequestParam(value = "image", required = false) List<MultipartFile> image,
            @ParameterObject @ModelAttribute @Valid HotelUpdateRequest hotelUpdateRequest
    ) {
        return ApiResponse.<HotelResponse>builder()
                .status(200)
                .message("Hotel updated successfully") //
                .data(hotelService.updateHotel(hotelId, hotelUpdateRequest, image)) //
                .build();
    }

    @Operation(summary = "Xóa khách sạn", description = "Xóa khách sạn khỏi hệ thống.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền xóa (Không phải chủ sở hữu/Admin)", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Khách sạn không tồn tại", content = @Content)
    })
    @DeleteMapping("/delete/{hotelId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteHotel(@PathVariable Integer hotelId) {
        hotelService.deleteHotel(hotelId); //
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Hotel deleted successfully")
                .build();
    }

    @Operation(summary = "Xem chi tiết khách sạn", description = "API Public.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Khách sạn không tìm thấy", content = @Content)
    })
    @GetMapping("/{hotelId}")
    public ApiResponse<HotelResponse> getHotelById(@PathVariable Integer hotelId) {
        return ApiResponse.<HotelResponse>builder()
                .status(200)
                .message("Success")
                .data(hotelService.getHotelById(hotelId)) //
                .build();
    }

    @Operation(summary = "Lấy danh sách khách sạn của tôi", description = "Xem danh sách khách sạn do user đang đăng nhập quản lý.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my-hotels")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CUSTOMER')")
    public ApiResponse<List<HotelResponse>> getMyHotels() {
        return ApiResponse.<List<HotelResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelService.getMyHotels()) //
                .build();
    }

    @Operation(summary = "Lấy tất cả khách sạn", description = "API Public.")
    @GetMapping("/all")
    public ApiResponse<List<HotelResponse>> getAllHotels() {
        return ApiResponse.<List<HotelResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelService.getAllHotels()) //
                .build();
    }


    @Operation(summary = "Tìm kiếm khách sạn", description = "Tìm theo vị trí, ngày check-in/out, số lượng người.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tìm kiếm thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ngày check-in/out không hợp lệ", content = @Content)
    })
    @GetMapping("/search")
    public ApiResponse<List<HotelResponse>> searchHotels(
            @RequestParam String location,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false, defaultValue = "1") Integer roomQuantity
    ) {
        return ApiResponse.<List<HotelResponse>>builder()
                .status(200)
                .message("Search results")
                .data(hotelService.searchHotels(location, checkInDate, checkOutDate, capacity, roomQuantity))
                .build();
    }

    @Operation(summary = "Lấy danh sách phòng theo Hotel ID", description = "Xem tất cả các phòng thuộc một khách sạn cụ thể.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Khách sạn không tồn tại", content = @Content)
    })
    @GetMapping("/{hotelId}/rooms")
    public ApiResponse<List<RoomResponse>> getRoomsByHotelId(@PathVariable Integer hotelId) {
        return ApiResponse.<List<RoomResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelService.getRoomsByHotelId(hotelId))
                .build();
    }

}
