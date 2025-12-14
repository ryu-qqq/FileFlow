package com.ryuqq.fileflow.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Web API Application Context Loading Test.
 *
 * <p>Spring 컨텍스트가 정상적으로 로딩되는지 검증합니다. 빈 누락, 순환 의존성, 구현체 미등록 등의 문제를 감지합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.data.redis.enabled=false")
@Import(TestMockConfig.class)
class WebApiApplicationContextTest {

    @Test
    void contextLoads() {
        // Spring Context가 정상적으로 로딩되면 테스트 통과
    }
}
