package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.util.UuidV7Generator;
import com.ryuqq.fileflow.domain.vo.MessageOutboxId;
import com.ryuqq.fileflow.domain.vo.OutboxStatus;

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
    private final String aggregateId;
    private final String payload;
    private final OutboxStatus status;
    private final int retryCount;
    private final int maxRetryCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime processedAt;

    /**
     * MessageOutbox Aggregate 생성자
     * <p>
     * private 생성자로 외부에서 직접 생성 불가.
     * 정적 팩토리 메서드를 통해서만 생성.
     * </p>
     *
     * @param id            메시지 고유 ID
     * @param eventType     이벤트 유형 (FileCreated, FileDeleted 등)
     * @param aggregateId   이벤트 발생 Aggregate ID
     * @param payload       이벤트 페이로드 (JSON)
     * @param status        메시지 상태
     * @param retryCount    재시도 횟수
     * @param maxRetryCount 최대 재시도 횟수
     * @param createdAt     생성 시각
     * @param processedAt   처리 완료 시각
     */
    private MessageOutbox(
            MessageOutboxId id,
            String eventType,
            String aggregateId,
            String payload,
            OutboxStatus status,
            int retryCount,
            int maxRetryCount,
            LocalDateTime createdAt,
            LocalDateTime processedAt
    ) {
        this.id = id;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.maxRetryCount = maxRetryCount;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
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
    public String getAggregateId() {
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
        return retryCount;
    }

    /**
     * 최대 재시도 횟수 조회
     */
    public int getMaxRetryCount() {
        return maxRetryCount;
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
     * 신규 메시지 생성 팩토리 메서드
     * <p>
     * ID는 null로 설정되며, 영속화 시점에 생성됩니다.
     * 초기 상태는 PENDING, retryCount는 0으로 설정됩니다.
     * </p>
     *
     * @param eventType     이벤트 유형
     * @param aggregateId   이벤트 발생 Aggregate ID
     * @param payload       이벤트 페이로드 (JSON)
     * @param maxRetryCount 최대 재시도 횟수
     * @return 신규 MessageOutbox Aggregate (ID null)
     */
    public static MessageOutbox forNew(
            String eventType,
            String aggregateId,
            String payload,
            int maxRetryCount
    ) {
        return new MessageOutbox(
                null, // ID는 영속화 시점에 생성
                eventType,
                aggregateId,
                payload,
                OutboxStatus.PENDING,
                0, // 초기 재시도 횟수
                maxRetryCount,
                LocalDateTime.now(),
                null
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
     * @param aggregateId   이벤트 발생 Aggregate ID
     * @param payload       이벤트 페이로드 (JSON)
     * @param status        메시지 상태
     * @param retryCount    재시도 횟수
     * @param maxRetryCount 최대 재시도 횟수
     * @param createdAt     생성 시각
     * @param processedAt   처리 완료 시각
     * @return MessageOutbox Aggregate
     * @throws IllegalArgumentException ID가 null일 때
     */
    public static MessageOutbox of(
            MessageOutboxId id,
            String eventType,
            String aggregateId,
            String payload,
            OutboxStatus status,
            int retryCount,
            int maxRetryCount,
            LocalDateTime createdAt,
            LocalDateTime processedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("ID는 null일 수 없습니다");
        }

        return new MessageOutbox(
                id,
                eventType,
                aggregateId,
                payload,
                status,
                retryCount,
                maxRetryCount,
                createdAt,
                processedAt
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
     * @param aggregateId   이벤트 발생 Aggregate ID
     * @param payload       이벤트 페이로드 (JSON)
     * @param status        메시지 상태
     * @param retryCount    재시도 횟수
     * @param maxRetryCount 최대 재시도 횟수
     * @param createdAt     생성 시각
     * @param processedAt   처리 완료 시각
     * @return 복원된 MessageOutbox Aggregate
     * @throws IllegalArgumentException ID가 null일 때
     */
    public static MessageOutbox reconstitute(
            MessageOutboxId id,
            String eventType,
            String aggregateId,
            String payload,
            OutboxStatus status,
            int retryCount,
            int maxRetryCount,
            LocalDateTime createdAt,
            LocalDateTime processedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("ID는 null일 수 없습니다");
        }

        return new MessageOutbox(
                id,
                eventType,
                aggregateId,
                payload,
                status,
                retryCount,
                maxRetryCount,
                createdAt,
                processedAt
        );
    }

    /**
     * 메시지 아웃박스 생성 팩토리 메서드 (레거시)
     * <p>
     * UUID v7을 자동 생성하고 초기 상태를 PENDING으로 설정합니다.
     * </p>
     *
     * @deprecated 대신 {@link #forNew(String, String, String, int)}를 사용하세요.
     * @param eventType     이벤트 유형
     * @param aggregateId   이벤트 발생 Aggregate ID
     * @param payload       이벤트 페이로드 (JSON)
     * @param maxRetryCount 최대 재시도 횟수
     * @return 생성된 MessageOutbox Aggregate
     */
    @Deprecated
    public static MessageOutbox create(
            String eventType,
            String aggregateId,
            String payload,
            int maxRetryCount
    ) {
        // UUID v7 자동 생성
        String id = UuidV7Generator.generate();

        // 현재 시각
        LocalDateTime now = LocalDateTime.now();

        return new MessageOutbox(
                MessageOutboxId.of(id),
                eventType,
                aggregateId,
                payload,
                OutboxStatus.PENDING, // 초기 상태는 PENDING
                0, // 초기 재시도 횟수 0
                maxRetryCount,
                now, // createdAt
                null  // processedAt는 null
        );
    }

    /**
     * 상태 전환 헬퍼 메서드
     * <p>
     * 새로운 상태로 MessageOutbox 객체를 생성합니다.
     * </p>
     *
     * @param newStatus   새로운 메시지 상태
     * @param processedAt 처리 완료 시각 (nullable)
     * @return 새로운 MessageOutbox 객체
     */
    private MessageOutbox withStatus(
            OutboxStatus newStatus,
            LocalDateTime processedAt
    ) {
        return new MessageOutbox(
                this.id,
                this.eventType,
                this.aggregateId,
                this.payload,
                newStatus,
                this.retryCount,
                this.maxRetryCount,
                this.createdAt,
                processedAt
        );
    }

    /**
     * 메시지를 발송 완료 상태로 변경
     *
     * @return 새로운 MessageOutbox 객체 (SENT 상태)
     */
    public MessageOutbox markAsSent() {
        return withStatus(OutboxStatus.SENT, LocalDateTime.now());
    }

    /**
     * 메시지를 실패 상태로 변경
     *
     * @return 새로운 MessageOutbox 객체 (FAILED 상태)
     */
    public MessageOutbox markAsFailed() {
        return withStatus(OutboxStatus.FAILED, LocalDateTime.now());
    }

    /**
     * 재시도 횟수 증가
     *
     * @return 새로운 MessageOutbox 객체 (retryCount + 1)
     */
    public MessageOutbox incrementRetryCount() {
        return new MessageOutbox(
                this.id,
                this.eventType,
                this.aggregateId,
                this.payload,
                this.status,
                this.retryCount + 1,
                this.maxRetryCount,
                this.createdAt,
                this.processedAt
        );
    }

    /**
     * 재시도 가능 여부 확인
     *
     * @return 재시도 가능하면 true, 불가능하면 false
     */
    public boolean canRetry() {
        return this.retryCount < this.maxRetryCount;
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

        LocalDateTime now = LocalDateTime.now();

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
