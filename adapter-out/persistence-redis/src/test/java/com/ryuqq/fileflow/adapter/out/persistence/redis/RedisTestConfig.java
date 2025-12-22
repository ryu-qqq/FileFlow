package com.ryuqq.fileflow.adapter.out.persistence.redis;

import org.springframework.boot.test.context.TestConfiguration;

/**
 * Redis 모듈 테스트용 Configuration.
 *
 * <p>테스트에서 필요한 Mock Bean들을 제공합니다.
 *
 * <p><strong>Note</strong>: Metrics는 AOP 기반 @DownstreamMetric 어노테이션으로 처리되어 더 이상 직접 mock이 필요하지 않습니다.
 */
@TestConfiguration
public class RedisTestConfig {
    // AOP 기반 메트릭 처리로 인해 별도의 mock bean 설정이 필요 없음
}
