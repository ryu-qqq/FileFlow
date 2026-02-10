package com.ryuqq.fileflow.adapter.out.client.http.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HttpClientProperties {

    private final int downloadConnectTimeout;
    private final int downloadReadTimeout;
    private final int callbackConnectTimeout;
    private final int callbackReadTimeout;

    public HttpClientProperties(
            @Value("${fileflow.http-client.download.connect-timeout:5000}")
                    int downloadConnectTimeout,
            @Value("${fileflow.http-client.download.read-timeout:60000}") int downloadReadTimeout,
            @Value("${fileflow.http-client.callback.connect-timeout:3000}")
                    int callbackConnectTimeout,
            @Value("${fileflow.http-client.callback.read-timeout:10000}") int callbackReadTimeout) {
        this.downloadConnectTimeout = downloadConnectTimeout;
        this.downloadReadTimeout = downloadReadTimeout;
        this.callbackConnectTimeout = callbackConnectTimeout;
        this.callbackReadTimeout = callbackReadTimeout;
    }

    public int downloadConnectTimeout() {
        return downloadConnectTimeout;
    }

    public int downloadReadTimeout() {
        return downloadReadTimeout;
    }

    public int callbackConnectTimeout() {
        return callbackConnectTimeout;
    }

    public int callbackReadTimeout() {
        return callbackReadTimeout;
    }
}
