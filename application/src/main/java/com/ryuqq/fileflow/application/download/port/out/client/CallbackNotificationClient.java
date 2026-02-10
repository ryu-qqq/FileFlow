package com.ryuqq.fileflow.application.download.port.out.client;

public interface CallbackNotificationClient {

    void notify(String callbackUrl, String downloadTaskId, String status);
}
