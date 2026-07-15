package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.dto.request.promotion.PromotionMutationRequest;
import org.example.hotelbookingservice.entity.Hotel;
import org.example.hotelbookingservice.entity.Promotion;
import org.example.hotelbookingservice.repository.HotelRepository;
import org.example.hotelbookingservice.repository.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceImplTest {
    private static final UUID HOTEL_ID = UUID.fromString("72000000-0000-4000-8000-000000000010");
    private static final UUID PROMOTION_ID = UUID.fromString("72000000-0000-4000-8000-000000000011");

    @Mock PromotionRepository promotionRepository;
    @Mock HotelRepository hotelRepository;

    @Test
    void listAdmin_mapsGlobalAndHotelScopedPromotions() {
        PromotionServiceImpl service = service();
        when(promotionRepository.searchAdmin(eq("SAVE"), eq(HOTEL_ID), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(promotion(hotel()))));

        var response = service.listAdmin("save", HOTEL_ID, true, 1, 20);

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().code()).isEqualTo("SAVE10");
        assertThat(response.data().getFirst().hotelId()).isEqualTo(HOTEL_ID);
        assertThat(response.meta().total()).isEqualTo(1);
    }

    @Test
    void listAdmin_usesEmptyTextSearchForBlankInput() {
        PromotionServiceImpl service = service();
        when(promotionRepository.searchAdmin(eq(""), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(promotion(null))));

        var response = service.listAdmin(null, null, null, 1, 20);

        assertThat(response.data()).hasSize(1);
        verify(promotionRepository).searchAdmin(eq(""), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void create_savesHotelScopedPromotionWithNormalizedCode() {
        PromotionServiceImpl service = service();
        Hotel hotel = hotel();
        when(hotelRepository.findById(HOTEL_ID)).thenReturn(Optional.of(hotel));
        when(promotionRepository.saveAndFlush(any(Promotion.class))).thenAnswer(invocation -> {
            Promotion promotion = invocation.getArgument(0);
            promotion.setId(PROMOTION_ID);
            return promotion;
        });
        ArgumentCaptor<Promotion> saved = ArgumentCaptor.forClass(Promotion.class);

        var response = service.create(request(" save10 ", HOTEL_ID));

        verify(promotionRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getCode()).isEqualTo("SAVE10");
        assertThat(saved.getValue().getHotel()).isSameAs(hotel);
        assertThat(saved.getValue().getUsedCount()).isZero();
        assertThat(response.code()).isEqualTo("SAVE10");
        assertThat(response.hotelId()).isEqualTo(HOTEL_ID);
    }

    @Test
    void searchPublic_delegatesGlobalAndHotelEligibilityToRepository() {
        PromotionServiceImpl service = service();
        when(promotionRepository.searchPublic(eq("SAVE"), eq(HOTEL_ID), eq(new BigDecimal("1000.00")), any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of(promotion(null), promotion(hotel())));

        var response = service.searchPublic("save", HOTEL_ID, new BigDecimal("1000.00"), 10);

        assertThat(response).extracting("code").containsExactly("SAVE10", "SAVE10");
        verify(promotionRepository).searchPublic(eq("SAVE"), eq(HOTEL_ID), eq(new BigDecimal("1000.00")), any(Instant.class), any(Pageable.class));
    }

    @Test
    void lookupPublic_usesCodeHotelAndSubtotalAsPreviewFilters() {
        PromotionServiceImpl service = service();
        when(promotionRepository.findPublicEligible(eq("SAVE10"), eq(HOTEL_ID), eq(new BigDecimal("1000.00")), any(Instant.class)))
                .thenReturn(Optional.of(promotion(hotel())));

        var response = service.lookupPublic("save10", HOTEL_ID, new BigDecimal("1000.00"));

        assertThat(response.code()).isEqualTo("SAVE10");
        assertThat(response.hotelId()).isEqualTo(HOTEL_ID);
    }

    private PromotionServiceImpl service() {
        return new PromotionServiceImpl(promotionRepository, hotelRepository);
    }

    private PromotionMutationRequest request(String code, UUID hotelId) {
        return new PromotionMutationRequest(
                hotelId,
                code,
                "Save Ten",
                "Optional UI text",
                "PERCENT",
                new BigDecimal("10.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"),
                100,
                1,
                Instant.parse("2026-07-01T00:00:00Z"),
                Instant.parse("2026-08-01T00:00:00Z"),
                true
        );
    }

    private Promotion promotion(Hotel hotel) {
        Promotion promotion = new Promotion();
        promotion.setId(PROMOTION_ID);
        promotion.setHotel(hotel);
        promotion.setCode("SAVE10");
        promotion.setName("Save Ten");
        promotion.setDiscountType("PERCENT");
        promotion.setDiscountValue(new BigDecimal("10.00"));
        promotion.setMaxDiscount(new BigDecimal("500.00"));
        promotion.setMinBookingAmount(new BigDecimal("1000.00"));
        promotion.setTotalUsageLimit(100);
        promotion.setPerUserUsageLimit(1);
        promotion.setUsedCount(0);
        promotion.setStartsAt(Instant.parse("2026-07-01T00:00:00Z"));
        promotion.setEndsAt(Instant.parse("2026-08-01T00:00:00Z"));
        promotion.setActive(true);
        promotion.setCreatedAt(Instant.parse("2026-07-01T00:00:00Z"));
        promotion.setUpdatedAt(Instant.parse("2026-07-01T00:00:00Z"));
        return promotion;
    }

    private Hotel hotel() {
        Hotel hotel = new Hotel();
        hotel.setId(HOTEL_ID);
        hotel.setName("Live Hotel");
        return hotel;
    }
}
