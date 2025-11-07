# ============================================================================
# Multi-Bootstrap Dockerfile for FileFlow
# ============================================================================
# Build Arguments로 Bootstrap 선택 가능
# Usage: docker build --build-arg BOOTSTRAP_NAME=web-api .
# ============================================================================

# Build Arguments (기본값: web-api)
ARG BOOTSTRAP_NAME=web-api
ARG EXPOSE_PORT=8080
ARG HEALTH_CHECK_PATH=/actuator/health

# ============================================================================
# Stage 1: Build (빌드 환경)
# ============================================================================
FROM eclipse-temurin:21-jdk-jammy AS builder

ARG BOOTSTRAP_NAME

LABEL stage=builder
LABEL maintainer="platform-team@ryuqqq.com"
LABEL bootstrap=${BOOTSTRAP_NAME}

WORKDIR /build

# ========================================
# Step 1: Gradle Wrapper 복사 및 권한 설정
# ========================================
COPY gradlew .
COPY gradle gradle/
RUN chmod +x gradlew

# ========================================
# Step 2: 의존성 파일만 먼저 복사 (캐싱 최적화)
# ========================================
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/libs.versions.toml gradle/

# 모든 모듈의 build.gradle.kts 복사 (의존성 다운로드용)
COPY domain/build.gradle.kts domain/
COPY application/build.gradle.kts application/
COPY adapter-in/rest-api/build.gradle.kts adapter-in/rest-api/
COPY adapter-in/scheduler/build.gradle.kts adapter-in/scheduler/
COPY adapter-out/build.gradle.kts adapter-out/
COPY adapter-out/persistence-mysql/build.gradle.kts adapter-out/persistence-mysql/
COPY adapter-out/persistence-redis/build.gradle.kts adapter-out/persistence-redis/
COPY adapter-out/abac-cel/build.gradle.kts adapter-out/abac-cel/
COPY adapter-out/aws-s3/build.gradle.kts adapter-out/aws-s3/
COPY adapter-out/http-client/build.gradle.kts adapter-out/http-client/
COPY adapter-out/image-processor/build.gradle.kts adapter-out/image-processor/
COPY adapter-out/metadata-extractor/build.gradle.kts adapter-out/metadata-extractor/
COPY bootstrap/bootstrap-web-api/build.gradle.kts bootstrap/bootstrap-web-api/
COPY bootstrap/bootstrap-scheduler-download/build.gradle.kts bootstrap/bootstrap-scheduler-download/
COPY bootstrap/bootstrap-scheduler-pipeline/build.gradle.kts bootstrap/bootstrap-scheduler-pipeline/
COPY bootstrap/bootstrap-scheduler-upload/build.gradle.kts bootstrap/bootstrap-scheduler-upload/

# ========================================
# Step 3: 의존성 다운로드 (캐싱)
# ========================================
RUN ./gradlew dependencies --no-daemon --stacktrace || true

# ========================================
# Step 4: 소스 코드 복사
# ========================================
COPY domain/src domain/src/
COPY application/src application/src/
COPY adapter-in/rest-api/src adapter-in/rest-api/src/
COPY adapter-in/scheduler/src adapter-in/scheduler/src/
COPY adapter-out/persistence-mysql/src adapter-out/persistence-mysql/src/
COPY adapter-out/persistence-redis/src adapter-out/persistence-redis/src/
COPY adapter-out/abac-cel/src adapter-out/abac-cel/src/
COPY adapter-out/aws-s3/src adapter-out/aws-s3/src/
COPY adapter-out/http-client/src adapter-out/http-client/src/
COPY adapter-out/image-processor/src adapter-out/image-processor/src/
COPY adapter-out/metadata-extractor/src adapter-out/metadata-extractor/src/
COPY bootstrap/bootstrap-web-api/src bootstrap/bootstrap-web-api/src/
COPY bootstrap/bootstrap-scheduler-download/src bootstrap/bootstrap-scheduler-download/src/
COPY bootstrap/bootstrap-scheduler-pipeline/src bootstrap/bootstrap-scheduler-pipeline/src/
COPY bootstrap/bootstrap-scheduler-upload/src bootstrap/bootstrap-scheduler-upload/src/

# ========================================
# Step 5: Checkstyle, SpotBugs 설정 파일
# ========================================
COPY config/ config/

# ========================================
# Step 6: 동적 Bootstrap 빌드 (Build Argument 사용)
# ========================================
RUN echo "========================================" && \
    echo "Building Bootstrap: ${BOOTSTRAP_NAME}" && \
    echo "========================================" && \
    ./gradlew clean :bootstrap:bootstrap-${BOOTSTRAP_NAME}:bootJar \
    -x test \
    -x checkstyleMain \
    -x checkstyleTest \
    -x spotbugsMain \
    -x spotbugsTest \
    --no-daemon \
    --stacktrace && \
    echo "✅ Build completed for ${BOOTSTRAP_NAME}"

# 빌드된 JAR 파일 확인
RUN ls -lh bootstrap/bootstrap-${BOOTSTRAP_NAME}/build/libs/

# ============================================================================
# Stage 2: Runtime (실행 환경)
# ============================================================================
FROM eclipse-temurin:21-jre-alpine

ARG BOOTSTRAP_NAME
ARG EXPOSE_PORT
ARG HEALTH_CHECK_PATH

LABEL maintainer="platform-team@ryuqqq.com"
LABEL description="FileFlow ${BOOTSTRAP_NAME} - Spring Boot Application"
LABEL version="1.0.0-SNAPSHOT"
LABEL bootstrap=${BOOTSTRAP_NAME}

# ========================================
# 애플리케이션 디렉토리 생성
# ========================================
WORKDIR /app

# ========================================
# Non-root 사용자 생성 (보안 강화)
# ========================================
RUN addgroup -S spring && \
    adduser -S spring -G spring && \
    chown -R spring:spring /app

# ========================================
# 시스템 의존성 설치
# ========================================
RUN apk add --no-cache \
    curl \
    tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# ========================================
# JAR 파일 복사 (동적 Bootstrap)
# ========================================
COPY --from=builder --chown=spring:spring \
    /build/bootstrap/bootstrap-${BOOTSTRAP_NAME}/build/libs/*.jar \
    /app/application.jar

# ========================================
# Non-root 사용자로 전환
# ========================================
USER spring:spring

# ========================================
# 포트 노출 (동적)
# ========================================
EXPOSE ${EXPOSE_PORT}

# ========================================
# Health Check 설정 (동적)
# ========================================
HEALTHCHECK --interval=30s \
            --timeout=3s \
            --retries=3 \
            --start-period=60s \
    CMD curl -f http://localhost:${EXPOSE_PORT}${HEALTH_CHECK_PATH} || exit 1

# ========================================
# JVM 옵션 설정
# ========================================
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+PrintFlagsFinal \
               -Djava.security.egd=file:/dev/./urandom"

# ========================================
# 애플리케이션 실행
# ========================================
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/application.jar"]
