# Hotel Booking Service

Dự án này sử dụng Spring Boot. Để đảm bảo bảo mật, các file cấu hình chứa mật khẩu không được đưa lên Git.

## 🚀 Hướng dẫn cài đặt & Chạy (Local)

### 1. Yêu cầu
- Java 17+
- MySQL
- Maven

### 2. Cấu hình Database
Tạo database MySQL tên là: `hotel_booking_service`

### 3. Cấu hình biến môi trường (File Local)
Dự án không có sẵn file cấu hình local. Bạn cần tự tạo file:
`src/main/resources/application-local.yml`

Copy nội dung mẫu sau vào file đó và sửa lại thông tin của bạn:

```yaml
spring:
  config:
    activate:
      on-profile: local
  datasource:
    url: jdbc:mysql://localhost:3306/hotel_booking_service
    username: root          # <--- Sửa user DB của bạn
    password: your_password # <--- Sửa pass DB của bạn
jpa:
    hibernate:
      ddl-auto: update      
    show-sql: true          
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect

springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: method
    try-it-out-enabled: true
    enabled: true
  packages-to-scan: org.example.hotelbookingservice.controller

jwt:
  secretKey: dien_key_jwt_bat_ky_vao_day

cloudinary:
  cloud-name: dien_cloud_name_cua_ban
  api-key: dien_api_key_cua_ban
  api-secret: dien_api_secret_cua_ban
```

### 4. Chạy lệnh
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
