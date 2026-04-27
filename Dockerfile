# ==========================================
# Stage 1: Build the application
# ==========================================
# Use an official Gradle image with JDK 17 (update to 21 if your project uses Java 21)
FROM gradle:8-jdk21 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy all project files from your host machine into the container
COPY . .

# Run the Gradle task to build a Fat JAR.
# Note: Ktor projects generated recently use 'buildFatJar'.
# Older ones might use 'shadowJar'.
RUN ./gradlew buildFatJar --no-daemon

# ==========================================
# Stage 2: Run the application
# ==========================================
# Use a lightweight JRE (Java Runtime Environment) base image
FROM eclipse-temurin:21-jre-jammy

# Set the working directory for the final image
WORKDIR /app

# Copy ONLY the built JAR file from the 'builder' stage above
# (The *-all.jar is the typical naming convention for Ktor fat jars)
COPY --from=builder /app/build/libs/*-all.jar ./ktor-backend.jar

# Tell Docker that this container listens on port 8080
EXPOSE 9090

# The command that runs when the container starts
CMD ["java", "-jar", "ktor-backend.jar"]
