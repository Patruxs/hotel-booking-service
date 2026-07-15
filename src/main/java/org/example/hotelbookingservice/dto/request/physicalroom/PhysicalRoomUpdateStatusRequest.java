package org.example.hotelbookingservice.dto.request.physicalroom;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.hotelbookingservice.enums.RoomCondition;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to update physical room status")
public class PhysicalRoomUpdateStatusRequest {

    @NotNull(message = "Room condition is required")
    @Schema(description = "New room condition (CLEAN, DIRTY, MAINTENANCE)", example = "CLEAN")
    private RoomCondition roomCondition;
}
