# ---------- Stage 1: Build ----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy only the POM first to leverage Docker layer caching for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy the rest of the source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Stage 2: Run ----------
FROM eclipse-temurin:17-jre-jammy AS run

# Run as a non-root user
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Default Spring Boot port
EXPOSE 8080

# Allow overriding JVM options and Spring profile at runtime
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE="prod"

# Basic container healthcheck (requires Spring Boot Actuator; remove if not using it)
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar app.jar"]
