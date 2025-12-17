package com.ryuqq.fileflow.application.download.service;

import com.ryuqq.fileflow.application.asset.port.out.query.FileAssetQueryPort;
import com.ryuqq.fileflow.application.download.dto.WebhookPayload;
import com.ryuqq.fileflow.application.download.manager.command.WebhookOutboxTransactionManager;
import com.ryuqq.fileflow.application.download.port.out.client.WebhookPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.download.aggregate.WebhookOutbox;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Webhook 전송 서비스.
 *
 * <p>WebhookOutbox를 기반으로 webhook을 호출하고 결과를 업데이트합니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>WebhookOutbox에서 페이로드 생성
 *   <li>WebhookPort를 통해 HTTP POST 호출
 *   <li>성공 시 markAsSent(), 실패 시 incrementRetry() 또는 markAsFailed()
 * </ol>
 */
@Service
public class WebhookSendService {

    private static final Logger log = LoggerFactory.getLogger(WebhookSendService.class);

    private final WebhookPort webhookPort;
    private final WebhookOutboxTransactionManager webhookOutboxTransactionManager;
    private final FileAssetQueryPort fileAssetQueryPort;
    private final Clock clock;
    private final String cdnHost;

    public WebhookSendService(
            WebhookPort webhookPort,
            WebhookOutboxTransactionManager webhookOutboxTransactionManager,
            FileAssetQueryPort fileAssetQueryPort,
            Clock clock,
            @Value("${cdn.host:https://cdn.set-of.com}") String cdnHost) {
        this.webhookPort = webhookPort;
        this.webhookOutboxTransactionManager = webhookOutboxTransactionManager;
        this.fileAssetQueryPort = fileAssetQueryPort;
        this.clock = clock;
        this.cdnHost = cdnHost;
    }

    /**
     * Webhook을 전송합니다.
     *
     * <p>전송 성공 시 markAsSent(), 실패 시 재시도 여부에 따라 incrementRetry() 또는 markAsFailed()를 호출합니다.
     *
     * @param outbox WebhookOutbox
     */
    @Transactional
    public void send(WebhookOutbox outbox) {
        log.info(
                "Webhook 전송 시작: outboxId={}, webhookUrl={}, retryCount={}",
                outbox.getIdValue(),
                outbox.getWebhookUrlValue(),
                outbox.getRetryCount());

        try {
            WebhookPayload payload = createPayload(outbox);
            webhookPort.call(outbox.getWebhookUrl(), payload);

            // 성공
            outbox.markAsSent(clock);
            webhookOutboxTransactionManager.update(outbox);

            log.info(
                    "Webhook 전송 성공: outboxId={}, downloadId={}",
                    outbox.getIdValue(),
                    outbox.getExternalDownloadIdValue());

        } catch (Exception e) {
            handleFailure(outbox, e);
        }
    }

    /**
     * WebhookOutbox에서 페이로드를 생성합니다.
     *
     * @param outbox WebhookOutbox
     * @return WebhookPayload
     */
    private WebhookPayload createPayload(WebhookOutbox outbox) {
        UUID fileAssetIdValue = outbox.getFileAssetIdValue();
        String fileUrl = resolveFileUrl(fileAssetIdValue);

        return new WebhookPayload(
                outbox.getExternalDownloadIdValue().toString(),
                outbox.getDownloadStatus().name(),
                fileAssetIdValue != null ? fileAssetIdValue.toString() : null,
                fileUrl,
                outbox.getErrorMessage(),
                Instant.now(clock));
    }

    /**
     * FileAssetId로 CDN 파일 URL을 조회합니다.
     *
     * @param fileAssetIdValue FileAsset ID UUID (nullable)
     * @return CDN 파일 URL 또는 null
     */
    private String resolveFileUrl(UUID fileAssetIdValue) {
        if (fileAssetIdValue == null) {
            return null;
        }

        return fileAssetQueryPort
                .findById(FileAssetId.of(fileAssetIdValue))
                .map(this::buildCdnUrl)
                .orElse(null);
    }

    /**
     * FileAsset에서 CDN URL을 생성합니다.
     *
     * @param fileAsset FileAsset
     * @return CDN URL
     */
    private String buildCdnUrl(FileAsset fileAsset) {
        String s3Key = fileAsset.getS3KeyValue();
        if (s3Key == null) {
            return null;
        }
        return cdnHost + "/" + s3Key;
    }

    /**
     * 전송 실패를 처리합니다.
     *
     * <p>재시도 가능하면 incrementRetry(), 최대 재시도 초과 시 markAsFailed()를 호출합니다.
     *
     * @param outbox WebhookOutbox
     * @param e 발생한 예외
     */
    private void handleFailure(WebhookOutbox outbox, Exception e) {
        String errorMessage = e.getMessage();

        if (outbox.canRetry()) {
            outbox.incrementRetry(errorMessage, clock);
            webhookOutboxTransactionManager.update(outbox);

            log.warn(
                    "Webhook 전송 실패 - 재시도 예정: outboxId={}, retryCount={}, error={}",
                    outbox.getIdValue(),
                    outbox.getRetryCount(),
                    errorMessage);
        } else {
            outbox.markAsFailed(errorMessage, clock);
            webhookOutboxTransactionManager.update(outbox);

            log.error(
                    "Webhook 전송 최종 실패: outboxId={}, downloadId={}, error={}",
                    outbox.getIdValue(),
                    outbox.getExternalDownloadIdValue(),
                    errorMessage);
        }
    }
}
