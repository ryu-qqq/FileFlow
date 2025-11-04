# ================================================================
# Fileflow Scheduler Pipeline Dockerfile
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
COPY adapter-out/image-processor/build.gradle.kts adapter-out/image-processor/
COPY adapter-out/metadata-extractor/build.gradle.kts adapter-out/metadata-extractor/
COPY bootstrap/bootstrap-scheduler-pipeline/build.gradle.kts bootstrap/bootstrap-scheduler-pipeline/

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY domain/src domain/src
COPY application/src application/src
COPY adapter-out/persistence-mysql/src adapter-out/persistence-mysql/src
COPY adapter-out/aws-s3/src adapter-out/aws-s3/src
COPY adapter-out/image-processor/src adapter-out/image-processor/src
COPY adapter-out/metadata-extractor/src adapter-out/metadata-extractor/src
COPY bootstrap/bootstrap-scheduler-pipeline/src bootstrap/bootstrap-scheduler-pipeline/src

# Build application (clean first to avoid QueryDSL Q-class conflicts)
RUN ./gradlew clean :bootstrap:bootstrap-scheduler-pipeline:bootJar --no-daemon

# ================================================================
# Stage 2: Runtime
# ================================================================
FROM eclipse-temurin:21-jre-jammy

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/bootstrap/bootstrap-scheduler-pipeline/build/libs/*.jar app.jar

# Set JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
