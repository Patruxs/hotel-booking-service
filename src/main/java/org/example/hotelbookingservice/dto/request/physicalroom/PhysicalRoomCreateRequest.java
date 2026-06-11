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
@Schema(description = "Request tạo/cập nhật phòng vật lý")
public class PhysicalRoomCreateRequest {

    @NotNull(message = "Room number is required")
    @Schema(description = "Số phòng vật lý", example = "101")
    private Integer roomNumber;

    @NotNull(message = "Room condition is required")
    @Schema(description = "Tình trạng phòng (CLEAN, DIRTY, MAINTENANCE)", example = "CLEAN")
    private RoomCondition roomCondition;

    @NotNull(message = "Room ID is required")
    @Schema(description = "ID loại phòng (Room) mà phòng vật lý này thuộc về", example = "1")
    private Integer roomId;
}
