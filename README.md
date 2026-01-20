# 🏨 Hotel Booking Service

A full-stack hotel booking management system built with Spring Boot, providing REST APIs for hotel reservations, user management, and administrative functions.

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Configuration Profiles](#-configuration-profiles)
- [API Documentation](#-api-documentation)
- [Default Accounts](#-default-accounts)
- [Project Structure](#-project-structure)

## ✨ Features

- 🔐 User authentication and authorization (JWT)
- 👥 Role-based access control (Admin/Customer)
- 🏨 Hotel and room management
- 📅 Booking system
- 🖼️ Image upload with Cloudinary integration
- 📧 Email notifications
- 📊 RESTful API with Swagger documentation
- 🔒 Spring Security integration

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Database ORM
- **MySQL** - Database
- **JWT** - Token-based authentication
- **Cloudinary** - Image storage
- **MapStruct** - Object mapping
- **Lombok** - Reduce boilerplate code
- **Swagger/OpenAPI** - API documentation
- **Maven** - Build tool

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21** or higher ([Download](https://www.oracle.com/java/technologies/downloads/))
- **MySQL 8.0+** ([Download](https://dev.mysql.com/downloads/))
- **Maven 3.6+** (or use the included Maven Wrapper)
- **Git** ([Download](https://git-scm.com/downloads))

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/patrickzs/hotel-booking-service.git
cd hotel-booking-service
```

### 2. Create MySQL Database

Create a new MySQL database:

```sql
CREATE DATABASE hotel_booking_service;
```

Or use MySQL Workbench/command line:

```bash
mysql -u root -p
CREATE DATABASE hotel_booking_service;
EXIT;
```

### 3. Configure Application Properties

The project uses Spring profiles for different environments. For local development, you need to create your own configuration file:

#### Step 3.1: Copy the example file

```bash
# On Windows (PowerShell)
Copy-Item src\main\resources\application-dev.yml.example src\main\resources\application-dev.yml

# On Linux/Mac
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
```

#### Step 3.2: Edit `src/main/resources/application-dev.yml`

Open the file and update with your actual credentials:

```yaml
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:mysql://localhost:3306/hotel_booking_service
    username: root
    password: YOUR_MYSQL_PASSWORD # ⚠️ Change this!
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
---
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
---
jwt:
  secretKey: YOUR_JWT_SECRET_KEY_HERE # ⚠️ Generate a secure key!

cloudinary:
  cloud-name: YOUR_CLOUDINARY_NAME # ⚠️ Get from cloudinary.com
  api-key: YOUR_CLOUDINARY_API_KEY
  api-secret: YOUR_CLOUDINARY_API_SECRET
```

> **🔒 Security Note**: The `application-dev.yml` file is in `.gitignore` and will NOT be committed to Git. This keeps your credentials safe!

#### Step 3.3: Generate JWT Secret Key (Optional but Recommended)

You can generate a secure JWT secret key using:

```bash
# Using OpenSSL
openssl rand -base64 32

# Or using online tool: https://generate-random.org/api-key-generator
```

### 4. Run the Application

#### Option A: Using Maven Wrapper (Recommended)

```bash
# On Windows
.\mvnw.cmd spring-boot:run

# On Linux/Mac
./mvnw spring-boot:run
```

#### Option B: Using Maven

```bash
mvn spring-boot:run
```

#### Option C: Using IDE (IntelliJ IDEA / Eclipse)

1. Import the project as a Maven project
2. Wait for dependencies to download
3. Run the main class: `HotelBookingServiceApplication.java`

The application will start on **http://localhost:8080**

### 5. Verify Installation

Once the application starts, you should see:

```
Started HotelBookingServiceApplication in X.XXX seconds
```

Visit these URLs to verify:

- **API Base**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

## 🔧 Configuration Profiles

The project supports multiple Spring profiles:

| Profile | File                   | Purpose               | Default |
| ------- | ---------------------- | --------------------- | ------- |
| `dev`   | `application-dev.yml`  | Local development     | ✅ Yes  |
| `test`  | `application-test.yml` | Testing environment   | No      |
| `prod`  | `application-prod.yml` | Production deployment | No      |

### Running with Different Profiles

```bash
# Development (default)
mvn spring-boot:run

# Test
mvn spring-boot:run -Ptest

# Production
mvn spring-boot:run -Pprod
```

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

**Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Here you can:

- View all available endpoints
- Test API calls directly from the browser
- See request/response schemas
- Authenticate with JWT tokens

## 🔑 Default Accounts

The application comes with pre-configured test accounts:

### Admin Account

- **Email**: `admin@gmail.com`
- **Password**: `admin123`
- **Role**: Administrator (full access)

### Customer Account

- **Email**: `customer@gmail.com`
- **Password**: `customer123`
- **Role**: Customer (limited access)

> **⚠️ Important**: Change these passwords in production!

## 📁 Project Structure

```
hotel-booking-service/
├── src/
│   ├── main/
│   │   ├── java/org/example/hotelbookingservice/
│   │   │   ├── controller/       # REST API endpoints
│   │   │   ├── service/          # Business logic
│   │   │   ├── repository/       # Data access layer
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── dto/              # Data transfer objects
│   │   │   ├── mapper/           # MapStruct mappers
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── security/         # Security & JWT
│   │   │   └── exception/        # Exception handling
│   │   └── resources/
│   │       ├── application.yml              # Base config
│   │       ├── application-dev.yml.example  # Dev template
│   │       ├── application-test.yml         # Test config
│   │       └── application-prod.yml         # Prod config
│   └── test/                     # Unit & integration tests
├── pom.xml                       # Maven dependencies
└── README.md                     # This file
```

## 🐛 Troubleshooting

### Database Connection Failed

**Problem**: `Communications link failure`

**Solution**:

- Verify MySQL is running: `mysql -u root -p`
- Check database exists: `SHOW DATABASES;`
- Verify credentials in `application-dev.yml`

### Port 8080 Already in Use

**Problem**: `Port 8080 is already in use`

**Solution**:

- Change port in `application.yml`: `server.port: 8081`
- Or kill the process using port 8080

### JWT Authentication Errors

**Problem**: `Invalid JWT token`

**Solution**:

- Ensure `jwt.secretKey` is set in `application-dev.yml`
- Use a base64-encoded key (at least 256 bits)

### Cloudinary Upload Fails

**Problem**: `Cloudinary configuration error`

**Solution**:

- Sign up at [cloudinary.com](https://cloudinary.com)
- Get your credentials from the dashboard
- Update `application-dev.yml` with your credentials

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is for educational purposes.

## 📧 Contact

For questions or support, please open an issue on GitHub.

---

**Happy Coding! 🚀**
