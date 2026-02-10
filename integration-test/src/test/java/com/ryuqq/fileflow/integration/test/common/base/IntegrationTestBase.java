package com.ryuqq.fileflow.integration.test.common.base;

import com.ryuqq.fileflow.integration.test.IntegrationTestApplication;
import com.ryuqq.fileflow.integration.test.common.config.IntegrationTestConfig;
import com.ryuqq.fileflow.integration.test.common.container.TestContainerConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * 비-HTTP 통합 테스트 Base 클래스.
 *
 * <p>TestContainers(MySQL, Redis, LocalStack)를 사용하여 Redis Consumer, Distributed Lock 등 비-HTTP 컴포넌트를
 * 테스트합니다.
 *
 * <p>HTTP 요청이 필요 없는 통합 테스트에서 사용합니다. HTTP API 테스트는 {@link E2ETestBase}를 사용하세요.
 */
@SpringBootTest(
        classes = IntegrationTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        TestContainerConfig.registerProperties(registry);
    }
}
