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
@Schema(description = "Request cập nhật tình trạng phòng vật lý")
public class PhysicalRoomUpdateStatusRequest {

    @NotNull(message = "Room condition is required")
    @Schema(description = "Tình trạng mới (CLEAN, DIRTY, MAINTENANCE)", example = "CLEAN")
    private RoomCondition roomCondition;
}
