package com.ryuqq.fileflow.adapter.out.client.http.client;

import com.ryuqq.fileflow.application.download.dto.response.CallbackPayload;
import com.ryuqq.fileflow.application.download.exception.PermanentCallbackFailureException;
import com.ryuqq.fileflow.application.download.port.out.client.CallbackNotificationClient;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class CallbackNotificationHttpClient implements CallbackNotificationClient {

    private static final Logger log = LoggerFactory.getLogger(CallbackNotificationHttpClient.class);

    private final RestClient restClient;

    public CallbackNotificationHttpClient(@Qualifier("callbackRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void notify(String callbackUrl, CallbackPayload payload) {
        log.info(
                "콜백 알림 전송: callbackUrl={}, taskId={}, status={}",
                callbackUrl,
                payload.downloadTaskId(),
                payload.status());

        try {
            restClient
                    .post()
                    .uri(URI.create(callbackUrl))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info(
                    "콜백 알림 전송 완료: callbackUrl={}, taskId={}",
                    callbackUrl,
                    payload.downloadTaskId());
        } catch (HttpClientErrorException e) {
            throw new PermanentCallbackFailureException(
                    "HTTP " + e.getStatusCode().value() + ": " + callbackUrl, e);
        }
    }
}
