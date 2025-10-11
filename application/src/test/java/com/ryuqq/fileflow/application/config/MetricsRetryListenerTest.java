package com.ryuqq.fileflow.application.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MetricsRetryListener 단위 테스트
 *
 * @author sangwon-ryu
 */
class MetricsRetryListenerTest {

    private MetricsRetryListener listener;
    private MeterRegistry meterRegistry;
    private RetryContext retryContext;
    private RetryCallback<Object, Throwable> retryCallback;
    private Counter counter;

    @BeforeEach
    void setUp() {
        meterRegistry = mock(MeterRegistry.class);
        retryContext = mock(RetryContext.class);
        retryCallback = mock(RetryCallback.class);
        counter = mock(Counter.class);

        // Default: MeterRegistry.counter() returns mock Counter
        when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);

        listener = new MetricsRetryListener(meterRegistry, "test-service", 3);
    }

    @Test
    @DisplayName("onError - 첫 번째 재시도 시 메트릭 기록")
    void onError_firstRetry_recordsMetrics() {
        // Given
        when(retryContext.getRetryCount()).thenReturn(1);
        RuntimeException exception = new RuntimeException("Test error");

        // When
        listener.onError(retryContext, retryCallback, exception);

        // Then
        verify(meterRegistry).counter(
                eq("test-service.retry.attempts"),
                eq("exception"), eq("RuntimeException"),
                eq("attempt"), eq("1")
        );
        verify(counter).increment();
    }

    @Test
    @DisplayName("onError - 두 번째 재시도 시 메트릭 기록")
    void onError_secondRetry_recordsMetrics() {
        // Given
        when(retryContext.getRetryCount()).thenReturn(2);
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // When
        listener.onError(retryContext, retryCallback, exception);

        // Then
        verify(meterRegistry).counter(
                eq("test-service.retry.attempts"),
                eq("exception"), eq("IllegalArgumentException"),
                eq("attempt"), eq("2")
        );
        verify(counter).increment();
    }

    @Test
    @DisplayName("onError - 최대 재시도 횟수에 도달한 경우 메트릭 기록")
    void onError_maxRetries_recordsMetrics() {
        // Given
        when(retryContext.getRetryCount()).thenReturn(3);
        RuntimeException exception = new RuntimeException("Max retries reached");

        // When
        listener.onError(retryContext, retryCallback, exception);

        // Then
        verify(meterRegistry).counter(
                eq("test-service.retry.attempts"),
                eq("exception"), eq("RuntimeException"),
                eq("attempt"), eq("3")
        );
        verify(counter).increment();
    }

    @Test
    @DisplayName("onError - null MeterRegistry인 경우 메트릭 기록하지 않음")
    void onError_nullMeterRegistry_noMetricsRecorded() {
        // Given
        MetricsRetryListener listenerWithNullRegistry = new MetricsRetryListener(null, "test-service", 3);
        when(retryContext.getRetryCount()).thenReturn(1);
        RuntimeException exception = new RuntimeException("Test error");

        // When & Then - No exception should be thrown
        listenerWithNullRegistry.onError(retryContext, retryCallback, exception);
        // No verification needed - just ensure no NPE
    }

    @Test
    @DisplayName("close - 재시도 후 성공한 경우 success 메트릭 기록")
    void close_successAfterRetries_recordsSuccessMetrics() {
        // Given
        when(retryContext.getRetryCount()).thenReturn(2);

        // When
        listener.close(retryContext, retryCallback, null);

        // Then
        verify(meterRegistry).counter(
                eq("test-service.retry.success"),
                eq("retries"), eq("2")
        );
        verify(counter).increment();
    }

    @Test
    @DisplayName("close - 첫 시도에서 성공한 경우 메트릭 기록하지 않음")
    void close_successFirstAttempt_noMetricsRecorded() {
        // Given
        when(retryContext.getRetryCount()).thenReturn(0);

        // When
        listener.close(retryContext, retryCallback, null);

        // Then
        verify(meterRegistry, never()).counter(anyString(), any(String[].class));
        verify(counter, never()).increment();
    }

    @Test
    @DisplayName("close - 최종 실패한 경우 exhausted 메트릭 기록")
    void close_finalFailure_recordsExhaustedMetrics() {
        // Given
        when(retryContext.getRetryCount()).thenReturn(3);
        RuntimeException exception = new RuntimeException("Final failure");

        // When
        listener.close(retryContext, retryCallback, exception);

        // Then
        verify(meterRegistry).counter(
                eq("test-service.retry.exhausted"),
                eq("exception"), eq("RuntimeException")
        );
        verify(counter).increment();
    }

    @Test
    @DisplayName("close - null MeterRegistry이고 성공한 경우 메트릭 기록하지 않음")
    void close_nullMeterRegistrySuccess_noMetricsRecorded() {
        // Given
        MetricsRetryListener listenerWithNullRegistry = new MetricsRetryListener(null, "test-service", 3);
        when(retryContext.getRetryCount()).thenReturn(2);

        // When & Then - No exception should be thrown
        listenerWithNullRegistry.close(retryContext, retryCallback, null);
        // No verification needed - just ensure no NPE
    }

    @Test
    @DisplayName("close - null MeterRegistry이고 실패한 경우 메트릭 기록하지 않음")
    void close_nullMeterRegistryFailure_noMetricsRecorded() {
        // Given
        MetricsRetryListener listenerWithNullRegistry = new MetricsRetryListener(null, "test-service", 3);
        when(retryContext.getRetryCount()).thenReturn(3);
        RuntimeException exception = new RuntimeException("Test error");

        // When & Then - No exception should be thrown
        listenerWithNullRegistry.close(retryContext, retryCallback, exception);
        // No verification needed - just ensure no NPE
    }

    @Test
    @DisplayName("Constructor - 필드 초기화 검증")
    void constructor_fieldsInitialized() {
        // Given & When
        MetricsRetryListener newListener = new MetricsRetryListener(meterRegistry, "my-service", 5);

        // Then
        assertThat(newListener).isNotNull();
        // 필드 접근은 리플렉션 없이는 불가능하지만, 동작 테스트로 검증
        when(retryContext.getRetryCount()).thenReturn(1);
        RuntimeException exception = new RuntimeException("Test");

        newListener.onError(retryContext, retryCallback, exception);

        verify(meterRegistry).counter(
                eq("my-service.retry.attempts"),
                eq("exception"), eq("RuntimeException"),
                eq("attempt"), eq("1")
        );
    }

    @Test
    @DisplayName("onError - 다양한 예외 타입에 대해 올바른 메트릭 태그 사용")
    void onError_variousExceptionTypes_correctMetricTags() {
        // Given
        when(retryContext.getRetryCount()).thenReturn(1);

        // When & Then - NullPointerException
        NullPointerException npe = new NullPointerException("Null pointer");
        listener.onError(retryContext, retryCallback, npe);
        verify(meterRegistry).counter(
                eq("test-service.retry.attempts"),
                eq("exception"), eq("NullPointerException"),
                eq("attempt"), eq("1")
        );

        // When & Then - IllegalStateException
        IllegalStateException ise = new IllegalStateException("Illegal state");
        listener.onError(retryContext, retryCallback, ise);
        verify(meterRegistry).counter(
                eq("test-service.retry.attempts"),
                eq("exception"), eq("IllegalStateException"),
                eq("attempt"), eq("1")
        );
    }

    @Test
    @DisplayName("close - 다양한 재시도 횟수에 대해 올바른 메트릭 기록")
    void close_variousRetryCounts_correctMetrics() {
        // Given & When - 1번 재시도 후 성공
        when(retryContext.getRetryCount()).thenReturn(1);
        listener.close(retryContext, retryCallback, null);

        verify(meterRegistry).counter(
                eq("test-service.retry.success"),
                eq("retries"), eq("1")
        );
        verify(counter).increment();

        // Reset mocks
        reset(meterRegistry, counter);
        when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);

        // Given & When - 3번 재시도 후 성공
        when(retryContext.getRetryCount()).thenReturn(3);
        listener.close(retryContext, retryCallback, null);

        verify(meterRegistry).counter(
                eq("test-service.retry.success"),
                eq("retries"), eq("3")
        );
        verify(counter).increment();
    }
}
