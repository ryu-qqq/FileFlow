package com.ryuqq.fileflow.adapter.sqs.config;

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
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SqsRetryConfig 단위 테스트
 *
 * @author sangwon-ryu
 */
class SqsRetryConfigTest {

    private SqsRetryConfig config;
    private RetryProperties retryProperties;
    private MeterRegistry meterRegistry;
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        retryProperties = new RetryProperties();
        // Default values: maxAttempts=3, initialInterval=1000L, maxInterval=10000L, multiplier=2.0
        meterRegistry = mock(MeterRegistry.class);
        circuitBreakerRegistry = mock(CircuitBreakerRegistry.class);
        config = new SqsRetryConfig(retryProperties);
    }

    @Test
    @DisplayName("sqsRetryTemplate 빈 생성 성공")
    void sqsRetryTemplate_beanCreation_success() {
        // When
        RetryTemplate retryTemplate = config.sqsRetryTemplate(meterRegistry);

        // Then
        assertThat(retryTemplate).isNotNull();
    }

    @Test
    @DisplayName("ExponentialBackOffPolicy 설정 검증 - initialInterval")
    void sqsRetryTemplate_exponentialBackOffPolicy_initialInterval() throws Exception {
        // When
        RetryTemplate retryTemplate = config.sqsRetryTemplate(meterRegistry);

        // Then - Use reflection to access private backOffPolicy field
        Field backOffPolicyField = RetryTemplate.class.getDeclaredField("backOffPolicy");
        backOffPolicyField.setAccessible(true);
        BackOffPolicy backOffPolicy = (BackOffPolicy) backOffPolicyField.get(retryTemplate);

        assertThat(backOffPolicy).isInstanceOf(ExponentialBackOffPolicy.class);

        ExponentialBackOffPolicy exponentialPolicy = (ExponentialBackOffPolicy) backOffPolicy;
        Field initialIntervalField = ExponentialBackOffPolicy.class.getDeclaredField("initialInterval");
        initialIntervalField.setAccessible(true);
        long initialInterval = (long) initialIntervalField.get(exponentialPolicy);

        assertThat(initialInterval).isEqualTo(1000L);
    }

    @Test
    @DisplayName("ExponentialBackOffPolicy 설정 검증 - maxInterval")
    void sqsRetryTemplate_exponentialBackOffPolicy_maxInterval() throws Exception {
        // When
        RetryTemplate retryTemplate = config.sqsRetryTemplate(meterRegistry);

        // Then
        Field backOffPolicyField = RetryTemplate.class.getDeclaredField("backOffPolicy");
        backOffPolicyField.setAccessible(true);
        BackOffPolicy backOffPolicy = (BackOffPolicy) backOffPolicyField.get(retryTemplate);

        ExponentialBackOffPolicy exponentialPolicy = (ExponentialBackOffPolicy) backOffPolicy;
        Field maxIntervalField = ExponentialBackOffPolicy.class.getDeclaredField("maxInterval");
        maxIntervalField.setAccessible(true);
        long maxInterval = (long) maxIntervalField.get(exponentialPolicy);

        assertThat(maxInterval).isEqualTo(10000L);
    }

    @Test
    @DisplayName("ExponentialBackOffPolicy 설정 검증 - multiplier")
    void sqsRetryTemplate_exponentialBackOffPolicy_multiplier() throws Exception {
        // When
        RetryTemplate retryTemplate = config.sqsRetryTemplate(meterRegistry);

        // Then
        Field backOffPolicyField = RetryTemplate.class.getDeclaredField("backOffPolicy");
        backOffPolicyField.setAccessible(true);
        BackOffPolicy backOffPolicy = (BackOffPolicy) backOffPolicyField.get(retryTemplate);

        ExponentialBackOffPolicy exponentialPolicy = (ExponentialBackOffPolicy) backOffPolicy;
        Field multiplierField = ExponentialBackOffPolicy.class.getDeclaredField("multiplier");
        multiplierField.setAccessible(true);
        double multiplier = (double) multiplierField.get(exponentialPolicy);

        assertThat(multiplier).isEqualTo(2.0);
    }

    @Test
    @DisplayName("SimpleRetryPolicy 설정 검증 - maxAttempts")
    void sqsRetryTemplate_simpleRetryPolicy_maxAttempts() throws Exception {
        // When
        RetryTemplate retryTemplate = config.sqsRetryTemplate(meterRegistry);

        // Then
        Field retryPolicyField = RetryTemplate.class.getDeclaredField("retryPolicy");
        retryPolicyField.setAccessible(true);
        RetryPolicy retryPolicy = (RetryPolicy) retryPolicyField.get(retryTemplate);

        assertThat(retryPolicy).isInstanceOf(SimpleRetryPolicy.class);

        SimpleRetryPolicy simplePolicy = (SimpleRetryPolicy) retryPolicy;
        Field maxAttemptsField = SimpleRetryPolicy.class.getDeclaredField("maxAttempts");
        maxAttemptsField.setAccessible(true);
        int maxAttempts = (int) maxAttemptsField.get(simplePolicy);

        assertThat(maxAttempts).isEqualTo(3);
    }

    @Test
    @DisplayName("커스텀 RetryProperties 설정 적용 검증")
    void sqsRetryTemplate_customRetryProperties_applied() throws Exception {
        // Given
        RetryProperties.Retry customRetry = retryProperties.getRetry();
        customRetry.setMaxAttempts(5);
        customRetry.setInitialIntervalMillis(2000L);
        customRetry.setMaxIntervalMillis(20000L);
        customRetry.setMultiplier(3.0);

        // When
        RetryTemplate retryTemplate = config.sqsRetryTemplate(meterRegistry);

        // Then
        Field retryPolicyField = RetryTemplate.class.getDeclaredField("retryPolicy");
        retryPolicyField.setAccessible(true);
        RetryPolicy retryPolicy = (RetryPolicy) retryPolicyField.get(retryTemplate);

        Field maxAttemptsField = SimpleRetryPolicy.class.getDeclaredField("maxAttempts");
        maxAttemptsField.setAccessible(true);
        int maxAttempts = (int) maxAttemptsField.get(retryPolicy);

        Field backOffPolicyField = RetryTemplate.class.getDeclaredField("backOffPolicy");
        backOffPolicyField.setAccessible(true);
        BackOffPolicy backOffPolicy = (BackOffPolicy) backOffPolicyField.get(retryTemplate);

        ExponentialBackOffPolicy exponentialPolicy = (ExponentialBackOffPolicy) backOffPolicy;
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
    @DisplayName("sqsCircuitBreaker 빈 생성 성공")
    void sqsCircuitBreaker_beanCreation_success() {
        // Given
        CircuitBreaker mockCircuitBreaker = mock(CircuitBreaker.class);
        CircuitBreaker.EventPublisher eventPublisher = mock(CircuitBreaker.EventPublisher.class);
        when(mockCircuitBreaker.getEventPublisher()).thenReturn(eventPublisher);
        when(eventPublisher.onStateTransition(org.mockito.ArgumentMatchers.any())).thenReturn(eventPublisher);
        when(eventPublisher.onError(org.mockito.ArgumentMatchers.any())).thenReturn(eventPublisher);
        when(eventPublisher.onSuccess(org.mockito.ArgumentMatchers.any())).thenReturn(eventPublisher);
        when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(mockCircuitBreaker);

        // When
        CircuitBreaker circuitBreaker = config.sqsCircuitBreaker(circuitBreakerRegistry);

        // Then
        assertThat(circuitBreaker).isNotNull();
        verify(circuitBreakerRegistry).circuitBreaker("sqs-message-processing");
        verify(mockCircuitBreaker).getEventPublisher();
    }

    @Test
    @DisplayName("sqsCircuitBreaker 이벤트 리스너 등록 검증")
    void sqsCircuitBreaker_eventListeners_registered() {
        // Given
        CircuitBreaker mockCircuitBreaker = mock(CircuitBreaker.class);
        CircuitBreaker.EventPublisher eventPublisher = mock(CircuitBreaker.EventPublisher.class);
        when(mockCircuitBreaker.getEventPublisher()).thenReturn(eventPublisher);
        when(eventPublisher.onStateTransition(org.mockito.ArgumentMatchers.any())).thenReturn(eventPublisher);
        when(eventPublisher.onError(org.mockito.ArgumentMatchers.any())).thenReturn(eventPublisher);
        when(eventPublisher.onSuccess(org.mockito.ArgumentMatchers.any())).thenReturn(eventPublisher);
        when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(mockCircuitBreaker);

        // When
        config.sqsCircuitBreaker(circuitBreakerRegistry);

        // Then
        verify(eventPublisher).onStateTransition(org.mockito.ArgumentMatchers.any());
        verify(eventPublisher).onError(org.mockito.ArgumentMatchers.any());
        verify(eventPublisher).onSuccess(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("isRetryableSqsError 메서드 - SqsException 5xx 재시도 가능")
    void isRetryableSqsError_sqsException5xx_returnsTrue() {
        // Given
        SqsException exception = mock(SqsException.class);
        when(exception.statusCode()).thenReturn(503);
        when(exception.isThrottlingException()).thenReturn(false);
        when(exception.getMessage()).thenReturn("Service Unavailable");

        // When
        boolean result = SqsRetryConfig.isRetryableSqsError(exception);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isRetryableSqsError 메서드 - SqsException 4xx 재시도 불가")
    void isRetryableSqsError_sqsException4xx_returnsFalse() {
        // Given
        SqsException exception = mock(SqsException.class);
        when(exception.statusCode()).thenReturn(403);
        when(exception.isThrottlingException()).thenReturn(false);
        when(exception.getMessage()).thenReturn("Forbidden");

        // When
        boolean result = SqsRetryConfig.isRetryableSqsError(exception);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isRetryableSqsError 메서드 - AwsRetryableErrorClassifier에 위임")
    void isRetryableSqsError_delegatesToAwsRetryableErrorClassifier() {
        // Given
        Exception exception = new RuntimeException("Test exception");

        // When
        boolean result = SqsRetryConfig.isRetryableSqsError(exception);

        // Then
        assertThat(result).isEqualTo(AwsRetryableErrorClassifier.isRetryable(exception));
    }
}
