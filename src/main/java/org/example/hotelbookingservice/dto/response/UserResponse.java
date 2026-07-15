package org.example.hotelbookingservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    @Schema(description = "User ID")
    private UUID id;

    @Schema(description = "Full name", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "Login email", example = "user@example.com")
    private String email;

    @Schema(description = "Phone number", example = "0987654321")
    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date of birth", example = "1995-05-20")
    private LocalDate dob;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Account creation time")
    private LocalDateTime createdAt;

    @Schema(description = "Activity status (True: Active, False: Locked)", example = "true")
    private Boolean activate;

}
