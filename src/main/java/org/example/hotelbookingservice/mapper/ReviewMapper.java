package org.example.hotelbookingservice.mapper;

import org.example.hotelbookingservice.dto.request.review.ReviewRequeset;
import org.example.hotelbookingservice.dto.response.ReviewResponse;
import org.example.hotelbookingservice.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "userfullName", ignore = true)
    @Mapping(target = "userAvatar", ignore = true)
    ReviewResponse toReviewResponse(Review review);

    List<ReviewResponse> toReviewResponseList(List<Review> reviews);

    // Request -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "point", ignore = true)
    @Mapping(target = "visible", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Review toReview(ReviewRequeset request);
}
