package org.example.hotelbookingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.AmenityRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.AmenityResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.AvailabilityResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.HotelCreateRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.HotelMemberResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.HotelResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.HotelStatusRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.HotelUpdateRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.InventoryRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.InventoryResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.MemberMutationRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.PaginatedResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.RoomRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.RoomResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.RoomTypeRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.RoomTypeResponse;
import org.example.hotelbookingservice.services.HotelOperationsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class HotelOperationsController {
    private final HotelOperationsService hotelOperationsService;

    @PostMapping("/hotels")
    public ApiResponse<HotelResponse> createHotel(
            @RequestBody @Valid HotelCreateRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<HotelResponse>builder()
                .status(201)
                .message("Hotel created successfully")
                .data(hotelOperationsService.createHotel(request, authentication))
                .build();
    }

    @GetMapping("/hotels")
    public ApiResponse<PaginatedResponse<HotelResponse>> listPublicHotels(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ApiResponse.<PaginatedResponse<HotelResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.listPublicHotels(limit, offset))
                .build();
    }

    @GetMapping("/hotels/{hotelId}")
    public ApiResponse<HotelResponse> publicHotelDetail(@PathVariable UUID hotelId) {
        return ApiResponse.<HotelResponse>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.publicHotelDetail(hotelId))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/manage")
    public ApiResponse<HotelResponse> managementHotelDetail(
            @PathVariable UUID hotelId,
            Authentication authentication
    ) {
        return ApiResponse.<HotelResponse>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.managementHotelDetail(hotelId, authentication))
                .build();
    }

    @PatchMapping("/hotels/{hotelId}")
    public ApiResponse<HotelResponse> updateHotel(
            @PathVariable UUID hotelId,
            @RequestBody @Valid HotelUpdateRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<HotelResponse>builder()
                .status(200)
                .message("Hotel updated successfully")
                .data(hotelOperationsService.updateHotel(hotelId, request, authentication))
                .build();
    }

    @PatchMapping("/hotels/{hotelId}/status")
    public ApiResponse<HotelResponse> updateHotelStatus(
            @PathVariable UUID hotelId,
            @RequestBody @Valid HotelStatusRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<HotelResponse>builder()
                .status(200)
                .message("Hotel status updated successfully")
                .data(hotelOperationsService.changeHotelStatus(hotelId, request.status(), authentication))
                .build();
    }

    @DeleteMapping("/hotels/{hotelId}")
    public ApiResponse<HotelResponse> archiveHotel(
            @PathVariable UUID hotelId,
            Authentication authentication
    ) {
        return ApiResponse.<HotelResponse>builder()
                .status(200)
                .message("Hotel archived successfully")
                .data(hotelOperationsService.archiveHotel(hotelId, authentication))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/members")
    public ApiResponse<List<HotelMemberResponse>> listMembers(
            @PathVariable UUID hotelId,
            Authentication authentication
    ) {
        return ApiResponse.<List<HotelMemberResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.listMembers(hotelId, authentication))
                .build();
    }

    @PostMapping("/hotels/{hotelId}/members")
    public ApiResponse<List<HotelMemberResponse>> addMembers(
            @PathVariable UUID hotelId,
            @RequestBody @Valid MemberMutationRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<List<HotelMemberResponse>>builder()
                .status(200)
                .message("Hotel members updated successfully")
                .data(hotelOperationsService.addMembers(hotelId, request.accountIds(), authentication))
                .build();
    }

    @DeleteMapping("/hotels/{hotelId}/members/{accountId}")
    public ApiResponse<Void> removeMember(
            @PathVariable UUID hotelId,
            @PathVariable UUID accountId,
            Authentication authentication
    ) {
        hotelOperationsService.removeMember(hotelId, accountId, authentication);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Hotel member removed successfully")
                .build();
    }

    @GetMapping("/amenities")
    public ApiResponse<List<AmenityResponse>> listAmenities(@RequestParam(required = false) Boolean isActive) {
        return ApiResponse.<List<AmenityResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.listAmenities(isActive))
                .build();
    }

    @GetMapping("/amenities/{amenityId}")
    public ApiResponse<AmenityResponse> amenityDetail(@PathVariable UUID amenityId) {
        return ApiResponse.<AmenityResponse>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.amenityDetail(amenityId))
                .build();
    }

    @PostMapping("/amenities")
    public ApiResponse<AmenityResponse> createAmenity(
            @RequestBody @Valid AmenityRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<AmenityResponse>builder()
                .status(201)
                .message("Amenity created successfully")
                .data(hotelOperationsService.createAmenity(request, authentication))
                .build();
    }

    @PutMapping("/amenities/{amenityId}")
    public ApiResponse<AmenityResponse> updateAmenity(
            @PathVariable UUID amenityId,
            @RequestBody @Valid AmenityRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<AmenityResponse>builder()
                .status(200)
                .message("Amenity updated successfully")
                .data(hotelOperationsService.updateAmenity(amenityId, request, authentication))
                .build();
    }

    @DeleteMapping("/amenities/{amenityId}")
    public ApiResponse<AmenityResponse> disableAmenity(
            @PathVariable UUID amenityId,
            Authentication authentication
    ) {
        return ApiResponse.<AmenityResponse>builder()
                .status(200)
                .message("Amenity disabled successfully")
                .data(hotelOperationsService.disableAmenity(amenityId, authentication))
                .build();
    }

    @PostMapping("/hotels/{hotelId}/room-types")
    public ApiResponse<RoomTypeResponse> createRoomType(
            @PathVariable UUID hotelId,
            @RequestBody @Valid RoomTypeRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<RoomTypeResponse>builder()
                .status(201)
                .message("Room type created successfully")
                .data(hotelOperationsService.createRoomType(hotelId, request, authentication))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/room-types")
    public ApiResponse<List<RoomTypeResponse>> listRoomTypes(
            @PathVariable UUID hotelId,
            @RequestParam(defaultValue = "false") boolean manage,
            Authentication authentication
    ) {
        if (manage) {
            hotelOperationsService.managementHotelDetail(hotelId, authentication);
        }
        return ApiResponse.<List<RoomTypeResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.listRoomTypes(hotelId, manage))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/room-types/available")
    public ApiResponse<PaginatedResponse<AvailabilityResponse>> listAvailability(
            @PathVariable UUID hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ApiResponse.<PaginatedResponse<AvailabilityResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.publicAvailability(hotelId, from, to, limit, offset))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/room-types/{roomTypeId}")
    public ApiResponse<RoomTypeResponse> roomTypeDetail(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomTypeId,
            @RequestParam(defaultValue = "false") boolean manage,
            Authentication authentication
    ) {
        if (manage) {
            hotelOperationsService.managementHotelDetail(hotelId, authentication);
        }
        return ApiResponse.<RoomTypeResponse>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.roomTypeDetail(hotelId, roomTypeId, !manage))
                .build();
    }

    @PatchMapping("/hotels/{hotelId}/room-types/{roomTypeId}")
    public ApiResponse<RoomTypeResponse> updateRoomType(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomTypeId,
            @RequestBody @Valid RoomTypeRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<RoomTypeResponse>builder()
                .status(200)
                .message("Room type updated successfully")
                .data(hotelOperationsService.updateRoomType(hotelId, roomTypeId, request, authentication))
                .build();
    }

    @DeleteMapping("/hotels/{hotelId}/room-types/{roomTypeId}")
    public ApiResponse<Void> deleteRoomType(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomTypeId,
            Authentication authentication
    ) {
        hotelOperationsService.deleteRoomType(hotelId, roomTypeId, authentication);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Room type deleted successfully")
                .build();
    }

    @PostMapping("/hotels/{hotelId}/rooms")
    public ApiResponse<RoomResponse> createRoom(
            @PathVariable UUID hotelId,
            @RequestBody @Valid RoomRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<RoomResponse>builder()
                .status(201)
                .message("Room created successfully")
                .data(hotelOperationsService.createRoom(hotelId, request, authentication))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/rooms")
    public ApiResponse<List<RoomResponse>> listRooms(
            @PathVariable UUID hotelId,
            Authentication authentication
    ) {
        return ApiResponse.<List<RoomResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.listRooms(hotelId, authentication))
                .build();
    }

    @PatchMapping("/hotels/{hotelId}/rooms/{roomId}")
    public ApiResponse<RoomResponse> updateRoom(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @RequestBody @Valid RoomRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<RoomResponse>builder()
                .status(200)
                .message("Room updated successfully")
                .data(hotelOperationsService.updateRoom(hotelId, roomId, request, authentication))
                .build();
    }

    @DeleteMapping("/hotels/{hotelId}/rooms/{roomId}")
    public ApiResponse<Void> deleteRoom(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            Authentication authentication
    ) {
        hotelOperationsService.deleteRoom(hotelId, roomId, authentication);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Room deleted successfully")
                .build();
    }

    @PutMapping("/hotels/{hotelId}/room-types/{roomTypeId}/inventory")
    public ApiResponse<InventoryResponse> upsertInventory(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomTypeId,
            @RequestBody @Valid InventoryRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<InventoryResponse>builder()
                .status(200)
                .message("Inventory saved successfully")
                .data(hotelOperationsService.upsertInventory(hotelId, roomTypeId, request, authentication))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/room-types/{roomTypeId}/inventory")
    public ApiResponse<List<InventoryResponse>> listInventory(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication
    ) {
        return ApiResponse.<List<InventoryResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.listInventory(hotelId, roomTypeId, from, to, authentication))
                .build();
    }
}
