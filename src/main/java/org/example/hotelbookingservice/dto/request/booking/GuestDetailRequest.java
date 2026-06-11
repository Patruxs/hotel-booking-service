package org.example.hotelbookingservice.dto.request.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Thông tin khách lưu trú")
public class GuestDetailRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 255)
    @Schema(description = "Họ tên khách", example = "Nguyen Van A")
    private String fullName;

    @NotBlank(message = "Identity number is required")
    @Size(max = 20)
    @Schema(description = "Số CCCD/Passport", example = "079203012345")
    private String identityNumber;
}
