package com.ryuqq.fileflow.adapter.s3.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * AWS S3 재시도 및 Circuit Breaker 설정
 *
 * 일시적인 오류에 대한 자동 재시도와 Circuit Breaker 패턴을 구현합니다.
 *
 * 재시도 전략:
 * - Exponential Backoff 알고리즘 사용
 * - 최대 재시도 횟수: 3회
 * - 초기 대기 시간: 1초
 * - 최대 대기 시간: 10초
 * - 배수: 2.0 (각 재시도마다 2배씩 증가)
 *
 * Circuit Breaker 설정:
 * - 실패율 임계값: 50%
 * - 대기 시간: 30초
 * - 슬라이딩 윈도우: 10개 호출
 * - 최소 호출 수: 5개
 *
 * 재시도 대상 오류:
 * - S3 일시적 오류 (5xx)
 * - 네트워크 타임아웃 (SocketTimeoutException)
 * - SDK 클라이언트 오류 (SdkClientException)
 *
 * @author sangwon-ryu
 */
@Configuration
@EnableRetry
public class S3RetryConfig {

    private static final Logger logger = LoggerFactory.getLogger(S3RetryConfig.class);

    // Retry 설정 상수
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_INTERVAL_MILLIS = 1000L;  // 1초
    private static final long MAX_INTERVAL_MILLIS = 10000L;     // 10초
    private static final double MULTIPLIER = 2.0;

    // Circuit Breaker 설정 상수
    private static final float FAILURE_RATE_THRESHOLD = 50.0f;  // 50%
    private static final int SLOW_CALL_RATE_THRESHOLD = 100;    // 100%
    private static final Duration SLOW_CALL_DURATION_THRESHOLD = Duration.ofSeconds(5);
    private static final Duration WAIT_DURATION_IN_OPEN_STATE = Duration.ofSeconds(30);
    private static final int SLIDING_WINDOW_SIZE = 10;
    private static final int MINIMUM_NUMBER_OF_CALLS = 5;

    /**
     * S3 작업을 위한 RetryTemplate 빈 생성
     *
     * @param meterRegistry CloudWatch 메트릭 전송을 위한 MeterRegistry (nullable)
     * @return 구성된 RetryTemplate 인스턴스
     */
    @Bean
    public RetryTemplate s3RetryTemplate(MeterRegistry meterRegistry) {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Exponential Backoff 정책 설정
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(INITIAL_INTERVAL_MILLIS);
        backOffPolicy.setMaxInterval(MAX_INTERVAL_MILLIS);
        backOffPolicy.setMultiplier(MULTIPLIER);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // 재시도 정책 설정: 재시도 대상 예외와 최대 시도 횟수
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(S3Exception.class, true);           // S3 일시적 오류
        retryableExceptions.put(SocketTimeoutException.class, true); // 네트워크 타임아웃
        retryableExceptions.put(SdkClientException.class, true);    // SDK 클라이언트 오류

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(MAX_RETRY_ATTEMPTS, retryableExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);

        // 재시도 리스너 등록 (로깅 및 메트릭)
        retryTemplate.registerListener(new S3RetryListener(meterRegistry));

        return retryTemplate;
    }

    /**
     * S3 작업을 위한 Circuit Breaker 빈 생성
     *
     * @param meterRegistry CloudWatch 메트릭 전송을 위한 MeterRegistry
     * @return 구성된 CircuitBreaker 인스턴스
     */
    @Bean
    public CircuitBreaker s3CircuitBreaker(MeterRegistry meterRegistry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(FAILURE_RATE_THRESHOLD)
                .slowCallRateThreshold(SLOW_CALL_RATE_THRESHOLD)
                .slowCallDurationThreshold(SLOW_CALL_DURATION_THRESHOLD)
                .waitDurationInOpenState(WAIT_DURATION_IN_OPEN_STATE)
                .slidingWindowSize(SLIDING_WINDOW_SIZE)
                .minimumNumberOfCalls(MINIMUM_NUMBER_OF_CALLS)
                .recordExceptions(
                        S3Exception.class,
                        SocketTimeoutException.class,
                        SdkClientException.class
                )
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker circuitBreaker = registry.circuitBreaker("s3-operations");

        // Circuit Breaker 메트릭 등록
        TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry)
                .bindTo(meterRegistry);

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
     * S3 재시도 이벤트 리스너
     *
     * 재시도 시작, 재시도 횟수, 재시도 간격 등을 로깅하고
     * CloudWatch 메트릭으로 전송합니다.
     */
    private static class S3RetryListener implements RetryListener {

        private static final Logger logger = LoggerFactory.getLogger(S3RetryListener.class);
        private final MeterRegistry meterRegistry;

        public S3RetryListener(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        @Override
        public <T, E extends Throwable> void onError(
                RetryContext context,
                RetryCallback<T, E> callback,
                Throwable throwable) {

            int retryCount = context.getRetryCount();
            String exceptionType = throwable.getClass().getSimpleName();

            logger.warn("S3 operation failed (attempt {}/{}): {} - {}",
                    retryCount,
                    MAX_RETRY_ATTEMPTS,
                    exceptionType,
                    throwable.getMessage());

            // CloudWatch 메트릭 전송
            if (meterRegistry != null) {
                meterRegistry.counter("s3.retry.attempts",
                        "exception", exceptionType,
                        "attempt", String.valueOf(retryCount)
                ).increment();
            }
        }

        @Override
        public <T, E extends Throwable> void close(
                RetryContext context,
                RetryCallback<T, E> callback,
                Throwable throwable) {

            if (throwable != null) {
                // 최종 실패
                logger.error("S3 operation finally failed after {} attempts: {}",
                        context.getRetryCount(),
                        throwable.getMessage());

                if (meterRegistry != null) {
                    meterRegistry.counter("s3.retry.exhausted",
                            "exception", throwable.getClass().getSimpleName()
                    ).increment();
                }
            } else {
                // 성공
                if (context.getRetryCount() > 0) {
                    logger.info("S3 operation succeeded after {} retries",
                            context.getRetryCount());

                    if (meterRegistry != null) {
                        meterRegistry.counter("s3.retry.success",
                                "retries", String.valueOf(context.getRetryCount())
                        ).increment();
                    }
                }
            }
        }
    }

    /**
     * S3 일시적 오류인지 판단하는 헬퍼 메서드
     *
     * @param exception 발생한 예외
     * @return 재시도 가능한 일시적 오류이면 true
     */
    public static boolean isRetryableS3Error(Exception exception) {
        if (exception instanceof S3Exception) {
            S3Exception s3Exception = (S3Exception) exception;
            int statusCode = s3Exception.statusCode();
            // 5xx 서버 오류는 재시도 가능
            return statusCode >= 500 && statusCode < 600;
        }

        if (exception instanceof SocketTimeoutException) {
            return true;
        }

        if (exception instanceof SdkClientException) {
            // SDK 클라이언트 오류 중 일부는 재시도 가능
            String message = exception.getMessage();
            return message != null && (
                    message.contains("Unable to execute HTTP request") ||
                    message.contains("connection") ||
                    message.contains("timeout")
            );
        }

        return false;
    }
}
