# ── Stage 1: Build the Spring Boot application ──
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build JAR package (skipping test compilation completely)
RUN mvn clean package -Dmaven.test.skip=true

# ── Stage 2: Minimal runtime image ──
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080 8081

ENTRYPOINT ["java", "-jar", "app.jar"]