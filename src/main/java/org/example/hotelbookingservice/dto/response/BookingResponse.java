package org.example.hotelbookingservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.hotelbookingservice.enums.BookingStatus;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingResponse {
    @Schema(description = "ID của booking")
    private Integer id;

    @Schema(description = "Ngày nhận phòng")
    private LocalDate checkinDate;

    @Schema(description = "Ngày trả phòng")
    private LocalDate checkoutDate;

    private Integer adultAmount;
    private Integer childrenAmount;

    @Schema(description = "Tổng giá tiền (đã tính theo số đêm)", example = "1500000.0")
    private Float totalPrice;

    private Float refund;

    @Schema(description = "Mã tham chiếu đặt phòng (Dùng để tra cứu)", example = "X82L9A")
    private String bookingReference;

    private String roomNumber;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String cancelReason;

    @Schema(description = "Trạng thái hiện tại")
    private BookingStatus status;
    private String specialRequire;
    private LocalDate createAt;

    @Schema(description = "Thông tin người đặt")
    private UserResponse user;

    @Schema(description = "Thông tin khách sạn")
    private HotelResponse hotel;

    @Schema(description = "Danh sách các phòng đã đặt trong booking này")
    private List<RoomResponse> rooms;


}

