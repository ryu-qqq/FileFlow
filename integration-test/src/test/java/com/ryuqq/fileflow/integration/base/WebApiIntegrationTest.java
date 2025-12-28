package com.ryuqq.fileflow.integration.base;

import com.ryuqq.fileflow.bootstrap.FileflowApplication;
import com.ryuqq.fileflow.integration.config.DatabaseCleaner;
import com.ryuqq.fileflow.integration.config.TestContainersConfig;
import com.ryuqq.fileflow.integration.config.WireMockConfig;
import com.ryuqq.fileflow.integration.helper.TestDataHelper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Web API 통합 테스트 베이스 클래스.
 *
 * <p>이 클래스를 상속받으면 다음이 자동으로 설정됩니다: - MySQL, Redis, LocalStack(S3/SQS) 컨테이너 - WireMock 서버 (외부 API 모킹)
 * - TestRestTemplate (실제 HTTP 요청) - DatabaseCleaner (테스트 격리) - TestDataHelper (테스트 데이터 삽입)
 *
 * <p>사용 예시: class MyIntegrationTest extends WebApiIntegrationTest { @Test void shouldDoSomething()
 * { // given testDataHelper.insertUser(...);
 *
 * <p>// when ResponseEntity<MyResponse> response = restTemplate.getForEntity( apiV1Url("/users/1"),
 * MyResponse.class);
 *
 * <p>// then assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK); } }
 */
@SpringBootTest(
        classes = FileflowApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    WireMockConfig.class,
    DatabaseCleaner.class,
    TestDataHelper.class
})
public abstract class WebApiIntegrationTest {

    @LocalServerPort protected int port;

    @Autowired protected TestRestTemplate restTemplate;

    @Autowired protected DatabaseCleaner databaseCleaner;

    @Autowired protected TestDataHelper testDataHelper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL
        registry.add("spring.datasource.url", TestContainersConfig::getMySqlJdbcUrl);
        registry.add("spring.datasource.username", TestContainersConfig::getMySqlUsername);
        registry.add("spring.datasource.password", TestContainersConfig::getMySqlPassword);

        // Redis
        registry.add("spring.data.redis.host", TestContainersConfig::getRedisHost);
        registry.add("spring.data.redis.port", TestContainersConfig::getRedisPort);

        // LocalStack S3 (aws.s3.* properties for S3ClientConfig)
        registry.add("aws.s3.endpoint", TestContainersConfig::getLocalStackEndpoint);
        registry.add("aws.s3.region", TestContainersConfig::getLocalStackRegion);
        registry.add("aws.s3.access-key", TestContainersConfig::getLocalStackAccessKey);
        registry.add("aws.s3.secret-key", TestContainersConfig::getLocalStackSecretKey);
        registry.add("aws.s3.bucket", TestContainersConfig::getTestBucketName);

        // LocalStack SQS
        registry.add("aws.sqs.endpoint", TestContainersConfig::getLocalStackEndpoint);
        registry.add("aws.sqs.region", TestContainersConfig::getLocalStackRegion);
        registry.add("aws.sqs.access-key", TestContainersConfig::getLocalStackAccessKey);
        registry.add("aws.sqs.secret-key", TestContainersConfig::getLocalStackSecretKey);

        // WireMock for external APIs
        registry.add("external.api.base-url", WireMockConfig::getBaseUrl);
    }

    @BeforeEach
    void setUpBase() {
        databaseCleaner.clean();
        WireMockConfig.reset();
    }

    // ========================================
    // URL Helper Methods
    // ========================================

    protected String baseUrl() {
        return "http://localhost:" + port;
    }

    protected String url(String path) {
        return baseUrl() + path;
    }

    protected String apiV1Url(String path) {
        return baseUrl() + "/api/v1" + path;
    }

    protected String apiV2Url(String path) {
        return baseUrl() + "/api/v2" + path;
    }
}
