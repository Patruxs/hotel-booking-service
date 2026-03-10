package org.example.hotelbookingservice.config;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hotelbookingservice.entity.*;
import org.example.hotelbookingservice.enums.BookingStatus;
import org.example.hotelbookingservice.enums.RoomType;
import org.example.hotelbookingservice.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserroleRepository userroleRepository;
    private final PasswordEncoder passwordEncoder;
    private final HotelRepository hotelRepository;
    private final AmenityRepository amenityRepository;
    private final HotelamenityRepository hotelamenityRepository;
    private final RoomRepository roomRepository;
    private final RoomamenityRepository roomamenityRepository;
    private final ImageRepository imageRepository;
    private final BookingRepository bookingRepository;
    private final BookingRoomRepository bookingRoomRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (roleRepository.count() > 0) {
            log.info("Database already seeded. Skipping...");
            return;
        }

        log.info("=== Starting Database Seeding ===");

        // 1. Roles
        Map<Integer, Role> roles = seedRoles();

        // 2. Users
        Map<Integer, User> users = seedUsers();

        // 3. User-Role assignments
        seedUserRoles(users, roles);

        // 4. Amenities
        Map<Integer, Amenity> amenities = seedAmenities();

        // 5. Hotels
        Map<Integer, Hotel> hotels = seedHotels(users);

        // 6. Hotel-Amenity assignments
        seedHotelAmenities(hotels, amenities);

        // 7. Rooms
        Map<Integer, Room> rooms = seedRooms(hotels);

        // 8. Room-Amenity assignments
        seedRoomAmenities(rooms, amenities);

        // 9. Images (hotel + room)
        seedImages(hotels, rooms);

        // 10. Bookings
        Map<Integer, Booking> bookings = seedBookings(users);

        // 11. Booking-Room assignments
        seedBookingRooms(bookings, rooms);

        log.info("=== Database Seeding Completed Successfully ===");
    }

    // ======================== 1. ROLES ========================
    private Map<Integer, Role> seedRoles() {
        Map<Integer, Role> map = new LinkedHashMap<>();

        map.put(1, createRole(1, "CUSTOMER"));
        map.put(2, createRole(2, "ADMIN"));

        log.info("Seeded {} roles", map.size());
        return map;
    }

    private Role createRole(int id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return roleRepository.save(role);
    }

    // ======================== 2. USERS ========================
    private Map<Integer, User> seedUsers() {
        Map<Integer, User> map = new LinkedHashMap<>();

        // User 1: Admin
        map.put(1, createUser("Admin User", "admin@gmail.com", "admin123", "1234567890",
                LocalDate.of(2000, 12, 3)));
        // User 2: Customer
        map.put(2, createUser("Customer User", "customer@gmail.com", "customer123", "0987654321",
                LocalDate.of(2000, 12, 3)));
        // User 3: Nguyen Van A
        map.put(3, createUser("Nguyen Van A", "nguyenvana@gmail.com", "customer123", "0912345678",
                LocalDate.of(1995, 5, 15)));
        // User 4: Le Thi B
        map.put(4, createUser("Le Thi B", "lethib@gmail.com", "customer123", "0987123456",
                LocalDate.of(1998, 8, 20)));
        // User 5: Tran Van Nam
        map.put(5, createUser("Tran Van Nam", "tranam@gmail.com", "customer123", "0909123123",
                LocalDate.of(1990, 2, 10)));

        log.info("Seeded {} users", map.size());
        return map;
    }

    private User createUser(String fullName, String email, String rawPassword, String phone, LocalDate dob) {
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .phone(phone)
                .dob(dob)
                .activate(true)
                .userRoles(new LinkedHashSet<>())
                .build();
        return userRepository.save(user);
    }

    // ======================== 3. USER-ROLE ========================
    private void seedUserRoles(Map<Integer, User> users, Map<Integer, Role> roles) {
        // SQL: (role_id, user_id) => (2,1),(1,2),(1,3),(1,4),(1,5)
        assignUserRole(users.get(1), roles.get(2)); // Admin User -> ADMIN
        assignUserRole(users.get(2), roles.get(1)); // Customer -> CUSTOMER
        assignUserRole(users.get(3), roles.get(1)); // Nguyen Van A -> CUSTOMER
        assignUserRole(users.get(4), roles.get(1)); // Le Thi B -> CUSTOMER
        assignUserRole(users.get(5), roles.get(1)); // Tran Van Nam -> CUSTOMER

        log.info("Seeded user-role assignments");
    }

    private void assignUserRole(User user, Role role) {
        UserroleId id = new UserroleId();
        id.setUserId(user.getId());
        id.setRoleId(role.getId());

        Userrole ur = new Userrole();
        ur.setId(id);
        ur.setUser(user);
        ur.setRole(role);
        userroleRepository.save(ur);
    }

    // ======================== 4. AMENITIES ========================
    private Map<Integer, Amenity> seedAmenities() {
        Map<Integer, Amenity> map = new LinkedHashMap<>();

        // Hotel Services (1-7)
        map.put(1, createAmenity("Free Wi-Fi", "Hotel Service"));
        map.put(2, createAmenity("Swimming Pool", "Hotel Service"));
        map.put(3, createAmenity("Fitness Center", "Hotel Service"));
        map.put(4, createAmenity("Spa & Wellness", "Hotel Service"));
        map.put(5, createAmenity("Parking", "Hotel Service"));
        map.put(6, createAmenity("Restaurant", "Hotel Service"));
        map.put(7, createAmenity("Bar", "Hotel Service"));

        // Room Features (8-14)
        map.put(8, createAmenity("Air Conditioning", "Room Feature"));
        map.put(9, createAmenity("Flat-screen TV", "Room Feature"));
        map.put(10, createAmenity("Minibar", "Room Feature"));
        map.put(11, createAmenity("Balcony", "Room Feature"));
        map.put(12, createAmenity("Private Bathroom", "Room Feature"));
        map.put(13, createAmenity("Hairdryer", "Room Feature"));
        map.put(14, createAmenity("Coffee Machine", "Room Feature"));

        log.info("Seeded {} amenities", map.size());
        return map;
    }

    private Amenity createAmenity(String name, String type) {
        Amenity a = new Amenity();
        a.setName(name);
        a.setType(type);
        return amenityRepository.save(a);
    }

    // ======================== 5. HOTELS ========================
    private Map<Integer, Hotel> seedHotels(Map<Integer, User> users) {
        Map<Integer, Hotel> map = new LinkedHashMap<>();
        User admin = users.get(1);

        // SQL columns: id, contactName, contactPhone, description, email, isActive,
        // location, name, phone, starRating, UserId
        map.put(1, createHotel("Luxury Hotel", "Ho Chi Minh City",
                "A 5-star luxury hotel in the heart of the city.", 5,
                "contact@example.com", "0909000999", "Manager", "0909000888", admin));

        map.put(2, createHotel("Hanoi Old Quarter Hotel", "Ha Noi",
                "Khách sạn phố cổ cổ kính.", 4,
                "hanoios@example.com", "02439998888", "Tran Hanoi", "0912000111", admin));

        map.put(3, createHotel("Danang Beach Resort", "Da Nang",
                "Resort ven biển Mỹ Khê.", 5,
                "danangbeach@example.com", "02363777666", "Le Da Nang", "0912000222", admin));

        map.put(4, createHotel("Hue Heritage Hotel", "Thua Thien Hue",
                "Khách sạn kiến trúc cung đình độc đáo.", 4,
                "info@hueheritage.com", "02343888777", "Nguyen Hue", "0912345678", admin));

        map.put(5, createHotel("Dalat Misty Resort", "Da Lat",
                "Resort nằm giữa đồi thông thơ mộng.", 5,
                "contact@dalatmisty.com", "02633777888", "Tran Da Lat", "0912345679", admin));

        map.put(6, createHotel("Nha Trang Ocean View", "Nha Trang",
                "Khách sạn mặt tiền biển trần phú.", 4,
                "booking@nhatrangocean.com", "02583666555", "Le Nha Trang", "0912345680", admin));

        map.put(7, createHotel("Phu Quoc Paradise", "Phu Quoc",
                "Khu nghỉ dưỡng bungalow sát biển.", 5,
                "sales@phuquocparadise.com", "02973999000", "Pham Phu Quoc", "0912345681", admin));

        map.put(8, createHotel("Sapa Cloud Hotel", "Sa Pa",
                "View nhìn thẳng ra thung lũng Mường Hoa.", 3,
                "info@sapacloud.com", "02143555444", "Hoang Sa Pa", "0912345682", admin));

        map.put(9, createHotel("Vung Tau Royal Villa", "Vung Tau",
                "Biệt thự nghỉ dưỡng cao cấp.", 4,
                "contact@vungtauvilla.com", "02543222333", "Vu Vung Tau", "0912345683", admin));

        map.put(10, createHotel("Mekong Riverside", "Can Tho",
                "Khách sạn nổi bật tại bến Ninh Kiều.", 4,
                "booking@mekongriverside.com", "02923111222", "Doan Can Tho", "0912345684", admin));

        map.put(11, createHotel("Hoi An Ancient House", "Hoi An",
                "Phong cách cổ cổ kính, yên bình.", 3,
                "info@hoianancient.com", "02353999888", "Bui Hoi An", "0912345685", admin));

        map.put(12, createHotel("Halong Bay View", "Ha Long",
                "Khách sạn view vịnh Hạ Long tuyệt đẹp.", 5,
                "sales@halongbayview.com", "02033666777", "Ngo Ha Long", "0912345686", admin));

        map.put(13, createHotel("Quy Nhon Coastal Resort", "Quy Nhon",
                "Resort Eo Gió hoang sơ hùng vĩ.", 4,
                "contact@quynhoncoast.com", "02563444555", "Ly Quy Nhon", "0912345687", admin));

        log.info("Seeded {} hotels", map.size());
        return map;
    }

    private Hotel createHotel(String name, String location, String description, int starRating,
            String email, String phone, String contactName, String contactPhone, User owner) {
        Hotel h = new Hotel();
        h.setName(name);
        h.setLocation(location);
        h.setDescription(description);
        h.setStarRating(starRating);
        h.setEmail(email);
        h.setPhone(phone);
        h.setContactName(contactName);
        h.setContactPhone(contactPhone);
        h.setIsActive(true);
        h.setUser(owner);
        return hotelRepository.save(h);
    }

    // ======================== 6. HOTEL-AMENITY ========================
    private void seedHotelAmenities(Map<Integer, Hotel> hotels, Map<Integer, Amenity> amenities) {
        // SQL: (amenity_id, hotel_id)
        int[][] pairs = {
                { 1, 1 }, { 2, 1 }, { 3, 1 }, { 4, 1 }, { 5, 1 }, { 6, 1 }, { 7, 1 }, // Hotel 1: all 7
                { 1, 2 }, { 6, 2 }, // Hotel 2
                { 2, 3 }, { 5, 3 }, { 7, 3 }, // Hotel 3
                { 1, 4 }, { 6, 4 }, // Hotel 4
                { 2, 5 }, { 6, 5 }, // Hotel 5
                { 2, 6 }, { 7, 6 }, // Hotel 6
                { 2, 7 }, { 4, 7 }, // Hotel 7
                { 1, 8 }, { 6, 8 }, // Hotel 8
                { 2, 9 }, { 5, 9 }, // Hotel 9
                { 1, 10 }, { 6, 10 }, // Hotel 10
                { 1, 11 }, { 4, 11 }, // Hotel 11
                { 2, 12 }, { 3, 12 }, // Hotel 12
                { 2, 13 }, { 6, 13 }, // Hotel 13
        };

        for (int[] pair : pairs) {
            int amenityId = pair[0];
            int hotelId = pair[1];

            HotelamenityId id = new HotelamenityId();
            id.setAmenityId(amenities.get(amenityId).getId());
            id.setHotelId(hotels.get(hotelId).getId());

            Hotelamenity ha = new Hotelamenity();
            ha.setId(id);
            ha.setAmenity(amenities.get(amenityId));
            ha.setHotel(hotels.get(hotelId));
            hotelamenityRepository.save(ha);
        }

        log.info("Seeded hotel-amenity assignments");
    }

    // ======================== 7. ROOMS ========================
    private Map<Integer, Room> seedRooms(Map<Integer, Hotel> hotels) {
        Map<Integer, Room> map = new LinkedHashMap<>();

        // SQL columns: id, amount, capacity, description, name, price, type, hotel_id
        map.put(1, createRoom(1, 1, "A beautiful room with city view.", "Deluxe Single Room",
                new BigDecimal("500000"), RoomType.SINGLE, hotels.get(1)));
        map.put(2, createRoom(1, 2, "A beautiful room with city view.", "Premier Double Room",
                new BigDecimal("800000"), RoomType.DOUBLE, hotels.get(1)));
        map.put(3, createRoom(5, 2, "Phòng nhìn ra phố cổ.", "Deluxe City View",
                new BigDecimal("900000"), RoomType.DOUBLE, hotels.get(2)));
        map.put(4, createRoom(2, 4, "Phòng gia đình rộng rãi.", "Family Suite",
                new BigDecimal("2500000"), RoomType.SUIT, hotels.get(2)));
        map.put(5, createRoom(8, 1, "Phòng đơn tiêu chuẩn.", "Standard Single",
                new BigDecimal("600000"), RoomType.SINGLE, hotels.get(3)));
        map.put(6, createRoom(6, 2, "Phòng đôi hướng biển.", "Ocean Double",
                new BigDecimal("1200000"), RoomType.DOUBLE, hotels.get(3)));
        map.put(7, createRoom(5, 2, "Phòng hạng sang view sông Hương.", "Royal Double",
                new BigDecimal("1500000"), RoomType.DOUBLE, hotels.get(4)));
        map.put(8, createRoom(3, 2, "Bungalow gỗ thông ấm áp.", "Pine Hill Bungalow",
                new BigDecimal("1800000"), RoomType.DOUBLE, hotels.get(5)));
        map.put(9, createRoom(10, 2, "Phòng hướng biển tầng cao.", "Ocean Executive",
                new BigDecimal("2000000"), RoomType.DOUBLE, hotels.get(6)));
        map.put(10, createRoom(4, 4, "Villa 2 phòng ngủ sát biển.", "Beachfront Villa",
                new BigDecimal("5000000"), RoomType.SUIT, hotels.get(7)));
        map.put(11, createRoom(8, 2, "Phòng có ban công ngắm mây.", "Cloud Hunting Room",
                new BigDecimal("900000"), RoomType.SINGLE, hotels.get(8)));
        map.put(12, createRoom(2, 6, "Căn hộ 3 phòng ngủ cho gia đình.", "Grand Family Suite",
                new BigDecimal("4500000"), RoomType.SUIT, hotels.get(9)));
        map.put(13, createRoom(6, 2, "Phòng view cầu Cần Thơ.", "River View Deluxe",
                new BigDecimal("1200000"), RoomType.DOUBLE, hotels.get(10)));
        map.put(14, createRoom(5, 2, "Phòng thiết kế phong cách Indochine.", "Heritage Double",
                new BigDecimal("1100000"), RoomType.DOUBLE, hotels.get(11)));
        map.put(15, createRoom(10, 3, "Phòng rộng rãi cho 3 người.", "Triple Bay View",
                new BigDecimal("2200000"), RoomType.TRIPLE, hotels.get(12)));
        map.put(16, createRoom(5, 2, "Phòng hướng eo gió lộng gió.", "Cliff Side Room",
                new BigDecimal("1600000"), RoomType.DOUBLE, hotels.get(13)));

        log.info("Seeded {} rooms", map.size());
        return map;
    }

    private Room createRoom(int amount, int capacity, String description, String name,
            BigDecimal price, RoomType type, Hotel hotel) {
        Room r = new Room();
        r.setAmount(amount);
        r.setCapacity(capacity);
        r.setDescription(description);
        r.setName(name);
        r.setPrice(price);
        r.setType(type);
        r.setHotel(hotel);
        return roomRepository.save(r);
    }

    // ======================== 8. ROOM-AMENITY ========================
    private void seedRoomAmenities(Map<Integer, Room> rooms, Map<Integer, Amenity> amenities) {
        // SQL: (amenity_id, room_id)
        int[][] pairs = {
                { 8, 1 }, { 9, 1 }, { 10, 1 }, { 11, 1 }, { 12, 1 }, { 13, 1 }, { 14, 1 }, // Room 1: all room amenities
                { 8, 2 }, { 9, 2 }, { 10, 2 }, { 11, 2 }, { 12, 2 }, { 13, 2 }, { 14, 2 }, // Room 2: all room amenities
                { 8, 3 }, { 9, 3 }, { 10, 4 }, { 14, 4 }, // Room 3 & 4
                { 8, 5 }, { 11, 6 }, { 8, 7 }, { 9, 7 }, // Room 5, 6, 7
                { 10, 8 }, { 11, 8 }, { 8, 9 }, { 11, 9 }, // Room 8, 9
                { 12, 10 }, { 14, 10 }, { 8, 11 }, { 13, 11 }, // Room 10, 11
                { 9, 12 }, { 14, 12 }, { 8, 13 }, { 11, 13 }, // Room 12, 13
                { 8, 14 }, { 12, 14 }, { 9, 15 }, { 10, 15 }, // Room 14, 15
                { 8, 16 }, { 11, 16 }, // Room 16
        };

        for (int[] pair : pairs) {
            int amenityId = pair[0];
            int roomId = pair[1];

            RoomamenityId id = new RoomamenityId();
            id.setAmenityId(amenities.get(amenityId).getId());
            id.setRoomId(rooms.get(roomId).getId());

            Roomamenity ra = new Roomamenity();
            ra.setId(id);
            ra.setAmenity(amenities.get(amenityId));
            ra.setRoom(rooms.get(roomId));
            roomamenityRepository.save(ra);
        }

        log.info("Seeded room-amenity assignments");
    }

    // ======================== 9. IMAGES ========================
    private void seedImages(Map<Integer, Hotel> hotels, Map<Integer, Room> rooms) {
        // Hotel images (hotel_id -> list of urls)
        String[][] hotelImages = {
                { "1", "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730347/hotel_10_ipm3kr.jpg",
                        "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730347/hotel_8_pj7foa.jpg" },
                { "2", "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730347/hotel_9_pw1id9.jpg",
                        "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730346/hotel_6_qmkm8i.jpg" },
                { "3", "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730346/hotel_5_jzaubp.jpg",
                        "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730347/hotel_10_ipm3kr.jpg" },
                { "4", "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730347/hotel_8_pj7foa.jpg",
                        "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730347/hotel_9_pw1id9.jpg" },
                { "5", "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730346/hotel_6_qmkm8i.jpg",
                        "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730346/hotel_5_jzaubp.jpg" },
                { "6", "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730347/hotel_10_ipm3kr.jpg",
                        "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730347/hotel_8_pj7foa.jpg" },
                { "7", "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730347/hotel_9_pw1id9.jpg",
                        "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730346/hotel_6_qmkm8i.jpg" },
                { "8", "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730346/hotel_5_jzaubp.jpg",
                        "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730347/hotel_10_ipm3kr.jpg" },
                { "9", "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730347/hotel_8_pj7foa.jpg",
                        "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730347/hotel_9_pw1id9.jpg" },
                { "10", "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730346/hotel_6_qmkm8i.jpg",
                        "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730346/hotel_5_jzaubp.jpg" },
                { "11", "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730347/hotel_10_ipm3kr.jpg",
                        "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730347/hotel_8_pj7foa.jpg" },
                { "12", "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730347/hotel_9_pw1id9.jpg",
                        "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730346/hotel_6_qmkm8i.jpg" },
                { "13", "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730346/hotel_5_jzaubp.jpg",
                        "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730347/hotel_10_ipm3kr.jpg" },
        };

        for (String[] entry : hotelImages) {
            int hotelId = Integer.parseInt(entry[0]);
            Hotel hotel = hotels.get(hotelId);
            for (int i = 1; i < entry.length; i++) {
                createImage(entry[i], hotel, null);
            }
        }

        // Room images (room_id -> url)
        String[] roomImageUrls = {
                "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730377/room_7_wmdniz.jpg", // Room 1
                "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730376/room_2_ryy7uk.jpg", // Room 2
                "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730375/room_6_jhwmo4.jpg", // Room 3
                "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730374/room_8_aebnsm.jpg", // Room 4
                "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730373/room_5_kexydn.jpg", // Room 5
                "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730372/room_3_vwnd2m.jpg", // Room 6
                "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730372/room_4_jy0jyd.jpg", // Room 7
                "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730377/room_7_wmdniz.jpg", // Room 8
                "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730376/room_2_ryy7uk.jpg", // Room 9
                "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730375/room_6_jhwmo4.jpg", // Room 10
                "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730374/room_8_aebnsm.jpg", // Room 11
                "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730373/room_5_kexydn.jpg", // Room 12
                "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730372/room_3_vwnd2m.jpg", // Room 13
                "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730372/room_4_jy0jyd.jpg", // Room 14
                "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730377/room_7_wmdniz.jpg", // Room 15
                "https://res.cloudinary.com/dw8eobcaf/image/upload/v1764730376/room_2_ryy7uk.jpg", // Room 16
        };

        for (int i = 0; i < roomImageUrls.length; i++) {
            createImage(roomImageUrls[i], null, rooms.get(i + 1));
        }

        log.info("Seeded images for hotels and rooms");
    }

    private void createImage(String url, Hotel hotel, Room room) {
        Image img = new Image();
        img.setPath(url);
        img.setHotel(hotel);
        img.setRoom(room);
        imageRepository.save(img);
    }

    // ======================== 10. BOOKINGS ========================
    private Map<Integer, Booking> seedBookings(Map<Integer, User> users) {
        Map<Integer, Booking> map = new LinkedHashMap<>();

        // SQL columns: id, adultAmount, bookingReference, cancelReason, checkinDate,
        // checkoutDate,
        // childrenAmount, createAt, customerName, refund, room_number, specialRequire,
        // status, totalPrice, user_id
        map.put(1, createBooking("BK-2025-001", 2, null,
                LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 12), 0,
                LocalDate.of(2025, 1, 5), "Customer User", null, "101A",
                "Late check-in", BookingStatus.CHECKED_OUT, 1000000f, users.get(2)));

        map.put(2, createBooking("BK-2025-002", 2, null,
                LocalDate.of(2025, 6, 15), LocalDate.of(2025, 6, 18), 1,
                LocalDate.of(2025, 5, 20), "Nguyen Van A", null, null,
                "High floor request", BookingStatus.BOOKED, 3600000f, users.get(3)));

        map.put(3, createBooking("BK-2025-003", 1, "Change flight schedule",
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 3), 0,
                LocalDate.of(2025, 2, 15), "Le Thi B", null, null,
                null, BookingStatus.CANCELLED, 900000f, users.get(4)));

        map.put(4, createBooking("BK-2025-004", 2, null,
                LocalDate.of(2025, 12, 3), LocalDate.of(2025, 12, 5), 0,
                LocalDate.of(2025, 11, 20), "Tran Van Nam", null, "205B",
                "Honeymoon setup", BookingStatus.CHECKED_IN, 2400000f, users.get(5)));

        map.put(5, createBooking("BK-2025-005", 4, null,
                LocalDate.of(2025, 7, 20), LocalDate.of(2025, 7, 25), 2,
                LocalDate.of(2025, 6, 1), "Customer User", null, null,
                "Extra bed needed", BookingStatus.BOOKED, 12500000f, users.get(2)));

        map.put(6, createBooking("BK-2025-006", 2, null,
                LocalDate.of(2025, 2, 14), LocalDate.of(2025, 2, 16), 0,
                LocalDate.of(2025, 1, 30), "Nguyen Van A", null, "301C",
                "Quiet room", BookingStatus.CHECKED_OUT, 1800000f, users.get(3)));

        map.put(7, createBooking("BK-2025-007", 2, "Personal reasons",
                LocalDate.of(2025, 4, 30), LocalDate.of(2025, 5, 2), 2,
                LocalDate.of(2025, 3, 10), "Tran Van Nam", null, null,
                null, BookingStatus.CANCELLED, 3000000f, users.get(5)));

        map.put(8, createBooking("BK-2025-008", 1, null,
                LocalDate.of(2025, 8, 10), LocalDate.of(2025, 8, 12), 0,
                LocalDate.of(2025, 7, 1), "Le Thi B", null, null,
                null, BookingStatus.BOOKED, 1800000f, users.get(4)));

        map.put(9, createBooking("BK-2025-009", 2, null,
                LocalDate.of(2025, 1, 25), LocalDate.of(2025, 1, 28), 1,
                LocalDate.of(2025, 1, 10), "Customer User", null, "405D",
                "Airport pickup", BookingStatus.CHECKED_OUT, 6000000f, users.get(2)));

        map.put(10, createBooking("BK-2025-010", 6, null,
                LocalDate.of(2025, 9, 2), LocalDate.of(2025, 9, 5), 2,
                LocalDate.of(2025, 8, 1), "Nguyen Van A", null, null,
                "Large family room", BookingStatus.BOOKED, 9000000f, users.get(3)));

        map.put(11, createBooking("BK-2025-011", 2, null,
                LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 4), 0,
                LocalDate.of(2025, 11, 15), "Tran Van Nam", null, "501E",
                "Sea view preferred", BookingStatus.CHECKED_IN, 3300000f, users.get(5)));

        map.put(12, createBooking("BK-2025-012", 2, null,
                LocalDate.of(2025, 5, 15), LocalDate.of(2025, 5, 16), 0,
                LocalDate.of(2025, 5, 1), "Le Thi B", null, "602F",
                null, BookingStatus.CHECKED_OUT, 1500000f, users.get(4)));

        map.put(13, createBooking("BK-2025-013", 2, null,
                LocalDate.of(2025, 10, 10), LocalDate.of(2025, 10, 15), 1,
                LocalDate.of(2025, 9, 20), "Customer User", null, null,
                "Vegan breakfast", BookingStatus.BOOKED, 6000000f, users.get(2)));

        map.put(14, createBooking("BK-2025-014", 2, "Found better price",
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 5), 0,
                LocalDate.of(2025, 5, 10), "Nguyen Van A", null, null,
                null, BookingStatus.CANCELLED, 8000000f, users.get(3)));

        map.put(15, createBooking("BK-2025-015", 2, null,
                LocalDate.of(2025, 11, 20), LocalDate.of(2025, 11, 22), 1,
                LocalDate.of(2025, 10, 5), "Tran Van Nam", null, null,
                "Anniversary", BookingStatus.BOOKED, 3200000f, users.get(5)));

        log.info("Seeded {} bookings", map.size());
        return map;
    }

    private Booking createBooking(String bookingRef, int adultAmount, String cancelReason,
            LocalDate checkin, LocalDate checkout, int childrenAmount,
            LocalDate createAt, String customerName, Float refund,
            String roomNumber, String specialRequire,
            BookingStatus status, float totalPrice, User user) {
        Booking b = new Booking();
        b.setBookingReference(bookingRef);
        b.setAdultAmount(adultAmount);
        b.setCancelReason(cancelReason);
        b.setCheckinDate(checkin);
        b.setCheckoutDate(checkout);
        b.setChildrenAmount(childrenAmount);
        b.setCreateAt(createAt);
        b.setCustomerName(customerName);
        b.setRefund(refund);
        b.setRoomNumber(roomNumber);
        b.setSpecialRequire(specialRequire);
        b.setStatus(status);
        b.setTotalPrice(totalPrice);
        b.setUser(user);
        return bookingRepository.save(b);
    }

    // ======================== 11. BOOKING-ROOM ========================
    private void seedBookingRooms(Map<Integer, Booking> bookings, Map<Integer, Room> rooms) {
        // SQL: (id, booking_id, room_id)
        int[][] pairs = {
                { 1, 1 }, { 2, 3 }, { 3, 5 }, { 4, 7 }, { 5, 4 },
                { 6, 11 }, { 7, 15 }, { 8, 9 }, { 9, 2 }, { 10, 12 },
                { 11, 14 }, { 12, 8 }, { 13, 13 }, { 14, 10 }, { 15, 16 },
        };

        for (int[] pair : pairs) {
            int bookingId = pair[0];
            int roomId = pair[1];

            Bookingroom br = new Bookingroom();
            br.setBooking(bookings.get(bookingId));
            br.setRoom(rooms.get(roomId));
            bookingRoomRepository.save(br);
        }

        log.info("Seeded booking-room assignments");
    }
}