package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.booking.BookingCreateRequest;
import org.example.hotelbookingservice.dto.request.booking.BookingUpdateRequest;
import org.example.hotelbookingservice.dto.response.BookingResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/api/v1/bookings")
@Tag(name = "Booking Management", description = "Quản lý đặt phòng (Tạo mới, hủy, cập nhật trạng thái, xem lịch sử)")
public interface BookingApi {

    @Operation(summary = "Lấy toàn bộ danh sách đặt phòng (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content) })
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<List<BookingResponse>> getAllBookings();

    @Operation(summary = "Tạo đặt phòng mới", description = "Khách hàng tạo booking.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Đặt phòng thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Lỗi logic (Ngày check-in > check-out, Phòng đã kín chỗ...)", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy phòng hoặc user", content = @Content) })
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CUSTOMER') ")
    ApiResponse<BookingResponse> createBooking(@RequestBody @Valid BookingCreateRequest bookingRequest);

    @Operation(summary = "Tìm booking theo mã xác nhận")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Mã booking không tồn tại", content = @Content) })
    @GetMapping("/get-by-confirmation-code/{confirmationCode}")
    ApiResponse<BookingResponse> getBookingByConfirmationCode(@PathVariable String confirmationCode);

    @Operation(summary = "Cập nhật trạng thái booking (ADMIN)", description = "Check-in, Check-out, Hủy.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking không tồn tại", content = @Content) })
    @PutMapping("/update/{bookingId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<BookingResponse> updateBooking(@PathVariable Integer bookingId, @RequestBody BookingUpdateRequest bookingRequest);

    @Operation(summary = "Hủy đặt phòng", description = "Khách hàng hoặc Admin hủy phòng.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hủy thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Không thể hủy (Đã check-out hoặc đã hủy trước đó)", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền hủy (Không chính chủ)", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking không tồn tại", content = @Content) })
    @DeleteMapping("/cancel/{bookingId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CUSTOMER')")
    ApiResponse<Void> cancelBooking(@PathVariable Integer bookingId, @RequestParam(required = false) String reason);
}
