package org.example.hotelbookingservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final URI VALIDATION_TYPE = URI.create("https://hotel-booking-service/errors/validation");
    private static final URI APP_TYPE = URI.create("https://hotel-booking-service/errors/application");
    private static final URI NOT_FOUND_TYPE = URI.create("https://hotel-booking-service/errors/not-found");
    private static final URI ACCESS_DENIED_TYPE = URI.create("https://hotel-booking-service/errors/access-denied");
    private static final URI BAD_REQUEST_TYPE = URI.create("https://hotel-booking-service/errors/bad-request");
    private static final URI PAYLOAD_TOO_LARGE_TYPE = URI.create("https://hotel-booking-service/errors/payload-too-large");
    private static final URI INTERNAL_TYPE = URI.create("https://hotel-booking-service/errors/internal");

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation Failed", "Request validation failed", VALIDATION_TYPE, request);
        problem.setProperty("errorCode", "VALIDATION_FAILED");
        problem.setProperty("violations", ex.getBindingResult().getFieldErrors().stream()
                .map(this::violation)
                .toList());
        return ResponseEntity.badRequest().body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Malformed Request", "Invalid JSON request format", BAD_REQUEST_TYPE, request);
        problem.setProperty("errorCode", "JSON_PARSE_ERROR");
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(AppException.class)
    public ProblemDetail handleAppException(AppException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        ProblemDetail problem = problem(errorCode.getHttpStatusCode(), "Application Error", ex.getMessage(), APP_TYPE, request);
        problem.setProperty("errorCode", errorCode.name());
        problem.setProperty("numericCode", errorCode.getCode());
        return problem;
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
        ProblemDetail problem = problem(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), NOT_FOUND_TYPE, request);
        problem.setProperty("errorCode", "NOT_FOUND");
        return problem;
    }

    @ExceptionHandler(InvalidBookingStateAndDateException.class)
    public ProblemDetail handleInvalidBookingState(InvalidBookingStateAndDateException ex, HttpServletRequest request) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Invalid Booking State", ex.getMessage(), BAD_REQUEST_TYPE, request);
        problem.setProperty("errorCode", "INVALID_BOOKING_STATE_AND_DATE");
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        ProblemDetail problem = problem(HttpStatus.FORBIDDEN, "Forbidden", ErrorCode.UNAUTHORIZED.getMessage(), ACCESS_DENIED_TYPE, request);
        problem.setProperty("errorCode", "FORBIDDEN");
        return problem;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        String detail = ex.getReason() == null ? "Request failed" : ex.getReason();
        ProblemDetail problem = problem(ex.getStatusCode(), "Request Failed", detail, BAD_REQUEST_TYPE, request);
        problem.setProperty("errorCode", "REQUEST_FAILED");
        return problem;
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problem = problem(HttpStatus.PAYLOAD_TOO_LARGE, "Payload Too Large", "File is too large. Maximum upload size exceeded.", PAYLOAD_TOO_LARGE_TYPE, request);
        problem.setProperty("errorCode", "FILE_TOO_LARGE");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnwantedException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}", request.getRequestURI(), ex);
        ProblemDetail problem = problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Internal Server Error", INTERNAL_TYPE, request);
        problem.setProperty("errorCode", "INTERNAL_ERROR");
        return problem;
    }

    private Map<String, String> violation(FieldError error) {
        return Map.of(
                "field", error.getField(),
                "message", error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage()
        );
    }

    private ProblemDetail problem(HttpStatusCode status, String title, String detail, URI type, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(type);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    private ProblemDetail problem(HttpStatusCode status, String title, String detail, URI type, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(type);
        if (request instanceof ServletWebRequest servletWebRequest) {
            problem.setInstance(URI.create(servletWebRequest.getRequest().getRequestURI()));
        }
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
