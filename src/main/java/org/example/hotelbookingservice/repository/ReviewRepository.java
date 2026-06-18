package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByHotelId(Integer hotelId);


    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM Booking b
        JOIN b.bookingrooms br
        WHERE b.user.id = :userId 
        AND br.room.hotel.id = :hotelId 
        AND b.status = 'CHECKED_OUT'
    """)
    boolean canUserReviewHotel(Integer userId, Integer hotelId);
}

