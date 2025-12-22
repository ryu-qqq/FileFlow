# AOP 기반 메트릭 수집 구현 가이드

이 문서는 Spring Boot 프로젝트에서 AOP 기반 커스텀 어노테이션을 활용한 메트릭 수집 시스템 구현 방법을 설명합니다.

---

## 1. 개요

### 1.1 목표
- 비즈니스 로직에서 메트릭 수집 코드 분리
- 선언적 어노테이션으로 메트릭 활성화
- Micrometer + Prometheus + Grafana 연동

### 1.2 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│  Service / Adapter Layer                                    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  @SessionMetric(operation="complete", type="single")│    │
│  │  public Response execute(Command cmd) {             │    │
│  │      // 순수 비즈니스 로직만 존재                      │    │
│  │  }                                                   │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  AOP Aspect Layer                                           │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  @Around("@annotation(sessionMetric)")              │    │
│  │  - Timer 시작/종료                                   │    │
│  │  - 성공/실패 카운터                                  │    │
│  │  - 태그 추가 (operation, type, status)              │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  Micrometer → Prometheus → Grafana                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. 의존성 설정

### 2.1 build.gradle (Version Catalog 사용 시)

```toml
# gradle/libs.versions.toml
[libraries]
spring-boot-starter-aop = { module = "org.springframework.boot:spring-boot-starter-aop" }
spring-boot-starter-actuator = { module = "org.springframework.boot:spring-boot-starter-actuator" }
micrometer-registry-prometheus = { module = "io.micrometer:micrometer-registry-prometheus" }
```

```gradle
// application/build.gradle
dependencies {
    implementation libs.spring.boot.starter.aop
    implementation libs.spring.boot.starter.actuator
    implementation libs.micrometer.registry.prometheus
}
```

### 2.2 application.yml 설정

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus, metrics
  metrics:
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active:local}
    export:
      prometheus:
        enabled: true
```

---

## 3. 커스텀 어노테이션 설계

### 3.1 비즈니스 메트릭 어노테이션

**세션 관련 메트릭:**
```java
package com.example.application.common.metrics.annotation;

import java.lang.annotation.*;

/**
 * 세션 관련 메트릭 수집 어노테이션.
 *
 * <p>수집 메트릭:
 * <ul>
 *   <li>session.{operation}.latency - 처리 시간</li>
 *   <li>session.{operation}.count - 성공/실패 카운트</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SessionMetric {
    /** 작업 유형 (예: initiate, complete, cancel) */
    String operation();

    /** 세션 타입 (예: single, multipart) */
    String type();

    /** 실패 시 abort 메트릭 기록 여부 */
    boolean recordAbortOnFailure() default true;
}
```

**파일 처리 메트릭:**
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FileAssetMetric {
    /** 작업 유형 (예: create, process, copy, delete) */
    String operation();

    /** 바이트 크기 기록 여부 */
    boolean recordBytes() default false;
}
```

### 3.2 다운스트림 메트릭 어노테이션 (Adapter용)

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DownstreamMetric {
    /** 대상 시스템 (예: s3, redis, external-api) */
    String target();

    /** 작업 유형 (예: get, put, delete) */
    String operation();

    /** 서비스명 (external-api인 경우) */
    String service() default "";

    /** 엔드포인트 (선택적) */
    String endpoint() default "";
}
```

---

## 4. Aspect 구현

### 4.1 세션 메트릭 Aspect

```java
package com.example.application.common.metrics.aspect;

import com.example.application.common.metrics.SessionMetrics;
import com.example.application.common.metrics.annotation.SessionMetric;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SessionMetricAspect {

    private final SessionMetrics sessionMetrics;

    public SessionMetricAspect(SessionMetrics sessionMetrics) {
        this.sessionMetrics = sessionMetrics;
    }

    @Around("@annotation(sessionMetric)")
    public Object recordSessionMetric(
            ProceedingJoinPoint joinPoint,
            SessionMetric sessionMetric) throws Throwable {

        String operation = sessionMetric.operation();
        String type = sessionMetric.type();

        Timer.Sample sample = sessionMetrics.startTimer();

        try {
            Object result = joinPoint.proceed();

            // 성공 메트릭 기록
            recordSuccessMetric(operation, type);
            sessionMetrics.stopTimer(sample, operation, type);

            return result;

        } catch (Throwable e) {
            // 실패 메트릭 기록
            if (sessionMetric.recordAbortOnFailure()) {
                sessionMetrics.recordSessionAbort(type, e.getClass().getSimpleName());
            }
            sessionMetrics.stopTimer(sample, operation + "-failed", type);

            throw e;
        }
    }

    private void recordSuccessMetric(String operation, String type) {
        switch (operation) {
            case "initiate" -> sessionMetrics.recordSessionInitiated(type);
            case "complete" -> sessionMetrics.recordSessionCompleted(type);
            case "cancel" -> sessionMetrics.recordSessionAbort(type, "user-cancelled");
        }
    }
}
```

### 4.2 다운스트림 메트릭 Aspect

```java
@Aspect
@Component
public class DownstreamMetricAspect {

    private final DownstreamMetrics downstreamMetrics;

    public DownstreamMetricAspect(DownstreamMetrics downstreamMetrics) {
        this.downstreamMetrics = downstreamMetrics;
    }

    @Around("@annotation(downstreamMetric)")
    public Object recordDownstreamMetric(
            ProceedingJoinPoint joinPoint,
            DownstreamMetric downstreamMetric) throws Throwable {

        String target = downstreamMetric.target();
        String operation = downstreamMetric.operation();
        String service = downstreamMetric.service();

        Timer.Sample sample = downstreamMetrics.startTimer();

        try {
            Object result = joinPoint.proceed();
            stopTimerByTarget(sample, target, operation, service, "success");
            return result;

        } catch (Throwable e) {
            stopTimerByTarget(sample, target, operation, service, "error");
            throw e;
        }
    }

    private void stopTimerByTarget(
            Timer.Sample sample,
            String target,
            String operation,
            String service,
            String status) {

        switch (target) {
            case "s3" -> downstreamMetrics.stopS3Timer(sample, operation);
            case "redis" -> downstreamMetrics.stopRedisTimer(sample, operation);
            case "external-api" -> downstreamMetrics.stopExternalApiTimer(sample, service, operation);
        }
    }
}
```

### 4.3 메트릭 수집 클래스

```java
@Component
public class DownstreamMetrics {

    private final MeterRegistry meterRegistry;

    public DownstreamMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopS3Timer(Timer.Sample sample, String operation) {
        sample.stop(Timer.builder("downstream.s3.latency")
                .tag("operation", operation)
                .register(meterRegistry));
    }

    public void stopRedisTimer(Timer.Sample sample, String operation) {
        sample.stop(Timer.builder("downstream.redis.latency")
                .tag("operation", operation)
                .register(meterRegistry));
    }

    public void stopExternalApiTimer(Timer.Sample sample, String service, String endpoint) {
        sample.stop(Timer.builder("downstream.external.api.latency")
                .tag("service", service)
                .tag("endpoint", endpoint)
                .register(meterRegistry));
    }
}
```

---

## 5. 적용 예시

### 5.1 Service 계층 적용

**Before (직접 메트릭 코드):**
```java
@Service
public class CompleteSingleUploadService implements CompleteSingleUploadUseCase {

    private final SessionMetrics sessionMetrics;
    // ... 다른 의존성

    @Override
    public Response execute(Command command) {
        Timer.Sample sample = sessionMetrics.startTimer();
        try {
            // 비즈니스 로직
            Response result = doBusinessLogic(command);

            sessionMetrics.recordSessionCompleted("single");
            sessionMetrics.stopTimer(sample, "complete", "single");
            return result;

        } catch (Exception e) {
            sessionMetrics.recordSessionAbort("single", e.getClass().getSimpleName());
            sessionMetrics.stopTimer(sample, "complete-failed", "single");
            throw e;
        }
    }
}
```

**After (어노테이션 기반):**
```java
@Service
public class CompleteSingleUploadService implements CompleteSingleUploadUseCase {

    // SessionMetrics 의존성 제거!

    @Override
    @SessionMetric(operation = "complete", type = "single")
    public Response execute(Command command) {
        // 순수 비즈니스 로직만 존재
        return doBusinessLogic(command);
    }
}
```

### 5.2 Adapter 계층 적용

**Before:**
```java
@Component
public class S3ClientAdapter implements S3ClientPort {

    private final S3Client s3Client;
    private final DownstreamMetrics downstreamMetrics;

    @Override
    public ETag putObject(Bucket bucket, Key key, byte[] data) {
        Timer.Sample sample = downstreamMetrics.startTimer();
        try {
            // S3 호출 로직
            return result;
        } finally {
            downstreamMetrics.stopS3Timer(sample, "put");
        }
    }
}
```

**After:**
```java
@Component
public class S3ClientAdapter implements S3ClientPort {

    private final S3Client s3Client;
    // DownstreamMetrics 의존성 제거!

    @Override
    @DownstreamMetric(target = "s3", operation = "put")
    public ETag putObject(Bucket bucket, Key key, byte[] data) {
        // 순수 S3 호출 로직만 존재
        return result;
    }
}
```

### 5.3 Redis Adapter 적용

```java
@Component
public class CacheAdapter implements CachePort {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @DownstreamMetric(target = "redis", operation = "set")
    public void set(CacheKey key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key.value(), value, ttl);
    }

    @Override
    @DownstreamMetric(target = "redis", operation = "get")
    public Optional<Object> get(CacheKey key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key.value()));
    }

    @Override
    @DownstreamMetric(target = "redis", operation = "delete")
    public void evict(CacheKey key) {
        redisTemplate.delete(key.value());
    }
}
```

---

## 6. 수집되는 메트릭 목록

### 6.1 비즈니스 메트릭

| 메트릭 이름 | 타입 | 태그 | 설명 |
|------------|------|------|------|
| `session.initiate.latency` | Timer | type, status | 세션 생성 지연시간 |
| `session.complete.latency` | Timer | type, status | 세션 완료 지연시간 |
| `session.initiated.count` | Counter | type | 생성된 세션 수 |
| `session.completed.count` | Counter | type | 완료된 세션 수 |
| `session.aborted.count` | Counter | type, reason | 중단된 세션 수 |
| `file.asset.process.latency` | Timer | status | 파일 처리 지연시간 |
| `file.asset.bytes` | DistributionSummary | operation | 처리된 바이트 |

### 6.2 다운스트림 메트릭

| 메트릭 이름 | 타입 | 태그 | 설명 |
|------------|------|------|------|
| `downstream.s3.latency` | Timer | operation | S3 API 지연시간 |
| `downstream.redis.latency` | Timer | operation | Redis 작업 지연시간 |
| `downstream.external.api.latency` | Timer | service, endpoint | 외부 API 지연시간 |

---

## 7. Grafana 대시보드 설정

### 7.1 PromQL 쿼리 예시

**세션 완료율:**
```promql
sum(rate(session_completed_count_total{type="single"}[5m]))
/
sum(rate(session_initiated_count_total{type="single"}[5m]))
* 100
```

**S3 평균 지연시간:**
```promql
rate(downstream_s3_latency_seconds_sum[5m])
/
rate(downstream_s3_latency_seconds_count[5m])
```

**Redis 작업별 P99 지연시간:**
```promql
histogram_quantile(0.99,
  sum(rate(downstream_redis_latency_seconds_bucket[5m])) by (le, operation)
)
```

### 7.2 Alert 설정 예시

```yaml
# S3 지연시간 Alert
- alert: S3HighLatency
  expr: |
    histogram_quantile(0.95,
      sum(rate(downstream_s3_latency_seconds_bucket[5m])) by (le)
    ) > 2
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "S3 P95 latency is above 2 seconds"

# 세션 실패율 Alert
- alert: HighSessionFailureRate
  expr: |
    sum(rate(session_aborted_count_total[5m]))
    /
    sum(rate(session_initiated_count_total[5m])) > 0.1
  for: 5m
  labels:
    severity: critical
  annotations:
    summary: "Session failure rate is above 10%"
```

---

## 8. 체크리스트

### 8.1 구현 체크리스트

- [ ] `spring-boot-starter-aop` 의존성 추가
- [ ] `spring-boot-starter-actuator` 의존성 추가
- [ ] `micrometer-registry-prometheus` 의존성 추가
- [ ] 커스텀 어노테이션 생성 (`@SessionMetric`, `@DownstreamMetric` 등)
- [ ] Metrics 클래스 생성 (`SessionMetrics`, `DownstreamMetrics`)
- [ ] Aspect 클래스 생성 및 `@Component` 등록
- [ ] 기존 Service/Adapter에서 메트릭 코드 제거
- [ ] 어노테이션 적용
- [ ] 테스트 코드에서 불필요한 Mock 제거

### 8.2 검증 체크리스트

- [ ] `/actuator/prometheus` 엔드포인트에서 메트릭 노출 확인
- [ ] Grafana 대시보드에서 메트릭 시각화 확인
- [ ] Alert 룰 동작 확인

---

## 9. 주의사항

### 9.1 Spring AOP 제약사항

1. **같은 클래스 내부 호출은 AOP 적용 안됨**
   ```java
   public class MyService {
       public void methodA() {
           methodB(); // AOP 적용 X (내부 호출)
       }

       @SessionMetric(operation = "test", type = "test")
       public void methodB() { }
   }
   ```

2. **private 메서드는 AOP 적용 안됨**

3. **final 클래스/메서드는 프록시 생성 불가**

### 9.2 성능 고려사항

- AOP 오버헤드는 일반적으로 마이크로초 수준 (무시 가능)
- 메트릭 태그 수가 많아지면 카디널리티 증가 → 메모리 사용량 증가
- 고빈도 호출 메서드에 적용 시 Timer.Sample 객체 생성 비용 고려

---

## 10. 참고 자료

- [Micrometer Documentation](https://micrometer.io/docs)
- [Spring AOP Documentation](https://docs.spring.io/spring-framework/reference/core/aop.html)
- [Prometheus Best Practices](https://prometheus.io/docs/practices/naming/)
