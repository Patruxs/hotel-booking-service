package org.example.hotelbookingservice.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import org.springframework.security.access.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Environment environment;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.<Map<String, String>>builder()
                        .status(400)
                        .message("Validation Error")
                        .data(errors)
                        .build()
        );
    }


    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.<Void>builder()
                        .status(errorCode.getCode())
                        .message(ex.getMessage())
                        .build()
        );
    }


    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNotFoundException(NotFoundException ex) {
        ErrorCode errorCode = ErrorCode.NOT_FOUND_EXCEPTION;
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.<String>builder()
                        .status(errorCode.getCode())
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(InvalidBookingStateAndDateException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidBookingState(InvalidBookingStateAndDateException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_BOOKING_STATE_AND_DATE_EXCEPTION;
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.<String>builder()
                        .status(errorCode.getCode())
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED; // Forbidden (403)

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.<Void>builder()
                        .status(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        ErrorCode errorCode = ErrorCode.FILE_TOO_LARGE;

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.<Void>builder()
                        .status(errorCode.getCode())
                        .message("File is too large! Maximum upload size exceeded.")
                        .build()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleJsonParseException(HttpMessageNotReadableException ex) {
        boolean isProd = environment.acceptsProfiles(Profiles.of("prod"));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.<String>builder()
                        .status(ErrorCode.JSON_PARSE_ERROR.getCode())
                        .message("Invalid JSON request format")
                        .data(isProd ? null : ex.getMessage()) // Debug info (nên ẩn khi production)
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleUnwantedException(Exception ex) {
        log.error("Unknown error occurred: ", ex);

        boolean isProd = environment.acceptsProfiles(Profiles.of("prod"));
        String errorMessage = isProd ? "Internal Server Error" : "Internal Server Error: " + ex.getMessage();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.<String>builder()
                        .status(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                        .message(errorMessage)
                        .build()
        );
    }


}
