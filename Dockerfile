
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Copy gradle files first for better caching
COPY build.gradle settings.gradle ./
COPY gradle/ ./gradle/

# Copy source code
COPY src/ ./src/

# Build the application
RUN gradle bootJar --no-daemon

# Runtime stage
FROM openjdk:17-jdk-alpine

# Install curl for health checks
RUN apk --no-cache add curl

VOLUME /tmp

# Copy the built jar
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 8085

# Run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]