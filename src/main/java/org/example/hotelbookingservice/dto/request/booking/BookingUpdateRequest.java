package org.example.hotelbookingservice.dto.request.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.hotelbookingservice.enums.BookingStatus;

@Data
public class BookingUpdateRequest {
    @Schema(description = "Trạng thái mới của booking (CHECKED_IN, CHECKED_OUT, CANCELLED)", example = "CHECKED_IN")
    private BookingStatus status;
    @Schema(description = "Lý do hủy (nếu có)", example = "Khách không đến")
    private String cancelReason;
    @Schema(description = "Số phòng gán cho khách khi Check-in", example = "205")
    private String roomNumber;

    @Schema(description = "Phí hư hao/thiệt hại (nếu có, áp dụng khi check-out)", example = "500000.0")
    private Float damageFee;
    @Schema(description = "Mô tả hư hao/thiệt hại", example = "Vỡ gương phòng tắm")
    private String damageDescription;
}
