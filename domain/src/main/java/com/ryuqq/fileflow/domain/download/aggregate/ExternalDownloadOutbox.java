package com.ryuqq.fileflow.domain.download.aggregate;

import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadOutboxId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 외부 다운로드 Outbox Aggregate.
 *
 * <p><strong>책임</strong>: SQS 발행을 위한 Outbox 패턴 지원
 *
 * <p><strong>발행 규칙</strong>:
 *
 * <ul>
 *   <li>신규 생성 시 published = false
 *   <li>SQS 발행 성공 후 markAsPublished() 호출
 *   <li>이미 발행된 Outbox는 재발행 불가
 * </ul>
 */
public class ExternalDownloadOutbox {

    private final ExternalDownloadOutboxId id;
    private final ExternalDownloadId externalDownloadId;
    private boolean published;
    private Instant publishedAt;
    private final Instant createdAt;

    private ExternalDownloadOutbox(
            ExternalDownloadOutboxId id,
            ExternalDownloadId externalDownloadId,
            boolean published,
            Instant publishedAt,
            Instant createdAt) {
        Objects.requireNonNull(externalDownloadId, "externalDownloadId must not be null");

        this.id = id;
        this.externalDownloadId = externalDownloadId;
        this.published = published;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
    }

    /**
     * 새 ExternalDownloadOutbox 생성 (ID null, published = false).
     *
     * @param externalDownloadId 외부 다운로드 ID
     * @param clock 시간 소스
     * @return 신규 ExternalDownloadOutbox
     */
    public static ExternalDownloadOutbox forNew(
            ExternalDownloadId externalDownloadId, Clock clock) {
        return new ExternalDownloadOutbox(
                ExternalDownloadOutboxId.forNew(),
                externalDownloadId,
                false,
                null,
                Instant.now(clock));
    }

    /**
     * 기존 ExternalDownloadOutbox 재구성 (조회용).
     *
     * @param id Outbox ID
     * @param externalDownloadId 외부 다운로드 ID
     * @param published 발행 여부
     * @param publishedAt 발행 시간 (nullable)
     * @param createdAt 생성 시간
     * @return 재구성된 ExternalDownloadOutbox
     */
    public static ExternalDownloadOutbox of(
            ExternalDownloadOutboxId id,
            ExternalDownloadId externalDownloadId,
            boolean published,
            Instant publishedAt,
            Instant createdAt) {
        return new ExternalDownloadOutbox(
                id, externalDownloadId, published, publishedAt, createdAt);
    }

    /**
     * 발행 완료 처리.
     *
     * @param clock 시간 소스
     * @throws IllegalStateException 이미 발행된 경우
     */
    public void markAsPublished(Clock clock) {
        if (published) {
            throw new IllegalStateException("이미 발행된 Outbox입니다.");
        }
        this.published = true;
        this.publishedAt = Instant.now(clock);
    }

    /**
     * ID 값 조회.
     *
     * @return ID의 UUID 값
     */
    public UUID getIdValue() {
        return id.value();
    }

    /**
     * ExternalDownloadId 값 조회.
     *
     * @return ExternalDownloadId의 UUID 값
     */
    public UUID getExternalDownloadIdValue() {
        return externalDownloadId.value();
    }

    // Getters
    public ExternalDownloadOutboxId getId() {
        return id;
    }

    public ExternalDownloadId getExternalDownloadId() {
        return externalDownloadId;
    }

    public boolean isPublished() {
        return published;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
