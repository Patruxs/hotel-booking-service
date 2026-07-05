package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
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

