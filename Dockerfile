# ============================================================================
# Multi-Stage Dockerfile for FileFlow
# ============================================================================
# Stage 1: Build (빌드 환경)
# - JDK 21 전체 포함
# - Gradle 의존성 캐싱
# - 소스 코드 빌드
# ============================================================================
FROM eclipse-temurin:21-jdk-jammy AS builder

LABEL stage=builder
LABEL maintainer="platform-team@ryuqqq.com"

WORKDIR /build

# ========================================
# Step 1: Gradle Wrapper 복사 및 권한 설정
# ========================================
COPY gradlew .
COPY gradle gradle/
RUN chmod +x gradlew

# ========================================
# Step 2: 의존성 파일만 먼저 복사 (캐싱 최적화)
# 의존성이 변경되지 않으면 이 레이어는 캐시됨
# ========================================
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/libs.versions.toml gradle/

# 멀티 모듈 프로젝트: 각 모듈의 build.gradle.kts 복사
COPY domain/build.gradle.kts domain/
COPY application/build.gradle.kts application/
COPY adapter/adapter-in-rest-api/build.gradle.kts adapter/adapter-in-rest-api/
COPY adapter/adapter-out-persistence-jpa/build.gradle.kts adapter/adapter-out-persistence-jpa/
COPY adapter/adapter-out-redis/build.gradle.kts adapter/adapter-out-redis/
COPY adapter/adapter-out-aws-s3/build.gradle.kts adapter/adapter-out-aws-s3/
COPY adapter/adapter-out-aws-sqs/build.gradle.kts adapter/adapter-out-aws-sqs/
COPY adapter/adapter-out-aws-textract/build.gradle.kts adapter/adapter-out-aws-textract/
COPY adapter/adapter-out-metadata-extraction/build.gradle.kts adapter/adapter-out-metadata-extraction/
COPY adapter/adapter-out-image-conversion/build.gradle.kts adapter/adapter-out-image-conversion/
COPY bootstrap/bootstrap-web-api/build.gradle.kts bootstrap/bootstrap-web-api/

# ========================================
# Step 3: 의존성 다운로드 (캐싱)
# 코드 변경 시에도 의존성은 재사용됨
# ========================================
RUN ./gradlew dependencies --no-daemon --stacktrace || true

# ========================================
# Step 4: 소스 코드 복사
# 코드가 변경되면 이 레이어부터 재빌드됨
# 현재 존재하는 모듈만 복사 (향후 추가 모듈은 src 생성 후 추가)
# ========================================
COPY domain/src domain/src/
COPY application/src application/src/
COPY adapter/adapter-out-persistence-jpa/src adapter/adapter-out-persistence-jpa/src/
COPY bootstrap/bootstrap-web-api/src bootstrap/bootstrap-web-api/src/

# TODO: 아래 모듈들은 src 디렉토리 생성 후 주석 해제
# COPY adapter/adapter-in-rest-api/src adapter/adapter-in-rest-api/src/
# COPY adapter/adapter-out-redis/src adapter/adapter-out-redis/src/
# COPY adapter/adapter-out-aws-s3/src adapter/adapter-out-aws-s3/src/
# COPY adapter/adapter-out-aws-sqs/src adapter/adapter-out-aws-sqs/src/
# COPY adapter/adapter-out-aws-textract/src adapter/adapter-out-aws-textract/src/
# COPY adapter/adapter-out-metadata-extraction/src adapter/adapter-out-metadata-extraction/src/
# COPY adapter/adapter-out-image-conversion/src adapter/adapter-out-image-conversion/src/

# ========================================
# Step 5: Checkstyle, SpotBugs 설정 파일 복사
# ========================================
COPY config/ config/

# ========================================
# Step 6: 애플리케이션 빌드
# 테스트는 별도 CI 단계에서 실행하므로 스킵
# ========================================
RUN ./gradlew :bootstrap:bootstrap-web-api:bootJar \
    -x test \
    -x checkstyleMain \
    -x checkstyleTest \
    -x spotbugsMain \
    -x spotbugsTest \
    --no-daemon \
    --stacktrace

# 빌드된 JAR 파일 확인
RUN ls -lh bootstrap/bootstrap-web-api/build/libs/

# ============================================================================
# Stage 2: Runtime (실행 환경)
# - JRE만 포함 (JDK 불필요)
# - Alpine Linux로 이미지 크기 최소화
# - Non-root 사용자로 보안 강화
# ============================================================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="platform-team@ryuqqq.com"
LABEL description="FileFlow API Server - Spring Boot Application"
LABEL version="1.0.0-SNAPSHOT"

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
# - curl: Health check용
# - tzdata: 타임존 설정용
# ========================================
RUN apk add --no-cache \
    curl \
    tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# ========================================
# JAR 파일 복사 (빌드 스테이지에서)
# ========================================
COPY --from=builder --chown=spring:spring \
    /build/bootstrap/bootstrap-web-api/build/libs/*.jar \
    /app/application.jar

# ========================================
# Non-root 사용자로 전환
# ========================================
USER spring:spring

# ========================================
# 포트 노출
# ========================================
EXPOSE 8080

# ========================================
# Health Check 설정
# - 30초마다 체크
# - 3초 타임아웃
# - 3회 실패 시 unhealthy
# - 60초 시작 대기 시간 (Spring Boot 시작 시간 고려)
# ========================================
HEALTHCHECK --interval=30s \
            --timeout=3s \
            --retries=3 \
            --start-period=60s \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# ========================================
# JVM 옵션 설정
# - 메모리 효율 최적화
# - GC 로깅
# - OOM 시 힙 덤프
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
