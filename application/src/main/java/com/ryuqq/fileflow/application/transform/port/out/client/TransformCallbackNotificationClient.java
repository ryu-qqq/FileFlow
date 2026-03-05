package com.ryuqq.fileflow.application.transform.port.out.client;

import com.ryuqq.fileflow.application.transform.dto.response.TransformCallbackPayload;

public interface TransformCallbackNotificationClient {

    void notify(String callbackUrl, TransformCallbackPayload payload);
}
