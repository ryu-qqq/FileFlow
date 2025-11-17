package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.util.UuidV7Generator;
import com.ryuqq.fileflow.domain.vo.AggregateId;
import com.ryuqq.fileflow.domain.vo.MessageOutboxId;
import com.ryuqq.fileflow.domain.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.vo.RetryCount;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 메시지 아웃박스 Aggregate Root
 * <p>
 * 이벤트 발행 보장을 위한 Transactional Outbox Pattern 구현체입니다.
 * </p>
 */
public class MessageOutbox {

    /**
     * SENT 상태 메시지 TTL (일 단위)
     */
    private static final int SENT_TTL_DAYS = 7;

    /**
     * FAILED 상태 메시지 TTL (일 단위)
     */
    private static final int FAILED_TTL_DAYS = 30;

    private final MessageOutboxId id;
    private final String eventType;
    private final AggregateId aggregateId;
    private final String payload;
    private OutboxStatus status;  // 가변: markAsSent(), markAsFailed()에서 변경
    private RetryCount retryCount;  // VO 적용
    private final Clock clock;
    private final LocalDateTime createdAt;
    private LocalDateTime processedAt;  // 가변: markAsSent(), markAsFailed()에서 변경
    private LocalDateTime updatedAt;  // 가변: 모든 비즈니스 메서드에서 자동 갱신

    /**
     * MessageOutbox Aggregate 생성자
     * <p>
     * private 생성자로 외부에서 직접 생성 불가.
     * 정적 팩토리 메서드를 통해서만 생성.
     * </p>
     *
     * @param id            메시지 고유 ID
     * @param eventType     이벤트 유형 (FileCreated, FileDeleted 등)
     * @param aggregateId   이벤트 발생 Aggregate ID (VO)
     * @param payload       이벤트 페이로드 (JSON)
     * @param status        메시지 상태
     * @param retryCount    재시도 횟수 (RetryCount VO)
     * @param clock         시간 생성용 Clock
     * @param createdAt     생성 시각
     * @param processedAt   처리 완료 시각
     * @param updatedAt     최종 수정 시각
     */
    private MessageOutbox(
            MessageOutboxId id,
            String eventType,
            AggregateId aggregateId,
            String payload,
            OutboxStatus status,
            RetryCount retryCount,
            Clock clock,
            LocalDateTime createdAt,
            LocalDateTime processedAt,
            LocalDateTime updatedAt
    ) {
        validateConstructorArguments(eventType, aggregateId, payload, status, clock, createdAt);

        this.id = id;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.clock = clock;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 생성자 인수 검증
     *
     * @throws IllegalArgumentException 필수 인수가 null이거나 유효하지 않을 때
     */
    private void validateConstructorArguments(
            String eventType,
            AggregateId aggregateId,
            String payload,
            OutboxStatus status,
            Clock clock,
            LocalDateTime createdAt
    ) {
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType은 null이거나 빈 값일 수 없습니다");
        }
        if (aggregateId == null) {
            throw new IllegalArgumentException("aggregateId는 null일 수 없습니다");
        }
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("payload는 null이거나 빈 값일 수 없습니다");
        }
        if (status == null) {
            throw new IllegalArgumentException("status는 null일 수 없습니다");
        }
        if (clock == null) {
            throw new IllegalArgumentException("clock은 null일 수 없습니다");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt은 null일 수 없습니다");
        }
    }

    /**
     * 메시지 고유 ID 조회
     */
    public MessageOutboxId getId() {
        return id;
    }

    /**
     * 이벤트 유형 조회
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * 이벤트 발생 Aggregate ID 조회
     */
    public AggregateId getAggregateId() {
        return aggregateId;
    }

    /**
     * 이벤트 페이로드 조회
     */
    public String getPayload() {
        return payload;
    }

    /**
     * 메시지 상태 조회
     */
    public OutboxStatus getStatus() {
        return status;
    }

    /**
     * 재시도 횟수 조회
     */
    public int getRetryCount() {
        return retryCount.current();
    }

    /**
     * 최대 재시도 횟수 조회
     */
    public int getMaxRetryCount() {
        return retryCount.max();
    }

    /**
     * 생성 시각 조회
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 처리 완료 시각 조회
     */
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    /**
     * 최종 수정 시각 조회
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 메시지 ID의 원시 값 조회
     * <p>
     * Law of Demeter 준수: getId().getValue() 체이닝 방지
     * </p>
     *
     * @return 메시지 ID 원시 값 (String)
     */
    public String getIdValue() {
        return id.getValue();
    }

    /**
     * Aggregate ID의 원시 값 조회
     * <p>
     * Law of Demeter 준수: getAggregateId().getValue() 체이닝 방지
     * </p>
     *
     * @return Aggregate ID 원시 값 (String)
     */
    public String getAggregateIdValue() {
        return aggregateId.getValue();
    }

    /**
     * 신규 메시지 생성 팩토리 메서드
     * <p>
     * ID는 null로 설정되며, 영속화 시점에 생성됩니다.
     * 초기 상태는 PENDING, retryCount는 0으로 설정됩니다.
     * </p>
     *
     * @param eventType     이벤트 유형
     * @param aggregateId   이벤트 발생 Aggregate ID (VO)
     * @param payload       이벤트 페이로드 (JSON)
     * @param clock         시간 생성용 Clock (테스트에서 고정 시간 사용 가능)
     * @return 신규 MessageOutbox Aggregate (ID null)
     */
    public static MessageOutbox forNew(
            String eventType,
            AggregateId aggregateId,
            String payload,
            Clock clock
    ) {
        LocalDateTime now = LocalDateTime.now(clock);
        return new MessageOutbox(
                null, // ID는 영속화 시점에 생성
                eventType,
                aggregateId,
                payload,
                OutboxStatus.PENDING,
                RetryCount.forOutbox(), // Outbox 재시도 전략 (최대 3회)
                clock,
                now, // createdAt
                null, // processedAt
                now  // updatedAt = createdAt (초기 생성 시)
        );
    }

    /**
     * ID 필수 메시지 생성 팩토리 메서드
     * <p>
     * ID가 반드시 있어야 하는 경우 사용합니다.
     * Application Layer에서 생성 후 전달하는 시나리오.
     * </p>
     *
     * @param id            메시지 고유 ID (필수)
     * @param eventType     이벤트 유형
     * @param aggregateId   이벤트 발생 Aggregate ID (VO)
     * @param payload       이벤트 페이로드 (JSON)
     * @param status        메시지 상태
     * @param retryCount    재시도 횟수 (RetryCount VO)
     * @param clock         시간 생성용 Clock
     * @param createdAt     생성 시각
     * @param processedAt   처리 완료 시각
     * @param updatedAt     최종 수정 시각
     * @return MessageOutbox Aggregate
     * @throws IllegalArgumentException ID가 null일 때
     */
    public static MessageOutbox of(
            MessageOutboxId id,
            String eventType,
            AggregateId aggregateId,
            String payload,
            OutboxStatus status,
            RetryCount retryCount,
            Clock clock,
            LocalDateTime createdAt,
            LocalDateTime processedAt,
            LocalDateTime updatedAt
    ) {
        validateIdNotNullOrNew(id, "ID는 null이거나 새로운 ID일 수 없습니다");

        return new MessageOutbox(
                id,
                eventType,
                aggregateId,
                payload,
                status,
                retryCount,
                clock,
                createdAt,
                processedAt,
                updatedAt
        );
    }

    /**
     * 영속성 복원용 팩토리 메서드
     * <p>
     * 데이터베이스에서 조회한 엔티티를 Aggregate로 변환할 때 사용합니다.
     * ID는 반드시 존재해야 하며, 모든 상태를 그대로 복원합니다.
     * </p>
     *
     * @param id            메시지 고유 ID (필수)
     * @param eventType     이벤트 유형
     * @param aggregateId   이벤트 발생 Aggregate ID (VO)
     * @param payload       이벤트 페이로드 (JSON)
     * @param status        메시지 상태
     * @param retryCount    재시도 횟수 (RetryCount VO)
     * @param clock         시간 생성용 Clock
     * @param createdAt     생성 시각
     * @param processedAt   처리 완료 시각
     * @param updatedAt     최종 수정 시각
     * @return 복원된 MessageOutbox Aggregate
     * @throws IllegalArgumentException ID가 null일 때
     */
    public static MessageOutbox reconstitute(
            MessageOutboxId id,
            String eventType,
            AggregateId aggregateId,
            String payload,
            OutboxStatus status,
            RetryCount retryCount,
            Clock clock,
            LocalDateTime createdAt,
            LocalDateTime processedAt,
            LocalDateTime updatedAt
    ) {
        validateIdNotNullOrNew(id, "재구성을 위한 ID는 null이거나 새로운 ID일 수 없습니다");

        return new MessageOutbox(
                id,
                eventType,
                aggregateId,
                payload,
                status,
                retryCount,
                clock,
                createdAt,
                processedAt,
                updatedAt
        );
    }

    /**
     * ID 검증: null이거나 새로운 ID일 때 예외 발생
     *
     * @param id           검증할 ID
     * @param errorMessage 예외 메시지
     * @throws IllegalArgumentException ID가 null이거나 새로운 ID일 때
     */
    private static void validateIdNotNullOrNew(MessageOutboxId id, String errorMessage) {
        if (id == null || id.isNew()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * 메시지를 발송 완료 상태로 변경
     * <p>
     * 가변 패턴: this 객체를 직접 변경합니다.
     * Spring @Transactional 내에서 안전하게 동작합니다.
     * </p>
     *
     * @param clock 시간 생성용 Clock (processedAt 생성에 사용)
     */
    public void markAsSent(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        this.status = OutboxStatus.SENT;
        this.processedAt = now;
        this.updatedAt = now;
    }

    /**
     * 메시지를 실패 상태로 변경
     * <p>
     * 가변 패턴: this 객체를 직접 변경합니다.
     * Spring @Transactional 내에서 안전하게 동작합니다.
     * </p>
     *
     * @param clock 시간 생성용 Clock (processedAt 생성에 사용)
     */
    public void markAsFailed(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        this.status = OutboxStatus.FAILED;
        this.processedAt = now;
        this.updatedAt = now;
    }

    /**
     * 재시도 횟수 증가
     * <p>
     * 가변 패턴: this 객체를 직접 변경합니다.
     * Spring @Transactional 내에서 안전하게 동작합니다.
     * </p>
     */
    public void incrementRetryCount() {
        this.retryCount = this.retryCount.increment();
    }

    /**
     * 재시도 가능 여부 확인
     *
     * @return 재시도 가능하면 true, 불가능하면 false
     */
    public boolean canRetry() {
        return this.retryCount.canRetry();
    }

    /**
     * TTL(Time To Live) 만료 여부 확인
     * <p>
     * SENT 상태: 7일 후 만료
     * FAILED 상태: 30일 후 만료
     * </p>
     *
     * @return 만료되었으면 true, 아니면 false
     */
    public boolean isExpired() {
        if (this.processedAt == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now(this.clock);

        if (this.status == OutboxStatus.SENT) {
            LocalDateTime expiryDate = this.processedAt.plusDays(SENT_TTL_DAYS);
            return now.isAfter(expiryDate);
        } else if (this.status == OutboxStatus.FAILED) {
            LocalDateTime expiryDate = this.processedAt.plusDays(FAILED_TTL_DAYS);
            return now.isAfter(expiryDate);
        }

        return false;
    }
}
