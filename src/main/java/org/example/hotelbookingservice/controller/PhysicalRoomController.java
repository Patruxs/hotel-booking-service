package org.example.hotelbookingservice.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.hotelbookingservice.api.PhysicalRoomApi;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.physicalroom.PhysicalRoomCreateRequest;
import org.example.hotelbookingservice.dto.request.physicalroom.PhysicalRoomUpdateStatusRequest;
import org.example.hotelbookingservice.dto.response.PhysicalRoomResponse;
import org.example.hotelbookingservice.enums.RoomCondition;
import org.example.hotelbookingservice.services.IPhysicalRoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/physical-rooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PhysicalRoomController implements PhysicalRoomApi {

    IPhysicalRoomService physicalRoomService;

    @Override
    public ApiResponse<PhysicalRoomResponse> createPhysicalRoom(@RequestBody PhysicalRoomCreateRequest request) {
        return ApiResponse.<PhysicalRoomResponse>builder()
                .status(201)
                .message("Physical room created successfully")
                .data(physicalRoomService.createPhysicalRoom(request))
                .build();
    }

    @Override
    public ApiResponse<PhysicalRoomResponse> updatePhysicalRoom(@PathVariable Integer id, @RequestBody PhysicalRoomCreateRequest request) {
        return ApiResponse.<PhysicalRoomResponse>builder()
                .status(200)
                .message("Physical room updated successfully")
                .data(physicalRoomService.updatePhysicalRoom(id, request))
                .build();
    }

    @Override
    public ApiResponse<PhysicalRoomResponse> updateStatus(@PathVariable Integer id, @RequestBody PhysicalRoomUpdateStatusRequest request) {
        return ApiResponse.<PhysicalRoomResponse>builder()
                .status(200)
                .message("Room status updated successfully")
                .data(physicalRoomService.updateStatus(id, request))
                .build();
    }

    @Override
    public ApiResponse<Void> deletePhysicalRoom(@PathVariable Integer id) {
        physicalRoomService.deletePhysicalRoom(id);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Physical room deleted successfully")
                .build();
    }

    @Override
    public ApiResponse<PhysicalRoomResponse> getById(@PathVariable Integer id) {
        return ApiResponse.<PhysicalRoomResponse>builder()
                .status(200)
                .message("Success")
                .data(physicalRoomService.getById(id))
                .build();
    }

    @Override
    public ApiResponse<List<PhysicalRoomResponse>> getByRoomId(@PathVariable Integer roomId) {
        return ApiResponse.<List<PhysicalRoomResponse>>builder()
                .status(200)
                .message("Success")
                .data(physicalRoomService.getByRoomId(roomId))
                .build();
    }

    @Override
    public ApiResponse<List<PhysicalRoomResponse>> getByCondition(@RequestParam RoomCondition condition) {
        return ApiResponse.<List<PhysicalRoomResponse>>builder()
                .status(200)
                .message("Success")
                .data(physicalRoomService.getByCondition(condition))
                .build();
    }

    @Override
    public ApiResponse<List<PhysicalRoomResponse>> getAll() {
        return ApiResponse.<List<PhysicalRoomResponse>>builder()
                .status(200)
                .message("Success")
                .data(physicalRoomService.getAll())
                .build();
    }
}
