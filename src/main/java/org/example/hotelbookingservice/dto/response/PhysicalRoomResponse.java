package org.example.hotelbookingservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.hotelbookingservice.enums.RoomCondition;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Physical room information")
public class PhysicalRoomResponse {
    @Schema(description = "Physical room ID")
    private Integer id;

    @Schema(description = "Room number", example = "D101")
    private String roomNumber;

    @Schema(description = "Room condition", example = "CLEAN")
    private RoomCondition roomCondition;

    @Schema(description = "Associated room type ID")
    private Integer roomId;

    @Schema(description = "Room type name")
    private String roomName;
}
