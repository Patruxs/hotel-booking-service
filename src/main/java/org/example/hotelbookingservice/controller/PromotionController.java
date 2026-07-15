package org.example.hotelbookingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.api.PromotionApi;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.promotion.PromotionMutationRequest;
import org.example.hotelbookingservice.dto.response.common.ListResponse;
import org.example.hotelbookingservice.dto.response.promotion.PromotionResponse;
import org.example.hotelbookingservice.services.IPromotionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PromotionController implements PromotionApi {
    private final IPromotionService promotionService;

    @Override
    public ApiResponse<ListResponse<PromotionResponse>> promotions(@RequestParam(required = false) String search,
                                                                   @RequestParam(required = false) UUID hotelId,
                                                                   @RequestParam(required = false) Boolean isActive,
                                                                   @RequestParam(defaultValue = "1") int page,
                                                                   @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.<ListResponse<PromotionResponse>>builder()
                .status(200)
                .message("Success")
                .data(promotionService.listAdmin(search, hotelId, isActive, page, limit))
                .build();
    }

    @Override
    public ApiResponse<PromotionResponse> promotion(@PathVariable UUID id) {
        return ApiResponse.<PromotionResponse>builder()
                .status(200)
                .message("Success")
                .data(promotionService.detail(id))
                .build();
    }

    @Override
    public ApiResponse<PromotionResponse> createPromotion(@RequestBody @Valid PromotionMutationRequest request) {
        return ApiResponse.<PromotionResponse>builder()
                .status(201)
                .message("Promotion created")
                .data(promotionService.create(request))
                .build();
    }

    @Override
    public ApiResponse<PromotionResponse> updatePromotion(@PathVariable UUID id, @RequestBody @Valid PromotionMutationRequest request) {
        return ApiResponse.<PromotionResponse>builder()
                .status(200)
                .message("Promotion updated")
                .data(promotionService.update(id, request))
                .build();
    }

    @Override
    public void deletePromotion(@PathVariable UUID id) {
        promotionService.delete(id);
    }

    @Override
    public ApiResponse<List<PromotionResponse>> publicPromotions(@RequestParam(required = false) String search,
                                                                 @RequestParam(required = false) UUID hotelId,
                                                                 @RequestParam(required = false) BigDecimal subtotal,
                                                                 @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.<List<PromotionResponse>>builder()
                .status(200)
                .message("Success")
                .data(promotionService.searchPublic(search, hotelId, subtotal, limit))
                .build();
    }

    @Override
    public ApiResponse<PromotionResponse> publicPromotion(@PathVariable String code,
                                                         @RequestParam(required = false) UUID hotelId,
                                                         @RequestParam(required = false) BigDecimal subtotal) {
        return ApiResponse.<PromotionResponse>builder()
                .status(200)
                .message("Success")
                .data(promotionService.lookupPublic(code, hotelId, subtotal))
                .build();
    }
}
