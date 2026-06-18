package org.example.hotelbookingservice.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.hotelbookingservice.enums.UserRole;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Fullname is required")
    @Size(min = 2, max = 50, message = "Fullname must be between 2 and 50 characters")
    @Schema(description = "Họ và tên người dùng", example = "Nguyen Van A")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Địa chỉ Email (Dùng để đăng nhập)", example = "customer@example.com")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10,12}$", message = "Phone number must be between 10 and 12 digits")
    @Schema(description = "Số điện thoại liên lạc", example = "0987654321")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "Mật khẩu (Tối thiểu 6 ký tự)", example = "password123")
    private String password;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Ngày sinh", example = "1995-05-20", type = "string", pattern = "yyyy-MM-dd")
    private LocalDate dob;

}
