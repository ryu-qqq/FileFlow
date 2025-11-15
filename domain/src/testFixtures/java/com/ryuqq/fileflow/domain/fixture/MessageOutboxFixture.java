package com.ryuqq.fileflow.domain.fixture;

import com.ryuqq.fileflow.domain.aggregate.MessageOutbox;
import com.ryuqq.fileflow.domain.vo.AggregateId;
import com.ryuqq.fileflow.domain.vo.MessageOutboxId;
import com.ryuqq.fileflow.domain.vo.OutboxStatus;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * MessageOutbox Aggregate TestFixture (Object Mother 패턴 + Builder 패턴)
 */
public class MessageOutboxFixture {

    /**
     * 기본 MessageOutbox 생성 (Builder 시작)
     */
    public static MessageOutboxBuilder anOutbox() {
        return new MessageOutboxBuilder();
    }

    /**
     * MessageOutbox.forNew() 팩토리 메서드 사용 (PENDING 상태, ID null)
     *
     * @param eventType     이벤트 유형
     * @param aggregateId   이벤트 발생 Aggregate ID
     * @param payload       이벤트 페이로드 (JSON)
     * @param maxRetryCount 최대 재시도 횟수
     * @return 신규 MessageOutbox Aggregate (ID null)
     */
    public static MessageOutbox createOutbox(String eventType, String aggregateId, String payload, int maxRetryCount) {
        return MessageOutbox.forNew(eventType, AggregateId.of(aggregateId), payload, maxRetryCount, Clock.systemUTC());
    }

    /**
     * MessageOutbox.create() 팩토리 메서드 사용 (레거시)
     *
     * @deprecated 대신 {@link #createOutbox(String, String, String, int)}를 사용하세요 (내부적으로 forNew() 사용)
     * @param eventType     이벤트 유형
     * @param aggregateId   이벤트 발생 Aggregate ID
     * @param payload       이벤트 페이로드 (JSON)
     * @param maxRetryCount 최대 재시도 횟수
     * @return 생성된 MessageOutbox Aggregate
     */
    @Deprecated
    public static MessageOutbox createOutboxLegacy(String eventType, String aggregateId, String payload, int maxRetryCount) {
        return MessageOutbox.create(eventType, aggregateId, payload, maxRetryCount);
    }

    /**
     * SENT 상태 메시지
     * <p>
     * 가변 패턴: 생성 후 markAsSent() 호출하여 상태 변경
     * </p>
     */
    public static MessageOutbox aSentOutbox() {
        MessageOutbox outbox = createOutbox(
                "FileCreated",
                "file-uuid-v7-123",
                "{\"fileName\":\"test.jpg\",\"fileSize\":1024000}",
                3
        );
        outbox.markAsSent(Clock.systemUTC());
        return outbox;
    }

    /**
     * FAILED 상태 메시지
     * <p>
     * 가변 패턴: 생성 후 markAsFailed() 호출하여 상태 변경
     * </p>
     */
    public static MessageOutbox aFailedOutbox() {
        MessageOutbox outbox = createOutbox(
                "FileCreated",
                "file-uuid-v7-123",
                "{\"fileName\":\"test.jpg\",\"fileSize\":1024000}",
                3
        );
        outbox.markAsFailed(Clock.systemUTC());
        return outbox;
    }

    /**
     * 만료된 메시지 (SENT 상태, 8일 전)
     */
    public static MessageOutbox anExpiredOutbox() {
        return anOutbox()
                .status(OutboxStatusFixture.sent())
                .createdAt(LocalDateTime.now().minusDays(8))
                .processedAt(LocalDateTime.now().minusDays(8))
                .build();
    }

    /**
     * MessageOutbox Builder (Plain Java, Lombok 금지)
     */
    public static class MessageOutboxBuilder {
        private MessageOutboxId id = MessageOutboxIdFixture.aMessageOutboxId();
        private String eventType = "FileCreated";
        private AggregateId aggregateId = AggregateId.of(UuidV7GeneratorFixture.aUuidV7());
        private String payload = "{\"fileName\":\"default.jpg\",\"fileSize\":1024}";
        private OutboxStatus status = OutboxStatusFixture.pending();
        private int retryCount = 0;
        private int maxRetryCount = 3;
        private Clock clock = Clock.systemUTC();
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime processedAt = null;

        public MessageOutboxBuilder id(MessageOutboxId id) {
            this.id = id;
            return this;
        }

        public MessageOutboxBuilder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public MessageOutboxBuilder aggregateId(String aggregateId) {
            this.aggregateId = AggregateId.of(aggregateId);
            return this;
        }

        public MessageOutboxBuilder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public MessageOutboxBuilder status(OutboxStatus status) {
            this.status = status;
            return this;
        }

        public MessageOutboxBuilder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public MessageOutboxBuilder maxRetryCount(int maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
            return this;
        }

        public MessageOutboxBuilder clock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public MessageOutboxBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public MessageOutboxBuilder processedAt(LocalDateTime processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        /**
         * MessageOutbox 객체 생성
         */
        public MessageOutbox build() {
            return MessageOutbox.reconstitute(
                    id,
                    eventType,
                    aggregateId,
                    payload,
                    status,
                    retryCount,
                    maxRetryCount,
                    clock,
                    createdAt,
                    processedAt
            );
        }
    }
}
