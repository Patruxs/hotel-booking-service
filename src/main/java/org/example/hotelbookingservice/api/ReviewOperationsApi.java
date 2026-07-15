package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.review.ReviewModerationRequest;
import org.example.hotelbookingservice.dto.request.review.ReviewRequest;
import org.example.hotelbookingservice.dto.request.review.ReviewUpdateRequest;
import org.example.hotelbookingservice.dto.response.common.ListResponse;
import org.example.hotelbookingservice.dto.response.review.RatingSummaryResponse;
import org.example.hotelbookingservice.dto.response.review.ReviewEligibilityResponse;
import org.example.hotelbookingservice.dto.response.review.ReviewResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@RequestMapping("/api/v1")
@Tag(name = "Review Operations", description = "Public review browsing, customer reviews, and review moderation")
public interface ReviewOperationsApi {

    @Operation(summary = "List public hotel reviews")
    @GetMapping("/hotels/{hotelId}/reviews")
    ApiResponse<ListResponse<ReviewResponse>> publicReviews(@PathVariable UUID hotelId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit);

    @Operation(summary = "Get hotel review summary")
    @GetMapping("/hotels/{hotelId}/reviews/summary")
    ApiResponse<RatingSummaryResponse> reviewSummary(@PathVariable UUID hotelId);

    @Operation(summary = "Create review")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @PostMapping("/hotels/{hotelId}/reviews")
    ApiResponse<ReviewResponse> createReview(@PathVariable UUID hotelId, @RequestBody @Valid ReviewRequest request, Authentication authentication);

    @Operation(summary = "Get my review eligibility for a hotel")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @GetMapping("/hotels/{hotelId}/reviews/eligibility")
    ApiResponse<ReviewEligibilityResponse> reviewEligibility(@PathVariable UUID hotelId, Authentication authentication);

    @Operation(summary = "List moderation reviews")
      @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER') or hasAuthority('OWNER')")
      @GetMapping("/admin/hotels/{hotelId}/reviews")
    ApiResponse<ListResponse<ReviewResponse>> moderationReviews(@PathVariable UUID hotelId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit, Authentication authentication);

    @Operation(summary = "List my reviews")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @GetMapping("/reviews/mine")
    ApiResponse<ListResponse<ReviewResponse>> myReviews(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit, Authentication authentication);

    @Operation(summary = "Update my review")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @PatchMapping("/reviews/{reviewId}/mine")
    ApiResponse<ReviewResponse> updateMyReview(@PathVariable UUID reviewId, @RequestBody @Valid ReviewUpdateRequest request, Authentication authentication);

    @Operation(summary = "Moderate review")
      @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER') or hasAuthority('OWNER')")
      @PatchMapping("/admin/hotels/{hotelId}/reviews/{reviewId}/moderation")
    ApiResponse<ReviewResponse> moderateReview(@PathVariable UUID hotelId, @PathVariable UUID reviewId, @RequestBody ReviewModerationRequest request, Authentication authentication);

    @Operation(summary = "Delete review")
      @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER') or hasAuthority('OWNER')")
      @DeleteMapping("/admin/hotels/{hotelId}/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteReview(@PathVariable UUID hotelId, @PathVariable UUID reviewId, Authentication authentication);
}
