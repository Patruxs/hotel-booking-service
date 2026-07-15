package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    default Optional<Review> findById(Integer id) {
        return id == null ? Optional.empty() : findById(new UUID(0L, id.longValue()));
    }

    @Query(value = "SELECT * FROM review WHERE hotel_id = :hotelId", nativeQuery = true)
    List<Review> findByHotelId(@Param("hotelId") Integer hotelId);


    @Query(value = """
        SELECT COUNT(*) > 0
        FROM booking b
        JOIN booking_room br ON br.booking_id = b.id
        JOIN room r ON r.id = br.room_id
        WHERE b.user_id = :userId
        AND r.hotel_id = :hotelId
        AND b.status = 'CHECKED_OUT'
    """, nativeQuery = true)
    boolean canUserReviewHotel(@Param("userId") Integer userId, @Param("hotelId") Integer hotelId);
}

