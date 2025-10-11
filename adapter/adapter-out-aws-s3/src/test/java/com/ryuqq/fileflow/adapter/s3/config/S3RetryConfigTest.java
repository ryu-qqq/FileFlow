package com.ryuqq.fileflow.adapter.s3.config;

import com.ryuqq.fileflow.application.config.AwsRetryableErrorClassifier;
import com.ryuqq.fileflow.application.config.RetryProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * S3RetryConfig 단위 테스트
 *
 * @author sangwon-ryu
 */
class S3RetryConfigTest {

    private S3RetryConfig config;
    private RetryProperties retryProperties;
    private MeterRegistry meterRegistry;
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        retryProperties = new RetryProperties();
        // Default values: maxAttempts=3, initialInterval=1000L, maxInterval=10000L, multiplier=2.0
        meterRegistry = mock(MeterRegistry.class);
        circuitBreakerRegistry = mock(CircuitBreakerRegistry.class);
        config = new S3RetryConfig(retryProperties);
    }

    @Test
    @DisplayName("s3RetryTemplate 빈 생성 성공")
    void s3RetryTemplate_beanCreation_success() {
        // When
        RetryTemplate retryTemplate = config.s3RetryTemplate(meterRegistry);

        // Then
        assertThat(retryTemplate).isNotNull();
    }

    @Test
    @DisplayName("ExponentialBackOffPolicy 설정 검증 - initialInterval")
    void s3RetryTemplate_exponentialBackOffPolicy_initialInterval() throws ReflectiveOperationException {
        // When
        RetryTemplate retryTemplate = config.s3RetryTemplate(meterRegistry);

        // Then
        ExponentialBackOffPolicy exponentialPolicy = getBackOffPolicy(retryTemplate);
        assertThat(exponentialPolicy).isNotNull();

        Field initialIntervalField = ExponentialBackOffPolicy.class.getDeclaredField("initialInterval");
        initialIntervalField.setAccessible(true);
        long initialInterval = (long) initialIntervalField.get(exponentialPolicy);

        assertThat(initialInterval).isEqualTo(1000L);
    }

    @Test
    @DisplayName("ExponentialBackOffPolicy 설정 검증 - maxInterval")
    void s3RetryTemplate_exponentialBackOffPolicy_maxInterval() throws ReflectiveOperationException {
        // When
        RetryTemplate retryTemplate = config.s3RetryTemplate(meterRegistry);

        // Then
        ExponentialBackOffPolicy exponentialPolicy = getBackOffPolicy(retryTemplate);
        Field maxIntervalField = ExponentialBackOffPolicy.class.getDeclaredField("maxInterval");
        maxIntervalField.setAccessible(true);
        long maxInterval = (long) maxIntervalField.get(exponentialPolicy);

        assertThat(maxInterval).isEqualTo(10000L);
    }

    @Test
    @DisplayName("ExponentialBackOffPolicy 설정 검증 - multiplier")
    void s3RetryTemplate_exponentialBackOffPolicy_multiplier() throws ReflectiveOperationException {
        // When
        RetryTemplate retryTemplate = config.s3RetryTemplate(meterRegistry);

        // Then
        ExponentialBackOffPolicy exponentialPolicy = getBackOffPolicy(retryTemplate);
        Field multiplierField = ExponentialBackOffPolicy.class.getDeclaredField("multiplier");
        multiplierField.setAccessible(true);
        double multiplier = (double) multiplierField.get(exponentialPolicy);

        assertThat(multiplier).isEqualTo(2.0);
    }

    @Test
    @DisplayName("SimpleRetryPolicy 설정 검증 - maxAttempts")
    void s3RetryTemplate_simpleRetryPolicy_maxAttempts() throws ReflectiveOperationException {
        // When
        RetryTemplate retryTemplate = config.s3RetryTemplate(meterRegistry);

        // Then
        SimpleRetryPolicy simplePolicy = getRetryPolicy(retryTemplate);
        assertThat(simplePolicy).isNotNull();

        Field maxAttemptsField = SimpleRetryPolicy.class.getDeclaredField("maxAttempts");
        maxAttemptsField.setAccessible(true);
        int maxAttempts = (int) maxAttemptsField.get(simplePolicy);

        assertThat(maxAttempts).isEqualTo(3);
    }

    @Test
    @DisplayName("커스텀 RetryProperties 설정 적용 검증")
    void s3RetryTemplate_customRetryProperties_applied() throws ReflectiveOperationException {
        // Given
        RetryProperties.Retry customRetry = retryProperties.getRetry();
        customRetry.setMaxAttempts(5);
        customRetry.setInitialIntervalMillis(2000L);
        customRetry.setMaxIntervalMillis(20000L);
        customRetry.setMultiplier(3.0);

        // When
        RetryTemplate retryTemplate = config.s3RetryTemplate(meterRegistry);

        // Then
        SimpleRetryPolicy simplePolicy = getRetryPolicy(retryTemplate);
        Field maxAttemptsField = SimpleRetryPolicy.class.getDeclaredField("maxAttempts");
        maxAttemptsField.setAccessible(true);
        int maxAttempts = (int) maxAttemptsField.get(simplePolicy);

        ExponentialBackOffPolicy exponentialPolicy = getBackOffPolicy(retryTemplate);
        Field initialIntervalField = ExponentialBackOffPolicy.class.getDeclaredField("initialInterval");
        initialIntervalField.setAccessible(true);
        long initialInterval = (long) initialIntervalField.get(exponentialPolicy);

        Field maxIntervalField = ExponentialBackOffPolicy.class.getDeclaredField("maxInterval");
        maxIntervalField.setAccessible(true);
        long maxInterval = (long) maxIntervalField.get(exponentialPolicy);

        Field multiplierField = ExponentialBackOffPolicy.class.getDeclaredField("multiplier");
        multiplierField.setAccessible(true);
        double multiplier = (double) multiplierField.get(exponentialPolicy);

        assertThat(maxAttempts).isEqualTo(5);
        assertThat(initialInterval).isEqualTo(2000L);
        assertThat(maxInterval).isEqualTo(20000L);
        assertThat(multiplier).isEqualTo(3.0);
    }

    @Test
    @DisplayName("s3CircuitBreaker 빈 생성 성공")
    void s3CircuitBreaker_beanCreation_success() {
        // Given
        CircuitBreaker mockCircuitBreaker = mock(CircuitBreaker.class);
        CircuitBreaker.EventPublisher eventPublisher = mock(CircuitBreaker.EventPublisher.class);
        when(mockCircuitBreaker.getEventPublisher()).thenReturn(eventPublisher);
        when(eventPublisher.onStateTransition(org.mockito.ArgumentMatchers.any())).thenReturn(eventPublisher);
        when(eventPublisher.onError(org.mockito.ArgumentMatchers.any())).thenReturn(eventPublisher);
        when(eventPublisher.onSuccess(org.mockito.ArgumentMatchers.any())).thenReturn(eventPublisher);
        when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(mockCircuitBreaker);

        // When
        CircuitBreaker circuitBreaker = config.s3CircuitBreaker(circuitBreakerRegistry);

        // Then
        assertThat(circuitBreaker).isNotNull();
        verify(circuitBreakerRegistry).circuitBreaker("s3-file-operations");
        verify(mockCircuitBreaker).getEventPublisher();
    }

    @Test
    @DisplayName("s3CircuitBreaker 이벤트 리스너 등록 검증")
    void s3CircuitBreaker_eventListeners_registered() {
        // Given
        CircuitBreaker mockCircuitBreaker = mock(CircuitBreaker.class);
        CircuitBreaker.EventPublisher eventPublisher = mock(CircuitBreaker.EventPublisher.class);
        when(mockCircuitBreaker.getEventPublisher()).thenReturn(eventPublisher);
        when(eventPublisher.onStateTransition(org.mockito.ArgumentMatchers.any())).thenReturn(eventPublisher);
        when(eventPublisher.onError(org.mockito.ArgumentMatchers.any())).thenReturn(eventPublisher);
        when(eventPublisher.onSuccess(org.mockito.ArgumentMatchers.any())).thenReturn(eventPublisher);
        when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(mockCircuitBreaker);

        // When
        config.s3CircuitBreaker(circuitBreakerRegistry);

        // Then
        verify(eventPublisher).onStateTransition(org.mockito.ArgumentMatchers.any());
        verify(eventPublisher).onError(org.mockito.ArgumentMatchers.any());
        verify(eventPublisher).onSuccess(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("isRetryableS3Error 메서드 - S3Exception 5xx 재시도 가능")
    void isRetryableS3Error_s3Exception5xx_returnsTrue() {
        // Given
        S3Exception exception = mock(S3Exception.class);
        when(exception.statusCode()).thenReturn(503);
        when(exception.isThrottlingException()).thenReturn(false);
        when(exception.getMessage()).thenReturn("Service Unavailable");

        // When
        boolean result = S3RetryConfig.isRetryableS3Error(exception);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isRetryableS3Error 메서드 - S3Exception 4xx 재시도 불가")
    void isRetryableS3Error_s3Exception4xx_returnsFalse() {
        // Given
        S3Exception exception = mock(S3Exception.class);
        when(exception.statusCode()).thenReturn(403);
        when(exception.isThrottlingException()).thenReturn(false);
        when(exception.getMessage()).thenReturn("Forbidden");

        // When
        boolean result = S3RetryConfig.isRetryableS3Error(exception);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isRetryableS3Error 메서드 - AwsRetryableErrorClassifier에 위임")
    void isRetryableS3Error_delegatesToAwsRetryableErrorClassifier() {
        // Given
        Exception exception = new RuntimeException("Test exception");

        // When
        boolean result = S3RetryConfig.isRetryableS3Error(exception);

        // Then
        assertThat(result).isEqualTo(AwsRetryableErrorClassifier.isRetryable(exception));
    }

    // Helper methods for reflection
    private ExponentialBackOffPolicy getBackOffPolicy(RetryTemplate retryTemplate) throws ReflectiveOperationException {
        Field field = RetryTemplate.class.getDeclaredField("backOffPolicy");
        field.setAccessible(true);
        return (ExponentialBackOffPolicy) field.get(retryTemplate);
    }

    private SimpleRetryPolicy getRetryPolicy(RetryTemplate retryTemplate) throws ReflectiveOperationException {
        Field field = RetryTemplate.class.getDeclaredField("retryPolicy");
        field.setAccessible(true);
        return (SimpleRetryPolicy) field.get(retryTemplate);
    }
}
