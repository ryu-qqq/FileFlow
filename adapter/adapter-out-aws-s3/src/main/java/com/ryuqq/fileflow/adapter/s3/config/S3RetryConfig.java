package com.ryuqq.fileflow.adapter.s3.config;

import com.ryuqq.fileflow.application.config.AwsRetryableErrorClassifier;
import com.ryuqq.fileflow.application.config.MetricsRetryListener;
import com.ryuqq.fileflow.application.config.RetryProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.HashMap;
import java.util.Map;

/**
 * AWS S3 재시도 및 Circuit Breaker 설정
 *
 * S3 파일 업로드/다운로드 실패 시 자동 재시도와 Circuit Breaker 패턴을 구현합니다.
 *
 * 재시도 전략:
 * - Exponential Backoff 알고리즘 사용
 * - application.yml의 fileflow.resilience 설정 기반
 *
 * Circuit Breaker 설정:
 * - ResilienceConfig에서 주입받은 CircuitBreakerRegistry 사용
 * - 설정은 application.yml의 fileflow.resilience.circuit-breaker 기반
 *
 * 재시도 대상 오류:
 * - AWS SDK의 isRetryable() 플래그가 true인 경우
 * - S3 일시적 오류 (5xx)
 * - 네트워크 관련 오류 (연결 실패, 타임아웃 등)
 *
 * @author sangwon-ryu
 */
@Configuration
@EnableRetry
public class S3RetryConfig {

    private static final Logger logger = LoggerFactory.getLogger(S3RetryConfig.class);
    private static final String SERVICE_NAME = "s3";
    private static final String CIRCUIT_BREAKER_NAME = "s3-file-operations";

    private final RetryProperties retryProperties;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param retryProperties 재시도 및 Circuit Breaker 설정 프로퍼티
     */
    public S3RetryConfig(RetryProperties retryProperties) {
        this.retryProperties = retryProperties;
    }

    /**
     * S3 파일 작업을 위한 RetryTemplate 빈 생성
     *
     * @param meterRegistry CloudWatch 메트릭 전송을 위한 MeterRegistry
     * @return 구성된 RetryTemplate 인스턴스
     */
    @Bean
    public RetryTemplate s3RetryTemplate(MeterRegistry meterRegistry) {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Exponential Backoff 정책 설정
        RetryProperties.Retry retryProps = retryProperties.getRetry();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retryProps.getInitialIntervalMillis());
        backOffPolicy.setMaxInterval(retryProps.getMaxIntervalMillis());
        backOffPolicy.setMultiplier(retryProps.getMultiplier());
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // 재시도 정책 설정: AWS SDK의 isRetryable() 기반 분류
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(S3Exception.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                retryProps.getMaxAttempts(),
                retryableExceptions
        ) {
            @Override
            public boolean canRetry(org.springframework.retry.RetryContext context) {
                Throwable lastException = context.getLastThrowable();
                if (lastException == null) {
                    return true;
                }

                // AWS SDK의 isThrottlingException()과 5xx 상태 코드 기반 정교한 재시도 판단
                if (lastException instanceof Exception) {
                    return AwsRetryableErrorClassifier.isRetryable((Exception) lastException);
                }
                return false;
            }
        };
        retryTemplate.setRetryPolicy(retryPolicy);

        // 재시도 리스너 등록 (로깅 및 메트릭)
        retryTemplate.registerListener(
                new MetricsRetryListener(meterRegistry, SERVICE_NAME, retryProps.getMaxAttempts())
        );

        return retryTemplate;
    }

    /**
     * S3 파일 작업을 위한 Circuit Breaker 빈 생성
     *
     * ResilienceConfig에서 주입받은 CircuitBreakerRegistry를 사용하여
     * S3 전용 Circuit Breaker 인스턴스를 생성합니다.
     *
     * @param circuitBreakerRegistry Spring이 관리하는 CircuitBreakerRegistry
     * @return 구성된 CircuitBreaker 인스턴스
     */
    @Bean
    public CircuitBreaker s3CircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);

        // Circuit Breaker 이벤트 리스너 등록
        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        logger.warn("S3 Circuit Breaker state transition: {} -> {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()))
                .onError(event ->
                        logger.error("S3 Circuit Breaker recorded error: {}",
                                event.getThrowable().getMessage()))
                .onSuccess(event ->
                        logger.debug("S3 Circuit Breaker recorded success"));

        return circuitBreaker;
    }

    /**
     * S3 일시적 오류인지 판단하는 헬퍼 메서드 (하위 호환성 유지)
     *
     * @param exception 발생한 예외
     * @return 재시도 가능한 일시적 오류이면 true
     * @deprecated Use {@link AwsRetryableErrorClassifier#isRetryable(Exception)} instead
     */
    @Deprecated
    public static boolean isRetryableS3Error(Exception exception) {
        return AwsRetryableErrorClassifier.isRetryable(exception);
    }
}
