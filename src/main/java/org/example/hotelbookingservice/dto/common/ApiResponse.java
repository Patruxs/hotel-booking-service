package org.example.hotelbookingservice.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int status;
    @Builder.Default
    private boolean success = true;
    private String message;
    private T data;
    @Builder.Default
    private Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .status(201)
                .message("Created")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> of(HttpStatus status, String message, T data) {
        return ApiResponse.<T>builder()
                .status(status.value())
                .message(message)
                .data(data)
                .build();
    }
}
