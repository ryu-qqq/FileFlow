# ========================================
# FileFlow Resizing Worker Dockerfile
# ========================================
# Based on Eclipse Temurin JRE 21 Alpine
# Metrics: ADOT Sidecar (Prometheus scrape)
# ========================================

FROM eclipse-temurin:21-jre-alpine

# Install wget for health checks
RUN apk add --no-cache wget

# Create non-root user for security
RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

# Copy the JAR file
COPY bootstrap/bootstrap-resizing-worker/build/libs/fileflow-resizing-worker-*.jar app.jar

# Set ownership
RUN chown -R app:app /app

# Switch to non-root user
USER app

# JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Expose management port (8083 for resizing worker)
EXPOSE 8083

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
