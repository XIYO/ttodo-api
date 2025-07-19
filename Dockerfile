# Use Eclipse Temurin JDK 21 for building
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew clean bootJar --no-daemon -x test

# Use Eclipse Temurin JRE 21 for runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Create non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Expose port 8080
EXPOSE 8080

# Set JVM options for container environment
ENV JAVA_OPTS="-Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]