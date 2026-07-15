package org.example.hotelbookingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.api.ReviewOperationsApi;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.response.common.ListResponse;
import org.example.hotelbookingservice.dto.response.review.RatingSummaryResponse;
import org.example.hotelbookingservice.dto.response.review.ReviewEligibilityResponse;
import org.example.hotelbookingservice.dto.request.review.ReviewModerationRequest;
import org.example.hotelbookingservice.dto.request.review.ReviewRequest;
import org.example.hotelbookingservice.dto.response.review.ReviewResponse;
import org.example.hotelbookingservice.dto.request.review.ReviewUpdateRequest;
import org.example.hotelbookingservice.services.IReviewOperationsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReviewOperationsController implements ReviewOperationsApi {
    private final IReviewOperationsService reviewOperationsService;

    @Override
    public ApiResponse<ListResponse<ReviewResponse>> publicReviews(@PathVariable UUID hotelId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.<ListResponse<ReviewResponse>>builder().status(200).message("Success").data(reviewOperationsService.listPublicReviews(hotelId, page, limit)).build();
    }

    @Override
    public ApiResponse<RatingSummaryResponse> reviewSummary(@PathVariable UUID hotelId) {
        return ApiResponse.<RatingSummaryResponse>builder().status(200).message("Success").data(reviewOperationsService.visibleRatingSummary(hotelId)).build();
    }

    @Override
    public ApiResponse<ReviewResponse> createReview(@PathVariable UUID hotelId, @RequestBody @Valid ReviewRequest request, Authentication authentication) {
        return ApiResponse.<ReviewResponse>builder().status(201).message("Review created").data(reviewOperationsService.createReview(hotelId, request, authentication)).build();
    }

    @Override
    public ApiResponse<ReviewEligibilityResponse> reviewEligibility(@PathVariable UUID hotelId, Authentication authentication) {
        return ApiResponse.<ReviewEligibilityResponse>builder().status(200).message("Success").data(reviewOperationsService.reviewEligibility(hotelId, authentication)).build();
    }

    @Override
    public ApiResponse<ListResponse<ReviewResponse>> moderationReviews(@PathVariable UUID hotelId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit, Authentication authentication) {
        return ApiResponse.<ListResponse<ReviewResponse>>builder().status(200).message("Success").data(reviewOperationsService.listModerationReviews(hotelId, page, limit, authentication)).build();
    }

    @Override
    public ApiResponse<ListResponse<ReviewResponse>> myReviews(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit, Authentication authentication) {
        return ApiResponse.<ListResponse<ReviewResponse>>builder().status(200).message("Success").data(reviewOperationsService.listMyReviews(page, limit, authentication)).build();
    }

    @Override
    public ApiResponse<ReviewResponse> updateMyReview(@PathVariable UUID reviewId, @RequestBody @Valid ReviewUpdateRequest request, Authentication authentication) {
        return ApiResponse.<ReviewResponse>builder().status(200).message("Review updated").data(reviewOperationsService.updateMyReview(reviewId, request, authentication)).build();
    }

    @Override
    public ApiResponse<ReviewResponse> moderateReview(@PathVariable UUID hotelId, @PathVariable UUID reviewId, @RequestBody ReviewModerationRequest request, Authentication authentication) {
        return ApiResponse.<ReviewResponse>builder().status(200).message("Review moderated").data(reviewOperationsService.moderateReview(hotelId, reviewId, request, authentication)).build();
    }

    @Override
    public void deleteReview(@PathVariable UUID hotelId, @PathVariable UUID reviewId, Authentication authentication) {
        reviewOperationsService.deleteReview(hotelId, reviewId, authentication);
    }
}
