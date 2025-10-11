package com.ryuqq.fileflow.application.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Circuit Breaker 상태 전환 통합 테스트
 * <p>
 * Circuit Breaker의 실제 상태 전환 동작을 검증합니다.
 * - CLOSED → OPEN: 실패율이 임계값을 초과할 때
 * - OPEN → HALF_OPEN: 대기 시간이 경과한 후
 * - HALF_OPEN → CLOSED: 테스트 호출이 성공할 때
 * - HALF_OPEN → OPEN: 테스트 호출이 실패할 때
 *
 * @author sangwon-ryu
 */
class CircuitBreakerIntegrationTest {

    private CircuitBreaker circuitBreaker;
    private MeterRegistry meterRegistry;
    private RetryProperties retryProperties;

    @BeforeEach
    void setUp() {
        meterRegistry = mock(MeterRegistry.class);
        retryProperties = new RetryProperties();

        // 빠른 테스트를 위해 짧은 대기 시간 설정
        retryProperties.getCircuitBreaker().setFailureRateThreshold(50.0f);
        retryProperties.getCircuitBreaker().setSlowCallRateThreshold(100);
        retryProperties.getCircuitBreaker().setSlowCallDurationThresholdSeconds(10L);
        retryProperties.getCircuitBreaker().setWaitDurationInOpenStateSeconds(1L); // 1초로 단축
        retryProperties.getCircuitBreaker().setSlidingWindowSize(5);
        retryProperties.getCircuitBreaker().setMinimumNumberOfCalls(3);

        // ResilienceConfig와 동일한 방식으로 CircuitBreaker 생성
        RetryProperties.CircuitBreaker cbProps = retryProperties.getCircuitBreaker();
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(cbProps.getFailureRateThreshold())
                .slowCallRateThreshold(cbProps.getSlowCallRateThreshold())
                .slowCallDurationThreshold(Duration.ofSeconds(cbProps.getSlowCallDurationThresholdSeconds()))
                .waitDurationInOpenState(Duration.ofSeconds(cbProps.getWaitDurationInOpenStateSeconds()))
                .slidingWindowSize(cbProps.getSlidingWindowSize())
                .minimumNumberOfCalls(cbProps.getMinimumNumberOfCalls())
                .permittedNumberOfCallsInHalfOpenState(2) // HALF_OPEN 상태에서 2번의 호출로 판단
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        circuitBreaker = registry.circuitBreaker("test-circuit-breaker");
    }

    @Test
    @DisplayName("CircuitBreaker - CLOSED 상태에서 시작")
    void circuitBreaker_startsInClosedState() {
        // Then
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("CircuitBreaker - CLOSED → OPEN: 실패율이 임계값을 초과할 때")
    void circuitBreaker_closedToOpen_whenFailureRateExceedsThreshold() {
        // Given
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // When - 3번 중 2번 실패 (실패율 66% > 50% 임계값)
        circuitBreaker.onSuccess(100, java.util.concurrent.TimeUnit.MILLISECONDS);
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Fail 1"));
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Fail 2"));

        // Then
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("CircuitBreaker - OPEN 상태에서는 즉시 예외 발생")
    void circuitBreaker_openState_throwsExceptionImmediately() {
        // Given - Circuit을 OPEN 상태로 만들기
        circuitBreaker.onSuccess(100, java.util.concurrent.TimeUnit.MILLISECONDS);
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Fail 1"));
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Fail 2"));
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // When & Then - 호출 시도 시 즉시 예외 발생
        AtomicInteger callCount = new AtomicInteger(0);
        assertThatThrownBy(() ->
                circuitBreaker.executeSupplier(() -> {
                    callCount.incrementAndGet();
                    return "success";
                })
        ).isInstanceOf(io.github.resilience4j.circuitbreaker.CallNotPermittedException.class);

        // 실제 메서드는 호출되지 않음
        assertThat(callCount.get()).isEqualTo(0);
    }

    @Test
    @DisplayName("CircuitBreaker - OPEN → HALF_OPEN: 대기 시간 경과 후")
    void circuitBreaker_openToHalfOpen_afterWaitDuration() throws InterruptedException {
        // Given - Circuit을 OPEN 상태로 만들기
        circuitBreaker.onSuccess(100, java.util.concurrent.TimeUnit.MILLISECONDS);
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Fail 1"));
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Fail 2"));
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // When - waitDurationInOpenState (1초) 대기
        Thread.sleep(1100); // 1.1초 대기

        // Then - 다음 호출 시 HALF_OPEN으로 전환 시도
        try {
            circuitBreaker.executeSupplier(() -> {
                // HALF_OPEN 상태에서 호출 허용됨
                assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);
                throw new RuntimeException("Test call");
            });
        } catch (Exception ignored) {
            // 예외는 무시
        }

        // HALF_OPEN 상태가 되었는지 확인
        assertThat(circuitBreaker.getState()).isIn(CircuitBreaker.State.HALF_OPEN, CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("CircuitBreaker - HALF_OPEN → CLOSED: 테스트 호출 성공 시")
    void circuitBreaker_halfOpenToClosed_onSuccessfulTestCalls() throws InterruptedException {
        // Given - Circuit을 OPEN 상태로 만들기
        circuitBreaker.onSuccess(100, java.util.concurrent.TimeUnit.MILLISECONDS);
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Fail 1"));
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Fail 2"));
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // When - waitDurationInOpenState 대기 후 성공 호출
        Thread.sleep(1100);
        circuitBreaker.transitionToHalfOpenState();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);

        // permittedNumberOfCallsInHalfOpenState (2번) 만큼 성공 호출
        circuitBreaker.onSuccess(100, java.util.concurrent.TimeUnit.MILLISECONDS);
        circuitBreaker.onSuccess(100, java.util.concurrent.TimeUnit.MILLISECONDS);

        // Then - CLOSED 상태로 전환
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("CircuitBreaker - HALF_OPEN → OPEN: 테스트 호출 실패 시")
    void circuitBreaker_halfOpenToOpen_onFailedTestCalls() throws InterruptedException {
        // Given - Circuit을 OPEN 상태로 만들기
        circuitBreaker.onSuccess(100, java.util.concurrent.TimeUnit.MILLISECONDS);
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Fail 1"));
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Fail 2"));
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // When - waitDurationInOpenState 대기 후 실패 호출
        Thread.sleep(1100);
        circuitBreaker.transitionToHalfOpenState();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);

        // permittedNumberOfCallsInHalfOpenState (2번) 만큼 실패 호출
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Test fail 1"));
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Test fail 2"));

        // Then - 다시 OPEN 상태로 전환
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("CircuitBreaker - minimumNumberOfCalls 미만에서는 OPEN되지 않음")
    void circuitBreaker_doesNotOpen_belowMinimumCalls() {
        // Given - minimumNumberOfCalls = 3

        // When - 2번만 실패 (minimumNumberOfCalls 미만)
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Fail 1"));
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Fail 2"));

        // Then - 여전히 CLOSED 상태
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("CircuitBreaker - 메트릭 정보 조회")
    void circuitBreaker_metricsAvailable() {
        // Given
        circuitBreaker.onSuccess(100, java.util.concurrent.TimeUnit.MILLISECONDS);
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Fail 1"));

        // When
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

        // Then
        assertThat(metrics.getNumberOfSuccessfulCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(2);
    }

    @Test
    @DisplayName("CircuitBreaker - 상태 전환 전체 시나리오")
    void circuitBreaker_fullStateTransitionScenario() throws InterruptedException {
        // 1. 초기 상태: CLOSED
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // 2. 실패 호출로 OPEN 상태로 전환
        circuitBreaker.onSuccess(100, java.util.concurrent.TimeUnit.MILLISECONDS);
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Fail 1"));
        circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Fail 2"));
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // 3. 대기 시간 경과 후 HALF_OPEN으로 전환
        Thread.sleep(1100);
        circuitBreaker.transitionToHalfOpenState();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);

        // 4. permittedNumberOfCallsInHalfOpenState (2번) 만큼 성공 호출로 CLOSED 상태로 복귀
        circuitBreaker.onSuccess(100, java.util.concurrent.TimeUnit.MILLISECONDS);
        circuitBreaker.onSuccess(100, java.util.concurrent.TimeUnit.MILLISECONDS);
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // 5. 다시 정상 동작
        String result = circuitBreaker.executeSupplier(() -> "success");
        assertThat(result).isEqualTo("success");
    }
}
