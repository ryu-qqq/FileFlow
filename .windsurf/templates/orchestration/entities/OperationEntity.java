package com.ryuqq.adapter.out.persistence.{domain_lower}.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * {Domain} Operation Entity
 *
 * <p>Orchestration Pattern의 Operation 상태를 저장하는 JPA 엔티티입니다.
 * IdemKey Unique 제약을 통해 중복 실행을 방지합니다.</p>
 *
 * <h3>핵심 컨벤션</h3>
 * <ul>
 *   <li><strong>Long FK 전략</strong>: JPA 관계 어노테이션 금지 (@ManyToOne, @OneToMany 등)</li>
 *   <li><strong>IdemKey Unique 제약</strong>: DB 레벨에서 중복 방지</li>
 *   <li><strong>Immutability</strong>: Setter 없이 생성자로만 초기화</li>
 * </ul>
 *
 * @author {author_name}
 * @since {version}
 */
@Entity
@Table(
    name = "{domain_lower}_operations",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_{domain_lower}_operations_idem_key",
            columnNames = {"idem_key"}
        )
    },
    indexes = {
        @Index(
            name = "idx_{domain_lower}_operations_state",
            columnList = "state"
        ),
        @Index(
            name = "idx_{domain_lower}_operations_created_at",
            columnList = "created_at"
        )
    }
)
public class {Domain}OperationEntity {

    /**
     * Operation ID (Primary Key)
     */
    @Id
    @Column(name = "op_id", nullable = false, length = 255)
    private String opId;

    /**
     * Idempotency Key (Unique)
     *
     * <p>동일한 IdemKey로 여러 번 요청해도 한 번만 실행되도록 보장합니다.</p>
     */
    @Column(name = "idem_key", nullable = false, length = 255, unique = true)
    private String idemKey;

    /**
     * Business Key
     *
     * <p>비즈니스 도메인의 식별자 (예: orderId, userId)</p>
     */
    @Column(name = "biz_key", nullable = false, length = 255)
    private String bizKey;

    /**
     * Domain
     *
     * <p>어떤 Domain의 Operation인지 식별 (예: PAYMENT, FILE_UPLOAD)</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "domain", nullable = false, length = 50)
    private Domain domain;

    /**
     * EventType
     *
     * <p>어떤 이벤트 타입인지 식별 (예: PAYMENT_REQUESTED, FILE_UPLOAD_REQUESTED)</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 100)
    private EventType eventType;

    /**
     * Operation State
     *
     * <p>현재 Operation의 상태 (PENDING, IN_PROGRESS, COMPLETED, FAILED, TIMEOUT)</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 50)
    private OperationState state;

    /**
     * Attempt Count
     *
     * <p>재시도 횟수 (Exponential Backoff 계산에 사용)</p>
     */
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    /**
     * Max Attempts
     *
     * <p>최대 재시도 횟수 (초과 시 TIMEOUT 처리)</p>
     */
    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    /**
     * Next Retry At
     *
     * <p>다음 재시도 예정 시간 (Exponential Backoff)</p>
     */
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    /**
     * Created At
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Updated At
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Completed At
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Error Code
     *
     * <p>실패 시 에러 코드 (Outcome.fail()의 errorCode)</p>
     */
    @Column(name = "error_code", length = 100)
    private String errorCode;

    /**
     * Error Message
     *
     * <p>실패 시 에러 메시지</p>
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    // TODO: 비즈니스별 추가 필드
    // 예시: transactionId, externalReferenceId 등

    /**
     * JPA를 위한 기본 생성자 (protected)
     */
    protected {Domain}OperationEntity() {
    }

    /**
     * 전체 생성자
     */
    public {Domain}OperationEntity(
        String opId,
        String idemKey,
        String bizKey,
        Domain domain,
        EventType eventType,
        OperationState state,
        int attemptCount,
        int maxAttempts,
        LocalDateTime nextRetryAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt,
        String errorCode,
        String errorMessage
    ) {
        this.opId = Objects.requireNonNull(opId, "opId must not be null");
        this.idemKey = Objects.requireNonNull(idemKey, "idemKey must not be null");
        this.bizKey = Objects.requireNonNull(bizKey, "bizKey must not be null");
        this.domain = Objects.requireNonNull(domain, "domain must not be null");
        this.eventType = Objects.requireNonNull(eventType, "eventType must not be null");
        this.state = Objects.requireNonNull(state, "state must not be null");
        this.attemptCount = attemptCount;
        this.maxAttempts = maxAttempts;
        this.nextRetryAt = nextRetryAt;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        this.completedAt = completedAt;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    // Getters (Setter 없음 - Immutability)

    public String getOpId() {
        return opId;
    }

    public String getIdemKey() {
        return idemKey;
    }

    public String getBizKey() {
        return bizKey;
    }

    public Domain getDomain() {
        return domain;
    }

    public EventType getEventType() {
        return eventType;
    }

    public OperationState getState() {
        return state;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    // Business Methods (상태 변경은 여기서만)

    /**
     * 상태 업데이트
     */
    public void updateState(OperationState newState) {
        this.state = Objects.requireNonNull(newState, "newState must not be null");
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 재시도 횟수 증가
     */
    public void incrementAttemptCount() {
        this.attemptCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 다음 재시도 시간 설정
     */
    public void setNextRetryAt(LocalDateTime nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 완료 처리
     */
    public void markCompleted() {
        this.state = OperationState.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 실패 처리
     */
    public void markFailed(String errorCode, String errorMessage) {
        this.state = OperationState.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        {Domain}OperationEntity that = ({Domain}OperationEntity) o;
        return Objects.equals(opId, that.opId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opId);
    }

    @Override
    public String toString() {
        return "{Domain}OperationEntity{" +
            "opId='" + opId + '\'' +
            ", idemKey='" + idemKey + '\'' +
            ", state=" + state +
            ", attemptCount=" + attemptCount +
            '}';
    }
}
