# Phase 2C: Events & Batch

**진행 상태**: ⏳ 대기 중 (0/10 - 0%)

## 개요

Phase 2C는 도메인 이벤트 발행, Idempotency 처리, 배치 작업을 구현합니다.
Anti-Corruption Layer를 통해 Spring Framework에 대한 도메인 격리를 유지하고,
만료된 세션을 정리하는 배치 작업을 추가합니다.

**핵심 목표**: 도메인 순수성 유지, 이벤트 기반 아키텍처, 배치 정리 작업

## 태스크 목록

### ⏳ KAN-326: UploadSession AbstractAggregateRoot 확장

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: UploadSession이 AbstractAggregateRoot를 확장하여 도메인 이벤트 발행 지원

**변경 사항**:
```java
// Before
public class UploadSession {
    // ...
}

// After
public class UploadSession extends AbstractAggregateRoot<UploadSession> {
    // 이벤트 발행 메서드 추가
    private void publishEvent(DomainEvent event) {
        registerEvent(event);
    }
}
```

**DoD**:
- [ ] Spring Data의 AbstractAggregateRoot 확장
- [ ] 기존 Unit Test 통과
- [ ] 이벤트 발행 확인 테스트

---

### ⏳ KAN-327: Domain Events 정의 (4개)

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: 4개 도메인 이벤트 정의

**이벤트 목록**:

#### 1. UploadSessionCreatedEvent
```java
public record UploadSessionCreatedEvent(
    String sessionId,
    String filename,
    String mimeType,
    long estimatedSize,
    Long userId,
    Long tenantId,
    Long orgId,
    LocalDateTime occurredAt
) implements DomainEvent {}
```

#### 2. MultipartUploadCompletedEvent
```java
public record MultipartUploadCompletedEvent(
    String sessionId,
    String providerUploadId,
    int totalParts,
    long finalSize,
    LocalDateTime occurredAt
) implements DomainEvent {}
```

#### 3. ExternalDownloadCompletedEvent
```java
public record ExternalDownloadCompletedEvent(
    String downloadId,
    String sessionId,
    String sourceUrl,
    long downloadedBytes,
    LocalDateTime occurredAt
) implements DomainEvent {}
```

#### 4. UploadSessionExpiredEvent
```java
public record UploadSessionExpiredEvent(
    String sessionId,
    UploadSessionStatus status,
    LocalDateTime expiredAt,
    LocalDateTime occurredAt
) implements DomainEvent {}
```

**DoD**:
- [ ] Record 패턴 사용
- [ ] DomainEvent 인터페이스 구현
- [ ] 불변성 보장

---

### ⏳ KAN-328: UploadEventPublisher 구현 (Anti-Corruption Layer)

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: Spring ApplicationEventPublisher를 도메인에서 격리

**구현 방식**:
```java
// Domain Port (domain 패키지)
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}

// Adapter Implementation (adapter-out 패키지)
@Component
public class SpringEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(DomainEvent event) {
        publisher.publishEvent(event);
    }
}
```

**DoD**:
- [ ] Anti-Corruption Layer 패턴 적용
- [ ] Domain이 Spring에 의존하지 않음
- [ ] Integration Test

---

### ⏳ KAN-329: UploadEventMapper 구현

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: 도메인 이벤트를 외부 시스템용 이벤트로 변환

**변환 예시**:
```java
// Domain Event
UploadSessionCreatedEvent domainEvent = new UploadSessionCreatedEvent(...);

// External Event (Kafka, SQS 등)
ExternalUploadEvent externalEvent = new ExternalUploadEvent(
    eventType: "upload.session.created",
    eventId: UUID.randomUUID(),
    occurredAt: domainEvent.occurredAt(),
    payload: {
        sessionId: domainEvent.sessionId(),
        userId: domainEvent.userId(),
        // ...
    }
);
```

**DoD**:
- [ ] 도메인 → 외부 이벤트 매핑
- [ ] Unit Test

---

### ⏳ KAN-330: IdempotencyMiddleware 구현

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: API 요청 Idempotency 처리 (중복 방지)

**구현 방식**:
- Interceptor 또는 Filter 사용
- Redis에 Idempotency 키 저장 (TTL: 24시간)
- 중복 요청 시 캐시된 응답 반환

**핵심 로직**:
```java
@Component
public class IdempotencyInterceptor implements HandlerInterceptor {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String idempotencyKey = request.getHeader("Idempotency-Key");

        if (idempotencyKey == null) {
            return true; // Idempotency 선택적
        }

        // Redis 조회
        String cacheKey = "idempotency:" + idempotencyKey;
        Object cachedResponse = redisTemplate.opsForValue().get(cacheKey);

        if (cachedResponse != null) {
            // 캐시된 응답 반환
            response.setStatus(HttpStatus.OK.value());
            response.getWriter().write(cachedResponse.toString());
            return false; // 요청 중단
        }

        return true; // 정상 처리
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler, Exception ex) {
        String idempotencyKey = request.getHeader("Idempotency-Key");

        if (idempotencyKey != null && response.getStatus() == 200) {
            // 응답 캐싱 (24시간)
            String cacheKey = "idempotency:" + idempotencyKey;
            redisTemplate.opsForValue().set(cacheKey,
                                           response.getBody(),
                                           24, TimeUnit.HOURS);
        }
    }
}
```

**DoD**:
- [ ] Redis 기반 구현
- [ ] TTL 24시간 설정
- [ ] Integration Test (중복 요청 시나리오)

---

### ⏳ KAN-331: UploadSessionExpirationBatchJob 구현

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: 만료된 UploadSession 정리 배치 작업

**Spring Batch 구성**:
```java
@Configuration
public class ExpirationBatchConfig {
    @Bean
    public Job expireSessionsJob(JobRepository jobRepository,
                                 Step expireSessionsStep) {
        return new JobBuilder("expireSessionsJob", jobRepository)
            .start(expireSessionsStep)
            .build();
    }

    @Bean
    public Step expireSessionsStep(JobRepository jobRepository,
                                   PlatformTransactionManager txManager,
                                   ItemReader<UploadSession> reader,
                                   ItemProcessor<UploadSession, UploadSession> processor,
                                   ItemWriter<UploadSession> writer) {
        return new StepBuilder("expireSessionsStep", jobRepository)
            .<UploadSession, UploadSession>chunk(100, txManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }
}
```

**만료 조건**:
- 상태가 IN_PROGRESS이고 expiresAt < 현재시각
- 만료된 세션 → EXPIRED 상태로 변경
- S3에서 미완료 Multipart Upload Abort

**DoD**:
- [ ] Spring Batch 구현
- [ ] 청크 단위 처리 (100개씩)
- [ ] Cron 스케줄링 (매일 새벽 3시)
- [ ] Integration Test

---

### ⏳ KAN-332: Multipart Upload 통합 테스트

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: Multipart Upload E2E 시나리오 테스트

**테스트 시나리오**:
1. Init → Presigned URL 생성 → 파트 업로드 → Complete
2. 파트 업로드 실패 → 재시도
3. 불완전 업로드 → Complete 실패 (409 Conflict)
4. 만료된 세션 → 배치 작업으로 정리

**DoD**:
- [ ] 4개 시나리오 모두 통과
- [ ] S3 Mock 사용
- [ ] MockMvc 기반 API 테스트

---

### ⏳ KAN-333: External Download 통합 테스트

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: External Download E2E 시나리오 테스트

**테스트 시나리오**:
1. Start → 다운로드 진행 → Complete → UploadSession 생성
2. 다운로드 실패 → 재시도 (3회)
3. 최대 재시도 초과 → FAILED 상태

**DoD**:
- [ ] 3개 시나리오 모두 통과
- [ ] HTTP Mock Server 사용
- [ ] S3 Mock 사용

---

### ⏳ KAN-334: Policy Evaluation 통합 테스트

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: Upload Policy 평가 통합 테스트

**테스트 시나리오**:
1. fileSize >= 100MB → MULTIPART
2. externalUrl 제공 → EXTERNAL
3. else → DIRECT

**DoD**:
- [ ] 정책 분기 테스트
- [ ] Unit Test + Integration Test

---

### ⏳ KAN-335: Event Publishing 통합 테스트

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: 도메인 이벤트 발행 통합 테스트

**테스트 시나리오**:
1. UploadSession 생성 → UploadSessionCreatedEvent 발행
2. Multipart 완료 → MultipartUploadCompletedEvent 발행
3. External Download 완료 → ExternalDownloadCompletedEvent 발행
4. 세션 만료 → UploadSessionExpiredEvent 발행

**DoD**:
- [ ] @DomainEvents 리스너 테스트
- [ ] 이벤트 발행 검증
- [ ] Integration Test

---

## 📊 Phase 2C 요약

### 아키텍처 구성
```
Domain Layer:
- UploadSession extends AbstractAggregateRoot
- DomainEvent (4개)
- DomainEventPublisher (Port)

Application Layer:
- Event Handlers (감사 로그, 알림 등)

Adapter Layer:
- SpringEventPublisher (Anti-Corruption)
- UploadEventMapper
- IdempotencyInterceptor (Redis)
- ExpirationBatchJob (Spring Batch)
```

### 성능 목표
- 이벤트 발행 오버헤드 < 5ms
- Idempotency 캐시 조회 < 10ms
- 배치 작업 처리 속도 > 100 세션/초

### Phase 2 완료 조건
- [ ] Phase 2A: Multipart Upload (10개 태스크)
- [ ] Phase 2B: External Download (6개 태스크)
- [ ] Phase 2C: Events & Batch (10개 태스크)
- [ ] 전체 통합 테스트 통과
- [ ] ArchUnit 테스트 통과
- [ ] API 문서 업데이트

### 다음 단계
Phase 2 완료 후 Phase 3 (고급 기능) 또는 프로덕션 배포
