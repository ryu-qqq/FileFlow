package com.ryuqq.fileflow.domain.download.event;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;
import java.time.Instant;
import java.util.Objects;

/**
 * 외부 다운로드 Webhook 트리거 이벤트.
 *
 * <p>ExternalDownload 완료(COMPLETED) 또는 실패(FAILED) 시 webhookUrl이 존재하면 발행됩니다.
 *
 * <p>이 이벤트를 수신한 리스너에서 WebhookOutbox를 생성하고 실제 webhook 호출을 수행합니다.
 *
 * @param downloadId 외부 다운로드 ID
 * @param webhookUrl 웹훅 URL
 * @param status 처리 결과 상태 (COMPLETED 또는 FAILED)
 * @param fileAssetId 생성된 파일 자산 ID (성공 시, nullable)
 * @param errorMessage 에러 메시지 (실패 시, nullable)
 * @param occurredAt 이벤트 발생 시간
 */
public record ExternalDownloadWebhookTriggeredEvent(
        ExternalDownloadId downloadId,
        WebhookUrl webhookUrl,
        ExternalDownloadStatus status,
        FileAssetId fileAssetId,
        String errorMessage,
        Instant occurredAt)
        implements DomainEvent {

    /**
     * 완료 시 이벤트 생성 팩토리 메서드.
     *
     * @param downloadId 외부 다운로드 ID
     * @param webhookUrl 웹훅 URL
     * @param fileAssetId 생성된 파일 자산 ID
     * @param occurredAt 이벤트 발생 시간
     * @return ExternalDownloadWebhookTriggeredEvent
     */
    public static ExternalDownloadWebhookTriggeredEvent forCompleted(
            ExternalDownloadId downloadId,
            WebhookUrl webhookUrl,
            FileAssetId fileAssetId,
            Instant occurredAt) {
        Objects.requireNonNull(downloadId, "downloadId must not be null");
        Objects.requireNonNull(webhookUrl, "webhookUrl must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");

        return new ExternalDownloadWebhookTriggeredEvent(
                downloadId,
                webhookUrl,
                ExternalDownloadStatus.COMPLETED,
                fileAssetId,
                null,
                occurredAt);
    }

    /**
     * 실패 시 이벤트 생성 팩토리 메서드.
     *
     * @param downloadId 외부 다운로드 ID
     * @param webhookUrl 웹훅 URL
     * @param errorMessage 에러 메시지
     * @param occurredAt 이벤트 발생 시간
     * @return ExternalDownloadWebhookTriggeredEvent
     */
    public static ExternalDownloadWebhookTriggeredEvent forFailed(
            ExternalDownloadId downloadId,
            WebhookUrl webhookUrl,
            String errorMessage,
            Instant occurredAt) {
        Objects.requireNonNull(downloadId, "downloadId must not be null");
        Objects.requireNonNull(webhookUrl, "webhookUrl must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");

        return new ExternalDownloadWebhookTriggeredEvent(
                downloadId,
                webhookUrl,
                ExternalDownloadStatus.FAILED,
                null,
                errorMessage,
                occurredAt);
    }

    /**
     * 완료 상태인지 확인합니다.
     *
     * @return COMPLETED 상태이면 true
     */
    public boolean isCompleted() {
        return status == ExternalDownloadStatus.COMPLETED;
    }

    /**
     * 실패 상태인지 확인합니다.
     *
     * @return FAILED 상태이면 true
     */
    public boolean isFailed() {
        return status == ExternalDownloadStatus.FAILED;
    }
}
