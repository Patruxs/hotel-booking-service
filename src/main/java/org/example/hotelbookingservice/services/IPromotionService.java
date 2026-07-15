package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.dto.request.promotion.PromotionMutationRequest;
import org.example.hotelbookingservice.dto.response.common.ListResponse;
import org.example.hotelbookingservice.dto.response.promotion.PromotionResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IPromotionService {
    ListResponse<PromotionResponse> listAdmin(String search, UUID hotelId, Boolean isActive, int page, int limit);

    PromotionResponse detail(UUID id);

    PromotionResponse create(PromotionMutationRequest request);

    PromotionResponse update(UUID id, PromotionMutationRequest request);

    void delete(UUID id);

    List<PromotionResponse> searchPublic(String search, UUID hotelId, BigDecimal subtotal, int limit);

    PromotionResponse lookupPublic(String code, UUID hotelId, BigDecimal subtotal);
}
