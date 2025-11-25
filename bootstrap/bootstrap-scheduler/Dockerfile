# ========================================
# FileFlow Scheduler Dockerfile
# ========================================
# Multi-stage build for optimized image size
# Based on Eclipse Temurin JRE 21 Alpine
# ========================================

FROM eclipse-temurin:21-jre-alpine

# Create non-root user for security
RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

# Copy the JAR file
COPY bootstrap/bootstrap-scheduler/build/libs/fileflow-scheduler-*.jar app.jar

# Set ownership
RUN chown -R app:app /app

# Switch to non-root user
USER app

# JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
