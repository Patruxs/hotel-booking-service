package org.example.hotelbookingservice.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @Schema(description = "Current password", example = "password123")
    private String oldPassword;
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @Schema(description = "New password (Minimum 8 characters, different from the old password)", example = "Newpass1!")
    private String newPassword;
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String confirmPassword;

    public String resolvedCurrentPassword() {
        return currentPassword != null ? currentPassword : oldPassword;
    }
}
