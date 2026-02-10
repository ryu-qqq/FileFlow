package com.ryuqq.fileflow.application.download.manager.client;

import com.ryuqq.fileflow.application.download.port.out.client.CallbackNotificationClient;
import org.springframework.stereotype.Component;

@Component
public class CallbackNotificationManager {

    private final CallbackNotificationClient callbackNotificationClient;

    public CallbackNotificationManager(CallbackNotificationClient callbackNotificationClient) {
        this.callbackNotificationClient = callbackNotificationClient;
    }

    public void notify(String callbackUrl, String downloadTaskId, String status) {
        callbackNotificationClient.notify(callbackUrl, downloadTaskId, status);
    }
}
