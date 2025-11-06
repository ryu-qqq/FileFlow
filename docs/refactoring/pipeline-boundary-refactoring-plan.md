# Pipeline 바운더리 리팩토링 계획서

## 📋 개요

**목적**: Pipeline 바운더리의 Critical/Important 이슈 해결 및 아키텍처 개선
**바운더리 컨텍스트**: `Pipeline` (파일 처리 파이프라인)
**패턴**: Transactional Outbox + Orchestrator Pattern (프로젝트 디폴트 컨벤션 준수)
**아키텍처**: CQRS (Command/Query 분리), Hexagonal Architecture

---

## 🎯 작업 범위

### 1. 명명 규칙 통일
- 현재: `PipelineOutbox` (바운더리 불명확)
- 변경: `PipelineOutbox` → 유지하되, 패키지 구조로 명확화
  - `domain.pipeline.PipelineOutbox`
  - `application.pipeline.orchestration.PipelineOrchestrator`

### 2. Orchestrator 패턴 선택
**프로젝트 디폴트 컨벤션 사용** (`docs/coding_convention/09-orchestration-patterns/`)

#### 선택한 패턴: **3-Phase Lifecycle with WAL**
```
Phase 1: Persist (WAL 저장)
Phase 2: Execute (비즈니스 로직 - @Async)
Phase 3: Finalize (상태 업데이트)
```

#### 이유:
1. **트랜잭션 경계 명확**: WAL 저장 → 비동기 실행 → 상태 업데이트 분리
2. **크래시 복구**: Finalizer/Reaper 패턴으로 실패 복구
3. **Idempotency 보장**: IdemKey + UNIQUE 제약
4. **Outcome Modeling**: Sealed interface로 성공/재시도/실패 구분

---

## 🚨 Critical Issues (우선순위 1)

### Issue #1: OutboxStatus 패키지 위치 문제 (DDD 위반)

#### 현재 상태
```
domain/
  ├── download/
  │   └── OutboxStatus.java  ❌ 잘못된 위치
  └── pipeline/
      └── PipelineOutbox.java  → OutboxStatus 의존 (DDD 경계 위반)
```

#### 변경 계획
```
domain/
  ├── common/
  │   └── OutboxStatus.java  ✅ 공통 패키지로 이동
  ├── download/
  └── pipeline/
      └── PipelineOutbox.java  → domain.common.OutboxStatus 의존
```

#### 작업 내용
1. **파일 이동**: `domain.download.OutboxStatus` → `domain.common.OutboxStatus`
2. **Import 수정**: 모든 참조 파일의 import 업데이트
   - `PipelineOutbox.java`
   - `PipelineOutboxJpaEntity.java`
   - `PipelineOutboxPersistenceAdapter.java`
   - 기타 Download 바운더리 파일들

#### 준수 컨벤션
- **Domain Layer**: Pure Java (Lombok 금지)
- **Law of Demeter**: Getter 체이닝 금지
- **Javadoc 필수**: `@author Sangwon Ryu`, `@since 1.0.0`

---

### Issue #2: Worker-Outbox 상태 불일치 (설계 결함)

#### 현재 문제
```java
// PipelineOutboxScheduler.java (❌ 잘못된 설계)
@Scheduled(...)
public void processOutboxMessages() {
    List<PipelineOutbox> messages = outboxManager.findNewMessages(10);

    for (PipelineOutbox outbox : messages) {
        outboxManager.markProcessing(outbox);  // PENDING → PROCESSING

        pipelineWorker.startPipeline(outbox.getFileId().getValue());  // @Async 호출

        outboxManager.markProcessed(outbox);  // ❌ Worker 완료 전에 COMPLETED로 변경!
    }
}
```

**문제**: Worker가 실제로 완료되기 전에 Outbox가 COMPLETED로 마킹됨

#### 변경 계획: Orchestrator Pattern 적용

##### 1. Orchestrator 구조 (컨벤션 준수)
```
application/
  └── pipeline/
      └── orchestration/
          ├── command/
          │   └── PipelineTriggerCommand.java  (Record 패턴)
          ├── entity/
          │   └── PipelineTriggerOperationEntity.java  (@UniqueConstraint)
          ├── orchestrator/
          │   └── PipelineTriggerOrchestrator.java  (@Async)
          ├── outcome/
          │   └── PipelineTriggerOutcome.java  (Sealed interface)
          ├── status/
          │   └── PipelineTriggerOperationStatus.java  (Enum)
          ├── wal/
          │   └── PipelineTriggerWriteAheadLog.java
          ├── finalizer/
          │   └── PipelineTriggerFinalizer.java  (@Scheduled)
          └── reaper/
              └── PipelineTriggerReaper.java  (@Scheduled)
```

##### 2. Command 설계 (Record 패턴)
```java
package com.ryuqq.fileflow.application.pipeline.orchestration.command;

import com.ryuqq.fileflow.domain.file.FileId;
import com.ryuqq.fileflow.domain.common.IdempotencyKey;

/**
 * Pipeline 트리거 Command
 *
 * <p><strong>패턴:</strong> Record 패턴 (Lombok 금지)</p>
 * <p><strong>Validation:</strong> Compact Constructor</p>
 *
 * @param idempotencyKey Idempotency 키 (중복 방지)
 * @param fileId 파일 ID
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record PipelineTriggerCommand(
    IdempotencyKey idempotencyKey,
    FileId fileId
) {
    /**
     * Compact Constructor (Validation)
     */
    public PipelineTriggerCommand {
        if (idempotencyKey == null) {
            throw new IllegalArgumentException("IdempotencyKey는 필수입니다.");
        }
        if (fileId == null) {
            throw new IllegalArgumentException("FileId는 필수입니다.");
        }
    }

    /**
     * Command 생성 Factory 메서드
     *
     * @param idempotencyKey Idempotency 키
     * @param fileId 파일 ID
     * @return PipelineTriggerCommand
     */
    public static PipelineTriggerCommand of(IdempotencyKey idempotencyKey, FileId fileId) {
        return new PipelineTriggerCommand(idempotencyKey, fileId);
    }
}
```

##### 3. Orchestrator 설계
```java
package com.ryuqq.fileflow.application.pipeline.orchestration.orchestrator;

import com.ryuqq.fileflow.application.pipeline.orchestration.command.PipelineTriggerCommand;
import com.ryuqq.fileflow.application.pipeline.orchestration.outcome.PipelineTriggerOutcome;
import com.ryuqq.fileflow.application.pipeline.orchestration.wal.PipelineTriggerWriteAheadLog;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

/**
 * Pipeline 트리거 Orchestrator
 *
 * <p><strong>패턴:</strong> 3-Phase Lifecycle</p>
 * <ul>
 *   <li>Phase 1: Persist - WAL 저장 (트랜잭션 내)</li>
 *   <li>Phase 2: Execute - 비즈니스 로직 (@Async, 트랜잭션 밖)</li>
 *   <li>Phase 3: Finalize - 상태 업데이트 (Finalizer가 처리)</li>
 * </ul>
 *
 * <p><strong>중요:</strong></p>
 * <ul>
 *   <li>❌ executeInternal()에 @Transactional 사용 금지</li>
 *   <li>✅ executeInternal()에 @Async 필수</li>
 *   <li>✅ Outcome 반환 (Ok/Retry/Fail)</li>
 *   <li>❌ Exception throw 금지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class PipelineTriggerOrchestrator {

    private final PipelineTriggerWriteAheadLog writeAheadLog;
    private final PipelineWorker pipelineWorker;

    public PipelineTriggerOrchestrator(
        PipelineTriggerWriteAheadLog writeAheadLog,
        PipelineWorker pipelineWorker
    ) {
        this.writeAheadLog = writeAheadLog;
        this.pipelineWorker = pipelineWorker;
    }

    /**
     * Pipeline 트리거 (Public 진입점)
     *
     * @param command Pipeline 트리거 Command
     * @return CompletableFuture<PipelineTriggerOutcome>
     */
    @Transactional
    public CompletableFuture<PipelineTriggerOutcome> trigger(PipelineTriggerCommand command) {
        // Phase 1: Persist (WAL 저장)
        writeAheadLog.persist(command);

        // Phase 2: Execute (비동기 실행)
        return executeInternal(command);
    }

    /**
     * 비즈니스 로직 실행 (Private, @Async)
     *
     * <p><strong>중요:</strong></p>
     * <ul>
     *   <li>✅ @Async 필수 (트랜잭션 밖에서 실행)</li>
     *   <li>❌ @Transactional 금지</li>
     *   <li>✅ Outcome 반환 (예외 throw 금지)</li>
     * </ul>
     *
     * @param command Pipeline 트리거 Command
     * @return CompletableFuture<PipelineTriggerOutcome>
     */
    @Async
    CompletableFuture<PipelineTriggerOutcome> executeInternal(PipelineTriggerCommand command) {
        try {
            // 외부 API 호출 (트랜잭션 밖)
            pipelineWorker.startPipeline(command.fileId().getValue());

            return CompletableFuture.completedFuture(
                PipelineTriggerOutcome.ok(command.idempotencyKey())
            );

        } catch (RetryableException e) {
            return CompletableFuture.completedFuture(
                PipelineTriggerOutcome.retry(command.idempotencyKey(), e.getMessage())
            );

        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                PipelineTriggerOutcome.fail(command.idempotencyKey(), e.getMessage())
            );
        }
    }
}
```

##### 4. Outcome Modeling (Sealed interface)
```java
package com.ryuqq.fileflow.application.pipeline.orchestration.outcome;

import com.ryuqq.fileflow.domain.common.IdempotencyKey;

/**
 * Pipeline 트리거 Outcome (Sealed interface)
 *
 * <p><strong>패턴:</strong> Outcome Modeling</p>
 * <ul>
 *   <li>Ok: 성공</li>
 *   <li>Retry: 재시도 가능한 실패</li>
 *   <li>Fail: 영구적 실패</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public sealed interface PipelineTriggerOutcome
    permits PipelineTriggerOutcome.Ok,
            PipelineTriggerOutcome.Retry,
            PipelineTriggerOutcome.Fail {

    IdempotencyKey idempotencyKey();

    /**
     * 성공 Outcome
     */
    record Ok(IdempotencyKey idempotencyKey) implements PipelineTriggerOutcome {
    }

    /**
     * 재시도 가능한 실패 Outcome
     */
    record Retry(IdempotencyKey idempotencyKey, String reason) implements PipelineTriggerOutcome {
    }

    /**
     * 영구적 실패 Outcome
     */
    record Fail(IdempotencyKey idempotencyKey, String reason) implements PipelineTriggerOutcome {
    }

    /**
     * Ok Outcome 생성
     */
    static Ok ok(IdempotencyKey idempotencyKey) {
        return new Ok(idempotencyKey);
    }

    /**
     * Retry Outcome 생성
     */
    static Retry retry(IdempotencyKey idempotencyKey, String reason) {
        return new Retry(idempotencyKey, reason);
    }

    /**
     * Fail Outcome 생성
     */
    static Fail fail(IdempotencyKey idempotencyKey, String reason) {
        return new Fail(idempotencyKey, reason);
    }
}
```

##### 5. Finalizer 설계 (@Scheduled)
```java
package com.ryuqq.fileflow.application.pipeline.orchestration.finalizer;

import com.ryuqq.fileflow.application.pipeline.orchestration.entity.PipelineTriggerOperationEntity;
import com.ryuqq.fileflow.application.pipeline.orchestration.status.PipelineTriggerOperationStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Pipeline 트리거 Finalizer
 *
 * <p><strong>역할:</strong> PROCESSING 상태 Operation을 COMPLETED/FAILED로 전환</p>
 * <p><strong>실행 주기:</strong> 30초마다</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class PipelineTriggerFinalizer {

    private final PipelineTriggerOperationRepository operationRepository;

    public PipelineTriggerFinalizer(PipelineTriggerOperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    /**
     * PROCESSING 상태 Operation 최종화
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    @Transactional
    public void finalize() {
        List<PipelineTriggerOperationEntity> operations =
            operationRepository.findByStatus(PipelineTriggerOperationStatus.PROCESSING);

        for (PipelineTriggerOperationEntity operation : operations) {
            // Outcome 확인 후 상태 업데이트
            if (operation.isCompleted()) {
                operation.markCompleted();
            } else if (operation.isFailed()) {
                operation.markFailed();
            }
            operationRepository.save(operation);
        }
    }
}
```

#### 준수 컨벤션
- **Orchestration Pattern**: 디폴트 컨벤션 (`docs/coding_convention/09-orchestration-patterns/`)
- **Command**: Record 패턴 (Lombok 금지)
- **Orchestrator**: `@Async` 필수, `@Transactional` 금지 (executeInternal)
- **Outcome**: Sealed interface (성공/재시도/실패 명확화)
- **WAL**: 트랜잭션 내 저장
- **Finalizer**: @Scheduled로 상태 업데이트

---

### Issue #3: Repository 메서드 누락 (컴파일 에러 위험)

#### 현재 문제
```java
// PipelineOutboxPersistenceAdapter.java
public List<PipelineOutbox> findByStatus(OutboxStatus status, int batchSize) {
    List<PipelineOutboxJpaEntity> entities =
        repository.findByStatusOrderByCreatedAtAsc(status);  // ❌ 메서드 없음!

    return entities.stream()
        .limit(batchSize)  // ⚠️ 메모리 필터링 (DB가 아님)
        .map(mapper::toDomain)
        .collect(Collectors.toList());
}
```

#### 변경 계획

##### 1. Repository 메서드 추가 (Spring Data JPA)
```java
package com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.entity.PipelineOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PipelineOutbox JPA Repository
 *
 * <p><strong>패턴:</strong> Spring Data JPA (Query Method)</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface PipelineOutboxJpaRepository extends JpaRepository<PipelineOutboxJpaEntity, Long> {

    /**
     * 상태별 Outbox 조회 (생성일 오름차순, Batch 크기 제한)
     *
     * <p><strong>중요:</strong> DB 레벨에서 LIMIT 적용 (메모리 필터링 금지)</p>
     *
     * @param status Outbox 상태
     * @param batchSize 조회 개수
     * @return PipelineOutboxJpaEntity 리스트
     */
    @Query("SELECT po FROM PipelineOutboxJpaEntity po " +
           "WHERE po.status = :status " +
           "ORDER BY po.createdAt ASC " +
           "LIMIT :batchSize")
    List<PipelineOutboxJpaEntity> findByStatusOrderByCreatedAtAsc(
        @Param("status") OutboxStatus status,
        @Param("batchSize") int batchSize
    );

    /**
     * 재시도 가능한 FAILED Outbox 조회
     *
     * @param status FAILED 상태
     * @param retryAfter 재시도 가능 시간
     * @param maxRetryCount 최대 재시도 횟수
     * @param batchSize 조회 개수
     * @return PipelineOutboxJpaEntity 리스트
     */
    @Query("SELECT po FROM PipelineOutboxJpaEntity po " +
           "WHERE po.status = :status " +
           "AND po.updatedAt <= :retryAfter " +
           "AND po.retryCount < :maxRetryCount " +
           "ORDER BY po.updatedAt ASC " +
           "LIMIT :batchSize")
    List<PipelineOutboxJpaEntity> findRetryableFailedOutboxes(
        @Param("status") OutboxStatus status,
        @Param("retryAfter") LocalDateTime retryAfter,
        @Param("maxRetryCount") int maxRetryCount,
        @Param("batchSize") int batchSize
    );

    /**
     * Stale PROCESSING Outbox 조회 (Worker 크래시 복구용)
     *
     * @param status PROCESSING 상태
     * @param staleThreshold Stale 기준 시간
     * @param batchSize 조회 개수
     * @return PipelineOutboxJpaEntity 리스트
     */
    @Query("SELECT po FROM PipelineOutboxJpaEntity po " +
           "WHERE po.status = :status " +
           "AND po.updatedAt <= :staleThreshold " +
           "ORDER BY po.updatedAt ASC " +
           "LIMIT :batchSize")
    List<PipelineOutboxJpaEntity> findStaleProcessingOutboxes(
        @Param("status") OutboxStatus status,
        @Param("staleThreshold") LocalDateTime staleThreshold,
        @Param("batchSize") int batchSize
    );
}
```

##### 2. Adapter 수정 (메서드 호출 변경)
```java
// PipelineOutboxPersistenceAdapter.java (수정 전)
public List<PipelineOutbox> findByStatus(OutboxStatus status, int batchSize) {
    List<PipelineOutboxJpaEntity> entities =
        repository.findByStatusOrderByCreatedAtAsc(status);  // ❌ batchSize 없음

    return entities.stream()
        .limit(batchSize)  // ❌ 메모리 필터링
        .map(mapper::toDomain)
        .collect(Collectors.toList());
}

// PipelineOutboxPersistenceAdapter.java (수정 후)
public List<PipelineOutbox> findByStatus(OutboxStatus status, int batchSize) {
    List<PipelineOutboxJpaEntity> entities =
        repository.findByStatusOrderByCreatedAtAsc(status, batchSize);  // ✅ DB 레벨 LIMIT

    return entities.stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
}
```

#### 준수 컨벤션
- **Persistence Layer**: Spring Data JPA Query Method
- **DB 레벨 제한**: `LIMIT` 사용 (메모리 필터링 금지)
- **Long FK 전략**: JPA 관계 어노테이션 금지 (`@ManyToOne`, `@OneToMany` 등)
- **Javadoc 필수**: `@author`, `@since` 포함

---

## 📌 Important Issues (우선순위 2)

### Issue #4: Domain Exception 미구현

#### 현재 문제
```java
// PipelineOutbox.java
public void startProcessing() {
    if (this.status != OutboxStatus.PENDING) {
        throw new IllegalStateException("PENDING 상태에서만 처리를 시작할 수 있습니다.");  // ❌ Generic Exception
    }
}
```

#### 변경 계획

##### 1. Domain Exception 설계
```java
package com.ryuqq.fileflow.domain.pipeline.exception;

/**
 * Pipeline 도메인 예외 (Base)
 *
 * <p><strong>패턴:</strong> Domain Exception Hierarchy</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class PipelineDomainException extends RuntimeException {

    private final String errorCode;

    protected PipelineDomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected PipelineDomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
```

```java
package com.ryuqq.fileflow.domain.pipeline.exception;

import com.ryuqq.fileflow.domain.common.OutboxStatus;

/**
 * Pipeline Outbox 상태 전환 예외
 *
 * <p><strong>발생 시점:</strong> 잘못된 상태에서 상태 전환 시도</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class InvalidOutboxStatusTransitionException extends PipelineDomainException {

    private static final String ERROR_CODE = "PIPELINE_001";

    public InvalidOutboxStatusTransitionException(OutboxStatus current, OutboxStatus target) {
        super(
            ERROR_CODE,
            String.format("상태 전환 실패: %s → %s는 허용되지 않습니다.", current, target)
        );
    }
}
```

##### 2. Domain 수정 (Exception 적용)
```java
// PipelineOutbox.java (수정 전)
public void startProcessing() {
    if (this.status != OutboxStatus.PENDING) {
        throw new IllegalStateException("PENDING 상태에서만 처리를 시작할 수 있습니다.");  // ❌
    }
    this.status = OutboxStatus.PROCESSING;
}

// PipelineOutbox.java (수정 후)
public void startProcessing() {
    if (this.status != OutboxStatus.PENDING) {
        throw new InvalidOutboxStatusTransitionException(this.status, OutboxStatus.PROCESSING);  // ✅
    }
    this.status = OutboxStatus.PROCESSING;
}
```

#### 준수 컨벤션
- **Domain Layer**: Pure Java (Lombok 금지)
- **Exception Hierarchy**: Base Exception → Specific Exception
- **ErrorCode**: 도메인별 고유 코드 (`PIPELINE_001`, `PIPELINE_002` 등)
- **Javadoc 필수**: 발생 시점, 해결 방법 명시

---

### Issue #5: Multi-tenant 하드코딩

#### 현재 문제
```java
// PipelineWorker.java
private void saveMetadataAsExtractedData(FileAsset fileAsset, FileMetadata metadata) {
    ExtractedData extractedData = ExtractedData.create(
        new FileAssetId(fileAsset.getIdValue()),
        1L,  // ❌ TODO: tenantId (하드코딩)
        1L,  // ❌ TODO: organizationId (하드코딩)
        metadata.getFileFormat(),
        ...
    );
}
```

#### 변경 계획

##### 1. FileAsset에 tenantId/organizationId 추가
```java
// FileAsset.java (Domain)
public class FileAsset {
    private final FileAssetId id;
    private final Long tenantId;  // ✅ 추가
    private final Long organizationId;  // ✅ 추가
    private final String fileName;
    // ...

    public Long getTenantId() {
        return tenantId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }
}
```

##### 2. PipelineWorker 수정
```java
// PipelineWorker.java (수정 후)
private void saveMetadataAsExtractedData(FileAsset fileAsset, FileMetadata metadata) {
    ExtractedData extractedData = ExtractedData.create(
        new FileAssetId(fileAsset.getIdValue()),
        fileAsset.getTenantId(),  // ✅ FileAsset에서 추출
        fileAsset.getOrganizationId(),  // ✅ FileAsset에서 추출
        metadata.getFileFormat(),
        ...
    );
}
```

#### 준수 컨벤션
- **Domain Layer**: Pure Java getter 사용
- **Law of Demeter**: `fileAsset.getTenantId()` (단일 Getter 호출)

---

### Issue #6: 테스트 커버리지 낮음 (~10%)

#### 현재 상태
- **기존 테스트**: `PipelineWorkerSimpleTest.java` (1개, FileAsset 없는 경우만 테스트)
- **커버리지**: ~10%

#### 변경 계획

##### 1. Domain Layer 테스트
```java
package com.ryuqq.fileflow.domain.pipeline;

import com.ryuqq.fileflow.domain.common.OutboxStatus;
import com.ryuqq.fileflow.domain.pipeline.exception.InvalidOutboxStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PipelineOutbox 단위 테스트
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@DisplayName("PipelineOutbox 단위 테스트")
class PipelineOutboxTest {

    @Test
    @DisplayName("forNew() - 새 Outbox 생성")
    void forNew_ShouldCreatePendingOutbox() {
        // Given
        IdempotencyKey key = new IdempotencyKey("test-key");
        FileId fileId = new FileId(1L);

        // When
        PipelineOutbox outbox = PipelineOutbox.forNew(key, fileId);

        // Then
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(outbox.getIdempotencyKey()).isEqualTo(key);
    }

    @Test
    @DisplayName("startProcessing() - PENDING → PROCESSING 성공")
    void startProcessing_FromPending_ShouldSucceed() {
        // Given
        PipelineOutbox outbox = PipelineOutbox.forNew(...);

        // When
        outbox.startProcessing();

        // Then
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.PROCESSING);
    }

    @Test
    @DisplayName("startProcessing() - COMPLETED에서 호출 시 예외")
    void startProcessing_FromCompleted_ShouldThrowException() {
        // Given
        PipelineOutbox outbox = PipelineOutbox.forNew(...);
        outbox.startProcessing();
        outbox.markCompleted();

        // When & Then
        assertThatThrownBy(() -> outbox.startProcessing())
            .isInstanceOf(InvalidOutboxStatusTransitionException.class)
            .hasMessageContaining("COMPLETED → PROCESSING는 허용되지 않습니다.");
    }
}
```

##### 2. Application Layer 테스트 (예정)
- `PipelineTriggerOrchestratorTest.java`
- `PipelineTriggerFinalizerTest.java`
- `PipelineTriggerReaperTest.java`
- `PipelineWorkerTest.java` (확장)

##### 3. Persistence Layer 테스트 (예정)
- `PipelineOutboxPersistenceAdapterTest.java`

#### 목표 커버리지
- **Domain Layer**: 90% 이상
- **Application Layer**: 80% 이상
- **Persistence Layer**: 70% 이상

#### 준수 컨벤션
- **Testing Layer**: `@DisplayName` 한글 사용
- **AssertJ**: `assertThat()` 사용 (JUnit assert 금지)
- **BDD Given-When-Then**: 명확한 구조
- **Object Mother Pattern**: 테스트 데이터 생성 헬퍼

---

## 💡 Nice to Have (우선순위 3)

### Issue #7: REST API 엔드포인트 부재

#### 현재 상태
- Pipeline 바운더리에 REST API 없음 (내부 시스템만 존재)

#### 변경 계획 (선택사항)
- **POST /api/v1/pipelines/trigger** - 수동 Pipeline 트리거
- **GET /api/v1/pipelines/status/{fileId}** - Pipeline 상태 조회

#### 준수 컨벤션
- **REST API Layer**: `@RestController`, `@RequestMapping`
- **DTO Pattern**: Request/Response DTO (Lombok 허용)
- **Mapper Pattern**: DTO ↔ Domain 변환
- **Exception Handling**: `@RestControllerAdvice`

---

### Issue #8: 메트릭 및 모니터링 미구현

#### 변경 계획 (선택사항)
- **Micrometer**: Pipeline 처리 시간, 성공률, 실패율
- **Logging**: Structured Logging (SLF4J + Logback)

---

### Issue #9: 인덱스 최적화

#### 현재 인덱스
```sql
CREATE INDEX IDX_status_created_at ON pipeline_outbox (status, created_at);
CREATE INDEX IDX_file_id ON pipeline_outbox (file_id);
```

#### 추가 고려사항
- `(status, updated_at)` - 재시도 쿼리 최적화
- `(idempotency_key)` - UNIQUE 제약으로 이미 커버됨

---

## 📊 작업 우선순위 요약

| 순위 | 작업 | 예상 시간 | 중요도 |
|------|------|-----------|--------|
| 1 | OutboxStatus 패키지 이동 | 30분 | 🔴 Critical |
| 2 | Orchestrator Pattern 적용 | 4시간 | 🔴 Critical |
| 3 | Repository 메서드 추가 | 1시간 | 🔴 Critical |
| 4 | Domain Exception 추가 | 1시간 | 🟡 Important |
| 5 | Multi-tenant 하드코딩 제거 | 1시간 | 🟡 Important |
| 6 | 테스트 커버리지 향상 | 3시간 | 🟡 Important |
| 7 | REST API 추가 | 2시간 | 🟢 Nice to Have |
| 8 | 메트릭/모니터링 | 2시간 | 🟢 Nice to Have |
| 9 | 인덱스 최적화 | 30분 | 🟢 Nice to Have |

**총 예상 시간**: Critical (5.5시간) + Important (5시간) + Nice to Have (4.5시간) = **15시간**

---

## 🔄 CQRS 패턴 준수

### Command Port (쓰기)
```java
public interface PipelineOutboxPort {
    PipelineOutbox save(PipelineOutbox outbox);
}
```

### Query Port (읽기)
```java
public interface PipelineOutboxQueryPort {
    List<PipelineOutbox> findByStatus(OutboxStatus status, int batchSize);
    List<PipelineOutbox> findStaleProcessingMessages(...);
    List<PipelineOutbox> findRetryableFailedMessages(...);
}
```

### Adapter (통합)
```java
@Component
public class PipelineOutboxPersistenceAdapter
    implements PipelineOutboxPort, PipelineOutboxQueryPort {
    // Command와 Query 모두 구현
}
```

---

## 🏗️ 레이어별 컨벤션 체크리스트

### ✅ Domain Layer
- [ ] Lombok 금지 (Pure Java)
- [ ] Law of Demeter 준수 (Getter 체이닝 금지)
- [ ] Tell, Don't Ask 원칙
- [ ] Domain Exception 사용
- [ ] Javadoc 필수 (`@author`, `@since`)

### ✅ Application Layer
- [ ] `@Transactional` 경계 명확
- [ ] `@Async` 외부 API 호출
- [ ] Port Interface 의존
- [ ] Command/Query 분리 (CQRS)
- [ ] Orchestrator Pattern 준수

### ✅ Persistence Layer
- [ ] Long FK 전략 (JPA 관계 금지)
- [ ] Spring Data JPA Query Method
- [ ] DB 레벨 LIMIT (메모리 필터링 금지)
- [ ] Index 최적화

### ✅ REST API Layer (예정)
- [ ] Controller/Service 분리
- [ ] DTO Pattern (Request/Response)
- [ ] Mapper Pattern (DTO ↔ Domain)
- [ ] Exception Handling (`@RestControllerAdvice`)

### ✅ Testing Layer
- [ ] `@DisplayName` 한글
- [ ] AssertJ 사용
- [ ] BDD Given-When-Then
- [ ] Object Mother Pattern

---

## 📝 다음 단계

1. **문서 리뷰**: 이 문서를 검토하고 우선순위 확정
2. **Critical Issues 작업**: Issue #1 → #2 → #3 순서로 진행
3. **Important Issues 작업**: Issue #4 → #5 → #6 순서로 진행
4. **Nice to Have**: 필요 시 진행

---

**작성자**: Claude Code
**작성일**: 2025-11-06
**문서 버전**: 1.0.0
**기반 컨벤션**: `docs/coding_convention/` (98개 규칙)
