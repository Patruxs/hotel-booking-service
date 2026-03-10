# 🏨 Hotel Booking Service

A full-stack hotel booking management system built with **Spring Boot**, deployed on **AWS** with fully automated **CI/CD pipeline**, **Infrastructure as Code (Terraform + Ansible)**, and **Monitoring (Prometheus + Grafana)**.

## 📐 Architecture Overview

```
  ┌──────────┐       ┌──────────────────────────────────────────────────┐
  │  GitHub   │       │              AWS Cloud (Singapore)               │
  │  Actions  │──────►│                                                  │
  │  CI/CD    │  SSH  │   ALB ──► EC2 App-1 (Backend + Frontend)        │
  └──────────┘       │        ──► EC2 App-2 (Backend + Frontend)        │
                      │                    │                              │
                      │                    ▼                              │
                      │              RDS MySQL 8.0                       │
  ┌──────────┐       │             (Private Subnet)                      │
  │ Docker   │       │                                                    │
  │ Hub      │       │   EC2 Monitor (Prometheus + Grafana)              │
  └──────────┘       └──────────────────────────────────────────────────┘
```

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Configuration Profiles](#-configuration-profiles)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Infrastructure](#-infrastructure)
- [Monitoring](#-monitoring)
- [API Documentation](#-api-documentation)
- [Default Accounts](#-default-accounts)
- [Project Structure](#-project-structure)
- [Troubleshooting](#-troubleshooting)

## ✨ Features

- 🔐 User authentication and authorization (JWT)
- 👥 Role-based access control (Admin / Customer)
- 🏨 Hotel and room management
- 📅 Booking system with date validation
- 🖼️ Image upload with Cloudinary integration
- 📧 Email notifications
- 📊 RESTful API with Swagger documentation
- 🔒 Spring Security integration
- 📈 Prometheus metrics & Grafana dashboards
- 🐳 Dockerized deployment with multi-stage builds
- 🚀 Automated CI/CD with GitHub Actions
- ☁️ AWS infrastructure managed by Terraform & Ansible

## 🛠️ Tech Stack

### Backend
| Technology | Purpose |
|-----------|---------|
| **Java 21** | Programming language |
| **Spring Boot 3.5.7** | Application framework |
| **Spring Security** | Authentication & Authorization |
| **Spring Data JPA** | Database ORM |
| **MySQL 8.0** | Relational database |
| **JWT (jjwt 0.12.6)** | Token-based authentication |
| **MapStruct** | Object mapping |
| **Lombok** | Reduce boilerplate code |
| **Cloudinary** | Image storage & CDN |
| **Spring Boot Actuator** | Health checks & metrics |
| **Micrometer Prometheus** | Metrics export |
| **Swagger / OpenAPI** | API documentation |
| **Apache POI** | Excel export |
| **Maven** | Build tool |

### DevOps & Infrastructure
| Technology | Purpose |
|-----------|---------|
| **Docker** | Containerization (multi-stage build) |
| **GitHub Actions** | CI/CD pipeline |
| **Terraform** | AWS infrastructure provisioning |
| **Ansible** | Configuration management & deployment |
| **AWS EC2** | Application servers (t2.micro) |
| **AWS ALB** | Load balancing |
| **AWS RDS** | Managed MySQL database |
| **AWS VPC** | Network isolation |
| **Prometheus** | Metrics collection |
| **Grafana** | Monitoring dashboards |

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21** or higher ([Download](https://www.oracle.com/java/technologies/downloads/))
- **MySQL 8.0+** ([Download](https://dev.mysql.com/downloads/))
- **Maven 3.6+** (or use the included Maven Wrapper)
- **Git** ([Download](https://git-scm.com/downloads))
- **Docker** (optional, for containerized deployment)

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/Patruxs/hotel-booking-service.git
cd hotel-booking-service
```

### 2. Create MySQL Database

```sql
CREATE DATABASE hotel_booking_service;
```

### 3. Configure Application Properties

The project uses Spring profiles for different environments. For local development, create your own configuration file:

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
    password: YOUR_MYSQL_PASSWORD       # ⚠️ Change this!
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update
---
jwt:
  secretKey: YOUR_JWT_SECRET_KEY_HERE   # ⚠️ Generate a secure key!

cloudinary:
  cloud-name: YOUR_CLOUDINARY_NAME     # ⚠️ Get from cloudinary.com
  api-key: YOUR_CLOUDINARY_API_KEY
  api-secret: YOUR_CLOUDINARY_API_SECRET
```

> **🔒 Security Note**: The `application-dev.yml` file is in `.gitignore` and will NOT be committed to Git.

#### Step 3.3: Generate JWT Secret Key

```bash
openssl rand -base64 32
```

### 4. Run the Application

```bash
# Using Maven Wrapper (Recommended)
./mvnw spring-boot:run        # Linux/Mac
.\mvnw.cmd spring-boot:run    # Windows

# Or using Maven
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

### 5. Verify Installation

Once the application starts, visit:

| URL | Description |
|-----|-------------|
| http://localhost:8080 | API Base |
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/v3/api-docs | OpenAPI Docs |
| http://localhost:8080/actuator/health | Health Check |
| http://localhost:8080/actuator/prometheus | Prometheus Metrics |

## 🔧 Configuration Profiles

| Profile | File | Purpose | Default |
|---------|------|---------|---------|
| `dev` | `application-dev.yml` | Local development | ✅ Yes |
| `test` | `application-test.yml` | Testing (H2 database) | No |
| `prod` | `application-prod.yml` | Production (AWS RDS) | No |

```bash
# Development (default)
mvn spring-boot:run

# Test
mvn spring-boot:run -Ptest

# Production
mvn spring-boot:run -Pprod
```

## 🔄 CI/CD Pipeline

The project includes a fully automated **GitHub Actions** CI/CD pipeline with 4 stages:

```
Push to main → 🧪 Test → 🐳 Build & Push Image → 🚀 Deploy to AWS → 📢 Notify
```

| Stage | Description |
|-------|-------------|
| **🧪 Test** | Run unit tests with `mvn test`, verify build |
| **🐳 Build** | Build Docker image (multi-stage), push to Docker Hub |
| **🚀 Deploy** | SSH into EC2 via Ansible, pull new image, restart containers |
| **📢 Notify** | Report pipeline results |

### Required GitHub Secrets

| Secret | Description |
|--------|-------------|
| `DOCKERHUB_USERNAME` | Docker Hub username |
| `DOCKERHUB_TOKEN` | Docker Hub access token |
| `EC2_SSH_KEY` | Content of SSH private key (`.pem` file) |
| `APP_EC2_1_IP` | Public IP of App EC2 instance 1 |
| `APP_EC2_2_IP` | Public IP of App EC2 instance 2 |
| `MONITOR_EC2_IP` | Public IP of Monitor EC2 instance |
| `ALB_DNS` | ALB DNS name |
| `RDS_ENDPOINT` | RDS MySQL endpoint |
| `FRONTEND_IMAGE` | Docker Hub frontend image |
| `CLOUDINARY_NAME` | Cloudinary cloud name |
| `CLOUDINARY_KEY` | Cloudinary API key |
| `CLOUDINARY_SECRET` | Cloudinary API secret |

### Docker

The project uses a **multi-stage Dockerfile**:

```dockerfile
# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-21 AS builder

# Stage 2: Run with lightweight JRE
FROM eclipse-temurin:21-jre
```

Docker image: `patruxs/hotel-booking-backend`

## ☁️ Infrastructure

All AWS infrastructure is managed as code in the [`infrastructure/`](infrastructure/) directory.

> See [`infrastructure/README.md`](infrastructure/README.md) for detailed documentation.

### AWS Resources (Terraform)

| Resource | Type | Purpose |
|----------|------|---------|
| VPC | `10.0.0.0/16` | Isolated network |
| Public Subnets | 2x (AZ-a, AZ-b) | EC2 instances, ALB |
| Private Subnets | 2x (AZ-a, AZ-b) | RDS Database |
| EC2 App-1 & App-2 | `t2.micro` | Backend + Frontend containers |
| EC2 Monitor | `t2.micro` | Prometheus + Grafana |
| ALB | Application | Load balancing + routing |
| RDS | `db.t3.micro` | MySQL 8.0 database |
| Security Groups | 4 groups | Network access control |

### Deployment (Ansible)

| Playbook | Purpose |
|----------|---------|
| `setup-docker.yml` | Install Docker on all EC2 instances |
| `deploy-app.yml` | Deploy Backend + Frontend containers |
| `deploy-monitor.yml` | Deploy Prometheus + Grafana |

## 📊 Monitoring

| Service | Port | URL |
|---------|------|-----|
| **Prometheus** | 9090 | `http://<MONITOR_IP>:9090` |
| **Grafana** | 3000 | `http://<MONITOR_IP>:3000` |

- Prometheus scrapes Spring Boot Actuator metrics (`/actuator/prometheus`) every **15 seconds** from both App EC2 instances
- Grafana visualizes: CPU, Memory, HTTP request rates, JVM metrics, and custom business metrics

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

**Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

- View all available endpoints
- Test API calls directly from the browser
- See request/response schemas
- Authenticate with JWT tokens

## 🔑 Default Accounts

| Role | Email | Password |
|------|-------|----------|
| **Admin** | `admin@gmail.com` | `admin123` |
| **Customer** | `customer@gmail.com` | `customer123` |

> **⚠️ Important**: Change these passwords in production!

## 📁 Project Structure

```
hotel-booking-service/
├── .github/
│   └── workflows/
│       └── ci-cd.yml                # GitHub Actions CI/CD pipeline
├── infrastructure/                   # Infrastructure as Code
│   ├── README.md                    # Infrastructure documentation
│   ├── .gitignore                   # Exclude sensitive infra files
│   ├── terraform/                   # AWS provisioning (VPC, EC2, RDS, ALB)
│   │   ├── provider.tf
│   │   ├── network.tf
│   │   ├── security.tf
│   │   ├── compute.tf
│   │   ├── rds.tf
│   │   └── outputs.tf
│   └── ansible/                     # Deployment automation
│       ├── inventory.ini
│       ├── setup-docker.yml
│       ├── deploy-app.yml
│       ├── deploy-monitor.yml
│       ├── docker-compose.yml
│       ├── docker-compose-monitor.yml
│       └── prometheus.yml
├── src/
│   ├── main/
│   │   ├── java/org/example/hotelbookingservice/
│   │   │   ├── controller/          # REST API endpoints
│   │   │   ├── services/            # Business logic
│   │   │   ├── repository/          # Data access layer (JPA)
│   │   │   ├── entity/              # JPA entities
│   │   │   ├── dto/                 # Data transfer objects
│   │   │   ├── mapper/              # MapStruct mappers
│   │   │   ├── config/              # Configuration & DataSeeder
│   │   │   ├── security/            # Security & JWT
│   │   │   ├── exception/           # Global exception handling
│   │   │   ├── enums/               # Enumerations
│   │   │   └── api/                 # API response wrappers
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml.example
│   │       ├── application-test.yml
│   │       └── application-prod.yml
│   └── test/                        # Unit & integration tests
├── Dockerfile                        # Multi-stage Docker build
├── .dockerignore                     # Docker build exclusions
├── pom.xml                           # Maven dependencies & build config
├── start-env.sh                      # Script to start AWS environment
├── stop-env.sh                       # Script to stop AWS environment
└── README.md                         # This file
```

## 🐛 Troubleshooting

### Database Connection Failed

**Problem**: `Communications link failure`

**Solution**:
- Verify MySQL is running: `mysql -u root -p`
- Check database exists: `SHOW DATABASES;`
- Verify credentials in `application-dev.yml`

### Port 8080 Already in Use

**Solution**: Change port in `application.yml`: `server.port: 8081`

### JWT Authentication Errors

**Solution**:
- Ensure `jwt.secretKey` is set in `application-dev.yml`
- Use a base64-encoded key (at least 256 bits)

### Cloudinary Upload Fails

**Solution**:
- Sign up at [cloudinary.com](https://cloudinary.com)
- Get your credentials from the dashboard
- Update `application-dev.yml` with your credentials

### Docker Build Fails

**Solution**:
- Ensure Docker is running
- Check `Dockerfile` and `.dockerignore` are present
- Run `docker build -t hotel-booking .` to debug locally

## 📝 License

This project is for educational purposes.

---

**Happy Coding! 🚀**
