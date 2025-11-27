package com.ryuqq.fileflow.bootstrap;

import com.ryuqq.fileflow.application.download.port.out.client.SqsPublishPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 테스트용 Mock 구성.
 *
 * <p>통합 테스트에서 인프라 의존성(SQS 등)을 Mock으로 대체합니다.
 */
@TestConfiguration
public class TestMockConfig {

    /**
     * SqsPublishPort Mock 빈.
     *
     * <p>실제 SQS 없이 테스트 가능하도록 No-op 구현 제공.
     *
     * @return SqsPublishPort mock 구현체
     */
    @Bean
    @Primary
    public SqsPublishPort sqsPublishPort() {
        return message -> true;
    }
}
