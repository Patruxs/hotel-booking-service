package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.physicalroom.PhysicalRoomCreateRequest;
import org.example.hotelbookingservice.dto.request.physicalroom.PhysicalRoomUpdateStatusRequest;
import org.example.hotelbookingservice.dto.response.PhysicalRoomResponse;
import org.example.hotelbookingservice.enums.RoomCondition;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/physical-rooms")
@Tag(name = "Physical Room Management", description = "Manage physical rooms (Room condition: CLEAN, DIRTY, MAINTENANCE)")
public interface PhysicalRoomApi {

    @Operation(summary = "Create new physical room (ADMIN)", description = "Add a new physical room and assign it to a room type (Room).")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Room number already exists or invalid data", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room type (Room) not found", content = @Content)
    })
    @PostMapping({"", "/create"})
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<PhysicalRoomResponse> createPhysicalRoom(@RequestBody @Valid PhysicalRoomCreateRequest request);

    @Operation(summary = "Update physical room (ADMIN)", description = "Edit physical room details (room number, room type, status/condition).")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Physical room not found", content = @Content)
    })
    @PutMapping({"/{id:\\d+}", "/update/{id}"})
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<PhysicalRoomResponse> updatePhysicalRoom(@PathVariable Integer id, @RequestBody @Valid PhysicalRoomCreateRequest request);

    @Operation(summary = "Update room condition (ADMIN, RECEPTIONIST)", description = "Change room condition (e.g., DIRTY -> CLEAN after housekeeping).")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Physical room not found", content = @Content)
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('RECEPTIONIST')")
    ApiResponse<PhysicalRoomResponse> updateStatus(@PathVariable Integer id, @RequestBody @Valid PhysicalRoomUpdateStatusRequest request);

    @Operation(summary = "Delete physical room (ADMIN)", description = "Delete physical room from the system.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Physical room not found", content = @Content)
    })
    @DeleteMapping({"/{id:\\d+}", "/delete/{id}"})
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> deletePhysicalRoom(@PathVariable Integer id);

    @Operation(summary = "Get physical room details", description = "Retrieve physical room details by ID.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('RECEPTIONIST')")
    ApiResponse<PhysicalRoomResponse> getById(@PathVariable Integer id);

    @Operation(summary = "Get physical rooms by room type (Room)", description = "View all physical rooms belonging to a specific room type.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping({"/rooms/{roomId}", "/by-room/{roomId}"})
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('RECEPTIONIST')")
    ApiResponse<List<PhysicalRoomResponse>> getByRoomId(@PathVariable Integer roomId);

    @Operation(summary = "Filter rooms by condition (CLEAN/DIRTY/MAINTENANCE)", description = "View physical rooms list filtered by condition.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping({"/conditions", "/by-condition"})
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('RECEPTIONIST')")
    ApiResponse<List<PhysicalRoomResponse>> getByCondition(@RequestParam RoomCondition condition);

    @Operation(summary = "Get all physical rooms (ADMIN, RECEPTIONIST)", description = "View the list of all physical rooms.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping({"", "/all"})
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('RECEPTIONIST')")
    ApiResponse<List<PhysicalRoomResponse>> getAll();
}
