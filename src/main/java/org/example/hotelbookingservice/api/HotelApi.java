package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.hotel.HotelCreateRequest;
import org.example.hotelbookingservice.dto.request.hotel.HotelUpdateRequest;
import org.example.hotelbookingservice.dto.response.HotelResponse;
import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/v1/hotels")
@Tag(name = "Hotel Management", description = "Hotel management information")
public interface HotelApi {

    @Operation(summary = "Add new hotel", description = "For ADMIN. Image upload required.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Hotel created successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Hotel already exists or invalid data", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Admin permission required", content = @Content) })
    @PostMapping(value = {"", "/add"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<HotelResponse> addHotel(@RequestParam(value = "image", required = false) List<MultipartFile> image, @ParameterObject @ModelAttribute @Valid HotelCreateRequest hotelCreateRequest);

    @Operation(summary = "Update hotel", description = "Update hotel information.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Update successful"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No ownership permission for this hotel", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hotel not found", content = @Content) })
    @PutMapping(value = {"/{hotelId:\\d+}", "/update/{hotelId}"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<HotelResponse> updateHotel(@PathVariable Integer hotelId, @RequestParam(value = "image", required = false) List<MultipartFile> image, @ParameterObject @ModelAttribute @Valid HotelUpdateRequest hotelUpdateRequest);

    @Operation(summary = "Delete hotel", description = "Delete hotel from the system.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deletion successful"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No delete permission (Not owner/Admin)", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hotel not found", content = @Content) })
    @DeleteMapping({"/{hotelId:\\d+}", "/delete/{hotelId}"})
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> deleteHotel(@PathVariable Integer hotelId);

    @Operation(summary = "Get hotel details", description = "Public API.")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hotel not found", content = @Content) })
    @GetMapping("/{hotelId:\\d+}")
    ApiResponse<HotelResponse> getHotelById(@PathVariable Integer hotelId);

    @Operation(summary = "Get my hotels", description = "Get list of hotels managed by the currently logged-in user.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my-hotels")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CUSTOMER')")
    ApiResponse<List<HotelResponse>> getMyHotels();

    @Operation(summary = "Get all hotels", description = "Public API.")
    @GetMapping("/all")
    ApiResponse<List<HotelResponse>> getAllHotels();

    @Operation(summary = "Search hotels", description = "Search by location, check-in/out date, and capacity.")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search successful"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid check-in/out date", content = @Content) })
    @GetMapping("/search")
    ApiResponse<List<HotelResponse>> searchHotels(@RequestParam String location, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate, @RequestParam(required = false) Integer capacity, @RequestParam(required = false, defaultValue = "1") Integer roomQuantity);

    @Operation(summary = "Get rooms by Hotel ID", description = "Get all rooms belonging to a specific hotel.")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hotel not found", content = @Content) })
    @GetMapping("/{hotelId:\\d+}/rooms")
    ApiResponse<List<RoomResponse>> getRoomsByHotelId(@PathVariable Integer hotelId);
}
