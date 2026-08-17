# ---------- Stage 1: build with Maven + JDK 21 ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies first: this layer only rebuilds when pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
# Tests run locally via `mvn test`; skipped here so the image build stays fast
# and doesn't need Docker-in-Docker (your integration tests need a DB).
RUN mvn clean package -DskipTests -B

# ---------- Stage 2: slim JRE runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a non-root user (security best practice)
RUN groupadd -r app && useradd -r -g app -u 1001 appuser

COPY --from=build /app/target/*.jar app.jar

USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]