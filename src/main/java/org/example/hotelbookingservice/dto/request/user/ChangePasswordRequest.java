package org.example.hotelbookingservice.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Old password is required")
    @Schema(description = "Mật khẩu hiện tại", example = "password123")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "Mật khẩu mới (Khác mật khẩu cũ)", example = "newpassword456")
    private String newPassword;
}
