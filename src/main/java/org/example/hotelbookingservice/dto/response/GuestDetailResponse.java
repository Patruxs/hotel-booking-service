package org.example.hotelbookingservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Thông tin khách lưu trú")
public class GuestDetailResponse {
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "Họ tên khách", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "Số CCCD/Passport", example = "079203012345")
    private String identityNumber;
}
