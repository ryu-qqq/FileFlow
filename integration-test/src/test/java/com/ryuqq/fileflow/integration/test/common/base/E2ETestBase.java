package com.ryuqq.fileflow.integration.test.common.base;

import com.ryuqq.fileflow.integration.test.IntegrationTestApplication;
import com.ryuqq.fileflow.integration.test.common.config.IntegrationTestConfig;
import com.ryuqq.fileflow.integration.test.common.container.TestContainerConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * E2E 통합 테스트 Base 클래스.
 *
 * <p>TestContainers(MySQL, Redis, LocalStack)를 사용하여 실제 인프라 환경에서 REST API를 테스트합니다.
 *
 * <p>RestAssured를 통한 HTTP 요청 헬퍼 메서드를 제공합니다.
 */
@SpringBootTest(
        classes = IntegrationTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
@ActiveProfiles("test")
public abstract class E2ETestBase {

    @LocalServerPort protected int port;

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        TestContainerConfig.registerProperties(registry);
    }

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
    }

    /** JSON Content-Type이 설정된 기본 RequestSpecification. */
    protected RequestSpecification givenJson() {
        return RestAssured.given().contentType(ContentType.JSON).accept(ContentType.JSON);
    }

    /**
     * Service Token 인증이 포함된 RequestSpecification. application-test.yml의
     * security.service-token.secret 값과 일치해야 합니다.
     */
    protected RequestSpecification givenServiceAuth() {
        return givenJson()
                .header("X-Service-Name", "integration-test")
                .header("X-Service-Token", "test-integration-token");
    }
}
