package com.ryuqq.fileflow.sdk.client.internal;

import com.ryuqq.fileflow.sdk.FileFlowClient;
import java.io.IOException;
import okhttp3.mockwebserver.MockWebServer;

final class ApiTestSupport {

    private ApiTestSupport() {}

    static MockWebServer startMockServer() throws IOException {
        MockWebServer server = new MockWebServer();
        server.start();
        return server;
    }

    static String baseUrl(MockWebServer server) {
        String url = server.url("/").toString();
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    static FileFlowClient createClient(MockWebServer server) {
        return FileFlowClient.builder()
                .baseUrl(baseUrl(server))
                .serviceName("test-service")
                .serviceToken("test-token")
                .build();
    }
}
