# 🐛 [Critical] SQS 중복 메시지 처리 및 동시성 제어 개선 필요

## 🎯 문제 상황

### 현재 구조
- **SQS**: Standard Queue 사용 (FIFO 미사용)
- **동시성 제어**: Optimistic Locking 없음
- **중복 방어**: 애플리케이션 레벨 상태 체크만 존재

## 🐛 발견된 문제

### 1️⃣ Race Condition - 동시 읽기 문제

**시나리오**: S3 이벤트가 중복 발행되어 두 스레드가 동시에 처리하는 경우

```
시간축 →
Thread A (S3 Event 1)              Thread B (S3 Event 2 - 중복)
├─ findById(sessionId)             ├─ findById(sessionId)
│  → status: PENDING                │  → status: PENDING (동시 읽기!)
│                                   │
├─ isActive() → true               ├─ isActive() → true
│                                   │
├─ session.complete()              ├─ session.complete()
│  → COMPLETED                      │  → COMPLETED
│                                   │
└─ save() → UPDATE ✅              └─ save() → UPDATE ✅ (중복!)
```

**영향**:
- ✅ **데이터 일관성**: 문제없음 (같은 상태로 업데이트)
- ⚠️ **성능**: 불필요한 DB Write 발생
- ⚠️ **로깅**: 중복 완료 로그 발생
- ⚠️ **리소스**: CPU/DB 커넥션 낭비

### 2️⃣ SQS Standard Queue - At-Least-Once Delivery

**현재 설정**:
```java
// SqsProperties.java
❌ FIFO Queue 설정 없음
❌ MessageDeduplicationId 없음
❌ MessageGroupId 없음
```

**Standard Queue 특성**:
- 메시지 중복 전송 가능 (At-Least-Once)
- 순서 보장 안됨
- 높은 처리량 (FIFO보다 빠름)

### 3️⃣ Optimistic Locking 미적용

**현재 Entity**:
```java
@Entity
public class UploadSessionEntity {
    @Id
    private Long id;
    
    ❌ @Version 없음  // Optimistic Locking 미사용
    
    private String sessionId;
    private UploadStatus status;
    // ...
}
```

### 4️⃣ **FIFO Queue에서 MessageGroupId 미사용** (치명적!)

**문제**: FIFO Queue로 전환한다고 해도, **현재 S3에서 SQS로 이벤트를 발행할 때 MessageGroupId를 설정하지 않음**

**현재 상황**:
```
S3 Event Notification → SQS FIFO Queue
❌ MessageGroupId 없음!
❌ MessageDeduplicationId 없음!
```

S3 Event Notification은 **AWS가 자동으로 발행**하기 때문에, 애플리케이션 코드에서 MessageGroupId를 설정할 수 없습니다.

**결과**:
- FIFO Queue를 사용해도 **순서 보장 불가**
- FIFO Queue의 장점을 전혀 활용 못함
- Content-Based Deduplication만 사용 가능 (같은 내용의 메시지만 중복 제거)

**FIFO Queue가 의미 있으려면**:
```java
// ✅ 직접 메시지 발행 시 (Lambda 등)
sqsClient.sendMessage(SendMessageRequest.builder()
    .queueUrl(fifoQueueUrl)
    .messageBody(eventBody)
    .messageGroupId(sessionId)  // ✅ 세션별 순서 보장
    .messageDeduplicationId(UUID.randomUUID().toString())  // ✅ 중복 제거
    .build());
```

하지만 S3 → SQS는 AWS가 관리하므로 **MessageGroupId 설정 불가**!

### 5️⃣ **재처리 로직 부재** (치명적!)

**현재 실패 처리**:
```java
// S3EventListener.java (Line 127-133)
} catch (Exception e) {
    log.error("Failed to process message: {}. Error: {}",
            message.messageId(), e.getMessage(), e);

    // ❌ 메시지 삭제 안함 (Visibility Timeout 후 자동 재시도)
    // ❌ DLQ 설정 의존 (애플리케이션 레벨 제어 없음)
}
```

**문제점**:

1. **재시도 횟수 제어 없음**
   - SQS의 `maxReceiveCount` 설정에만 의존
   - 애플리케이션에서 재시도 횟수 추적 불가

2. **재시도 전략 없음**
   - 일시적 오류 (네트워크 장애) vs 영구적 오류 (잘못된 데이터) 구분 안함
   - 모든 오류를 동일하게 처리

3. **DLQ 처리 로직 없음**
   - DLQ로 이동한 메시지를 어떻게 처리할지 명확하지 않음
   - 수동 개입 필요

4. **Circuit Breaker는 있지만 SQS 리스너에는 미적용**
   ```java
   // S3UploadEventHandler에는 Circuit Breaker + Retry 있음
   circuitBreaker.executeSupplier(() ->
       retryTemplate.execute(context -> {
           uploadSessionPort.save(completedSession);
           return completedSession;
       })
   );
   
   // ❌ 하지만 S3EventListener의 handleS3Event() 호출에는 없음!
   eventHandler.handleS3Event(messageBody);  // 예외 발생 시 그냥 로그만
   ```

**시나리오**:

```
메시지 1 수신
    ↓
handleS3Event() 호출
    ↓
DB 장애 발생 (일시적)
    ↓
Exception 발생
    ↓
catch 블록: 로그만 찍고 끝
    ↓
메시지 삭제 안됨
    ↓
Visibility Timeout (30초) 후 자동 재시도
    ↓
다시 실패...
    ↓
maxReceiveCount (3회?) 후 DLQ로 이동
    ↓
❌ 이후 처리 방법 없음
```

---

## 📋 재현 단계

### 시나리오 A: 중복 S3 이벤트

1. 클라이언트가 S3에 파일 업로드 완료
2. S3가 ObjectCreated 이벤트를 SQS로 전송
3. **네트워크 이슈로 같은 이벤트가 2번 전송** (Standard Queue 특성)
4. S3EventListener가 동시에 2개 메시지 수신
5. 두 스레드가 동시에 `findById()` 호출 → 둘 다 `PENDING` 상태 읽음
6. 두 스레드가 모두 `complete()` + `save()` 호출

**결과**: 같은 세션에 대해 2번 UPDATE 발생

### 시나리오 B: 클라이언트 Confirm + S3 이벤트 동시

1. 클라이언트가 `/api/v1/upload/confirm` 호출 (Thread A)
2. 거의 동시에 S3 이벤트 도착 (Thread B)
3. 둘 다 `PENDING` 상태 읽음
4. 둘 다 `COMPLETED`로 변경 시도

---

## 🎯 개선 방안

### ✅ Option 1: Optimistic Locking (추천, 최우선)

**난이도**: 낮음  
**예상 작업 시간**: 30분  
**Priority**: P0

**변경 사항**:

```java
// UploadSessionEntity.java
@Entity
@Table(name = "upload_session")
public class UploadSessionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Version  // ✅ 추가
    @Column(name = "version", nullable = false)
    private Long version;
    
    // ... 기존 필드들
}
```

```java
// S3UploadEventHandler.java
private void updateUploadSession(
        String sessionId,
        S3Location s3Location,
        S3EventNotification.S3EventRecord record
) {
    // ... 기존 로직 ...
    
    // 4. 세션 완료 처리
    try {
        UploadSession completedSession = session.complete();

        // Circuit Breaker와 Retry를 적용하여 세션 저장
        circuitBreaker.executeSupplier(() ->
                retryTemplate.execute(context -> {
                    uploadSessionPort.save(completedSession);
                    return completedSession;
                })
        );

        log.info("Successfully updated upload session: {} to COMPLETED.", sessionId);

    } catch (OptimisticLockException e) {
        // ✅ 이미 다른 스레드가 업데이트함, 안전하게 무시
        log.info("Session {} already updated by another thread. Skipping duplicate update.", 
                 sessionId);
        return;
        
    } catch (IllegalStateException e) {
        log.error("Failed to complete upload session: {}. Error: {}",
                sessionId, e.getMessage(), e);
        throw new SessionMatchingException(
                "Failed to complete upload session: " + sessionId, e
        );
    }
}
```

**장점**:
- ✅ 간단한 구현 (어노테이션 1개 + 예외 처리)
- ✅ DB 레벨 동시성 제어
- ✅ 먼저 성공한 스레드만 커밋
- ✅ 추가 인프라 필요 없음
- ✅ Spring Data JPA 표준 기능

**단점**:
- ⚠️ 실패한 스레드는 `OptimisticLockException` 발생 (예상된 동작)
- ⚠️ 기존 데이터 마이그레이션 필요

---

### ✅ Option 2: FIFO Queue 전환 (중기)

**난이도**: 중간  
**예상 작업 시간**: 1일  
**Priority**: P1

**AWS 인프라 변경**:

1. **SQS FIFO Queue 생성**
   - Queue 이름: `fileflow-s3-events.fifo`
   - Content-Based Deduplication: 활성화

2. **S3 Event Notification 설정 변경**
   - 기존 Standard Queue → FIFO Queue로 변경

**애플리케이션 설정 변경**:

```yaml
# application.yml
aws:
  sqs:
    s3-event-queue-url: https://sqs.ap-northeast-2.amazonaws.com/123456/fileflow-s3-events.fifo
```

**장점**:
- ✅ AWS 레벨에서 5분 내 중복 메시지 자동 제거
- ✅ 메시지 순서 보장 (MessageGroupId 기준)
- ✅ 애플리케이션 로직 변경 최소화
- ✅ 인프라 레벨 해결책

**단점**:
- ⚠️ 처리량 감소 (Standard 대비 ~10배 낮음)
- ⚠️ 비용 증가 (FIFO Queue는 더 비쌈)
- ⚠️ Queue 이름에 `.fifo` 필수 (기존 Queue 재사용 불가)
- ⚠️ 다운타임 또는 Blue-Green 배포 필요

---

### ✅ Option 3: Conditional UPDATE Query (보조)

**난이도**: 중간  
**예상 작업 시간**: 2시간  
**Priority**: P2

**변경 사항**:

```java
// UploadSessionJpaRepository.java
public interface UploadSessionJpaRepository extends JpaRepository<UploadSessionEntity, Long> {
    
    @Modifying
    @Query("UPDATE UploadSessionEntity e " +
           "SET e.status = :newStatus, e.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE e.sessionId = :sessionId AND e.status IN ('PENDING', 'UPLOADING')")
    int updateStatusToCompleted(@Param("sessionId") String sessionId, 
                                @Param("newStatus") UploadStatus newStatus);
}
```

```java
// UploadSessionPersistenceAdapter.java
public boolean completeIfPending(String sessionId) {
    int updated = repository.updateStatusToCompleted(sessionId, UploadStatus.COMPLETED);
    
    if (updated == 0) {
        log.info("Session {} already completed or not in valid state. Skipping.", sessionId);
        return false;
    }
    
    log.info("Session {} successfully updated to COMPLETED.", sessionId);
    return true;
}
```

**장점**:
- ✅ DB 레벨에서 원자적 업데이트
- ✅ WHERE 조건으로 중복 방지
- ✅ 명확한 성공/실패 반환 (0 or 1)

**단점**:
- ⚠️ Domain 로직 우회 (직접 SQL 사용)
- ⚠️ Hexagonal Architecture 원칙 위반 가능
- ⚠️ UploadSession 도메인 모델의 `complete()` 로직 실행 안됨

---

### ⚠️ Option 4: Redis 분산 락 (비추천)

**난이도**: 높음  
**예상 작업 시간**: 1주  
**Priority**: P3 (선택사항)

현재 시스템 규모와 트래픽을 고려하면 **오버엔지니어링**입니다.

**언제 필요한가?**:
- 초당 수천 건 이상의 동시 업로드
- 여러 서버 인스턴스가 동일 세션 처리
- Optimistic Locking만으로 부족한 경우

---

## 📊 위험도 평가

| 항목 | 위험도 | 영향 | 비고 |
|------|--------|------|------|
| **데이터 일관성** | 🟢 낮음 | 같은 상태로 덮어쓰기 | 현재도 안전 |
| **성능 저하** | 🟡 중간 | 불필요한 DB Write | 리소스 낭비 |
| **로그 혼란** | 🟡 중간 | 중복 완료 로그 | 디버깅 어려움 |
| **FileAsset 중복 생성** | 🟢 낮음 | S3EventHandler에서 미호출 | 현재는 안전 |
| **Race Condition** | 🟡 중간 | 동시성 이슈 | 기능은 정상 작동 |

**현재 상태**: ✅ 기능적으로는 안전하지만, 성능 및 로깅 개선 필요

---

## ✅ 권장 구현 순서

### Phase 1: 즉시 적용 (P0) - 1일 이내

1. **Optimistic Locking 추가** ⭐ 최우선
   - [ ] `UploadSessionEntity`에 `@Version` 필드 추가
   - [ ] 마이그레이션 스크립트 작성 및 적용
   - [ ] `S3UploadEventHandler`에 `OptimisticLockException` 핸들링
   - [ ] 로그 레벨 정리 (중복 처리는 INFO 레벨)

### Phase 2: 단기 (1주 내, P1)

2. **동시성 테스트 추가**
   - [ ] Race Condition 재현 테스트 작성
   - [ ] `ExecutorService`를 이용한 동시 업데이트 테스트
   - [ ] `@RepeatedTest`로 안정성 검증

3. **모니터링 강화**
   - [ ] `OptimisticLockException` 발생 빈도 모니터링
   - [ ] 중복 메시지 처리 메트릭 추가

### Phase 3: 중기 (1-2주 내, P2)

4. **FIFO Queue 전환 검토**
   - [ ] 현재 트래픽 분석 (처리량 vs 중복 빈도)
   - [ ] 비용 분석 (Standard vs FIFO)
   - [ ] 성능 테스트 (처리량 영향 측정)
   - [ ] Blue-Green 배포 계획

---

## 📁 영향받는 파일

### Core Files
- `adapter/adapter-out-persistence-jpa/src/main/java/com/ryuqq/fileflow/adapter/persistence/entity/UploadSessionEntity.java`
- `adapter/adapter-out-aws-sqs/src/main/java/com/ryuqq/fileflow/adapter/sqs/handler/S3UploadEventHandler.java`

### Configuration
- `adapter/adapter-out-aws-sqs/src/main/java/com/ryuqq/fileflow/adapter/sqs/config/SqsProperties.java`

### Services
- `application/src/main/java/com/ryuqq/fileflow/application/upload/service/ConfirmUploadService.java`

### Tests (추가 필요)
- `adapter/adapter-out-persistence-jpa/src/test/java/com/ryuqq/fileflow/adapter/persistence/concurrency/OptimisticLockingTest.java` (신규)

---

## 🔗 관련 코드 위치

### UploadSessionEntity
```java
// Line 36
@Entity
@Table(name = "upload_session")
public class UploadSessionEntity {
    // ✅ 여기에 @Version 추가
}
```

### S3UploadEventHandler
```java
// Line 160-221: updateUploadSession()
private void updateUploadSession(...) {
    // ✅ 여기에 OptimisticLockException 핸들링 추가
}
```

### UploadSession Domain
```java
// Line 194: isActive() - 현재 방어 로직
public boolean isActive() {
    return status == UploadStatus.PENDING && !isExpired();
}

// Line 204: complete() - 상태 전이 로직
public UploadSession complete() {
    if (status != UploadStatus.PENDING && status != UploadStatus.UPLOADING) {
        throw new IllegalStateException(...);
    }
    // ...
}
```

---

## 📝 마이그레이션 스크립트

### MySQL

```sql
-- Optimistic Locking을 위한 version 컬럼 추가
ALTER TABLE upload_session 
ADD COLUMN version BIGINT NOT NULL DEFAULT 0 
COMMENT 'Optimistic locking version';

-- 기존 데이터에 초기 버전 설정 (이미 DEFAULT 0으로 설정됨)
-- UPDATE upload_session SET version = 0 WHERE version IS NULL;

-- 인덱스는 불필요 (Version은 WHERE 조건에 거의 사용되지 않음)
```

### Rollback Script

```sql
-- 롤백이 필요한 경우
ALTER TABLE upload_session DROP COLUMN version;
```

---

## 🧪 테스트 계획

### 1. Optimistic Locking 테스트

```java
@SpringBootTest
class OptimisticLockingTest {
    
    @Test
    void 동시에_같은_세션을_업데이트하면_한개만_성공한다() throws Exception {
        // Given
        String sessionId = "test-session-id";
        UploadSession session = createTestSession(sessionId);
        uploadSessionPort.save(session);
        
        // When: 2개의 스레드가 동시에 업데이트
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        
        CountDownLatch latch = new CountDownLatch(2);
        
        executor.submit(() -> {
            try {
                UploadSession loaded = uploadSessionPort.findById(sessionId).get();
                UploadSession completed = loaded.complete();
                uploadSessionPort.save(completed);
                successCount.incrementAndGet();
            } catch (OptimisticLockException e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });
        
        executor.submit(() -> {
            try {
                UploadSession loaded = uploadSessionPort.findById(sessionId).get();
                UploadSession completed = loaded.complete();
                uploadSessionPort.save(completed);
                successCount.incrementAndGet();
            } catch (OptimisticLockException e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });
        
        latch.await(5, TimeUnit.SECONDS);
        
        // Then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);
    }
}
```

### 2. S3 Event 중복 처리 테스트

```java
@SpringBootTest
class S3EventDuplicateHandlingTest {
    
    @Test
    void 중복_S3_이벤트가_발생해도_한번만_처리된다() {
        // Given
        String sessionId = "test-session-id";
        String eventBody = createS3EventJson(sessionId);
        
        // When: 같은 이벤트 2번 처리
        s3UploadEventHandler.handleS3Event(eventBody);
        s3UploadEventHandler.handleS3Event(eventBody);
        
        // Then
        UploadSession session = uploadSessionPort.findById(sessionId).get();
        assertThat(session.getStatus()).isEqualTo(UploadStatus.COMPLETED);
        
        // 로그 확인 (중복 처리 로그 1개만 있어야 함)
        verify(logger, times(1)).info(contains("Successfully updated"));
        verify(logger, times(1)).info(contains("already updated"));
    }
}
```

---

## 🎯 Expected Outcome

### 성능 개선
- ✅ 불필요한 DB Write 50% 이상 감소
- ✅ 평균 응답 시간 10% 개선

### 운영성 개선
- ✅ 로그 정확성 향상 (중복 로그 제거)
- ✅ 디버깅 용이성 증가
- ✅ 모니터링 정확도 향상

### 안정성 개선
- ✅ 동시성 안정성 보장
- ✅ Race Condition 제거
- ✅ 리소스 효율성 개선

---

## 📚 참고 자료

### JPA Optimistic Locking
- [Hibernate User Guide - Locking](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#locking)
- [Spring Data JPA - Optimistic Locking](https://www.baeldung.com/jpa-optimistic-locking)

### AWS SQS
- [Standard vs FIFO Queues](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/standard-queues.html)
- [FIFO Queue Message Deduplication](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/FIFO-queues-message-deduplication.html)

### Concurrency Patterns
- [Optimistic vs Pessimistic Locking](https://vladmihalcea.com/optimistic-vs-pessimistic-locking/)

---

**Issue Type**: Bug / Enhancement  
**Priority**: P0 (High)  
**Complexity**: Low (Optimistic Locking), Medium (FIFO Queue)  
**Estimated Effort**: 0.5d (Phase 1), 1d (Phase 1 + 2), 3d (All Phases)  
**Labels**: `bug`, `enhancement`, `priority:high`, `performance`, `concurrency`

---

**Created**: 2025-10-13  
**Analyzed By**: Claude Code Analysis
