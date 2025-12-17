package com.ryuqq.fileflow.domain.download.aggregate;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.download.vo.WebhookOutboxId;
import com.ryuqq.fileflow.domain.download.vo.WebhookOutboxStatus;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Webhook Outbox Aggregate.
 *
 * <p><strong>책임</strong>: Webhook 발송을 위한 Outbox 패턴 지원
 *
 * <p><strong>발송 규칙</strong>:
 *
 * <ul>
 *   <li>신규 생성 시 status = PENDING
 *   <li>발송 성공 시 markAsSent() → SENT
 *   <li>발송 실패 시 incrementRetry() → retryCount 증가, PENDING 유지
 *   <li>최대 재시도(2회) 초과 시 markAsFailed() → FAILED
 * </ul>
 *
 * <p><strong>상태 전이</strong>:
 *
 * <pre>
 * forNew() → PENDING
 *   ↓
 * markAsSent() → SENT (종료)
 *   또는
 * incrementRetry() → PENDING (재시도, retryCount < MAX)
 *   ↓
 * markAsFailed() → FAILED (최종 실패, 종료)
 * </pre>
 */
public class WebhookOutbox {

    /** 최대 재시도 횟수. */
    public static final int MAX_RETRY_COUNT = 2;

    private final WebhookOutboxId id;
    private final ExternalDownloadId externalDownloadId;
    private final WebhookUrl webhookUrl;
    private WebhookOutboxStatus status;
    private final ExternalDownloadStatus downloadStatus;
    private final FileAssetId fileAssetId;
    private final String errorMessage;
    private int retryCount;
    private String lastErrorMessage;
    private Instant sentAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private WebhookOutbox(
            WebhookOutboxId id,
            ExternalDownloadId externalDownloadId,
            WebhookUrl webhookUrl,
            WebhookOutboxStatus status,
            ExternalDownloadStatus downloadStatus,
            FileAssetId fileAssetId,
            String errorMessage,
            int retryCount,
            String lastErrorMessage,
            Instant sentAt,
            Instant createdAt,
            Instant updatedAt) {
        Objects.requireNonNull(externalDownloadId, "externalDownloadId must not be null");
        Objects.requireNonNull(webhookUrl, "webhookUrl must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(downloadStatus, "downloadStatus must not be null");

        this.id = id;
        this.externalDownloadId = externalDownloadId;
        this.webhookUrl = webhookUrl;
        this.status = status;
        this.downloadStatus = downloadStatus;
        this.fileAssetId = fileAssetId;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
        this.lastErrorMessage = lastErrorMessage;
        this.sentAt = sentAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 새 WebhookOutbox 생성 (ID null, status = PENDING).
     *
     * @param externalDownloadId 외부 다운로드 ID
     * @param webhookUrl 웹훅 URL
     * @param downloadStatus 다운로드 결과 상태 (COMPLETED 또는 FAILED)
     * @param fileAssetId 생성된 파일 자산 ID (성공 시, nullable)
     * @param errorMessage 에러 메시지 (실패 시, nullable)
     * @param clock 시간 소스
     * @return 신규 WebhookOutbox
     */
    public static WebhookOutbox forNew(
            ExternalDownloadId externalDownloadId,
            WebhookUrl webhookUrl,
            ExternalDownloadStatus downloadStatus,
            FileAssetId fileAssetId,
            String errorMessage,
            Clock clock) {
        Instant now = Instant.now(clock);
        return new WebhookOutbox(
                WebhookOutboxId.forNew(),
                externalDownloadId,
                webhookUrl,
                WebhookOutboxStatus.PENDING,
                downloadStatus,
                fileAssetId,
                errorMessage,
                0,
                null,
                null,
                now,
                now);
    }

    /**
     * 기존 WebhookOutbox 재구성 (조회용).
     *
     * @param id Outbox ID
     * @param externalDownloadId 외부 다운로드 ID
     * @param webhookUrl 웹훅 URL
     * @param status 발송 상태
     * @param downloadStatus 다운로드 결과 상태
     * @param fileAssetId 파일 자산 ID (nullable)
     * @param errorMessage 에러 메시지 (nullable)
     * @param retryCount 재시도 횟수
     * @param lastErrorMessage 마지막 에러 메시지 (nullable)
     * @param sentAt 발송 시간 (nullable)
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     * @return 재구성된 WebhookOutbox
     */
    public static WebhookOutbox of(
            WebhookOutboxId id,
            ExternalDownloadId externalDownloadId,
            WebhookUrl webhookUrl,
            WebhookOutboxStatus status,
            ExternalDownloadStatus downloadStatus,
            FileAssetId fileAssetId,
            String errorMessage,
            int retryCount,
            String lastErrorMessage,
            Instant sentAt,
            Instant createdAt,
            Instant updatedAt) {
        return new WebhookOutbox(
                id,
                externalDownloadId,
                webhookUrl,
                status,
                downloadStatus,
                fileAssetId,
                errorMessage,
                retryCount,
                lastErrorMessage,
                sentAt,
                createdAt,
                updatedAt);
    }

    /**
     * 발송 성공 처리.
     *
     * @param clock 시간 소스
     * @throws IllegalStateException 이미 종료 상태인 경우
     */
    public void markAsSent(Clock clock) {
        if (status.isTerminal()) {
            throw new IllegalStateException("이미 종료 상태입니다. 현재 상태: " + status);
        }
        this.status = WebhookOutboxStatus.SENT;
        this.sentAt = Instant.now(clock);
        this.updatedAt = Instant.now(clock);
    }

    /**
     * 재시도 횟수 증가.
     *
     * <p>최대 재시도 횟수에 도달하면 {@link #markAsFailed(String, Clock)}를 호출해야 합니다.
     *
     * @param webhookErrorMessage 웹훅 호출 에러 메시지
     * @param clock 시간 소스
     * @throws IllegalStateException 이미 종료 상태인 경우
     * @throws IllegalStateException 최대 재시도 횟수를 초과한 경우
     */
    public void incrementRetry(String webhookErrorMessage, Clock clock) {
        if (status.isTerminal()) {
            throw new IllegalStateException("이미 종료 상태입니다. 현재 상태: " + status);
        }
        if (retryCount >= MAX_RETRY_COUNT) {
            throw new IllegalStateException(
                    "최대 재시도 횟수를 초과했습니다. markAsFailed()를 호출하세요. retryCount: " + retryCount);
        }
        this.retryCount++;
        this.lastErrorMessage = webhookErrorMessage;
        this.updatedAt = Instant.now(clock);
    }

    /**
     * 최종 실패 처리.
     *
     * @param webhookErrorMessage 웹훅 호출 에러 메시지
     * @param clock 시간 소스
     * @throws IllegalStateException 이미 종료 상태인 경우
     */
    public void markAsFailed(String webhookErrorMessage, Clock clock) {
        if (status.isTerminal()) {
            throw new IllegalStateException("이미 종료 상태입니다. 현재 상태: " + status);
        }
        this.status = WebhookOutboxStatus.FAILED;
        this.lastErrorMessage = webhookErrorMessage;
        this.updatedAt = Instant.now(clock);
    }

    /**
     * 재시도 가능 여부 확인.
     *
     * @return PENDING 상태이고 최대 재시도 횟수 미만이면 true
     */
    public boolean canRetry() {
        return status.canRetry() && retryCount < MAX_RETRY_COUNT;
    }

    /**
     * 최대 재시도 횟수 도달 여부 확인.
     *
     * @return 최대 재시도 횟수에 도달했으면 true
     */
    public boolean hasReachedMaxRetry() {
        return retryCount >= MAX_RETRY_COUNT;
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

    /**
     * WebhookUrl 값 조회.
     *
     * @return 웹훅 URL 문자열
     */
    public String getWebhookUrlValue() {
        return webhookUrl.value();
    }

    /**
     * FileAssetId 값 조회.
     *
     * @return FileAssetId의 UUID 값 또는 null
     */
    public UUID getFileAssetIdValue() {
        return fileAssetId != null ? fileAssetId.value() : null;
    }

    // Getters
    public WebhookOutboxId getId() {
        return id;
    }

    public ExternalDownloadId getExternalDownloadId() {
        return externalDownloadId;
    }

    public WebhookUrl getWebhookUrl() {
        return webhookUrl;
    }

    public WebhookOutboxStatus getStatus() {
        return status;
    }

    public ExternalDownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public FileAssetId getFileAssetId() {
        return fileAssetId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
