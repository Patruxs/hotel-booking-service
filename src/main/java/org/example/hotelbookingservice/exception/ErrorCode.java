package org.example.hotelbookingservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // --- Lỗi Server / Hệ thống ---
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1002, "Invalid message key", HttpStatus.BAD_REQUEST),
    JSON_PARSE_ERROR(10026, "Invalid JSON format or data type", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(10027, "File size exceeds the maximum limit", HttpStatus.PAYLOAD_TOO_LARGE),
    INVALID_FILE_FORMAT(10028, "Invalid file format. Only PNG and JPG are allowed", HttpStatus.BAD_REQUEST),

    // --- Lỗi Validation chung ---
    NAME_VALUE_REQUIRED_EXCEPTION(10011, "Name value required", HttpStatus.BAD_REQUEST),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),

    // --- Lỗi Authentication / Authorization ---
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIAL_EXCEPTION(10010, "Invalid credentials", HttpStatus.UNAUTHORIZED),
    INVALID_PASSWORD_EXCEPTION(10012, "Password doesn't match", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED(10019, "Account has been locked by Admin", HttpStatus.FORBIDDEN),
    OLD_PASSWORD_INCORRECT(10017, "Old password is not correct", HttpStatus.BAD_REQUEST),
    PASSWORD_CHANGE_INVALID(10018, "New password cannot be the same as old password", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),

    // --- Lỗi User ---
    USER_EXISTED(1000, "User already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1001, "User not exists", HttpStatus.NOT_FOUND),
    NOT_FOUND_EMAIL_EXCEPTION(10014, "Not found email", HttpStatus.NOT_FOUND),

    // --- Lỗi Chung (Not Found) ---
    NOT_FOUND_EXCEPTION(10013, "Not found", HttpStatus.NOT_FOUND),

    // --- Lỗi Room / Hotel ---
    NOT_FOUND_ROOM(10015, "Not found Room", HttpStatus.NOT_FOUND),
    ROOM_ALREADY_EXISTS(10016, "Room already exists", HttpStatus.BAD_REQUEST),
    ROOM_NOT_BELONG_TO_HOTEL(10023, "Room not belong to hotel", HttpStatus.BAD_REQUEST),
    HOTEL_ALREADY_EXISTS(10024, "Hotel already exists", HttpStatus.BAD_REQUEST),
    AMENITY_EXISTED(10020, "Amenity already exists", HttpStatus.BAD_REQUEST),
    AMENITY_IN_USE(10031, "Amenity is currently in use by a hotel or room", HttpStatus.BAD_REQUEST),
    NOT_FOUND_AMENITY(10025,"Amenity Id not exists", HttpStatus.BAD_REQUEST),
    ROOM_NUMBER_OCCUPIED(10030, "Room number is currently occupied by another guest", HttpStatus.CONFLICT),
    IMAGE_REQUIRED(10029, "At least one image is required", HttpStatus.BAD_REQUEST),

    // --- Lỗi Booking / Review ---
    INVALID_BOOKING_STATE_AND_DATE_EXCEPTION(1009, "Invalid booking dates or state", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_COMPLETED(10021, "Booking must be completed before reviewing", HttpStatus.BAD_REQUEST),
    REVIEW_NOT_ALLOWED(10022, "Review not allowed for this booking/hotel", HttpStatus.BAD_REQUEST),


    ;


    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;


}