package com.ryuqq.fileflow.application.download.manager.client;

import com.ryuqq.fileflow.application.common.metric.annotation.OutboundClientMetric;
import com.ryuqq.fileflow.application.download.dto.response.CallbackPayload;
import com.ryuqq.fileflow.application.download.port.out.client.CallbackNotificationClient;
import org.springframework.stereotype.Component;

@Component
public class CallbackNotificationManager {

    private final CallbackNotificationClient callbackNotificationClient;

    public CallbackNotificationManager(CallbackNotificationClient callbackNotificationClient) {
        this.callbackNotificationClient = callbackNotificationClient;
    }

    @OutboundClientMetric(system = "HTTP", operation = "callback_notification")
    public void notify(String callbackUrl, CallbackPayload payload) {
        callbackNotificationClient.notify(callbackUrl, payload);
    }
}
