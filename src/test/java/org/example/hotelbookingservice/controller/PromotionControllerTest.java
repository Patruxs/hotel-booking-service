package org.example.hotelbookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.hotelbookingservice.dto.request.promotion.PromotionMutationRequest;
import org.example.hotelbookingservice.dto.response.common.ListResponse;
import org.example.hotelbookingservice.dto.response.common.PageMeta;
import org.example.hotelbookingservice.dto.response.promotion.PromotionResponse;
import org.example.hotelbookingservice.services.IPromotionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromotionController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PromotionControllerTest {
    private static final UUID PROMOTION_ID = UUID.fromString("72000000-0000-4000-8000-000000000011");
    private static final UUID HOTEL_ID = UUID.fromString("72000000-0000-4000-8000-000000000010");

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean IPromotionService promotionService;
    @MockitoBean org.example.hotelbookingservice.security.JwtUtils jwtUtils;
    @MockitoBean org.example.hotelbookingservice.security.CustomUserDetailsService customUserDetailsService;
    @MockitoBean org.example.hotelbookingservice.exception.CustomAccessDenialHandler customAccessDenialHandler;
    @MockitoBean org.example.hotelbookingservice.exception.CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Test
    void promotions_whenAdminListRequested_shouldReturnListEnvelope() throws Exception {
        when(promotionService.listAdmin("save", HOTEL_ID, true, 1, 20)).thenReturn(
                new ListResponse<>(List.of(response()), new PageMeta(20, 0, 1), 1, 20, 1L, 1)
        );

        mockMvc.perform(get("/api/v1/admin/promotions")
                        .param("search", "save")
                        .param("hotelId", HOTEL_ID.toString())
                        .param("isActive", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.data[0].code").value("SAVE10"))
                .andExpect(jsonPath("$.data.data[0].hotelId").value(HOTEL_ID.toString()))
                .andExpect(jsonPath("$.data.meta.total").value(1));
    }

    @Test
    void createPromotion_whenValidRequest_shouldReturnCreatedPromotion() throws Exception {
        PromotionMutationRequest request = request();
        when(promotionService.create(any(PromotionMutationRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/v1/admin/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Promotion created"))
                .andExpect(jsonPath("$.data.code").value("SAVE10"));
    }

    @Test
    void updatePromotion_whenValidRequest_shouldReturnUpdatedPromotion() throws Exception {
        PromotionMutationRequest request = request();
        when(promotionService.update(eq(PROMOTION_ID), any(PromotionMutationRequest.class))).thenReturn(response());

        mockMvc.perform(patch("/api/v1/admin/promotions/{id}", PROMOTION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Promotion updated"))
                .andExpect(jsonPath("$.data.id").value(PROMOTION_ID.toString()));
    }

    @Test
    void deletePromotion_whenExisting_shouldReturnNoContent() throws Exception {
        doNothing().when(promotionService).delete(PROMOTION_ID);

        mockMvc.perform(delete("/api/v1/admin/promotions/{id}", PROMOTION_ID))
                .andExpect(status().isNoContent());

        verify(promotionService).delete(PROMOTION_ID);
    }

    @Test
    void publicPromotions_whenSearched_shouldReturnEligiblePromotions() throws Exception {
        when(promotionService.searchPublic("save", HOTEL_ID, new BigDecimal("1000.00"), 10)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/promotions/public")
                        .param("search", "save")
                        .param("hotelId", HOTEL_ID.toString())
                        .param("subtotal", "1000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].code").value("SAVE10"));
    }

    @Test
    void publicPromotionLookup_whenCodeMatches_shouldReturnPromotion() throws Exception {
        when(promotionService.lookupPublic("SAVE10", HOTEL_ID, new BigDecimal("1000.00"))).thenReturn(response());

        mockMvc.perform(get("/api/v1/promotions/public/{code}", "SAVE10")
                        .param("hotelId", HOTEL_ID.toString())
                        .param("subtotal", "1000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.code").value("SAVE10"));
    }

    private PromotionMutationRequest request() {
        return new PromotionMutationRequest(
                HOTEL_ID,
                "SAVE10",
                "Save Ten",
                null,
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

    private PromotionResponse response() {
        return new PromotionResponse(
                PROMOTION_ID,
                "SAVE10",
                "Save Ten",
                null,
                "PERCENT",
                new BigDecimal("10.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"),
                100,
                0,
                1,
                Instant.parse("2026-07-01T00:00:00Z"),
                Instant.parse("2026-08-01T00:00:00Z"),
                true,
                HOTEL_ID,
                null,
                Instant.parse("2026-07-01T00:00:00Z"),
                Instant.parse("2026-07-01T00:00:00Z")
        );
    }
}
