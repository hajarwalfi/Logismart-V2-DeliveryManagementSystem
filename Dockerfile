# Multi-stage Dockerfile for Logismart API
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy pom files for dependency resolution
COPY pom.xml .
COPY logismart-security/pom.xml ./logismart-security/
COPY logismart-api/pom.xml ./logismart-api/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY logismart-security/src ./logismart-security/src
COPY logismart-api/src ./logismart-api/src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S logismart && adduser -S logismart -G logismart

# Copy the built JAR from build stage
COPY --from=build /app/logismart-api/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R logismart:logismart /app

# Switch to non-root user
USER logismart

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application with Docker profile
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]
