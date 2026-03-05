package com.ryuqq.fileflow.application.transform.manager.client;

import com.ryuqq.fileflow.application.common.metric.annotation.OutboundClientMetric;
import com.ryuqq.fileflow.application.transform.dto.response.TransformCallbackPayload;
import com.ryuqq.fileflow.application.transform.port.out.client.TransformCallbackNotificationClient;
import org.springframework.stereotype.Component;

@Component
public class TransformCallbackNotificationManager {

    private final TransformCallbackNotificationClient transformCallbackNotificationClient;

    public TransformCallbackNotificationManager(
            TransformCallbackNotificationClient transformCallbackNotificationClient) {
        this.transformCallbackNotificationClient = transformCallbackNotificationClient;
    }

    @OutboundClientMetric(system = "HTTP", operation = "transform_callback_notification")
    public void notify(String callbackUrl, TransformCallbackPayload payload) {
        transformCallbackNotificationClient.notify(callbackUrl, payload);
    }
}
