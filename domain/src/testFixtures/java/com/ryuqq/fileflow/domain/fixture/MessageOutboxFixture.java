package com.ryuqq.fileflow.domain.fixture;

import com.ryuqq.fileflow.domain.aggregate.MessageOutbox;
import com.ryuqq.fileflow.domain.vo.OutboxStatus;

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
     * MessageOutbox.create() 팩토리 메서드 사용 (PENDING 상태, UUID v7 자동 생성)
     *
     * @param eventType     이벤트 유형
     * @param aggregateId   이벤트 발생 Aggregate ID
     * @param payload       이벤트 페이로드 (JSON)
     * @param maxRetryCount 최대 재시도 횟수
     * @return 생성된 MessageOutbox Aggregate
     */
    public static MessageOutbox createOutbox(String eventType, String aggregateId, String payload, int maxRetryCount) {
        return MessageOutbox.create(eventType, aggregateId, payload, maxRetryCount);
    }

    /**
     * MessageOutbox Builder (Plain Java, Lombok 금지)
     */
    public static class MessageOutboxBuilder {
        private String id = UuidV7GeneratorFixture.aUuidV7();
        private String eventType = "FileCreated";
        private String aggregateId = UuidV7GeneratorFixture.aUuidV7();
        private String payload = "{\"fileName\":\"default.jpg\",\"fileSize\":1024}";
        private OutboxStatus status = OutboxStatusFixture.pending();
        private int retryCount = 0;
        private int maxRetryCount = 3;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime processedAt = null;

        public MessageOutboxBuilder id(String id) {
            this.id = id;
            return this;
        }

        public MessageOutboxBuilder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public MessageOutboxBuilder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
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
    }
}
