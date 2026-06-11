package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.physicalroom.PhysicalRoomCreateRequest;
import org.example.hotelbookingservice.dto.request.physicalroom.PhysicalRoomUpdateStatusRequest;
import org.example.hotelbookingservice.dto.response.PhysicalRoomResponse;
import org.example.hotelbookingservice.enums.RoomCondition;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/physical-rooms")
@Tag(name = "Physical Room Management", description = "Quản lý phòng vật lý (Tình trạng phòng: CLEAN, DIRTY, MAINTENANCE)")
public interface PhysicalRoomApi {

    @Operation(summary = "Tạo phòng vật lý mới (ADMIN)", description = "Thêm phòng vật lý mới và gắn vào loại phòng (Room).")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Số phòng đã tồn tại hoặc dữ liệu không hợp lệ", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy loại phòng (Room)", content = @Content)
    })
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<PhysicalRoomResponse> createPhysicalRoom(@RequestBody @Valid PhysicalRoomCreateRequest request);

    @Operation(summary = "Cập nhật phòng vật lý (ADMIN)", description = "Sửa thông tin phòng vật lý (số phòng, loại phòng, tình trạng).")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy phòng vật lý", content = @Content)
    })
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<PhysicalRoomResponse> updatePhysicalRoom(@PathVariable Integer id, @RequestBody @Valid PhysicalRoomCreateRequest request);

    @Operation(summary = "Cập nhật tình trạng phòng (ADMIN, RECEPTIONIST)", description = "Thay đổi tình trạng phòng (VD: DIRTY → CLEAN sau khi dọn phòng).")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật trạng thái thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy phòng vật lý", content = @Content)
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('RECEPTIONIST')")
    ApiResponse<PhysicalRoomResponse> updateStatus(@PathVariable Integer id, @RequestBody @Valid PhysicalRoomUpdateStatusRequest request);

    @Operation(summary = "Xóa phòng vật lý (ADMIN)", description = "Xóa phòng vật lý khỏi hệ thống.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy phòng vật lý", content = @Content)
    })
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> deletePhysicalRoom(@PathVariable Integer id);

    @Operation(summary = "Xem chi tiết phòng vật lý", description = "Lấy thông tin phòng vật lý theo ID.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('RECEPTIONIST')")
    ApiResponse<PhysicalRoomResponse> getById(@PathVariable Integer id);

    @Operation(summary = "Lấy danh sách phòng vật lý theo loại phòng (Room)", description = "Xem tất cả phòng vật lý thuộc loại phòng cụ thể.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/by-room/{roomId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('RECEPTIONIST')")
    ApiResponse<List<PhysicalRoomResponse>> getByRoomId(@PathVariable Integer roomId);

    @Operation(summary = "Lọc phòng theo tình trạng (CLEAN/DIRTY/MAINTENANCE)", description = "Xem danh sách phòng vật lý theo trạng thái.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/by-condition")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('RECEPTIONIST')")
    ApiResponse<List<PhysicalRoomResponse>> getByCondition(@RequestParam RoomCondition condition);

    @Operation(summary = "Lấy tất cả phòng vật lý (ADMIN, RECEPTIONIST)", description = "Xem toàn bộ danh sách phòng vật lý.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('RECEPTIONIST')")
    ApiResponse<List<PhysicalRoomResponse>> getAll();
}
