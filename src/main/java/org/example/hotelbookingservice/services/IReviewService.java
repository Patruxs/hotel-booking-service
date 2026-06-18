package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.dto.request.review.ReviewRequeset;
import org.example.hotelbookingservice.dto.response.ReviewResponse;

import java.util.List;

public interface IReviewService {
    ReviewResponse addReview(ReviewRequeset request);

    List<ReviewResponse> getReviewsByHotelId(Integer hotelId);


    void deleteReview(Integer reviewId);
}
