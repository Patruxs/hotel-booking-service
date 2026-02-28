package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.room.RoomCreateRequest;
import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.example.hotelbookingservice.enums.RoomType;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/v1/rooms")
@Tag(name = "Room Management", description = "Quản lý phòng khách sạn (Thêm, sửa, xóa, tìm kiếm phòng trống)")
public interface RoomApi {

    @Operation(summary = "Thêm phòng mới (ADMIN)", description = "Thêm phòng vào khách sạn.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo phòng thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Phòng đã tồn tại hoặc dữ liệu sai", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền Admin hoặc không sở hữu khách sạn", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Khách sạn không tồn tại", content = @Content) })
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<RoomResponse> addRoom(@RequestParam(value = "image", required = false) MultipartFile image, @ParameterObject @ModelAttribute @Valid RoomCreateRequest roomCreateRequest);

    @Operation(summary = "Cập nhật thông tin phòng (ADMIN)", description = "Sửa thông tin phòng.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Phòng không tồn tại", content = @Content) })
    @PutMapping(value = "/update/{roomId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<RoomResponse> updateRoom(@PathVariable Integer roomId, @RequestParam(value = "photo", required = false) MultipartFile photo, @ParameterObject @ModelAttribute RoomCreateRequest roomCreateRequest);

    @Operation(summary = "Lấy danh sách tất cả phòng", description = "API Public.")
    @GetMapping("/all")
    ApiResponse<List<RoomResponse>> getAllRooms();

    @Operation(summary = "Xem chi tiết phòng", description = "Lấy thông tin phòng theo ID.")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy phòng", content = @Content) })
    @GetMapping("/{roomId}")
    ApiResponse<RoomResponse> getRoomById(@PathVariable Integer roomId);

    @Operation(summary = "Xóa phòng (ADMIN)", description = "Xóa phòng khỏi hệ thống.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy phòng", content = @Content) })
    @DeleteMapping("/delete/{roomId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> deleteRoom(@PathVariable Integer roomId);

    @Operation(summary = "Tìm phòng trống theo ngày", description = "Tìm các phòng có thể đặt.")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ngày check-in/out không hợp lệ hoặc thiếu", content = @Content) })
    @GetMapping("/all-available-rooms")
    ApiResponse<List<RoomResponse>> getAvailableRooms(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate, @RequestParam(required = false) String roomType);

    @Operation(summary = "Lấy danh sách loại phòng", description = "Trả về các enum loại phòng (SINGLE, DOUBLE, SUIT, TRIPLE).")
    @GetMapping("/types")
    ApiResponse<List<RoomType>> getRoomTypes();

    @Operation(summary = "Tìm kiếm phòng chung", description = "Tìm kiếm phòng theo từ khóa (Tên, mô tả, giá...).")
    @GetMapping("/search")
    ApiResponse<List<RoomResponse>> searchRoom(@RequestParam String input);

    @Operation(summary = "Tìm phòng trống theo ID khách sạn", description = "Lấy danh sách phòng còn trống của một khách sạn cụ thể trong khoảng thời gian Check-in/Check-out.")
    @GetMapping("/hotel/{hotelId}/available")
    ApiResponse<List<RoomResponse>> getAvailableRoomsByHotel(@PathVariable Integer hotelId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate);
}
