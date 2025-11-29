# ========================================
# FileFlow Web API Dockerfile
# ========================================
# Multi-stage build for optimized image size
# Based on Eclipse Temurin JRE 21 Alpine
# With OpenTelemetry Java Agent for observability
# ========================================

FROM eclipse-temurin:21-jre-alpine

# Create non-root user for security
RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

# Download AWS ADOT Java Agent for OpenTelemetry
ADD https://github.com/aws-observability/aws-otel-java-instrumentation/releases/latest/download/aws-opentelemetry-agent.jar /app/aws-opentelemetry-agent.jar

# Copy the JAR file
COPY bootstrap/bootstrap-web-api/build/libs/fileflow-api-*.jar app.jar

# Set ownership
RUN chown -R app:app /app

# Switch to non-root user
USER app

# JVM options for container environment with OTel agent
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"
ENV OTEL_AGENT_OPTS="-javaagent:/app/aws-opentelemetry-agent.jar"

# OpenTelemetry configuration (can be overridden by ECS Task Definition)
ENV OTEL_SERVICE_NAME="fileflow-web-api"
ENV OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4317"
ENV OTEL_EXPORTER_OTLP_PROTOCOL="grpc"
# OTLP Exporters - ECS Task에 otel-collector sidecar 배포됨
ENV OTEL_METRICS_EXPORTER="otlp"
ENV OTEL_TRACES_EXPORTER="otlp"
ENV OTEL_LOGS_EXPORTER="none"
ENV OTEL_PROPAGATORS="xray,tracecontext,baggage"
ENV OTEL_RESOURCE_ATTRIBUTES="service.namespace=fileflow"

# Expose port
EXPOSE 8080

# Health check (increased timeouts for Spring Boot initialization)
HEALTHCHECK --interval=30s --timeout=10s --start-period=180s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application with OTel agent
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS $OTEL_AGENT_OPTS -jar app.jar"]
