package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.amenity.AmenityRequest;
import org.example.hotelbookingservice.dto.request.amenity.HotelAmenityRemoveRequest;
import org.example.hotelbookingservice.dto.request.amenity.RoomAmenityRemoveRequest;
import org.example.hotelbookingservice.dto.response.AmenityResponse;
import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/api/v1/amenities")
@Tag(name = "Amenity Management", description = "Manage amenity catalog (Wifi, Swimming Pool, Gym, Spa...)")
public interface AmenityApi {

    @Operation(summary = "Get list of all amenities in the system", description = "Used for Admin when they want to view the list to select amenities to add to rooms/hotels.")
    @GetMapping("/all")
    ApiResponse<List<AmenityResponse>> getAllAmenities();

    @Operation(summary = "Get hotel-level amenities by Hotel ID (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/hotel/{hotelId}/hotel-amenities")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<List<AmenityResponse>> getHotelAmenities(@PathVariable Integer hotelId);

    @Operation(summary = "Get list of amenities for all rooms belonging to Hotel A")
    @GetMapping("/hotel/{hotelId}/room-amenities")
    ApiResponse<List<RoomResponse>> getRoomAmenitiesByHotel(@PathVariable Integer hotelId);

    @Operation(summary = "View amenity details")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Amenity does not exist", content = @Content) })
    @GetMapping("/{id:\\d+}")
    ApiResponse<AmenityResponse> getAmenityById(@PathVariable Integer id);

    @Operation(summary = "Create a new amenity (ADMIN or OWNER)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Created successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Amenity name already exists", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No amenity management permission", content = @Content) })
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER')")
    ApiResponse<AmenityResponse> createAmenity(@RequestBody @Valid AmenityRequest request);

    @Operation(summary = "Update amenity (ADMIN or OWNER)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updated successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Duplicate name", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No permission", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Amenity not found", content = @Content) })
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER')")
    ApiResponse<AmenityResponse> updateAmenity(@PathVariable Integer id, @RequestBody @Valid AmenityRequest request);

    @Operation(summary = "Delete amenity (ADMIN or OWNER)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deleted successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No permission", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Amenity not found", content = @Content) })
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER')")
    ApiResponse<Void> deleteAmenity(@PathVariable Integer id);

    @Operation(summary = "Remove list of amenities from Hotel (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/hotel/{hotelId}/remove")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> removeHotelAmenities(@PathVariable Integer hotelId, @RequestBody @Valid HotelAmenityRemoveRequest request);

    @Operation(summary = "Remove list of amenities from Room (ADMIN)", description = "Requires providing exact Hotel ID and Room ID to ensure data integrity.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/hotel/{hotelId}/room/{roomId}/remove")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> removeRoomAmenities(@PathVariable Integer hotelId, @PathVariable Integer roomId, @RequestBody @Valid RoomAmenityRemoveRequest request);

    @Operation(summary = "Get list of amenities for a specific room belonging to a hotel")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room not found", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Room does not belong to this hotel", content = @Content) })
    @GetMapping("/hotel/{hotelId}/room/{roomId}")
    ApiResponse<List<AmenityResponse>> getAmenitiesByRoom(@PathVariable Integer hotelId, @PathVariable Integer roomId);
}
