# Stage 1: Build JAR bên trong Docker (không cần cài Maven trên máy)
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy pom.xml trước để cache dependencies (chỉ re-download khi pom.xml thay đổi)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source và build
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2: Chỉ lấy JAR, bỏ Maven và source code — image nhỏ hơn nhiều
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
