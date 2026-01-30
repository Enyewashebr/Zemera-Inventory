# Multi-stage build for Java 17 Maven project

# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first (for better Docker layer caching)
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/inventory-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose port (Render will set PORT env var, default to 10000 for free tier)
EXPOSE 10000

# Run the application
# The PORT environment variable will be set by Render
CMD ["sh", "-c", "java -jar app.jar"]
