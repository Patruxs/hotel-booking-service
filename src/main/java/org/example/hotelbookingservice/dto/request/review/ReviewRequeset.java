package org.example.hotelbookingservice.dto.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequeset {
    @NotNull(message = "Booking ID is required")
    private Integer bookingId;

    @NotNull(message = "Hotel ID is required")
    private Integer hotelId;

    @NotNull(message = "Rating is required")
    @Min(1) @Max(5)
    private Integer point;

    @NotBlank(message = "Review content cannot be blank")
    private String description;
}
