package com.ryuqq.fileflow.integration.test.common.config;

import static org.mockito.Mockito.mock;

import com.ryuqq.fileflow.adapter.in.redis.config.RedisConsumerProperties;
import com.ryuqq.fileflow.application.asset.port.out.client.MetadataExtractionPort;
import com.ryuqq.fileflow.application.download.port.out.client.DownloadQueueClient;
import com.ryuqq.fileflow.application.transform.port.out.client.ImageTransformClient;
import com.ryuqq.fileflow.application.transform.port.out.client.TransformQueueClient;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * E2E 통합 테스트 공통 설정.
 *
 * <p>Bootstrap 모듈에서 제공되어야 하지만 아직 구현되지 않은 인프라 Bean을 제공합니다.
 *
 * <p>RedissonClient는 persistence-redis 모듈의 RedissonConfig가 제공합니다. DynamicPropertySource를 통해
 * redisson.singleServerConfig.address가 TestContainers Redis로 설정됩니다.
 *
 * <p>StringRedisTemplate은 spring-boot-starter-data-redis의 자동설정이 제공합니다. DynamicPropertySource를 통해
 * spring.data.redis.host/port가 TestContainers Redis로 설정됩니다.
 */
@TestConfiguration
@EnableConfigurationProperties(RedisConsumerProperties.class)
public class IntegrationTestConfig {

    /**
     * UTC 기준 시스템 Clock Bean.
     *
     * <p>TimeProvider가 의존하는 Clock 인스턴스를 제공합니다. 운영 환경에서는 Bootstrap 모듈의 Config에서 제공되어야 합니다.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * ImageTransformClient Mock Bean.
     *
     * <p>image-transform-client 모듈(scrimage)은 bootstrap-web-api에 포함되지 않습니다. Session E2E 테스트에서는 이미지
     * 변환 기능을 사용하지 않으므로 Mock으로 대체합니다.
     */
    @Bean
    public ImageTransformClient imageTransformClient() {
        return mock(ImageTransformClient.class);
    }

    /**
     * MetadataExtractionPort Mock Bean.
     *
     * <p>image-transform-client 모듈에서 구현되는 포트입니다. Session E2E 테스트에서는 메타데이터 추출 기능을 사용하지 않으므로 Mock으로
     * 대체합니다.
     */
    @Bean
    public MetadataExtractionPort metadataExtractionPort() {
        return mock(MetadataExtractionPort.class);
    }

    /**
     * DownloadQueueClient Mock Bean.
     *
     * <p>sqs-publisher 모듈에서 구현되는 포트입니다. E2E 테스트에서는 실제 SQS 큐 발행이 불필요하므로 Mock으로 대체합니다.
     */
    @Bean
    public DownloadQueueClient downloadQueueClient() {
        return mock(DownloadQueueClient.class);
    }

    /**
     * TransformQueueClient Mock Bean.
     *
     * <p>sqs-publisher 모듈에서 구현되는 포트입니다. E2E 테스트에서는 실제 SQS 큐 발행이 불필요하므로 Mock으로 대체합니다.
     */
    @Bean
    public TransformQueueClient transformQueueClient() {
        return mock(TransformQueueClient.class);
    }
}
