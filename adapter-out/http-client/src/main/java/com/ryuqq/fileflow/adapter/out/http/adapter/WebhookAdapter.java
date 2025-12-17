package com.ryuqq.fileflow.adapter.out.http.adapter;

import com.ryuqq.fileflow.application.download.dto.WebhookPayload;
import com.ryuqq.fileflow.application.download.port.out.client.WebhookPort;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Webhook 호출 어댑터.
 *
 * <p>ExternalDownload 완료 후 결과를 콜백 URL로 전송하는 Outbound Adapter.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>WebhookUrl과 WebhookPayload를 받아 HTTP POST 요청 생성
 *   <li>JSON 형식으로 페이로드 전송
 *   <li>응답 확인 (2xx 성공, 그 외 예외)
 * </ol>
 *
 * <p><strong>에러 처리</strong>:
 *
 * <ul>
 *   <li>4xx/5xx 응답: IllegalStateException
 *   <li>타임아웃: WebClientException
 *   <li>연결 실패: IllegalStateException
 * </ul>
 */
@Component
public class WebhookAdapter implements WebhookPort {

    private static final Logger log = LoggerFactory.getLogger(WebhookAdapter.class);

    private final WebClient webhookWebClient;

    public WebhookAdapter(WebClient webhookWebClient) {
        this.webhookWebClient = webhookWebClient;
    }

    @Override
    public void call(WebhookUrl webhookUrl, WebhookPayload payload) {
        String url = webhookUrl.value();
        log.info(
                "Webhook 호출 시작: url={}, externalDownloadId={}, status={}",
                url,
                payload.externalDownloadId(),
                payload.status());

        try {
            webhookWebClient
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info(
                    "Webhook 호출 성공: url={}, externalDownloadId={}",
                    url,
                    payload.externalDownloadId());

        } catch (WebClientResponseException e) {
            log.error(
                    "Webhook 호출 실패: url={}, status={}, message={}",
                    url,
                    e.getStatusCode(),
                    e.getMessage());
            throw new IllegalStateException(
                    "Webhook 호출 실패: " + url + ", status=" + e.getStatusCode(), e);

        } catch (Exception e) {
            log.error("Webhook 호출 예외: url={}, error={}", url, e.getMessage(), e);
            throw new IllegalStateException("Webhook 호출 예외: " + url, e);
        }
    }
}
