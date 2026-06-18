package org.example.hotelbookingservice.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email của người dùng", example = "admin@gmail.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "Mật khẩu", example = "admin123")
    private String password;
}
