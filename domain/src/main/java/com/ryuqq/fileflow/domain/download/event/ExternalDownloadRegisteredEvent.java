package com.ryuqq.fileflow.domain.download.event;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 외부 다운로드 등록 이벤트.
 *
 * <p>ExternalDownload가 생성될 때 발행됩니다.
 */
public record ExternalDownloadRegisteredEvent(
        ExternalDownloadId downloadId,
        SourceUrl sourceUrl,
        long tenantId,
        long organizationId,
        WebhookUrl webhookUrl,
        LocalDateTime occurredAt)
        implements DomainEvent {

    /**
     * 이벤트 생성 팩토리 메서드.
     *
     * @param downloadId 다운로드 ID
     * @param sourceUrl 소스 URL
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param webhookUrl 웹훅 URL (nullable)
     * @param occurredAt 이벤트 발생 시간 (Instant)
     * @return ExternalDownloadRegisteredEvent
     */
    public static ExternalDownloadRegisteredEvent of(
            ExternalDownloadId downloadId,
            SourceUrl sourceUrl,
            long tenantId,
            long organizationId,
            WebhookUrl webhookUrl,
            Instant occurredAt) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(occurredAt, ZoneId.systemDefault());
        return new ExternalDownloadRegisteredEvent(
                downloadId, sourceUrl, tenantId, organizationId, webhookUrl, localDateTime);
    }
}
