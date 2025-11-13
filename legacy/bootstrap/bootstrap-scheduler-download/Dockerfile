# ================================================================
# Fileflow Scheduler Download Dockerfile
# ================================================================
# Multi-stage build for optimal image size
# Stage 1: Build
# Stage 2: Runtime
# ================================================================

# ================================================================
# Stage 1: Build
# ================================================================
FROM gradle:8.5-jdk21 AS build

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and configuration files
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY gradle.properties .

# Copy all module build files
COPY build.gradle.kts .
COPY domain/build.gradle.kts domain/
COPY application/build.gradle.kts application/
COPY adapter-out/persistence-mysql/build.gradle.kts adapter-out/persistence-mysql/
COPY adapter-out/aws-s3/build.gradle.kts adapter-out/aws-s3/
COPY bootstrap/bootstrap-scheduler-download/build.gradle.kts bootstrap/bootstrap-scheduler-download/

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY domain/src domain/src
COPY application/src application/src
COPY adapter-out/persistence-mysql/src adapter-out/persistence-mysql/src
COPY adapter-out/aws-s3/src adapter-out/aws-s3/src
COPY bootstrap/bootstrap-scheduler-download/src bootstrap/bootstrap-scheduler-download/src

# Build application (clean first to avoid QueryDSL Q-class conflicts)
RUN ./gradlew clean :bootstrap:bootstrap-scheduler-download:bootJar --no-daemon

# ================================================================
# Stage 2: Runtime
# ================================================================
FROM eclipse-temurin:21-jre-jammy

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/bootstrap/bootstrap-scheduler-download/build/libs/*.jar app.jar

# Set JVM options
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
