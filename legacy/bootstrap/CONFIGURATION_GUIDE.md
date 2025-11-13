# FileFlow 설정 가이드

## 📋 목차

1. [개요](#개요)
2. [설정 파일 구조](#설정-파일-구조)
3. [환경별 설정](#환경별-설정)
4. [데이터베이스 설정](#데이터베이스-설정)
5. [Prometheus 메트릭](#prometheus-메트릭)
6. [CloudWatch 로그](#cloudwatch-로그)
7. [환경변수 가이드](#환경변수-가이드)

---

## 개요

FileFlow는 세 개의 독립적인 실행 가능한 애플리케이션으로 구성되어 있습니다:

| 애플리케이션 | 설명 | 포트 |
|------------|------|------|
| **bootstrap-web-api** | REST API 서버 | 8083 |
| **bootstrap-scheduler-download** | 다운로드 스케줄러 (백그라운드) | 9091 (Actuator) |
| **bootstrap-scheduler-pipeline** | 파이프라인 스케줄러 (백그라운드) | 9092 (Actuator) |

각 애플리케이션은:
- ✅ **독립적인 DB 설정** 지원
- ✅ **Prometheus 메트릭** 노출
- ✅ **CloudWatch 로그** 연동
- ✅ **환경별 프로파일** (local, prod)

---

## 설정 파일 구조

### Bootstrap 모듈

```
bootstrap/
├── bootstrap-web-api/
│   └── src/main/resources/
│       ├── application.yml              # 공통 설정
│       ├── application-local.yml        # 로컬 환경
│       ├── application-prod.yml         # 프로덕션 환경
│       ├── application-docker.yml       # Docker 환경 (기존)
│       └── logback-spring.xml           # 로그 설정
│
├── bootstrap-scheduler-download/
│   └── src/main/resources/
│       ├── application.yml              # 공통 설정
│       ├── application-local.yml        # 로컬 환경
│       ├── application-prod.yml         # 프로덕션 환경
│       └── logback-spring.xml           # 로그 설정
│
└── bootstrap-scheduler-pipeline/
    └── src/main/resources/
        ├── application.yml              # 공통 설정
        ├── application-local.yml        # 로컬 환경
        ├── application-prod.yml         # 프로덕션 환경
        └── logback-spring.xml           # 로그 설정
```

### Adapter 모듈

```
adapter-out/persistence-mysql/
└── src/main/resources/
    ├── application.yml                  # 공통 설정
    ├── application-local.yml            # 로컬 환경
    └── application-prod.yml             # 프로덕션 환경

adapter-in/rest-api/
└── src/main/resources/
    └── application.yml                  # API 엔드포인트 설정 (공통)
```

---

## 환경별 설정

### 프로파일 활성화

```bash
# 로컬 환경
export SPRING_PROFILES_ACTIVE=local

# 프로덕션 환경
export SPRING_PROFILES_ACTIVE=prod

# Docker 환경 (Web API만)
export SPRING_PROFILES_ACTIVE=docker
```

### Local 환경 특징

- 🔧 **개발 편의성** 우선
- 📊 **상세한 로깅** (DEBUG, TRACE)
- 🔍 **모든 Actuator 엔드포인트** 노출
- 🧹 **Flyway clean** 허용
- 💾 **작은 커넥션 풀** 크기
- 📝 **콘솔 + 파일 로깅**

### Production 환경 특징

- 🔒 **보안** 우선
- 📈 **성능 최적화**
- 🚨 **최소 로깅** (WARN, INFO)
- 🔐 **환경변수로 민감 정보 주입**
- 💪 **큰 커넥션 풀** 크기
- ☁️ **JSON 로그 → CloudWatch**

---

## 데이터베이스 설정

### 1. Web API

#### Local 환경
```yaml
# application-local.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/fileflow?...
    username: fileflow_user
    password: fileflow_password
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

#### Production 환경
```bash
# 환경변수 설정
export DB_URL="jdbc:mysql://prod-db-host:3306/fileflow?..."
export DB_USERNAME="fileflow_prod_user"
export DB_PASSWORD="secure_password"
export DB_POOL_MAX_SIZE=50
export DB_POOL_MIN_IDLE=10
```

### 2. Download Scheduler

#### Local 환경
```yaml
# application-local.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/fileflow?...
    username: fileflow_user
    password: fileflow_password
    hikari:
      maximum-pool-size: 10
      minimum-idle: 3
```

#### Production 환경
```bash
# 환경변수 설정 (독립적인 DB 사용 가능)
export DOWNLOAD_DB_URL="jdbc:mysql://scheduler-db-host:3306/fileflow?..."
export DOWNLOAD_DB_USERNAME="scheduler_user"
export DOWNLOAD_DB_PASSWORD="secure_password"
export DOWNLOAD_DB_POOL_MAX_SIZE=15
export DOWNLOAD_DB_POOL_MIN_IDLE=5
```

### 3. Pipeline Scheduler

#### Local 환경
```yaml
# application-local.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/fileflow?...
    username: fileflow_user
    password: fileflow_password
    hikari:
      maximum-pool-size: 10
      minimum-idle: 3
```

#### Production 환경
```bash
# 환경변수 설정 (독립적인 DB 사용 가능)
export PIPELINE_DB_URL="jdbc:mysql://scheduler-db-host:3306/fileflow?..."
export PIPELINE_DB_USERNAME="scheduler_user"
export PIPELINE_DB_PASSWORD="secure_password"
export PIPELINE_DB_POOL_MAX_SIZE=15
export PIPELINE_DB_POOL_MIN_IDLE=5
```

### 동일한 DB 사용 시

세 애플리케이션이 동일한 DB를 사용하는 경우:

```bash
# 공통 환경변수 설정
export DB_URL="jdbc:mysql://db-host:3306/fileflow?..."
export DB_USERNAME="fileflow_user"
export DB_PASSWORD="secure_password"

# 각 애플리케이션에서 동일한 변수 참조
export DOWNLOAD_DB_URL=$DB_URL
export DOWNLOAD_DB_USERNAME=$DB_USERNAME
export DOWNLOAD_DB_PASSWORD=$DB_PASSWORD

export PIPELINE_DB_URL=$DB_URL
export PIPELINE_DB_USERNAME=$DB_USERNAME
export PIPELINE_DB_PASSWORD=$DB_PASSWORD
```

---

## Prometheus 메트릭

### 메트릭 엔드포인트

| 애플리케이션 | 엔드포인트 | 포트 |
|------------|-----------|------|
| Web API | `http://localhost:8083/actuator/prometheus` | 8083 |
| Download Scheduler | `http://localhost:9091/actuator/prometheus` | 9091 |
| Pipeline Scheduler | `http://localhost:9092/actuator/prometheus` | 9092 |

### Prometheus 스크랩 설정

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'fileflow-web-api'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['web-api:8083']
        labels:
          application: 'fileflow-web-api'
          service: 'web-api'
          environment: 'production'

  - job_name: 'fileflow-scheduler-download'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['scheduler-download:9091']
        labels:
          application: 'fileflow-scheduler-download'
          service: 'download-scheduler'
          environment: 'production'

  - job_name: 'fileflow-scheduler-pipeline'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['scheduler-pipeline:9092']
        labels:
          application: 'fileflow-scheduler-pipeline'
          service: 'pipeline-scheduler'
          environment: 'production'
```

### 주요 메트릭

- `jvm_memory_used_bytes` - JVM 메모리 사용량
- `hikaricp_connections_active` - 활성 DB 커넥션 수
- `http_server_requests_seconds` - HTTP 요청 처리 시간
- `logback_events_total` - 로그 이벤트 수

---

## CloudWatch 로그

### 로그 구성

모든 애플리케이션은 **Logstash JSON Encoder**를 사용하여 구조화된 로그를 출력합니다.

#### Local 환경
- 📝 **일반 텍스트 로그** (콘솔 + 파일)
- 🔍 **상세한 로그 레벨** (DEBUG, TRACE)

#### Production 환경
- 📊 **JSON 로그** (콘솔 → CloudWatch)
- ☁️ **ECS/Fargate에서 자동 수집**
- 🏷️ **메타데이터 포함** (traceId, spanId, etc.)

### CloudWatch 로그 그룹

| 애플리케이션 | 로그 그룹 | 로그 스트림 |
|------------|----------|-----------|
| Web API | `/aws/ecs/fileflow-web-api` | `web-api-{HOSTNAME}` |
| Download Scheduler | `/aws/ecs/fileflow-scheduler-download` | `download-scheduler-{HOSTNAME}` |
| Pipeline Scheduler | `/aws/ecs/fileflow-scheduler-pipeline` | `pipeline-scheduler-{HOSTNAME}` |

### JSON 로그 예시

```json
{
  "@timestamp": "2025-11-03T10:30:45.123+09:00",
  "level": "INFO",
  "logger_name": "com.ryuqq.fileflow.application.download.DownloadService",
  "message": "Download completed successfully",
  "application": "fileflow-scheduler-download",
  "service": "download-scheduler",
  "traceId": "abc123",
  "spanId": "def456",
  "downloadId": "12345"
}
```

---

## 환경변수 가이드

### Web API (Production)

```bash
# ========================================
# Spring Profile
# ========================================
SPRING_PROFILES_ACTIVE=prod

# ========================================
# Database
# ========================================
DB_URL=jdbc:mysql://prod-db.example.com:3306/fileflow?useSSL=true&serverTimezone=UTC
DB_USERNAME=fileflow_prod
DB_PASSWORD=secure_password_here
DB_POOL_MAX_SIZE=50
DB_POOL_MIN_IDLE=10

# ========================================
# Redis
# ========================================
REDIS_HOST=redis.example.com
REDIS_PORT=6379
REDIS_PASSWORD=redis_password_here

# ========================================
# AWS
# ========================================
AWS_REGION=ap-northeast-2
AWS_S3_BUCKET=fileflow-prod-bucket
AWS_CLOUDWATCH_LOG_GROUP=/aws/ecs/fileflow-web-api
AWS_CLOUDWATCH_LOG_STREAM=web-api-${HOSTNAME}

# ========================================
# Server
# ========================================
SERVER_PORT=8083
```

### Download Scheduler (Production)

```bash
# ========================================
# Spring Profile
# ========================================
SPRING_PROFILES_ACTIVE=prod

# ========================================
# Database (독립적인 DB 사용 가능)
# ========================================
DOWNLOAD_DB_URL=jdbc:mysql://scheduler-db.example.com:3306/fileflow?useSSL=true&serverTimezone=UTC
DOWNLOAD_DB_USERNAME=scheduler_user
DOWNLOAD_DB_PASSWORD=secure_password_here
DOWNLOAD_DB_POOL_MAX_SIZE=15
DOWNLOAD_DB_POOL_MIN_IDLE=5

# ========================================
# Redis
# ========================================
REDIS_HOST=redis.example.com
REDIS_PORT=6379
REDIS_PASSWORD=redis_password_here

# ========================================
# AWS
# ========================================
AWS_REGION=ap-northeast-2
AWS_S3_BUCKET=fileflow-prod-bucket
AWS_CLOUDWATCH_LOG_GROUP=/aws/ecs/fileflow-scheduler-download
AWS_CLOUDWATCH_LOG_STREAM=download-scheduler-${HOSTNAME}

# ========================================
# Scheduler Configuration
# ========================================
DOWNLOAD_SCHEDULER_FIXED_DELAY=30000
DOWNLOAD_SCHEDULER_INITIAL_DELAY=10000
DOWNLOAD_SCHEDULER_BATCH_SIZE=10
DOWNLOAD_SCHEDULER_MAX_RETRY=3

# ========================================
# Actuator
# ========================================
ACTUATOR_PORT=9091
```

### Pipeline Scheduler (Production)

```bash
# ========================================
# Spring Profile
# ========================================
SPRING_PROFILES_ACTIVE=prod

# ========================================
# Database (독립적인 DB 사용 가능)
# ========================================
PIPELINE_DB_URL=jdbc:mysql://scheduler-db.example.com:3306/fileflow?useSSL=true&serverTimezone=UTC
PIPELINE_DB_USERNAME=scheduler_user
PIPELINE_DB_PASSWORD=secure_password_here
PIPELINE_DB_POOL_MAX_SIZE=15
PIPELINE_DB_POOL_MIN_IDLE=5

# ========================================
# Redis
# ========================================
REDIS_HOST=redis.example.com
REDIS_PORT=6379
REDIS_PASSWORD=redis_password_here

# ========================================
# AWS
# ========================================
AWS_REGION=ap-northeast-2
AWS_S3_BUCKET=fileflow-prod-bucket
AWS_CLOUDWATCH_LOG_GROUP=/aws/ecs/fileflow-scheduler-pipeline
AWS_CLOUDWATCH_LOG_STREAM=pipeline-scheduler-${HOSTNAME}

# ========================================
# Scheduler Configuration
# ========================================
PIPELINE_SCHEDULER_FIXED_DELAY=30000
PIPELINE_SCHEDULER_INITIAL_DELAY=10000
PIPELINE_SCHEDULER_BATCH_SIZE=10
PIPELINE_SCHEDULER_MAX_RETRY=3

# ========================================
# Actuator
# ========================================
ACTUATOR_PORT=9092
```

---

## 실행 방법

### Local 환경

```bash
# Web API
./gradlew :bootstrap:bootstrap-web-api:bootRun

# Download Scheduler
./gradlew :bootstrap:bootstrap-scheduler-download:bootRun

# Pipeline Scheduler
./gradlew :bootstrap:bootstrap-scheduler-pipeline:bootRun
```

### Production 환경 (JAR)

```bash
# Web API
java -jar \
  -Dspring.profiles.active=prod \
  -Xms512m -Xmx1024m \
  fileflow-web-api.jar

# Download Scheduler
java -jar \
  -Dspring.profiles.active=prod \
  -Xms256m -Xmx512m \
  fileflow-scheduler-download.jar

# Pipeline Scheduler
java -jar \
  -Dspring.profiles.active=prod \
  -Xms256m -Xmx512m \
  fileflow-scheduler-pipeline.jar
```

### Docker 환경

```bash
# Web API
docker run -d \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=... \
  -e DB_USERNAME=... \
  -e DB_PASSWORD=... \
  -p 8083:8083 \
  fileflow-web-api:latest

# Download Scheduler
docker run -d \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DOWNLOAD_DB_URL=... \
  -e DOWNLOAD_DB_USERNAME=... \
  -e DOWNLOAD_DB_PASSWORD=... \
  -p 9091:9091 \
  fileflow-scheduler-download:latest

# Pipeline Scheduler
docker run -d \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e PIPELINE_DB_URL=... \
  -e PIPELINE_DB_USERNAME=... \
  -e PIPELINE_DB_PASSWORD=... \
  -p 9092:9092 \
  fileflow-scheduler-pipeline:latest
```

---

## 헬스체크

### Web API
```bash
curl http://localhost:8083/actuator/health
```

### Download Scheduler
```bash
curl http://localhost:9091/actuator/health
```

### Pipeline Scheduler
```bash
curl http://localhost:9092/actuator/health
```

---

## 트러블슈팅

### 1. DB 커넥션 에러

**증상**: `HikariPool - Connection is not available`

**해결**:
- 커넥션 풀 크기 증가: `DB_POOL_MAX_SIZE` 환경변수 조정
- DB 서버 `max_connections` 확인
- 커넥션 누수 확인: `leak-detection-threshold` 로그 확인

### 2. Prometheus 메트릭 수집 안됨

**증상**: Prometheus에서 타겟이 DOWN 상태

**해결**:
- Actuator 포트 확인 (스케줄러: 9091, 9092)
- 방화벽/보안그룹 설정 확인
- `/actuator/prometheus` 엔드포인트 직접 접근 테스트

### 3. CloudWatch 로그 안보임

**증상**: CloudWatch에 로그가 쌓이지 않음

**해결**:
- ECS Task Role에 CloudWatch Logs 권한 확인
- 로그 그룹/스트림 이름 확인
- `logback-spring.xml` 설정 확인
- `SPRING_PROFILES_ACTIVE=prod` 설정 확인

---

## 참고 자료

- [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [Prometheus Metrics](https://prometheus.io/docs/introduction/overview/)
- [AWS CloudWatch Logs](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/WhatIsCloudWatchLogs.html)
