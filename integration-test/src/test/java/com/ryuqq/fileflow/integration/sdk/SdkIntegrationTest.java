package com.ryuqq.fileflow.integration.sdk;

import com.ryuqq.fileflow.integration.base.WebApiIntegrationTest;
import com.ryuqq.fileflow.sdk.client.FileFlowClient;
import org.junit.jupiter.api.BeforeEach;

/**
 * SDK 통합 테스트 베이스 클래스.
 *
 * <p>실제 서버와 통신하는 FileFlowClient를 사용하여 SDK의 E2E 동작을 검증합니다.
 */
public abstract class SdkIntegrationTest extends WebApiIntegrationTest {

    protected FileFlowClient fileFlowClient;

    protected static final String TEST_SERVICE_TOKEN = "test-integration-token";

    @BeforeEach
    void setUpSdk() {
        fileFlowClient = FileFlowClient.builder()
                .baseUrl(baseUrl() + "/api/v1/file")
                .serviceToken(TEST_SERVICE_TOKEN)
                .build();
    }
}
