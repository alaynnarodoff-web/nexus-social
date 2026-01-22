# 1. Base image
FROM gradle:8.5-jdk21

WORKDIR /app

# 2. Copy files
COPY build.gradle settings.gradle ./
COPY src ./src

# 3. Build the specific Spring Boot Jar
# We use 'bootJar' instead of 'build' to ensure we get the executable file
RUN gradle clean bootJar -x test

# 4. The Magic Step: Find the jar and rename it to 'app.jar'
# This handles the version number changing or weird folder structures
RUN mv build/libs/*SNAPSHOT.jar app.jar

# 5. Run the renamed file
# Now we don't need wildcards because we know the file is named 'app.jar'
CMD ["java", "-jar", "app.jar"]
