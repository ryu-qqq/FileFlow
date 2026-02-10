package com.ryuqq.fileflow.integration.test.e2e.sdk;

import com.ryuqq.fileflow.integration.test.common.base.E2ETestBase;
import com.ryuqq.fileflow.sdk.FileFlowClient;
import org.junit.jupiter.api.BeforeEach;

/**
 * SDK 통합 테스트 Base 클래스.
 *
 * <p>E2ETestBase를 상속하여 TestContainers 환경을 사용하면서, FileFlowClient SDK를 통해 API를 호출하여 직렬화/역직렬화, 인증 헤더,
 * 에러 핸들링이 실제 서버와 정상 동작하는지 검증합니다.
 */
public abstract class SdkTestBase extends E2ETestBase {

    protected FileFlowClient client;

    @BeforeEach
    void setUpSdkClient() {
        client =
                FileFlowClient.builder()
                        .baseUrl("http://localhost:" + port)
                        .serviceName("integration-test")
                        .serviceToken("test-integration-token")
                        .build();
    }
}
