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
@Schema(description = "Thông tin phòng vật lý")
public class PhysicalRoomResponse {
    @Schema(description = "ID phòng vật lý")
    private Integer id;

    @Schema(description = "Số phòng", example = "101")
    private Integer roomNumber;

    @Schema(description = "Tình trạng phòng", example = "CLEAN")
    private RoomCondition roomCondition;

    @Schema(description = "ID loại phòng liên kết")
    private Integer roomId;

    @Schema(description = "Tên loại phòng")
    private String roomName;
}
