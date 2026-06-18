package org.example.hotelbookingservice.dto.request.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.hotelbookingservice.enums.BookingStatus;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingCreateRequest {

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date must be in the future or today")
    @Schema(description = "Ngày nhận phòng (YYYY-MM-DD)", example = "2024-12-24")
    private LocalDate checkinDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    @Schema(description = "Ngày trả phòng (YYYY-MM-DD)", example = "2024-12-26")
    private LocalDate checkoutDate;

    @Min(value = 1, message = "Must have at least 1 adult")
    @Schema(description = "Số lượng người lớn", example = "2")
    private Integer adultAmount;

    @Min(value = 0, message = "Children amount cannot be negative")
    @Schema(description = "Số lượng trẻ em", example = "1")
    private Integer childrenAmount;

    @NotNull(message = "Hotel ID is required")
    @Schema(description = "ID của khách sạn", example = "1")
    private Integer hotelId;

    @NotNull(message = "Room ID is required")
    @Schema(description = "ID của loại phòng muốn đặt", example = "5")
    private Integer roomId;

    @Min(value = 1, message = "Minimum number of rooms booked is 1")
    @Schema(description = "Số lượng phòng cần đặt", example = "1")
    private Integer roomQuantity = 1;

    @Schema(description = "Yêu cầu đặc biệt (tùy chọn)", example = "Cần phòng tầng cao, view đẹp")
    private String specialRequire;

    @Valid
    @Schema(description = "Danh sách khách lưu trú (tùy chọn)")
    private List<GuestDetailRequest> guestDetails;

}

