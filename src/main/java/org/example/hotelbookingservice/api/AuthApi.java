package org.example.hotelbookingservice.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.auth.LoginRequest;
import org.example.hotelbookingservice.dto.request.auth.RegisterRequest;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.RegistrationResponse;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.TokenResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Các API liên quan đến xác thực người dùng (Đăng ký, Đăng nhập)")
public interface AuthApi {

    @Operation(summary = "Đăng ký tài khoản mới", description = "Tạo tài khoản khách hàng (CUSTOMER).")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Đăng ký thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Lỗi Validation (Email sai định dạng, mật khẩu yếu...) hoặc Email đã tồn tại", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content) })
    @PostMapping("/register")
    ApiResponse<RegistrationResponse> registerUser(@RequestBody @Valid RegisterRequest request);

    @Operation(summary = "Đăng nhập", description = "Xác thực người dùng và trả về JWT Token.")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng nhập thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Sai mật khẩu hoặc tài khoản bị khóa", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Email không tồn tại", content = @Content) })
    @PostMapping("/login")
    ApiResponse<TokenResponse> loginUser(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response);

    @Operation(summary = "Đăng xuất", description = "Xóa context xác thực phía server. Client cần tự xóa token ở LocalStorage.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng xuất thành công"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập", content = @Content) })
    @PostMapping("/logout")
    ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response);
}
