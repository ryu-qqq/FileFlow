# ========================================
# FileFlow Download Worker Dockerfile
# ========================================
# Multi-stage build for optimized image size
# Based on Eclipse Temurin JRE 21 Alpine
# With OpenTelemetry Java Agent for observability
# ========================================

FROM eclipse-temurin:21-jre-alpine

# Install wget for health checks
RUN apk add --no-cache wget

# Create non-root user for security
RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

# Download AWS ADOT Java Agent for OpenTelemetry
ADD https://github.com/aws-observability/aws-otel-java-instrumentation/releases/latest/download/aws-opentelemetry-agent.jar /app/aws-opentelemetry-agent.jar

# Copy the JAR file
COPY bootstrap/bootstrap-download-worker/build/libs/fileflow-download-worker-*.jar app.jar

# Set ownership
RUN chown -R app:app /app

# Switch to non-root user
USER app

# JVM options for container environment with OTel agent
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"
ENV OTEL_AGENT_OPTS="-javaagent:/app/aws-opentelemetry-agent.jar"

# OpenTelemetry configuration (can be overridden by ECS Task Definition)
ENV OTEL_SERVICE_NAME="fileflow-download-worker"
ENV OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4317"
ENV OTEL_EXPORTER_OTLP_PROTOCOL="grpc"
ENV OTEL_EXPORTER_OTLP_TIMEOUT="10000"
ENV OTEL_EXPERIMENTAL_EXPORTER_OTLP_RETRY_ENABLED="true"
# Metrics: ADOT Collector가 Prometheus로 PULL (중복 방지)
ENV OTEL_METRICS_EXPORTER="none"
# Traces: Java Agent가 OTLP로 PUSH
ENV OTEL_TRACES_EXPORTER="otlp"
ENV OTEL_LOGS_EXPORTER="none"
ENV OTEL_PROPAGATORS="xray,tracecontext,baggage"
ENV OTEL_RESOURCE_ATTRIBUTES="service.namespace=fileflow,deployment.environment=prod"

# Expose management port
EXPOSE 8082

# Run the application with OTel agent
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS $OTEL_AGENT_OPTS -jar app.jar"]
