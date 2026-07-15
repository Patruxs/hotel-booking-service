package org.example.hotelbookingservice.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String token;
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String newPassword;
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String password;
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String confirmPassword;

    public String resolvedPassword() {
        return newPassword != null ? newPassword : password;
    }
}
