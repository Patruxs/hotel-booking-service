package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.promotion.PromotionMutationRequest;
import org.example.hotelbookingservice.dto.response.common.ListResponse;
import org.example.hotelbookingservice.dto.response.promotion.PromotionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1")
@Tag(name = "Promotions", description = "Manage admin promotions and public promotion lookup")
public interface PromotionApi {
    @Operation(summary = "List admin promotions")
    @GetMapping("/admin/promotions")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<ListResponse<PromotionResponse>> promotions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID hotelId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    );

    @Operation(summary = "Get admin promotion detail")
    @GetMapping("/admin/promotions/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<PromotionResponse> promotion(@PathVariable UUID id);

    @Operation(summary = "Create promotion")
    @PostMapping("/admin/promotions")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<PromotionResponse> createPromotion(@RequestBody @Valid PromotionMutationRequest request);

    @Operation(summary = "Update promotion")
    @RequestMapping(path = "/admin/promotions/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<PromotionResponse> updatePromotion(@PathVariable UUID id, @RequestBody @Valid PromotionMutationRequest request);

    @Operation(summary = "Delete promotion")
    @DeleteMapping("/admin/promotions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')")
    void deletePromotion(@PathVariable UUID id);

    @Operation(summary = "Search public eligible promotions")
    @GetMapping("/promotions/public")
    ApiResponse<List<PromotionResponse>> publicPromotions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID hotelId,
            @RequestParam(required = false) BigDecimal subtotal,
            @RequestParam(defaultValue = "10") int limit
    );

    @Operation(summary = "Look up a public eligible promotion code")
    @GetMapping("/promotions/public/{code}")
    ApiResponse<PromotionResponse> publicPromotion(
            @PathVariable String code,
            @RequestParam(required = false) UUID hotelId,
            @RequestParam(required = false) BigDecimal subtotal
    );
}
