package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.config.UploadProperties;
import org.example.hotelbookingservice.dto.request.review.ReviewModerationRequest;
import org.example.hotelbookingservice.dto.request.review.ReviewRequest;
import org.example.hotelbookingservice.dto.request.review.ReviewUpdateRequest;
import org.example.hotelbookingservice.dto.response.common.ListResponse;
import org.example.hotelbookingservice.dto.response.review.RatingSummaryResponse;
import org.example.hotelbookingservice.dto.response.review.ReviewEligibilityResponse;
import org.example.hotelbookingservice.dto.response.review.ReviewResponse;
import org.example.hotelbookingservice.repository.ReviewImageRepository;
import org.example.hotelbookingservice.services.IFileStorageService;
import org.example.hotelbookingservice.services.IReviewOperationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReviewOperationsServiceImpl extends Milestone6ServiceSupport implements IReviewOperationsService {
    public ReviewOperationsServiceImpl(NamedParameterJdbcTemplate jdbcTemplate, IFileStorageService fileStorageService, UploadProperties uploadProperties) {
        super(jdbcTemplate, fileStorageService, uploadProperties);
    }

    @Autowired
    public ReviewOperationsServiceImpl(NamedParameterJdbcTemplate jdbcTemplate,
                                       IFileStorageService fileStorageService,
                                       UploadProperties uploadProperties,
                                       ReviewImageRepository reviewImageRepository) {
        super(jdbcTemplate, fileStorageService, uploadProperties, null, null, reviewImageRepository, null, null);
    }

    @Override
    @Transactional
    public ReviewResponse createReview(UUID hotelId, ReviewRequest request, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        BookingReviewSource booking = requireCompletedBookingForReview(hotelId, request.bookingId(), user.accountId());
        UUID reviewId = UUID.randomUUID();
        try {
            jdbcTemplate.update("""
                    insert into reviews (id, booking_id, hotel_id, account_id, rating, comment)
                    values (:id, :bookingId, :hotelId, :accountId, :rating, :comment)
                    """, new MapSqlParameterSource("id", reviewId)
                    .addValue("bookingId", booking.id())
                    .addValue("hotelId", booking.hotelId())
                    .addValue("accountId", user.accountId())
                    .addValue("rating", request.rating())
                    .addValue("comment", trimToNull(request.comment())));
        } catch (DuplicateKeyException ex) {
            throw conflict("This booking has already been reviewed");
        }
        insertReviewImages(reviewId, request.imageIds(), user);
        notifyHotelOperators(hotelId, "REVIEW_CREATED", "New review received", "A customer left a review.", "/admin/hotels/" + hotelId + "/reviews");
        return reviewDetail(reviewId, false);
    }

    @Override
    public ReviewEligibilityResponse reviewEligibility(UUID hotelId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        return jdbcTemplate.query("""
                with completed_stays as (
                    select b.id, b.check_out, (r.id is not null) reviewed
                    from bookings b
                    join hotels h on h.id = b.hotel_id
                    left join reviews r on r.booking_id = b.id
                    where b.hotel_id = :hotelId
                      and b.account_id = :accountId
                      and b.status = 'COMPLETED'
                      and h.deleted_at is null
                      and h.status in ('ACTIVE', 'SUSPENDED')
                )
                select (
                    select id
                    from completed_stays
                    where not reviewed
                    order by check_out desc, id
                    limit 1
                ) booking_id,
                exists(select 1 from completed_stays) has_completed_stay
                """, new MapSqlParameterSource("hotelId", hotelId).addValue("accountId", user.accountId()), rs -> {
            if (!rs.next()) {
                return new ReviewEligibilityResponse(false, null, "NO_COMPLETED_STAY");
            }
            UUID bookingId = rs.getObject("booking_id", UUID.class);
            if (bookingId != null) {
                return new ReviewEligibilityResponse(true, bookingId, "ELIGIBLE");
            }
            String reason = rs.getBoolean("has_completed_stay") ? "ALL_STAYS_REVIEWED" : "NO_COMPLETED_STAY";
            return new ReviewEligibilityResponse(false, null, reason);
        });
    }

    @Override
    public ListResponse<ReviewResponse> listPublicReviews(UUID hotelId, int page, int limit) {
        return listReviews(hotelId, true, page, limit, null);
    }

    @Override
    public ListResponse<ReviewResponse> listModerationReviews(UUID hotelId, int page, int limit, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user, "reviews.manage", hotelId);
        return listReviews(hotelId, false, page, limit, null);
    }

    @Override
    public ListResponse<ReviewResponse> listMyReviews(int page, int limit, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        return listReviews(null, false, page, limit, user.accountId());
    }

    @Override
    @Transactional
    public ReviewResponse updateMyReview(UUID reviewId, ReviewUpdateRequest request, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        ReviewResponse current = reviewDetail(reviewId, false);
        if (!current.accountId().equals(user.accountId())) {
            throw forbidden("Cannot edit another customer's review");
        }
        requireReviewWritableHotel(current.hotelId());
        jdbcTemplate.update("""
                update reviews
                set rating = coalesce(:rating, rating),
                    comment = coalesce(:comment, comment),
                    updated_at = now()
                where id = :id
                """, new MapSqlParameterSource("id", reviewId)
                .addValue("rating", request.rating())
                .addValue("comment", trimToNull(request.comment())));
        return reviewDetail(reviewId, false);
    }

    @Override
    @Transactional
    public ReviewResponse moderateReview(UUID hotelId, UUID reviewId, ReviewModerationRequest request, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user, "reviews.manage", hotelId);
        boolean visible = request.visible() == null || request.visible();
        int updated = jdbcTemplate.update("""
                update reviews
                set visible = :visible, updated_at = now()
                where id = :id and hotel_id = :hotelId and deleted_at is null
                """, new MapSqlParameterSource("id", reviewId).addValue("hotelId", hotelId).addValue("visible", visible));
        if (updated == 0) {
            throw notFound("Review not found");
        }
        return reviewDetail(reviewId, false);
    }

    @Override
    @Transactional
    public void deleteReview(UUID hotelId, UUID reviewId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user, "reviews.manage", hotelId);
        int updated = jdbcTemplate.update("""
                update reviews
                set visible = false,
                    deleted_at = coalesce(deleted_at, now()),
                    updated_at = now()
                where id = :id and hotel_id = :hotelId and deleted_at is null
                """, new MapSqlParameterSource("id", reviewId).addValue("hotelId", hotelId));
        if (updated == 0) {
            throw notFound("Review not found");
        }
    }

    @Override
    public RatingSummaryResponse visibleRatingSummary(UUID hotelId) {
        return jdbcTemplate.queryForObject("""
                select coalesce(round(avg(rating), 1), 0) average_rating, count(*) review_count
                from reviews
                where hotel_id = :hotelId
                  and visible
                  and deleted_at is null
                  and exists (
                      select 1
                      from hotels h
                      where h.id = reviews.hotel_id and h.status = 'ACTIVE' and h.deleted_at is null
                  )
                """, new MapSqlParameterSource("hotelId", hotelId), (rs, rowNum) ->
                new RatingSummaryResponse(rs.getBigDecimal("average_rating"), rs.getLong("review_count")));
    }
}
