package org.example.hotelbookingservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.dto.request.promotion.PromotionMutationRequest;
import org.example.hotelbookingservice.dto.response.common.ListResponse;
import org.example.hotelbookingservice.dto.response.common.PageMeta;
import org.example.hotelbookingservice.dto.response.promotion.PromotionResponse;
import org.example.hotelbookingservice.entity.Hotel;
import org.example.hotelbookingservice.entity.Promotion;
import org.example.hotelbookingservice.exception.NotFoundException;
import org.example.hotelbookingservice.repository.HotelRepository;
import org.example.hotelbookingservice.repository.PromotionRepository;
import org.example.hotelbookingservice.services.IPromotionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements IPromotionService {
    private static final int MAX_LIMIT = 100;

    private final PromotionRepository promotionRepository;
    private final HotelRepository hotelRepository;

    @Override
    @Transactional(readOnly = true)
    public ListResponse<PromotionResponse> listAdmin(String search, UUID hotelId, Boolean isActive, int page, int limit) {
        int safePage = Math.max(1, page);
        int safeLimit = safeLimit(limit);
        var pageable = PageRequest.of(safePage - 1, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = promotionRepository.searchAdmin(normalizeSearch(search), hotelId, isActive, pageable);
        List<PromotionResponse> data = result.getContent().stream().map(PromotionResponse::from).toList();

        return new ListResponse<>(
                data,
                new PageMeta(safeLimit, (safePage - 1) * safeLimit, result.getTotalElements()),
                safePage,
                safeLimit,
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionResponse detail(UUID id) {
        return PromotionResponse.from(findPromotion(id));
    }

    @Override
    @Transactional
    public PromotionResponse create(PromotionMutationRequest request) {
        Promotion promotion = new Promotion();
        apply(request, promotion, true);
        return PromotionResponse.from(promotionRepository.saveAndFlush(promotion));
    }

    @Override
    @Transactional
    public PromotionResponse update(UUID id, PromotionMutationRequest request) {
        Promotion promotion = findPromotion(id);
        apply(request, promotion, false);
        return PromotionResponse.from(promotionRepository.saveAndFlush(promotion));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Promotion promotion = findPromotion(id);
        promotionRepository.delete(promotion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> searchPublic(String search, UUID hotelId, BigDecimal subtotal, int limit) {
        return promotionRepository.searchPublic(
                        normalizeSearch(search),
                        hotelId,
                        subtotal,
                        Instant.now(),
                        PageRequest.of(0, safeLimit(limit), Sort.by(Sort.Direction.ASC, "code"))
                )
                .stream()
                .map(PromotionResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionResponse lookupPublic(String code, UUID hotelId, BigDecimal subtotal) {
        String normalizedCode = normalizeCode(code);
        return promotionRepository.findPublicEligible(normalizedCode, hotelId, subtotal, Instant.now())
                .map(PromotionResponse::from)
                .orElseThrow(() -> new NotFoundException("Promotion not found or not eligible"));
    }

    private void apply(PromotionMutationRequest request, Promotion promotion, boolean create) {
        validate(request);
        Instant now = Instant.now();
        promotion.setHotel(resolveHotel(request.hotelId()));
        promotion.setCode(normalizeCode(request.code()));
        promotion.setName(request.name().trim());
        promotion.setDiscountType(request.discountType().trim().toUpperCase(Locale.ROOT));
        promotion.setDiscountValue(request.discountValue());
        promotion.setMaxDiscount(request.maxDiscountAmount());
        promotion.setMinBookingAmount(request.minBookingAmount());
        promotion.setTotalUsageLimit(request.totalUsageLimit());
        promotion.setPerUserUsageLimit(request.perUserLimit());
        promotion.setStartsAt(request.startAt());
        promotion.setEndsAt(request.endAt());
        promotion.setActive(request.isActive() == null || request.isActive());
        if (create) {
            promotion.setUsedCount(0);
            promotion.setCreatedAt(now);
        }
        promotion.setUpdatedAt(now);
    }

    private void validate(PromotionMutationRequest request) {
        String discountType = request.discountType() == null ? "" : request.discountType().trim().toUpperCase(Locale.ROOT);
        if (!"PERCENT".equals(discountType) && !"FIXED".equals(discountType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "discountType must be PERCENT or FIXED");
        }
        if ("PERCENT".equals(discountType) && request.discountValue() != null && request.discountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Percent discount cannot exceed 100");
        }
        if (request.totalUsageLimit() != null && request.totalUsageLimit() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "totalUsageLimit must be zero or greater");
        }
        if (request.perUserLimit() != null && request.perUserLimit() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "perUserLimit must be zero or greater");
        }
        if (request.startAt() != null && request.endAt() != null && !request.endAt().isAfter(request.startAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endAt must be after startAt");
        }
    }

    private Promotion findPromotion(UUID id) {
        return promotionRepository.findById(id).orElseThrow(() -> new NotFoundException("Promotion not found"));
    }

    private Hotel resolveHotel(UUID hotelId) {
        if (hotelId == null) {
            return null;
        }
        return hotelRepository.findById(hotelId).orElseThrow(() -> new NotFoundException("Hotel not found"));
    }

    private int safeLimit(int limit) {
        if (limit <= 0) {
            return 20;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String normalizeSearch(String search) {
        return search == null || search.isBlank() ? "" : search.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Promotion code is required");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
