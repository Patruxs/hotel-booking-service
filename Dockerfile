# ----- Giai đoạn 1: Build (Biên dịch) -----
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn


RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

# ----- Giai đoạn 2: Runtime (Chạy) -----
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]