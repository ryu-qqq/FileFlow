package com.ryuqq.fileflow.application.download.port.out.client;

import com.ryuqq.fileflow.application.download.dto.response.CallbackPayload;

public interface CallbackNotificationClient {

    void notify(String callbackUrl, CallbackPayload payload);
}
