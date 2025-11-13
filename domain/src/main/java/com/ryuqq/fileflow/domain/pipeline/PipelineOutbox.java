package com.ryuqq.fileflow.domain.pipeline;

import com.ryuqq.fileflow.domain.download.IdempotencyKey;
import com.ryuqq.fileflow.domain.common.OutboxStatus;
import com.ryuqq.fileflow.domain.file.asset.FileAssetId;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Pipeline Outbox Aggregate Root
 *
 * <p>Transactional Outbox Pattern을 구현하는 도메인 객체입니다.</p>
 * <p>FileAsset 저장 시 Pipeline 처리 이벤트를 안전하게 발행하기 위해
 * 트랜잭션 내에서 이벤트를 저장하고, 별도 프로세스에서 비동기로 처리합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Law of Demeter - Getter 체이닝 방지</li>
 *   <li>✅ Tell, Don't Ask 패턴 적용</li>
 *   <li>✅ Value Object ID 래핑 (PipelineOutboxId, IdempotencyKey 등)</li>
 *   <li>✅ Aggregate 경계 내에서 일관성 보장</li>
 *   <li>✅ Domain Event 발행 (수동 이벤트 관리)</li>
 * </ul>
 *
 * <h3>Outbox 패턴</h3>
 * <ul>
 *   <li>멱등성 보장: {@link IdempotencyKey}로 중복 이벤트 발행 방지</li>
 *   <li>상태 전이: PENDING → PROCESSING → COMPLETED/FAILED</li>
 *   <li>재시도 메커니즘: 실패 시 재시도 카운터 증가</li>
 * </ul>
 *
 * <h3>사용 시나리오</h3>
 * <ul>
 *   <li>FileAsset 저장 → PipelineOutbox 저장 (동일 트랜잭션)</li>
 *   <li>Scheduler가 PENDING Outbox 조회</li>
 *   <li>Worker가 Pipeline 처리 실행</li>
 *   <li>처리 완료 → COMPLETED 상태 전환</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class PipelineOutbox {

    private final PipelineOutboxId id;
    private final IdempotencyKey idempotencyKey;
    private final FileAssetId fileId;
    private final Clock clock;
    private OutboxStatus status;
    private Integer retryCount;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 수동 이벤트 관리
    private final List<Object> domainEvents = new ArrayList<>();

    /**
     * 신규 Outbox를 생성하고 Domain Event를 등록합니다 (Static Factory Method).
     *
     * <p><strong>ID 없이 신규 도메인 객체를 생성</strong>합니다 (DB 저장 전 상태).</p>
     * <p>ID = null로 초기화됩니다.</p>
     *
     * <p><strong>Domain Event 발행:</strong></p>
     * <ul>
     *   <li>생성 시 PipelineOutboxCreatedEvent 등록</li>
     *   <li>Event는 Repository.save() 호출 시 트랜잭션 커밋과 함께 발행됨</li>
     *   <li>Spring Data의 AbstractAggregateRoot 메커니즘 사용</li>
     * </ul>
     *
     * <p><strong>사용 시기</strong>: FileCommandManager에서 FileAsset 저장 시 Outbox 메시지 저장</p>
     * <p><strong>예시</strong>:</p>
     * <pre>{@code
     * // FileCommandManager.java (Application Layer)
     * FileAsset savedFileAsset = filePort.save(fileAsset);
     *
     * PipelineOutbox outbox = PipelineOutbox.forNew(
     *     IdempotencyKey.generate(),
     *     FileAssetId.of(savedFileAsset.getIdValue())
     * );
     * pipelineOutboxPort.save(outbox); // 이 시점에 Event 발행 (트랜잭션 커밋 시)
     * }</pre>
     *
     * <p><strong>초기 상태</strong>:</p>
     * <ul>
     *   <li>status = PENDING</li>
     *   <li>retryCount = 0</li>
     *   <li>createdAt = 현재 시간</li>
     * </ul>
     *
     * @param idempotencyKey 멱등성 키 (중복 방지)
     * @param fileId         FileAsset ID
     * @return 생성된 PipelineOutbox (ID = null, Event 등록됨)
     * @throws IllegalArgumentException 필수 필드가 null인 경우
     */
    public static PipelineOutbox forNew(
        IdempotencyKey idempotencyKey,
        FileAssetId fileId
    ) {
        PipelineOutbox outbox = new PipelineOutbox(
            null,
            idempotencyKey,
            fileId,
            Clock.systemDefaultZone()
        );

        // Domain Event 등록 (수동 관리)
        // Note: ID는 save() 후에 생성되므로, Event에는 임시로 null 전달
        // EventListener에서는 fileId를 사용하여 처리
        outbox.addDomainEvent(new PipelineOutboxCreatedEvent(
            null, // ID는 save() 후 생성됨
            fileId,
            outbox.getCreatedAt()
        ));

        return outbox;
    }

    /**
     * Outbox를 생성합니다 (기존 ID 존재, Static Factory Method).
     *
     * <p><strong>ID가 이미 있는 도메인 객체를 생성</strong>합니다.</p>
     *
     * <p><strong>사용 시기</strong>: 테스트 또는 ID가 미리 정해진 특수한 경우</p>
     *
     * @param id             Outbox 식별자 (필수)
     * @param idempotencyKey 멱등성 키
     * @param fileId         FileAsset ID
     * @return 생성된 PipelineOutbox (ID 포함)
     * @throws IllegalArgumentException id가 null이거나 필수 필드가 유효하지 않은 경우
     */
    public static PipelineOutbox of(
        PipelineOutboxId id,
        IdempotencyKey idempotencyKey,
        FileAssetId fileId
    ) {
        if (id == null) {
            throw new IllegalArgumentException("Outbox ID는 필수입니다");
        }
        return new PipelineOutbox(
            id,
            idempotencyKey,
            fileId,
            Clock.systemDefaultZone()
        );
    }

    /**
     * Outbox 생성자 (package-private).
     *
     * @param id             Outbox 식별자 (null 가능 - 신규 생성 시)
     * @param idempotencyKey 멱등성 키
     * @param fileId         FileAsset ID
     * @param clock          시간 제공자
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     */
    PipelineOutbox(
        PipelineOutboxId id,
        IdempotencyKey idempotencyKey,
        FileAssetId fileId,
        Clock clock
    ) {
        validateRequiredFields(idempotencyKey, fileId);

        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.fileId = fileId;
        this.clock = clock;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id             Outbox ID
     * @param idempotencyKey 멱등성 키
     * @param fileId         FileAsset ID
     * @param status         Outbox 상태
     * @param retryCount     재시도 횟수
     * @param clock          시간 제공자
     * @param createdAt      생성 일시
     * @param updatedAt      수정 일시
     */
    private PipelineOutbox(
        PipelineOutboxId id,
        IdempotencyKey idempotencyKey,
        FileAssetId fileId,
        OutboxStatus status,
        Integer retryCount,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.fileId = fileId;
        this.status = status;
        this.retryCount = retryCount;
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * DB에서 조회한 데이터로 Outbox 재구성 (Static Factory Method)
     *
     * <p><strong>Persistence Layer → Domain Layer 변환 전용</strong></p>
     *
     * @param id             Outbox ID (필수 - DB에서 조회된 ID)
     * @param idempotencyKey 멱등성 키
     * @param fileId         FileAsset ID
     * @param status         Outbox 상태
     * @param retryCount     재시도 횟수
     * @param createdAt      생성 일시
     * @param updatedAt      수정 일시
     * @return 재구성된 PipelineOutbox
     * @throws IllegalArgumentException id가 null인 경우
     */
    public static PipelineOutbox reconstitute(
        PipelineOutboxId id,
        IdempotencyKey idempotencyKey,
        FileAssetId fileId,
        OutboxStatus status,
        Integer retryCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new PipelineOutbox(
            id,
            idempotencyKey,
            fileId,
            status,
            retryCount,
            Clock.systemDefaultZone(),
            createdAt,
            updatedAt
        );
    }

    /**
     * 필수 필드의 유효성을 검증합니다.
     *
     * @param idempotencyKey 멱등성 키
     * @param fileId         FileAsset ID
     * @throws IllegalArgumentException 필수 필드가 null인 경우
     */
    private static void validateRequiredFields(
        IdempotencyKey idempotencyKey,
        FileAssetId fileId
    ) {
        if (idempotencyKey == null) {
            throw new IllegalArgumentException("Idempotency Key는 필수입니다");
        }
        if (fileId == null) {
            throw new IllegalArgumentException("File ID는 필수입니다");
        }
    }

    /**
     * Outbox 처리를 시작합니다 (PENDING → PROCESSING).
     *
     * @throws IllegalStateException PENDING 상태가 아닌 경우
     */
    public void startProcessing() {
        if (this.status != OutboxStatus.PENDING) {
            throw new IllegalStateException(
                "PENDING 상태에서만 처리를 시작할 수 있습니다. 현재 상태: " + this.status
            );
        }
        this.status = OutboxStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Outbox 처리를 완료합니다 (PROCESSING → COMPLETED).
     *
     * @throws IllegalStateException PROCESSING 상태가 아닌 경우
     */
    public void complete() {
        if (this.status != OutboxStatus.PROCESSING) {
            throw new IllegalStateException(
                "PROCESSING 상태에서만 완료할 수 있습니다. 현재 상태: " + this.status
            );
        }
        this.status = OutboxStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Outbox 처리를 실패 처리하고 재시도 카운터를 증가시킵니다 (PROCESSING → FAILED).
     *
     * @throws IllegalStateException PROCESSING 상태가 아닌 경우
     */
    public void fail() {
        if (this.status != OutboxStatus.PROCESSING) {
            throw new IllegalStateException(
                "PROCESSING 상태에서만 실패 처리할 수 있습니다. 현재 상태: " + this.status
            );
        }
        this.status = OutboxStatus.FAILED;
        this.retryCount++;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 재시도를 위해 상태를 PENDING으로 되돌립니다 (FAILED → PENDING).
     *
     * @throws IllegalStateException FAILED 상태가 아닌 경우
     */
    public void retryFromFailed() {
        if (this.status != OutboxStatus.FAILED) {
            throw new IllegalStateException(
                "FAILED 상태에서만 재시도할 수 있습니다. 현재 상태: " + this.status
            );
        }
        this.status = OutboxStatus.PENDING;
        this.updatedAt = LocalDateTime.now(clock);
    }

    // ===== Manager 호환 메서드 =====

    /**
     * Outbox를 처리 중 상태로 변경합니다 (Manager 호환용).
     */
    public void markProcessing() {
        startProcessing();
    }

    /**
     * Outbox를 처리 완료 상태로 변경합니다 (Manager 호환용).
     */
    public void markProcessed() {
        complete();
    }

    /**
     * Outbox를 실패 상태로 변경합니다 (Manager 호환용).
     *
     * @param errorMessage 에러 메시지 (현재 사용하지 않음)
     */
    public void markFailed(String errorMessage) {
        fail();
    }

    /**
     * Outbox를 영구 실패 상태로 변경합니다 (Manager 호환용).
     *
     * @param errorMessage 최종 에러 메시지 (현재 사용하지 않음)
     */
    public void markPermanentlyFailed(String errorMessage) {
        if (this.status != OutboxStatus.PROCESSING) {
            throw new IllegalStateException(
                "PROCESSING 상태에서만 영구 실패 처리할 수 있습니다. 현재 상태: " + this.status
            );
        }
        this.status = OutboxStatus.FAILED;
        this.retryCount = Integer.MAX_VALUE; // 영구 실패 표시
        this.updatedAt = LocalDateTime.now(clock);
    }

    // ===== 상태 확인 메서드 =====

    public boolean isStatus(OutboxStatus targetStatus) {
        return this.status == targetStatus;
    }

    public boolean isPending() {
        return this.status == OutboxStatus.PENDING;
    }

    public boolean isProcessing() {
        return this.status == OutboxStatus.PROCESSING;
    }

    public boolean isCompleted() {
        return this.status == OutboxStatus.COMPLETED;
    }

    public boolean isFailed() {
        return this.status == OutboxStatus.FAILED;
    }

    /**
     * 특정 File ID를 가지는지 확인합니다.
     *
     * @param targetFileAssetId 확인할 File ID
     * @return 동일한 File ID를 가지면 true
     */
    public boolean hasFileAssetId(FileAssetId targetFileAssetId) {
        if (targetFileAssetId == null) {
            return false;
        }
        return this.fileId.equals(targetFileAssetId);
    }

    /**
     * 특정 Idempotency Key를 가지는지 확인합니다.
     *
     * @param targetKey 확인할 Idempotency Key
     * @return 동일한 키를 가지면 true
     */
    public boolean hasIdempotencyKey(IdempotencyKey targetKey) {
        return this.idempotencyKey.isSameAs(targetKey);
    }

    // ===== Getter 메서드 (Law of Demeter 준수) =====

    public PipelineOutboxId getId() {
        return id;
    }

    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public IdempotencyKey getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getIdempotencyKeyValue() {
        return idempotencyKey.value();
    }

    public FileAssetId getFileAssetId() {
        return fileId;
    }

    public Long getFileAssetIdValue() {
        return fileId.value();
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ===== equals, hashCode, toString =====

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PipelineOutbox that = (PipelineOutbox) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PipelineOutbox{" +
            "id=" + id +
            ", idempotencyKey=" + idempotencyKey +
            ", fileId=" + fileId +
            ", status=" + status +
            ", retryCount=" + retryCount +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }

    // ===== 수동 이벤트 관리 메서드 =====

    /**
     * Domain Event를 추가합니다.
     *
     * @param event Domain Event
     */
    protected void addDomainEvent(Object event) {
        this.domainEvents.add(event);
    }

    /**
     * 등록된 모든 Domain Event를 반환합니다.
     *
     * @return Domain Event 목록 (Unmodifiable)
     */
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 모든 Domain Event를 제거합니다.
     *
     * <p>Repository Adapter에서 Event 발행 후 호출해야 합니다.</p>
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
