package com.ryuqq.fileflow.adapter.out.client.http.client;

import com.ryuqq.fileflow.application.download.port.out.client.CallbackNotificationClient;
import java.net.URI;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CallbackNotificationHttpClient implements CallbackNotificationClient {

    private static final Logger log = LoggerFactory.getLogger(CallbackNotificationHttpClient.class);

    private final RestClient restClient;

    public CallbackNotificationHttpClient(@Qualifier("callbackRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void notify(String callbackUrl, String downloadTaskId, String status) {
        log.info(
                "콜백 알림 전송: callbackUrl={}, taskId={}, status={}",
                callbackUrl,
                downloadTaskId,
                status);

        Map<String, String> payload =
                Map.of(
                        "downloadTaskId", downloadTaskId,
                        "status", status);

        restClient
                .post()
                .uri(URI.create(callbackUrl))
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();

        log.info("콜백 알림 전송 완료: callbackUrl={}, taskId={}", callbackUrl, downloadTaskId);
    }
}
