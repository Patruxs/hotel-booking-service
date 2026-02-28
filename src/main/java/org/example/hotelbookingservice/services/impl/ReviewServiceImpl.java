package org.example.hotelbookingservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.dto.request.review.ReviewRequeset;
import org.example.hotelbookingservice.dto.response.ReviewResponse;
import org.example.hotelbookingservice.entity.Booking;
import org.example.hotelbookingservice.entity.Hotel;
import org.example.hotelbookingservice.entity.Review;
import org.example.hotelbookingservice.entity.User;
import org.example.hotelbookingservice.enums.BookingStatus;
import org.example.hotelbookingservice.exception.AppException;
import org.example.hotelbookingservice.exception.ErrorCode;
import org.example.hotelbookingservice.mapper.ReviewMapper;
import org.example.hotelbookingservice.repository.BookingRepository;
import org.example.hotelbookingservice.repository.HotelRepository;
import org.example.hotelbookingservice.repository.ReviewRepository;
import org.example.hotelbookingservice.services.IReviewService;
import org.example.hotelbookingservice.services.IUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements IReviewService {
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final ReviewMapper reviewMapper;
    private final IUserService userService;

    @Override
    public ReviewResponse addReview(ReviewRequeset request) {

        User currentUser = userService.getCurrentLoggedInUser();

        // Check if Booking exists
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));


        //Is this booking of the currently logged in User?
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Is this booking from the requested Hotel?
        //Get the first room in the booking -> check hotel ID
        boolean isCorrectHotel = booking.getBookingrooms().stream()
                .anyMatch(br -> br.getRoom().getHotel().getId().equals(request.getHotelId()));

        if (!isCorrectHotel) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        //Booking status must be CHECKED_OUT (must be reviewed after staying)
        if (booking.getStatus() != BookingStatus.CHECKED_OUT) {
            throw new RuntimeException("You can only review after checking out.");
        }

        // Get info hotel
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));


        Review review = reviewMapper.toReview(request);
        review.setUser(currentUser);
        review.setHotel(hotel);
        review.setCreateAt(LocalDate.now());

        Review savedReview = reviewRepository.save(review);


        return reviewMapper.toReviewResponse(savedReview);
    }

    @Override
    public List<ReviewResponse> getReviewsByHotelId(Integer hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new AppException(ErrorCode.NOT_FOUND_EXCEPTION);
        }
        List<Review> reviews = reviewRepository.findByHotelId(hotelId);
        return reviewMapper.toReviewResponseList(reviews);
    }

    @Override
    public void deleteReview(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        User currentUser = userService.getCurrentLoggedInUser();


        boolean isAdmin = currentUser.getUserRoles().stream()
                .anyMatch(r -> r.getRole().getName().equals(org.example.hotelbookingservice.enums.UserRole.ADMIN.name()));

        if (!isAdmin && !review.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        reviewRepository.delete(review);

    }


}
