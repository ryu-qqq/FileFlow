package com.ryuqq.fileflow.application.download.port.out.client;

import com.ryuqq.fileflow.application.download.dto.WebhookPayload;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;

/**
 * Webhook 호출 포트.
 *
 * <p>외부 다운로드 완료 후 결과를 콜백 URL로 전송
 */
public interface WebhookPort {

    /**
     * Webhook URL로 결과를 전송합니다.
     *
     * @param webhookUrl 콜백 URL
     * @param payload Webhook 페이로드
     */
    void call(WebhookUrl webhookUrl, WebhookPayload payload);
}
