package org.example.hotelbookingservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.example.hotelbookingservice.enums.UserRole;

@Data
@Builder
public class LoginResponse {
    @Schema(description = "JWT access token used to authenticate subsequent requests")
    private String accessToken;
    @Schema(
            description = "Primary account role",
            example = "OWNER",
            allowableValues = {"CUSTOMER", "ADMIN", "OWNER", "RECEPTIONIST", "MANAGER"}
    )
    private UserRole role;
    @Schema(description = "Token expiration time", example = "6 months")
    private String expirationTime;
    @Schema(description = "Account activation status", example = "true")
    private Boolean isActive;
}
