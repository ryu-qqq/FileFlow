package com.ryuqq.fileflow.adapter.out.client.http.client;

import com.ryuqq.fileflow.application.download.exception.PermanentCallbackFailureException;
import com.ryuqq.fileflow.application.transform.dto.response.TransformCallbackPayload;
import com.ryuqq.fileflow.application.transform.port.out.client.TransformCallbackNotificationClient;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class TransformCallbackNotificationHttpClient
        implements TransformCallbackNotificationClient {

    private static final Logger log =
            LoggerFactory.getLogger(TransformCallbackNotificationHttpClient.class);

    private final RestClient restClient;

    public TransformCallbackNotificationHttpClient(
            @Qualifier("callbackRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void notify(String callbackUrl, TransformCallbackPayload payload) {
        log.info(
                "변환 콜백 알림 전송: callbackUrl={}, transformRequestId={}, status={}",
                callbackUrl,
                payload.transformRequestId(),
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
                    "변환 콜백 알림 전송 완료: callbackUrl={}, transformRequestId={}",
                    callbackUrl,
                    payload.transformRequestId());
        } catch (HttpClientErrorException e) {
            throw new PermanentCallbackFailureException(
                    "HTTP " + e.getStatusCode().value() + ": " + callbackUrl, e);
        }
    }
}
