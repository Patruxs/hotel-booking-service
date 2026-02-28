package org.example.hotelbookingservice.repository;

import jakarta.persistence.EntityManager;
import org.example.hotelbookingservice.entity.Amenity;
import org.example.hotelbookingservice.entity.Hotel;
import org.example.hotelbookingservice.entity.Image;
import org.example.hotelbookingservice.entity.Room;
import org.example.hotelbookingservice.entity.Roomamenity;
import org.example.hotelbookingservice.entity.RoomamenityId;
import org.example.hotelbookingservice.entity.User;
import org.example.hotelbookingservice.enums.RoomType;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class RoomRepositoryPerformanceTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void setup() {
        User user = User.builder()
                .fullName("Test User")
                .password("password")
                .email("test@example.com")
                .phone("123456789")
                .dob(java.time.LocalDate.of(1990, 1, 1))
                .activate(true)
                .build();
        entityManager.persist(user);

        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");
        hotel.setLocation("Test Address");
        hotel.setDescription("Test Description");
        hotel.setStarRating(5);
        hotel.setEmail("hotel@example.com");
        hotel.setPhone("987654321");
        hotel.setIsActive(true);
        hotel.setContactName("Contact Name");
        hotel.setContactPhone("987654321");
        hotel.setUser(user);
        entityManager.persist(hotel);

        Amenity amenity = new Amenity();
        amenity.setName("WiFi");
        amenity.setType("Room");
        entityManager.persist(amenity);

        for (int i = 0; i < 5; i++) {
            Room room = new Room();
            room.setName("Room " + i);
            room.setCapacity(2);
            room.setPrice(BigDecimal.valueOf(100));
            room.setDescription("Room Description " + i);
            room.setType(RoomType.SINGLE);
            room.setAmount(10);
            room.setHotel(hotel);
            entityManager.persist(room);

            Image image = new Image();
            image.setPath("image" + i + ".jpg");
            image.setRoom(room);
            image.setHotel(hotel);
            entityManager.persist(image);
            room.getImages().add(image);

            Roomamenity roomAmenity = new Roomamenity();
            RoomamenityId id = new RoomamenityId();
            id.setRoomId(room.getId());
            id.setAmenityId(amenity.getId());
            roomAmenity.setId(id);
            roomAmenity.setRoom(room);
            roomAmenity.setAmenity(amenity);
            entityManager.persist(roomAmenity);
            room.getRoomAmenities().add(roomAmenity);
        }

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void testGetAllRoomsNPlusOne() {
        Session session = entityManager.unwrap(Session.class);
        Statistics statistics = session.getSessionFactory().getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        List<Room> rooms = roomRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        for (Room room : rooms) {
            int imageCount = room.getImages().size();
            for (Image image : room.getImages()) {
                image.getPath();
            }
            int amenityCount = room.getRoomAmenities().size();
            for (Roomamenity ra : room.getRoomAmenities()) {
                ra.getAmenity().getName();
            }
        }

        long queryCount = statistics.getPrepareStatementCount();
        System.out.println("Executed SQL Queries: " + queryCount);

        // Before optimization, it will be > 10 queries (1+2N or more).
        // After optimization, it should be small (e.g., 1 for room + 1 for images + 1 for room_amenity + 1 for amenity).

        assertThat(queryCount).isLessThanOrEqualTo(4);
    }
}
