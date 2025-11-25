# ==========================================
# PHASE 1: The Factory (Building the App)
# ==========================================
# Updated to JDK 21 to match your Gradle configuration
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test

# ==========================================
# PHASE 2: The Delivery Truck (Running the App)
# ==========================================
# Updated to JRE 21 to match the builder
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]