# Sentry 에러 트래킹 통합 가이드

이 문서는 FileFlow 프로젝트에 Sentry 에러 트래킹을 설정하고 운영하는 방법을 설명합니다.

---

## 1. 개요

### 1.1 목표
- 프로덕션 환경의 에러를 실시간으로 수집 및 추적
- 에러 발생 시 traceId, userId 등 컨텍스트 자동 포함
- 코드 변경 없이 모든 `log.error()` 호출 자동 캡처

### 1.2 아키텍처

```
┌─────────────────────────────────────────────────────────────────────────┐
│  Application Entry Points                                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐         │
│  │   REST API      │  │  SQS Listener   │  │   Scheduler     │         │
│  │  (web-api)      │  │  (workers)      │  │  (scheduler)    │         │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘         │
│           │                    │                    │                   │
│           ▼                    ▼                    ▼                   │
│  ┌─────────────────────────────────────────────────────────────┐       │
│  │  GlobalException  │  handleException()  │  catch block      │       │
│  │  Handler          │  in Listener        │  in Scheduler     │       │
│  └─────────────────────────────────────────────────────────────┘       │
│                              │                                          │
│                              ▼                                          │
│  ┌─────────────────────────────────────────────────────────────┐       │
│  │               log.error(message, exception)                  │       │
│  │                      + MDC Context                           │       │
│  │          (traceId, spanId, userId, tenantId, ...)           │       │
│  └─────────────────────────────────────────────────────────────┘       │
│                              │                                          │
└──────────────────────────────┼──────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  Logback (logback-spring.xml)                                           │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────┐       │
│  │  SentryAppender (prod, staging profiles)                    │       │
│  │  - ThresholdFilter: ERROR level only                        │       │
│  │  - MDC 컨텍스트 자동 포함                                     │       │
│  └─────────────────────────────────────────────────────────────┘       │
│                              │                                          │
└──────────────────────────────┼──────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        Sentry Cloud                                     │
│  - Issues Dashboard                                                     │
│  - Performance Monitoring                                               │
│  - Alerts & Notifications                                               │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.3 핵심 원칙

| 원칙 | 설명 |
|------|------|
| **Zero-Code Capture** | `log.error()` 호출만으로 Sentry 전송 (추가 코드 불필요) |
| **Context Propagation** | MDC의 traceId, userId 등 자동 포함 |
| **Environment Isolation** | prod/staging만 활성화, local/test는 비활성화 |
| **Performance First** | 샘플링으로 성능 영향 최소화 |

---

## 2. Sentry 프로젝트 설정

### 2.1 Sentry 계정 생성

1. https://sentry.io 접속
2. 회원가입 또는 로그인
3. Organization 생성 (예: `ryuqq`)
4. Project 생성:
   - Platform: **Java** → **Spring Boot**
   - Project Name: `fileflow-api`, `fileflow-scheduler` 등

### 2.2 DSN 확인

프로젝트 생성 후 **Settings > Client Keys (DSN)** 에서 DSN 확인:

```
https://<public_key>@<organization_id>.ingest.us.sentry.io/<project_id>
```

**예시:**
```
https://db794698cabc2d95c2d7f5d77abe3f17@o4510661281644544.ingest.us.sentry.io/4510661296193536
```

### 2.3 요금제

| 플랜 | 월 이벤트 | 가격 |
|------|----------|------|
| Developer | 5,000 | 무료 |
| Team | 50,000 | $26/월 |
| Business | 100,000+ | $80/월~ |

> **권장**: 개발/스테이징은 Developer, 프로덕션은 Team 이상

---

## 3. 의존성 설정

### 3.1 gradle/libs.versions.toml

```toml
[versions]
sentry = "7.3.0"

[libraries]
# Sentry
sentry-spring-boot = { module = "io.sentry:sentry-spring-boot-starter-jakarta", version.ref = "sentry" }
sentry-logback = { module = "io.sentry:sentry-logback", version.ref = "sentry" }
```

### 3.2 각 Bootstrap 모듈 build.gradle

```gradle
dependencies {
    // Sentry Error Tracking
    implementation libs.sentry.spring.boot
    implementation libs.sentry.logback
}
```

**적용 대상 모듈:**
- `bootstrap/bootstrap-web-api`
- `bootstrap/bootstrap-scheduler`
- `bootstrap/bootstrap-download-worker`
- `bootstrap/bootstrap-resizing-worker`

---

## 4. Application 설정

### 4.1 application.yml

```yaml
# ===============================================
# Sentry Configuration (Error Tracking)
# ===============================================
sentry:
  # DSN - 환경변수에서 주입 (비어있으면 Sentry 비활성화)
  dsn: ${SENTRY_DSN:}

  # 환경 구분 (Issues 필터링에 사용)
  environment: ${SPRING_PROFILES_ACTIVE:local}

  # 릴리즈 버전 (배포 추적에 사용)
  release: ${APP_VERSION:unknown}

  # 서버 식별자 (인스턴스 구분)
  server-name: ${HOSTNAME:unknown}

  # 트레이스 샘플링 (0.0 ~ 1.0)
  # 프로덕션에서는 0.1 (10%) 권장
  traces-sample-rate: ${SENTRY_TRACES_SAMPLE_RATE:0.1}

  # 개인정보 전송 비활성화
  send-default-pii: false

  # 로깅 설정
  logging:
    # ERROR 레벨만 Sentry 이벤트로 전송
    minimum-event-level: error
    # INFO 레벨 이상은 Breadcrumb으로 기록
    minimum-breadcrumb-level: info
```

### 4.2 환경변수 설정

**로컬 개발 (.env 또는 IDE 설정):**
```bash
# 비워두면 Sentry 비활성화
SENTRY_DSN=
```

**프로덕션 (ECS Task Definition, K8s ConfigMap 등):**
```bash
SENTRY_DSN=https://xxx@xxx.ingest.us.sentry.io/xxx
SPRING_PROFILES_ACTIVE=prod
APP_VERSION=1.2.3
SENTRY_TRACES_SAMPLE_RATE=0.1
```

---

## 5. Logback 설정

### 5.1 logback-spring.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="30 seconds">

    <!-- ============================================ -->
    <!-- Production Profile (prod, staging)          -->
    <!-- ============================================ -->
    <springProfile name="prod,staging">

        <!-- Sentry Appender for Error Tracking -->
        <appender name="SENTRY" class="io.sentry.logback.SentryAppender">
            <!-- DSN is configured in application.yml -->
            <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
                <level>ERROR</level>
            </filter>
        </appender>

        <!-- JSON Structured Logging -->
        <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                <!-- MDC 필드 포함 -->
                <includeMdcKeyName>traceId</includeMdcKeyName>
                <includeMdcKeyName>spanId</includeMdcKeyName>
                <includeMdcKeyName>userId</includeMdcKeyName>
                <includeMdcKeyName>tenantId</includeMdcKeyName>
                <!-- ... -->
            </encoder>
        </appender>

        <root level="INFO">
            <appender-ref ref="JSON_CONSOLE"/>
            <appender-ref ref="SENTRY"/>
        </root>

        <!-- Sentry SDK 노이즈 감소 -->
        <logger name="io.sentry" level="WARN"/>

    </springProfile>

</configuration>
```

### 5.2 핵심 포인트

| 설정 | 설명 |
|------|------|
| `ThresholdFilter: ERROR` | ERROR 레벨만 Sentry로 전송 |
| `prod,staging` 프로파일 | 로컬/테스트에서는 비활성화 |
| MDC 포함 | traceId, userId 등 컨텍스트 자동 전송 |
| `io.sentry` WARN | Sentry SDK 자체 로그 노이즈 감소 |

---

## 6. 진입점별 에러 처리 전략

### 6.1 REST API (GlobalExceptionHandler)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleInternalServerError(Exception e) {
        // log.error() → Logback → SentryAppender → Sentry
        log.error("Internal server error occurred", e);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 오류"));
    }
}
```

**특징:**
- 모든 REST API 예외가 이곳으로 집중
- MDC에 userId, tenantId, requestId 자동 설정 (UserContextFilter)
- `log.error()` 호출 시 Sentry로 자동 전송

### 6.2 SQS Listener

```java
@Component
public class ExternalDownloadSqsListener {

    private static final Logger log = LoggerFactory.getLogger(ExternalDownloadSqsListener.class);

    @SqsListener(value = "${queue-url}", acknowledgementMode = "MANUAL")
    public void handleMessage(@Payload Message payload, Acknowledgement ack) {
        try {
            // 비즈니스 로직
            processMessage(payload);
            ack.acknowledge();
        } catch (Exception e) {
            handleException(payload.getId(), e);
        }
    }

    private void handleException(String messageId, Exception e) {
        // log.error() → Logback → SentryAppender → Sentry
        log.error("[ExternalDownload] 처리 실패: id={}, error={}",
                  messageId, e.getMessage(), e);

        // ACK 미전송 → SQS 재시도 → 3회 실패 시 DLQ
        throw new ProcessingException("Processing failed: " + messageId, e);
    }
}
```

**특징:**
- try-catch로 감싸서 에러 로깅
- `log.error()`에 예외 객체 전달 필수 (`e`)
- ACK 미전송으로 SQS 재시도 활용

### 6.3 Scheduler

```java
@Component
public class WebhookOutboxRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(WebhookOutboxRetryScheduler.class);

    @Scheduled(fixedRate = 300000)
    public void retryUnsentWebhooks() {
        boolean lockAcquired = distributedLockPort.tryLock(...);
        if (!lockAcquired) return;

        try {
            // 비즈니스 로직
            RetryResult result = retryUseCase.execute();
            log.info("Retry completed: {}", result);
        } catch (Exception e) {
            // log.error() → Logback → SentryAppender → Sentry
            log.error("Webhook retry failed", e);
            throw e;  // 다음 스케줄에서 재시도
        } finally {
            distributedLockPort.unlock(...);
        }
    }
}
```

**특징:**
- 분산 락으로 중복 실행 방지
- catch 블록에서 `log.error()` 호출
- finally에서 락 해제 보장

---

## 7. 에러 로깅 Best Practices

### 7.1 올바른 log.error() 사용법

```java
// ✅ 올바른 사용 - 예외 객체를 마지막 인자로 전달
log.error("Processing failed: id={}", messageId, exception);

// ✅ 올바른 사용 - 컨텍스트 정보 포함
log.error("[Download] Failed: downloadId={}, url={}, error={}",
          downloadId, url, e.getMessage(), e);

// ❌ 잘못된 사용 - 예외 객체 누락 (스택트레이스 없음)
log.error("Processing failed: " + exception.getMessage());

// ❌ 잘못된 사용 - 민감 정보 포함
log.error("Login failed: userId={}, password={}", userId, password, e);
```

### 7.2 MDC 컨텍스트 활용

```java
// 진입점에서 MDC 설정 (Filter, Interceptor)
MDC.put("traceId", traceId);
MDC.put("userId", userId);
MDC.put("downloadId", downloadId);

try {
    // 비즈니스 로직
    // 이 범위 내의 모든 log.error()는 MDC 컨텍스트 포함
} finally {
    MDC.clear();
}
```

### 7.3 에러 레벨 가이드라인

| 레벨 | 사용 시점 | Sentry 전송 |
|------|----------|-------------|
| `ERROR` | 즉시 조치 필요한 오류 (시스템 장애, 데이터 손실 위험) | ✅ |
| `WARN` | 잠재적 문제 (재시도 성공, 성능 저하) | ❌ |
| `INFO` | 정상 처리 흐름 (요청 시작/완료) | ❌ |
| `DEBUG` | 디버깅용 상세 정보 | ❌ |

---

## 8. Sentry 대시보드 활용

### 8.1 Issues 탭

- **에러 그룹화**: 같은 유형의 에러가 자동 그룹화
- **필터링**: environment, release, tag로 필터
- **할당**: 팀원에게 이슈 할당 가능

### 8.2 유용한 검색 쿼리

```
# 특정 환경의 에러
environment:prod

# 특정 릴리즈의 에러
release:fileflow-1.2.3

# 특정 사용자 관련 에러
user.id:12345

# 특정 traceId 추적
traceId:abc123def456
```

### 8.3 Alert 설정

**권장 Alert 규칙:**
- 새로운 에러 타입 발생 시 Slack 알림
- 동일 에러 100회 이상 발생 시 이메일 알림
- 특정 환경(prod)에서만 알림 활성화

---

## 9. 트러블슈팅

### 9.1 Sentry에 이벤트가 안 올라올 때

```bash
# 1. DSN 확인
echo $SENTRY_DSN

# 2. 프로파일 확인 (prod/staging이어야 함)
echo $SPRING_PROFILES_ACTIVE

# 3. 로그 레벨 확인 (ERROR여야 함)
grep "level" logback-spring.xml

# 4. Sentry SDK 디버그 모드 활성화
sentry:
  debug: true
```

### 9.2 너무 많은 이벤트가 올라올 때

```yaml
# application.yml
sentry:
  # 샘플링 비율 낮추기
  traces-sample-rate: 0.01  # 1%

  # 특정 예외 무시
  ignored-exceptions-for-type:
    - org.springframework.web.client.HttpClientErrorException
```

### 9.3 성능 영향 최소화

```yaml
sentry:
  # 비동기 전송 (기본값)
  async: true

  # 전송 큐 크기
  max-queue-size: 100

  # 전송 타임아웃
  shutdown-timeout-millis: 2000
```

---

## 10. 체크리스트

### 10.1 설정 체크리스트

- [ ] `gradle/libs.versions.toml`에 Sentry 의존성 추가
- [ ] 각 bootstrap `build.gradle`에 의존성 추가
- [ ] `application.yml`에 Sentry 설정 추가
- [ ] `logback-spring.xml`에 SentryAppender 추가
- [ ] 환경변수 `SENTRY_DSN` 설정

### 10.2 운영 체크리스트

- [ ] Sentry 프로젝트 생성 및 DSN 발급
- [ ] 환경별 DSN 분리 (dev/staging/prod)
- [ ] Alert 규칙 설정
- [ ] 팀 초대 및 권한 설정
- [ ] Slack/Email 연동

### 10.3 코드 체크리스트

- [ ] GlobalExceptionHandler에서 `log.error()` 사용
- [ ] SQS Listener에서 예외 처리 및 `log.error()` 사용
- [ ] Scheduler에서 예외 처리 및 `log.error()` 사용
- [ ] MDC 컨텍스트 설정 (traceId, userId 등)

---

## 11. 참고 자료

- [Sentry Java Documentation](https://docs.sentry.io/platforms/java/)
- [Sentry Spring Boot Integration](https://docs.sentry.io/platforms/java/guides/spring-boot/)
- [Sentry Logback Integration](https://docs.sentry.io/platforms/java/guides/logback/)
- [FileFlow Observability Guide](../coding_convention/06-observability/sentry-observability-guide.md)
