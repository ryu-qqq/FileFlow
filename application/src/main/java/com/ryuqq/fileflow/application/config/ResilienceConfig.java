package com.ryuqq.fileflow.application.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j 공통 설정
 *
 * Circuit Breaker Registry를 Spring Bean으로 관리하고
 * 메트릭을 자동으로 등록합니다.
 *
 * @author sangwon-ryu
 */
@Configuration
@EnableConfigurationProperties(RetryProperties.class)
public class ResilienceConfig {

    private final RetryProperties retryProperties;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param retryProperties 재시도 및 Circuit Breaker 설정 프로퍼티
     */
    public ResilienceConfig(RetryProperties retryProperties) {
        this.retryProperties = retryProperties;
    }

    /**
     * CircuitBreakerRegistry를 Spring Bean으로 등록
     *
     * 여러 어댑터에서 공통으로 사용할 수 있도록
     * CircuitBreakerRegistry를 중앙에서 관리합니다.
     *
     * @param meterRegistry CloudWatch 메트릭 전송을 위한 MeterRegistry
     * @return 구성된 CircuitBreakerRegistry 인스턴스
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(MeterRegistry meterRegistry) {
        RetryProperties.CircuitBreaker cbProps = retryProperties.getCircuitBreaker();

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(cbProps.getFailureRateThreshold())
                .slowCallRateThreshold(cbProps.getSlowCallRateThreshold())
                .slowCallDurationThreshold(Duration.ofSeconds(cbProps.getSlowCallDurationThresholdSeconds()))
                .waitDurationInOpenState(Duration.ofSeconds(cbProps.getWaitDurationInOpenStateSeconds()))
                .slidingWindowSize(cbProps.getSlidingWindowSize())
                .minimumNumberOfCalls(cbProps.getMinimumNumberOfCalls())
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);

        // Circuit Breaker 메트릭 자동 등록
        TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry)
                .bindTo(meterRegistry);

        return registry;
    }
}
