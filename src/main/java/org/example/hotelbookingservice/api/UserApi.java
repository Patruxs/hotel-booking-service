package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.user.ChangePasswordRequest;
import org.example.hotelbookingservice.dto.request.user.UserUpdateRequest;
import org.example.hotelbookingservice.dto.response.BookingResponse;
import org.example.hotelbookingservice.dto.response.UserResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Quản lý người dùng (Thông tin cá nhân, Lịch sử đặt phòng, Quản trị viên)")
public interface UserApi {

    @Operation(summary = "Lấy danh sách tất cả người dùng (ADMIN)", description = "Chỉ Admin mới có quyền truy cập.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập (Token không hợp lệ)", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập (Không phải ADMIN)", content = @Content) })
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<List<UserResponse>> getAllUser();

    @Operation(summary = "Cập nhật thông tin cá nhân", description = "Người dùng tự cập nhật thông tin của mình.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (Số điện thoại sai, ngày sinh sai...)", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập", content = @Content) })
    @PutMapping("/update")
    ApiResponse<UserResponse> updateOwnAccount(@RequestBody @Valid UserUpdateRequest request);

    @Operation(summary = "Xóa tài khoản cá nhân", description = "Người dùng tự xóa tài khoản của mình.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập", content = @Content) })
    @DeleteMapping("/delete")
    ApiResponse<Void> deleteOwnAccount();

    @Operation(summary = "Lấy thông tin profile", description = "Lấy thông tin của người dùng đang đăng nhập.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy user (Token lỗi)", content = @Content) })
    @GetMapping("/get-logged-in-profile-info")
    ApiResponse<UserResponse> getOwnAccountDetails();

    @Operation(summary = "Lịch sử đặt phòng", description = "Lấy danh sách các booking của người dùng hiện tại.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập", content = @Content) })
    @GetMapping("/get-user-bookings")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    ApiResponse<List<BookingResponse>> getMyBookingHistory();

    @Operation(summary = "Đổi mật khẩu", description = "Yêu cầu mật khẩu cũ phải chính xác.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Mật khẩu cũ không đúng hoặc Mật khẩu mới trùng mật khẩu cũ", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập", content = @Content) })
    @PutMapping("/change-password")
    ApiResponse<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request);

    @Operation(summary = "Khóa tài khoản (ADMIN)", description = "Admin khóa tài khoản người dùng.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Khóa thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền Admin", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User không tồn tại", content = @Content) })
    @PutMapping("/{userId}/lock")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> lockUser(@PathVariable Integer userId);

    @Operation(summary = "Mở khóa tài khoản (ADMIN)", description = "Admin mở khóa tài khoản người dùng.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mở khóa thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền Admin", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User không tồn tại", content = @Content) })
    @PutMapping("/{userId}/unlock")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> unlockUser(@PathVariable Integer userId);
}
