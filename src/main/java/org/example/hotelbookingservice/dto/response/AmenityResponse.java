package org.example.hotelbookingservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmenityResponse {
    @Schema(description = "ID của tiện ích")
    private Integer id;

    @Schema(description = "Tên tiện ích", example = "Swimming Pool")
    private String name;

    @Schema(description = "Phân loại tiện ích", example = "Hotel Service")
    private String type;
}
