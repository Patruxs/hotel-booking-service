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
    @Schema(description = "Booking ID")
    private Integer id;

    @Schema(description = "Check-in date")
    private LocalDate checkinDate;

    @Schema(description = "Check-out date")
    private LocalDate checkoutDate;

    private Integer adultAmount;
    private Integer childrenAmount;

    @Schema(description = "Total price (calculated based on number of nights)", example = "1500000.0")
    private Float totalPrice;

    private Float refund;

    @Schema(description = "Booking reference code (Used for lookup)", example = "X82L9A")
    private String bookingReference;

    private String roomNumber;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String cancelReason;

    @Schema(description = "Current status")
    private BookingStatus status;
    private String specialRequire;
    private LocalDate createAt;

    @Schema(description = "Booker information")
    private UserResponse user;

    @Schema(description = "Hotel information")
    private HotelResponse hotel;

    @Schema(description = "List of rooms booked in this booking")
    private List<RoomResponse> rooms;

    @Schema(description = "Damage fee (if any)")
    private Float damageFee;

    @Schema(description = "Damage description")
    private String damageDescription;

    @Schema(description = "List of guest details")
    private List<GuestDetailResponse> guestDetails;

}

