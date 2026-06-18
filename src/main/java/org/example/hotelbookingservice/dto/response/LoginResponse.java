package org.example.hotelbookingservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.example.hotelbookingservice.enums.UserRole;

@Data
@Builder
public class LoginResponse {
    @Schema(description = "JWT Token dùng để xác thực các request sau này")
    private String token;
    @Schema(description = "Vai trò của người dùng (CUSTOMER, ADMIN)", example = "ADMIN")
    private UserRole role;
    @Schema(description = "Thời gian hết hạn của token", example = "6 months")
    private String expirationTime;
    @Schema(description = "Trạng thái kích hoạt của tài khoản", example = "true")
    private Boolean isActive;
}
