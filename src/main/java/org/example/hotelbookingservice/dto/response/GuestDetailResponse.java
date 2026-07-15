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
@Schema(description = "Guest information")
public class GuestDetailResponse {
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "Guest full name", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "Citizen ID/Passport number", example = "079203012345")
    private String identityNumber;
}
