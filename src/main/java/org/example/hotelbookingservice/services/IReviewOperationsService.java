package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.dto.response.common.ListResponse;
import org.example.hotelbookingservice.dto.response.review.RatingSummaryResponse;
import org.example.hotelbookingservice.dto.response.review.ReviewEligibilityResponse;
import org.example.hotelbookingservice.dto.request.review.ReviewModerationRequest;
import org.example.hotelbookingservice.dto.request.review.ReviewRequest;
import org.example.hotelbookingservice.dto.response.review.ReviewResponse;
import org.example.hotelbookingservice.dto.request.review.ReviewUpdateRequest;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface IReviewOperationsService {
    ReviewResponse createReview(UUID hotelId, ReviewRequest request, Authentication authentication);

    ReviewEligibilityResponse reviewEligibility(UUID hotelId, Authentication authentication);

    ListResponse<ReviewResponse> listPublicReviews(UUID hotelId, int page, int limit);

    ListResponse<ReviewResponse> listModerationReviews(UUID hotelId, int page, int limit, Authentication authentication);

    ListResponse<ReviewResponse> listMyReviews(int page, int limit, Authentication authentication);

    ReviewResponse updateMyReview(UUID reviewId, ReviewUpdateRequest request, Authentication authentication);

    ReviewResponse moderateReview(UUID hotelId, UUID reviewId, ReviewModerationRequest request, Authentication authentication);

    void deleteReview(UUID hotelId, UUID reviewId, Authentication authentication);

    RatingSummaryResponse visibleRatingSummary(UUID hotelId);
}
