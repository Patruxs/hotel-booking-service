package org.example.hotelbookingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "cloudinary.cloud-name=test",
    "cloudinary.api-key=test",
    "cloudinary.api-secret=test",
    "jwt.secretKey=bXlfc3VwZXJfc2VjcmV0X2tleV9oZXJlX211c3RfYmVfbG9uZ2VyX3RoYW5fMjU2X2JpdHM="
})
class HotelBookingServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
