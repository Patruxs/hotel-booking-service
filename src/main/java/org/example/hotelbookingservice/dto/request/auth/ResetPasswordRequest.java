package org.example.hotelbookingservice.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String token;
    private String newPassword;
    private String password;
    private String confirmPassword;

    public String resolvedPassword() {
        return newPassword != null ? newPassword : password;
    }
}
