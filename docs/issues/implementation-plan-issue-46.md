# Issue #46 구현 계획서
## SQS 중복 메시지 처리 및 동시성 제어 개선

**작성일**: 2025-10-13
**담당**: 개발팀
**우선순위**: P0 (Critical)
**예상 작업 시간**: 4-6시간

---

## 📋 작업 개요

### 문제 정의
- **현상**: SQS Standard Queue의 At-Least-Once Delivery 특성으로 중복 메시지 발생 가능
- **근본 원인**: Optimistic Locking 미적용으로 Race Condition 발생
- **영향도**: 불필요한 DB Write, 중복 로그, 리소스 낭비 (데이터 일관성은 안전)

### 해결 방안
- **Option 1 (채택)**: JPA Optimistic Locking (@Version 필드 추가)
- **장점**: 간단한 구현, 표준 기능, 인프라 변경 없음
- **작업 범위**: Entity 수정, 예외 처리, DB 마이그레이션, 테스트 작성

---

## 🎯 구현 상세

### Phase 1: Core Implementation (1-2시간)

#### 1.1 UploadSessionEntity 수정
**파일**: `adapter/adapter-out-persistence-jpa/src/main/java/com/ryuqq/fileflow/adapter/persistence/entity/UploadSessionEntity.java`

**변경사항**:
```java
@Entity
@Table(name = "upload_session", indexes = { /* ... */ })
public class UploadSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ✅ 추가: Optimistic Locking 버전 필드
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "session_id", nullable = false, unique = true, length = 36)
    private String sessionId;

    // ... 나머지 필드들
}
```

**추가 작업**:
- `getVersion()` getter 메서드 추가
- `reconstituteWithId()` factory method에 version 파라미터 추가
- JavaDoc 업데이트

---

#### 1.2 UploadSessionMapper 수정
**파일**: `adapter/adapter-out-persistence-jpa/src/main/java/com/ryuqq/fileflow/adapter/persistence/mapper/UploadSessionMapper.java`

**변경사항**:
```java
public UploadSessionEntity toEntity(UploadSession domain, String presignedUrlJson, String multipartUploadInfoJson) {
    if (domain.getId() == null) {
        // 신규 생성
        return UploadSessionEntity.of(
            domain.getSessionId(),
            // ... 기존 파라미터들
        );
    } else {
        // 기존 엔티티 업데이트
        return UploadSessionEntity.reconstituteWithId(
            domain.getId(),
            domain.getSessionId(),
            // ... 기존 파라미터들,
            domain.getCreatedAt(),
            domain.getVersion()  // ✅ 추가
        );
    }
}

public UploadSession toDomain(UploadSessionEntity entity) {
    return UploadSession.reconstitute(
        entity.getId(),
        // ... 기존 파라미터들,
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.getVersion()  // ✅ 추가
    );
}
```

---

#### 1.3 UploadSession 도메인 모델 수정
**파일**: `domain/src/main/java/com/ryuqq/fileflow/domain/upload/vo/UploadSession.java`

**변경사항**:
```java
public class UploadSession {
    private final Long id;
    private final String sessionId;
    private final Long version;  // ✅ 추가
    // ... 나머지 필드들

    // reconstitute() factory method에 version 파라미터 추가
    public static UploadSession reconstitute(
        Long id,
        String sessionId,
        // ... 기존 파라미터들,
        Long version  // ✅ 추가
    ) {
        return new UploadSession(id, sessionId, /* ... */, version);
    }

    public Long getVersion() {
        return version;
    }
}
```

---

### Phase 2: Exception Handling (30분)

#### 2.1 S3UploadEventHandler 수정
**파일**: `adapter/adapter-out-aws-sqs/src/main/java/com/ryuqq/fileflow/adapter/sqs/handler/S3UploadEventHandler.java`

**변경 위치**: `updateUploadSession()` 메서드 (Line 195-221)

**변경사항**:
```java
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

    log.info("Successfully updated upload session: {} to COMPLETED. " +
            "S3 object: {}, Size: {} bytes, ETag: {}",
            sessionId,
            s3Location.toUri(),
            record.getS3().getObject().getSize(),
            record.getS3().getObject().geteTag()
    );

} catch (OptimisticLockException e) {  // ✅ 추가
    // 이미 다른 스레드가 업데이트함, 안전하게 무시
    log.info("Session {} already updated by another thread. Skipping duplicate update. " +
             "This is expected behavior for duplicate S3 events.", sessionId);
    return;

} catch (IllegalStateException e) {
    log.error("Failed to complete upload session: {}. Error: {}",
            sessionId, e.getMessage(), e);
    throw new SessionMatchingException(
            "Failed to complete upload session: " + sessionId, e
    );
}
```

**Import 추가**:
```java
import javax.persistence.OptimisticLockException;
```

---

#### 2.2 ConfirmUploadService 검증
**파일**: `application/src/main/java/com/ryuqq/fileflow/application/upload/service/ConfirmUploadService.java`

**현재 상태 분석**:
- Line 147에서 `uploadSessionPort.save()` 호출
- 이미 JPA save() 메서드 사용 중
- **결론**: 자동으로 OptimisticLockException 발생, 추가 처리 불필요
- 단, 로그 레벨 확인 필요 (현재 예외 전파 방식 검토)

---

### Phase 3: Database Migration (30분)

#### 3.1 마이그레이션 스크립트 작성
**파일**: `adapter/adapter-out-persistence-jpa/src/main/resources/db/migration/V13__add_version_for_optimistic_locking.sql`

**내용**:
```sql
-- ================================================================
-- Migration: V13__add_version_for_optimistic_locking.sql
-- Purpose: Add version column for JPA Optimistic Locking
-- Issue: #46 - SQS 중복 메시지 처리 및 동시성 제어 개선
-- Author: Development Team
-- Date: 2025-10-13
-- ================================================================

-- Step 1: Add version column with default value
ALTER TABLE upload_session
ADD COLUMN version BIGINT NOT NULL DEFAULT 0
COMMENT 'Optimistic locking version for concurrency control (Issue #46)';

-- Step 2: Verify column addition
-- Expected: version column exists with BIGINT type
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_DEFAULT,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'upload_session'
  AND COLUMN_NAME = 'version';

-- Step 3: Initialize existing records (already handled by DEFAULT 0)
-- All existing records will have version = 0 after ALTER TABLE

-- ================================================================
-- Rollback Instructions (for emergency use only):
-- ================================================================
-- To rollback this migration:
-- ALTER TABLE upload_session DROP COLUMN version;
-- ================================================================

-- Note: No index needed for version column
-- Version is used in WHERE clause during UPDATE but not for lookups
-- Index would not provide significant performance benefit
```

**검증 쿼리**:
```sql
-- 마이그레이션 후 검증
SELECT COUNT(*) as total_records,
       COUNT(DISTINCT version) as distinct_versions,
       MIN(version) as min_version,
       MAX(version) as max_version
FROM upload_session;
-- Expected: all records have version = 0 initially
```

---

### Phase 4: Testing (2-3시간)

#### 4.1 OptimisticLockingConcurrencyTest
**파일**: `adapter/adapter-out-persistence-jpa/src/test/java/com/ryuqq/fileflow/adapter/persistence/concurrency/OptimisticLockingConcurrencyTest.java`

**목적**: JPA Optimistic Locking 기본 동작 검증

**테스트 시나리오**:
```java
@SpringBootTest
@Transactional
class OptimisticLockingConcurrencyTest {

    @Autowired
    private UploadSessionPort uploadSessionPort;

    @Test
    @DisplayName("동시에 같은 세션을 업데이트하면 한 개만 성공한다")
    void whenConcurrentUpdate_thenOnlyOneSucceeds() throws Exception {
        // Given: PENDING 상태의 세션 생성
        String sessionId = "test-session-" + UUID.randomUUID();
        UploadSession session = createTestSession(sessionId, UploadStatus.PENDING);
        uploadSessionPort.save(session);

        // When: 2개의 스레드가 동시에 같은 세션을 COMPLETED로 업데이트
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        Runnable updateTask = () -> {
            try {
                startLatch.await(); // 동시 실행 보장

                // 트랜잭션 내에서 실행
                TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                transactionTemplate.execute(status -> {
                    UploadSession loaded = uploadSessionPort.findById(sessionId).orElseThrow();
                    UploadSession completed = loaded.complete();
                    uploadSessionPort.save(completed);
                    return null;
                });

                successCount.incrementAndGet();

            } catch (OptimisticLockException e) {
                failCount.incrementAndGet();
            } catch (Exception e) {
                fail("Unexpected exception: " + e.getMessage());
            } finally {
                doneLatch.countDown();
            }
        };

        executor.submit(updateTask);
        executor.submit(updateTask);

        startLatch.countDown(); // 동시 실행 시작
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then: 1개는 성공, 1개는 OptimisticLockException
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);

        // DB 검증: COMPLETED 상태, version = 1
        UploadSession finalSession = uploadSessionPort.findById(sessionId).orElseThrow();
        assertThat(finalSession.getStatus()).isEqualTo(UploadStatus.COMPLETED);
        assertThat(finalSession.getVersion()).isEqualTo(1L);
    }

    @RepeatedTest(10)
    @DisplayName("반복 테스트: 동시성 안정성 검증")
    void repeatedConcurrencyTest() throws Exception {
        whenConcurrentUpdate_thenOnlyOneSucceeds();
    }

    private UploadSession createTestSession(String sessionId, UploadStatus status) {
        // 테스트용 세션 생성 로직
        return UploadSession.of(
            sessionId,
            null, // idempotencyKey
            "test-tenant",
            "test-uploader",
            "test-policy",
            "test.jpg",
            "image/jpeg",
            1024L,
            null, // checksum
            status,
            "https://presigned-url",
            null, // multipartInfo
            LocalDateTime.now().plusHours(1)
        );
    }
}
```

**예상 결과**:
- ✅ successCount = 1, failCount = 1
- ✅ 최종 상태: COMPLETED, version = 1
- ✅ @RepeatedTest 10회 모두 성공

---

#### 4.2 S3EventDuplicateHandlingIntegrationTest
**파일**: `adapter/adapter-out-aws-sqs/src/test/java/com/ryuqq/fileflow/adapter/sqs/integration/S3EventDuplicateHandlingIntegrationTest.java`

**목적**: 중복 S3 이벤트 처리 시나리오 검증

**테스트 시나리오**:
```java
@SpringBootTest
class S3EventDuplicateHandlingIntegrationTest {

    @Autowired
    private S3UploadEventHandler s3UploadEventHandler;

    @Autowired
    private UploadSessionPort uploadSessionPort;

    @Test
    @DisplayName("중복 S3 이벤트가 발생해도 한 번만 처리된다")
    void whenDuplicateS3Event_thenProcessedOnce() {
        // Given: PENDING 상태의 세션 생성
        String sessionId = "test-session-" + UUID.randomUUID();
        UploadSession session = createTestSession(sessionId);
        uploadSessionPort.save(session);

        // S3 이벤트 JSON 생성
        String s3EventJson = createS3EventJson(sessionId, "uploads/" + sessionId + "/test.jpg");

        // When: 같은 이벤트를 2번 처리 (중복 이벤트 시뮬레이션)
        s3UploadEventHandler.handleS3Event(s3EventJson); // 첫 번째 처리
        s3UploadEventHandler.handleS3Event(s3EventJson); // 중복 처리

        // Then: 세션은 COMPLETED 상태, 1번만 업데이트됨
        UploadSession finalSession = uploadSessionPort.findById(sessionId).orElseThrow();
        assertThat(finalSession.getStatus()).isEqualTo(UploadStatus.COMPLETED);
        assertThat(finalSession.getVersion()).isEqualTo(1L); // 한 번만 업데이트

        // 로그 검증 (선택적)
        // verify(logger, times(1)).info(contains("Successfully updated"));
        // verify(logger, times(1)).info(contains("already updated by another thread"));
    }

    @Test
    @DisplayName("동시에 중복 S3 이벤트가 발생해도 한 번만 처리된다")
    void whenConcurrentDuplicateS3Events_thenProcessedOnce() throws Exception {
        // Given
        String sessionId = "test-session-" + UUID.randomUUID();
        UploadSession session = createTestSession(sessionId);
        uploadSessionPort.save(session);

        String s3EventJson = createS3EventJson(sessionId, "uploads/" + sessionId + "/test.jpg");

        // When: 2개 스레드가 동시에 같은 이벤트 처리
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        Runnable processTask = () -> {
            try {
                startLatch.await();
                s3UploadEventHandler.handleS3Event(s3EventJson);
            } catch (Exception e) {
                // OptimisticLockException은 내부에서 처리되므로 여기까지 전파 안됨
            } finally {
                doneLatch.countDown();
            }
        };

        executor.submit(processTask);
        executor.submit(processTask);

        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        assertThat(completed).isTrue();

        UploadSession finalSession = uploadSessionPort.findById(sessionId).orElseThrow();
        assertThat(finalSession.getStatus()).isEqualTo(UploadStatus.COMPLETED);
        assertThat(finalSession.getVersion()).isEqualTo(1L);
    }

    private String createS3EventJson(String sessionId, String s3Key) {
        // S3 이벤트 JSON 생성 로직
        return String.format("""
            {
              "Records": [{
                "eventName": "ObjectCreated:Put",
                "s3": {
                  "bucket": {"name": "test-bucket"},
                  "object": {
                    "key": "%s",
                    "size": 1024,
                    "eTag": "abc123"
                  }
                }
              }]
            }
            """, s3Key);
    }
}
```

**예상 결과**:
- ✅ 중복 이벤트 처리 시 INFO 로그 발생
- ✅ DB는 1번만 업데이트 (version = 1)
- ✅ 최종 상태: COMPLETED

---

#### 4.3 ConcurrentConfirmAndS3EventIntegrationTest
**파일**: `adapter/adapter-in-rest-api/src/test/java/com/ryuqq/fileflow/adapter/rest/integration/ConcurrentConfirmAndS3EventIntegrationTest.java`

**목적**: Client Confirm + S3 Event 동시 처리 검증

**테스트 시나리오**:
```java
@SpringBootTest
@AutoConfigureMockMvc
class ConcurrentConfirmAndS3EventIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConfirmUploadService confirmUploadService;

    @Autowired
    private S3UploadEventHandler s3UploadEventHandler;

    @Autowired
    private UploadSessionPort uploadSessionPort;

    @MockBean
    private VerifyS3ObjectPort verifyS3ObjectPort;

    @Test
    @DisplayName("Client Confirm과 S3 Event가 동시에 발생해도 한 번만 처리된다")
    void whenConcurrentConfirmAndS3Event_thenProcessedOnce() throws Exception {
        // Given: PENDING 상태의 세션 생성
        String sessionId = "test-session-" + UUID.randomUUID();
        String uploadPath = "uploads/" + sessionId + "/test.jpg";

        UploadSession session = createTestSession(sessionId);
        uploadSessionPort.save(session);

        // S3 검증 Mock 설정
        when(verifyS3ObjectPort.doesObjectExist(anyString(), anyString())).thenReturn(true);
        when(verifyS3ObjectPort.getObjectETag(anyString(), anyString())).thenReturn("abc123");

        // When: Client Confirm과 S3 Event를 동시에 처리
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicReference<Exception> confirmException = new AtomicReference<>();
        AtomicReference<Exception> s3EventException = new AtomicReference<>();

        // Thread 1: Client Confirm
        Runnable confirmTask = () -> {
            try {
                startLatch.await();
                ConfirmUploadCommand command = new ConfirmUploadCommand(
                    sessionId,
                    uploadPath,
                    "abc123"
                );
                confirmUploadService.confirm(command);
            } catch (Exception e) {
                confirmException.set(e);
            } finally {
                doneLatch.countDown();
            }
        };

        // Thread 2: S3 Event
        Runnable s3EventTask = () -> {
            try {
                startLatch.await();
                String s3EventJson = createS3EventJson(sessionId, uploadPath);
                s3UploadEventHandler.handleS3Event(s3EventJson);
            } catch (Exception e) {
                s3EventException.set(e);
            } finally {
                doneLatch.countDown();
            }
        };

        executor.submit(confirmTask);
        executor.submit(s3EventTask);

        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then: 둘 중 하나는 성공, 나머지는 OptimisticLockException (또는 내부 처리)
        assertThat(completed).isTrue();

        // 최종 세션 상태 검증
        UploadSession finalSession = uploadSessionPort.findById(sessionId).orElseThrow();
        assertThat(finalSession.getStatus()).isEqualTo(UploadStatus.COMPLETED);
        assertThat(finalSession.getVersion()).isEqualTo(1L); // 한 번만 업데이트

        // 예외 검증 (선택적)
        // 둘 중 하나는 OptimisticLockException 발생 가능
        // 하지만 S3UploadEventHandler는 내부에서 처리하므로 외부로 전파 안됨
    }
}
```

**예상 결과**:
- ✅ 두 스레드 중 하나만 성공적으로 완료
- ✅ 최종 상태: COMPLETED, version = 1
- ✅ 데이터 일관성 유지

---

### Phase 5: Integration & Validation (1시간)

#### 5.1 기존 테스트 실행
**실행 명령**:
```bash
# 전체 테스트 실행
./gradlew clean test

# 특정 모듈만 실행
./gradlew :adapter:adapter-out-persistence-jpa:test
./gradlew :adapter:adapter-out-aws-sqs:test
./gradlew :application:test
```

**검증 항목**:
- ✅ 모든 기존 테스트 통과
- ✅ OptimisticLockException 관련 실패 없음
- ✅ UploadSessionEntity 관련 테스트 통과

#### 5.2 통합 테스트 결과 분석
**확인 항목**:
1. **성공률**: 새로운 테스트 3개 모두 통과
2. **반복 테스트**: @RepeatedTest 안정성 검증
3. **로그 검증**: INFO 레벨 로그 정상 출력
4. **성능 영향**: version 컬럼 추가로 인한 쿼리 성능 확인

#### 5.3 문서 업데이트
**업데이트 대상**:
1. `docs/issues/concurrency-and-duplicate-message-handling.md`
   - Phase 1 완료 체크
   - 구현 결과 기록
2. README 또는 CHANGELOG
   - Issue #46 해결 내역 추가

---

## 📊 검증 기준

### 기능 검증
- [ ] OptimisticLockException 정상 발생 및 처리
- [ ] 중복 S3 이벤트 처리 시 1번만 업데이트
- [ ] Client Confirm + S3 Event 동시 처리 시 1번만 업데이트
- [ ] version 컬럼 자동 증가 (0 → 1 → 2 ...)

### 성능 검증
- [ ] 단위 테스트 실행 시간 < 10초
- [ ] 통합 테스트 실행 시간 < 30초
- [ ] version 컬럼 추가로 인한 쿼리 성능 저하 없음 (< 5%)

### 안정성 검증
- [ ] @RepeatedTest(10) 모두 성공
- [ ] 동시성 테스트 100% 성공률
- [ ] 기존 테스트 100% 통과

---

## 🚀 배포 계획

### 1. 개발 환경 배포
- 브랜치: `feature/KAN-46-optimistic-locking-concurrency-fix`
- 테스트: 모든 테스트 통과 확인
- DB 마이그레이션: V13 자동 실행

### 2. 스테이징 환경 배포
- PR 생성 및 코드 리뷰
- 스테이징 DB 마이그레이션 검증
- 통합 테스트 재실행

### 3. 프로덕션 배포
- DB 마이그레이션 계획:
  - ALTER TABLE은 비파괴적 (DEFAULT 0)
  - 다운타임 없음
  - 롤백 가능 (DROP COLUMN)
- 모니터링:
  - OptimisticLockException 발생 빈도
  - 중복 메시지 처리 메트릭
  - DB 쿼리 성능

---

## 📝 체크리스트

### 코드 구현
- [ ] UploadSessionEntity @Version 필드 추가
- [ ] UploadSessionMapper version 필드 매핑 추가
- [ ] UploadSession 도메인 version 필드 추가
- [ ] S3UploadEventHandler OptimisticLockException 처리
- [ ] DB 마이그레이션 V13 작성

### 테스트 작성
- [ ] OptimisticLockingConcurrencyTest 작성
- [ ] S3EventDuplicateHandlingIntegrationTest 작성
- [ ] ConcurrentConfirmAndS3EventIntegrationTest 작성
- [ ] 모든 테스트 통과 확인

### 문서화
- [ ] 구현 계획서 작성 (현재 문서)
- [ ] JavaDoc 업데이트
- [ ] CHANGELOG 업데이트
- [ ] 이슈 문서 업데이트

### 배포 준비
- [ ] PR 생성
- [ ] 코드 리뷰 요청
- [ ] DB 마이그레이션 검증
- [ ] 배포 계획 수립

---

## 🔗 참고 자료

### 내부 문서
- [Issue #46 원본 문서](./concurrency-and-duplicate-message-handling.md)
- UploadSessionEntity: `adapter/adapter-out-persistence-jpa/.../UploadSessionEntity.java`
- S3UploadEventHandler: `adapter/adapter-out-aws-sqs/.../S3UploadEventHandler.java`
- ConfirmUploadService: `application/.../ConfirmUploadService.java`

### 외부 참고
- [Hibernate Optimistic Locking](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#locking)
- [Spring Data JPA Optimistic Locking](https://www.baeldung.com/jpa-optimistic-locking)
- [AWS SQS Standard Queue](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/standard-queues.html)

---

**최종 업데이트**: 2025-10-13
**상태**: 계획 완료, 구현 대기 중
