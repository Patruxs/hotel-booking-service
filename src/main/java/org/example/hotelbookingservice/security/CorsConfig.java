package org.example.hotelbookingservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Áp dụng cho tất cả các endpoint
                        .allowedOrigins(
                                "http://localhost:5173",      // Cho phép dev local (nếu bạn dùng Vite)
                                "http://localhost:3000",      // Cho phép dev local (nếu dùng Create React App)
                                "http://160.191.245.207",     // [QUAN TRỌNG] IP VPS của bạn (Frontend chạy port 80)
                                "http://160.191.245.207:80"   // Dự phòng nếu trình duyệt gửi kèm port
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Thêm OPTIONS
                        .allowedHeaders("*") // Cho phép tất cả header (Authorization, Content-Type...)
                        .allowCredentials(true) // Cho phép gửi Cookies/Auth headers
                        .maxAge(3600); // Cache cấu hình CORS trong 1 giờ để giảm tải request OPTIONS
            }
        };
    }
}