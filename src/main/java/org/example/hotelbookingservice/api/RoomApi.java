package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.room.RoomCreateRequest;
import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.example.hotelbookingservice.enums.RoomType;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/v1/rooms")
@Tag(name = "Room Management", description = "Hotel room management (Add, edit, delete, search available rooms)")
public interface RoomApi {

    @Operation(summary = "Add new room (ADMIN)", description = "Add a room to the hotel.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Room created successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Room already exists or invalid data", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No Admin privilege or do not own the hotel", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hotel does not exist", content = @Content) })
    @PostMapping(value = {"", "/add"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<RoomResponse> addRoom(@RequestParam(value = "image", required = false) MultipartFile image, @ParameterObject @ModelAttribute @Valid RoomCreateRequest roomCreateRequest);

    @Operation(summary = "Update room details (ADMIN)", description = "Edit room details.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updated successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room does not exist", content = @Content) })
    @PutMapping(value = {"/{roomId:\\d+}", "/update/{roomId}"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<RoomResponse> updateRoom(@PathVariable Integer roomId, @RequestParam(value = "photo", required = false) MultipartFile photo, @ParameterObject @ModelAttribute RoomCreateRequest roomCreateRequest);

    @Operation(summary = "Get list of all rooms", description = "Public API.")
    @GetMapping({"", "/all"})
    ApiResponse<List<RoomResponse>> getAllRooms();

    @Operation(summary = "View room details", description = "Get room details by ID.")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room not found", content = @Content) })
    @GetMapping("/{roomId:\\d+}")
    ApiResponse<RoomResponse> getRoomById(@PathVariable Integer roomId);

    @Operation(summary = "Delete room (ADMIN)", description = "Delete room from the system.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deleted successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room not found", content = @Content) })
    @DeleteMapping({"/{roomId:\\d+}", "/delete/{roomId}"})
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> deleteRoom(@PathVariable Integer roomId);

    @Operation(summary = "Find available rooms by date", description = "Find rooms that can be booked.")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Check-in/out date is invalid or missing", content = @Content) })
    @GetMapping({"/available", "/all-available-rooms"})
    ApiResponse<List<RoomResponse>> getAvailableRooms(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate, @RequestParam(required = false) String roomType);

    @Operation(summary = "Get room types list", description = "Returns room type enums (SINGLE, DOUBLE, SUIT, TRIPLE).")
    @GetMapping({"/types", "/legacy-types"})
    ApiResponse<List<RoomType>> getRoomTypes();

    @Operation(summary = "General room search", description = "Search for rooms by keyword (Name, description, price...).")
    @GetMapping("/search")
    ApiResponse<List<RoomResponse>> searchRoom(@RequestParam String input);

    @Operation(summary = "Find available rooms by hotel ID", description = "Get available rooms of a specific hotel within a Check-in/Check-out period.")
    @GetMapping({"/hotels/{hotelId}/available", "/hotel/{hotelId}/available"})
    ApiResponse<List<RoomResponse>> getAvailableRoomsByHotel(@PathVariable Integer hotelId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate);
}
