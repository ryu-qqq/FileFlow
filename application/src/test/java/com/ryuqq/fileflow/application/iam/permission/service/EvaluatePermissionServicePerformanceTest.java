package com.ryuqq.fileflow.application.iam.permission.service;

import com.ryuqq.fileflow.application.iam.abac.port.out.AbacEvaluatorPort;
import com.ryuqq.fileflow.application.iam.permission.dto.command.EvaluatePermissionCommand;
import com.ryuqq.fileflow.application.iam.permission.dto.context.EvaluationContext;
import com.ryuqq.fileflow.application.iam.permission.dto.context.ResourceAttributes;
import com.ryuqq.fileflow.application.iam.permission.dto.response.EvaluatePermissionResponse;
import com.ryuqq.fileflow.application.iam.permission.port.out.GrantRepositoryPort;
import com.ryuqq.fileflow.domain.iam.permission.Grant;
import com.ryuqq.fileflow.domain.iam.permission.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * EvaluatePermissionService Performance Test
 *
 * <p>권한 평가 엔진의 성능을 측정하여 P95 레이턴시 목표를 검증합니다.</p>
 *
 * <p><strong>성능 목표:</strong></p>
 * <ul>
 *   <li>P95 Latency: < 50ms</li>
 *   <li>P99 Latency: < 100ms</li>
 *   <li>측정 반복 횟수: 1000회</li>
 * </ul>
 *
 * <p><strong>테스트 시나리오:</strong></p>
 * <ul>
 *   <li>No Grant (Cache Miss): 가장 빠른 경로</li>
 *   <li>Simple Grant (No Condition): 조건 없는 허용</li>
 *   <li>Conditional Grant (ABAC): CEL 조건 평가 포함</li>
 *   <li>Multiple Grants: 여러 Grant 스캔</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Tag("performance")
@Tag("application")
@Tag("slow")
@DisplayName("EvaluatePermissionService 성능 테스트")
class EvaluatePermissionServicePerformanceTest {

    private static final int WARMUP_ITERATIONS = 100;
    private static final int MEASUREMENT_ITERATIONS = 1000;
    private static final long P95_TARGET_MS = 50L;
    private static final long P99_TARGET_MS = 100L;

    private GrantRepositoryPort grantRepositoryPort;
    private AbacEvaluatorPort abacEvaluatorPort;
    private EvaluatePermissionService evaluatePermissionService;

    @BeforeEach
    void setUp() {
        grantRepositoryPort = mock(GrantRepositoryPort.class);
        abacEvaluatorPort = mock(AbacEvaluatorPort.class);
        evaluatePermissionService = new EvaluatePermissionService(
            grantRepositoryPort,
            abacEvaluatorPort
        );
    }

    @Test
    @DisplayName("Scenario 1: No Grant (Cache Miss) - 가장 빠른 경로")
    void performanceTest_NoGrant() {
        // Arrange
        EvaluationContext context = new EvaluationContext(1001L, 10L, 100L, "UPLOADER");
        EvaluatePermissionCommand command = EvaluatePermissionCommand.builder()
            .userContext(context)
            .permissionCode("file.upload")
            .scope(Scope.ORGANIZATION)
            .resourceAttributes(ResourceAttributes.empty())
            .build();

        when(grantRepositoryPort.findEffectiveGrants(1001L, 10L, 100L))
            .thenReturn(Collections.emptyList());

        // Act & Assert
        PerformanceMetrics metrics = measurePerformance("No Grant", () -> {
            EvaluatePermissionResponse response = evaluatePermissionService.execute(command);
            assertThat(response.allowed()).isFalse();
        });

        printMetrics(metrics);
        assertPerformanceTarget(metrics, "No Grant");
    }

    @Test
    @DisplayName("Scenario 2: Simple Grant (No Condition) - 조건 없는 허용")
    void performanceTest_SimpleGrant() {
        // Arrange
        EvaluationContext context = new EvaluationContext(1001L, 10L, 100L, "UPLOADER");
        EvaluatePermissionCommand command = EvaluatePermissionCommand.builder()
            .userContext(context)
            .permissionCode("file.upload")
            .scope(Scope.ORGANIZATION)
            .resourceAttributes(ResourceAttributes.empty())
            .build();

        Grant simpleGrant = new Grant(
            "UPLOADER",
            "file.upload",
            Scope.ORGANIZATION,
            null
        );

        when(grantRepositoryPort.findEffectiveGrants(1001L, 10L, 100L))
            .thenReturn(List.of(simpleGrant));

        // Act & Assert
        PerformanceMetrics metrics = measurePerformance("Simple Grant", () -> {
            EvaluatePermissionResponse response = evaluatePermissionService.execute(command);
            assertThat(response.allowed()).isTrue();
        });

        printMetrics(metrics);
        assertPerformanceTarget(metrics, "Simple Grant");
    }

    @Test
    @DisplayName("Scenario 3: Conditional Grant (ABAC) - CEL 조건 평가 포함")
    void performanceTest_ConditionalGrant() {
        // Arrange
        String conditionExpr = "res.size_mb <= 20";
        EvaluationContext context = new EvaluationContext(1001L, 10L, 100L, "UPLOADER");
        ResourceAttributes resourceAttributes = ResourceAttributes.builder()
            .attribute("size_mb", 15.5)
            .build();

        EvaluatePermissionCommand command = EvaluatePermissionCommand.builder()
            .userContext(context)
            .permissionCode("file.upload")
            .scope(Scope.ORGANIZATION)
            .resourceAttributes(resourceAttributes)
            .build();

        Grant conditionalGrant = new Grant(
            "UPLOADER",
            "file.upload",
            Scope.ORGANIZATION,
            conditionExpr
        );

        when(grantRepositoryPort.findEffectiveGrants(1001L, 10L, 100L))
            .thenReturn(List.of(conditionalGrant));
        when(abacEvaluatorPort.evaluateCondition(eq(conditionExpr), anyMap()))
            .thenReturn(true);

        // Act & Assert
        PerformanceMetrics metrics = measurePerformance("Conditional Grant (ABAC)", () -> {
            EvaluatePermissionResponse response = evaluatePermissionService.execute(command);
            assertThat(response.allowed()).isTrue();
        });

        printMetrics(metrics);
        assertPerformanceTarget(metrics, "Conditional Grant (ABAC)");
    }

    @Test
    @DisplayName("Scenario 4: Multiple Grants - 여러 Grant 스캔")
    void performanceTest_MultipleGrants() {
        // Arrange
        EvaluationContext context = new EvaluationContext(1001L, 10L, 100L, "UPLOADER");
        EvaluatePermissionCommand command = EvaluatePermissionCommand.builder()
            .userContext(context)
            .permissionCode("file.upload")
            .scope(Scope.ORGANIZATION)
            .resourceAttributes(ResourceAttributes.empty())
            .build();

        List<Grant> multipleGrants = List.of(
            new Grant("VIEWER", "file.view", Scope.SELF, null),
            new Grant("VIEWER", "file.download", Scope.SELF, null),
            new Grant("UPLOADER", "file.delete", Scope.SELF, null),
            new Grant("UPLOADER", "file.upload", Scope.ORGANIZATION, null),
            new Grant("ADMIN", "file.upload", Scope.TENANT, null)
        );

        when(grantRepositoryPort.findEffectiveGrants(1001L, 10L, 100L))
            .thenReturn(multipleGrants);

        // Act & Assert
        PerformanceMetrics metrics = measurePerformance("Multiple Grants (5개)", () -> {
            EvaluatePermissionResponse response = evaluatePermissionService.execute(command);
            assertThat(response.allowed()).isTrue();
        });

        printMetrics(metrics);
        assertPerformanceTarget(metrics, "Multiple Grants");
    }

    /**
     * 성능 측정 헬퍼 메서드
     *
     * @param scenarioName 시나리오 이름
     * @param task 측정할 작업
     * @return 성능 메트릭
     */
    private PerformanceMetrics measurePerformance(String scenarioName, Runnable task) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            task.run();
        }

        // Measurement
        List<Long> latencies = new ArrayList<>(MEASUREMENT_ITERATIONS);
        StopWatch stopWatch = new StopWatch(scenarioName);

        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            stopWatch.start("iteration-" + i);
            task.run();
            stopWatch.stop();
            latencies.add(stopWatch.getLastTaskTimeMillis());
        }

        return calculateMetrics(scenarioName, latencies);
    }

    /**
     * 성능 메트릭 계산
     *
     * @param scenarioName 시나리오 이름
     * @param latencies 레이턴시 목록 (ms)
     * @return 성능 메트릭
     */
    private PerformanceMetrics calculateMetrics(String scenarioName, List<Long> latencies) {
        Collections.sort(latencies);

        int size = latencies.size();
        long min = latencies.get(0);
        long max = latencies.get(size - 1);
        long p50 = latencies.get(size / 2);
        long p95 = latencies.get((int) (size * 0.95));
        long p99 = latencies.get((int) (size * 0.99));
        double avg = latencies.stream().mapToLong(Long::longValue).average().orElse(0.0);

        return new PerformanceMetrics(scenarioName, min, max, avg, p50, p95, p99);
    }

    /**
     * 성능 메트릭 출력
     *
     * @param metrics 성능 메트릭
     */
    private void printMetrics(PerformanceMetrics metrics) {
        System.out.println("\n========================================");
        System.out.println("Performance Test Results: " + metrics.scenarioName);
        System.out.println("========================================");
        System.out.println(String.format("Iterations: %d", MEASUREMENT_ITERATIONS));
        System.out.println(String.format("Min: %d ms", metrics.min));
        System.out.println(String.format("Max: %d ms", metrics.max));
        System.out.println(String.format("Avg: %.2f ms", metrics.avg));
        System.out.println(String.format("P50: %d ms", metrics.p50));
        System.out.println(String.format("P95: %d ms (Target: < %d ms)", metrics.p95, P95_TARGET_MS));
        System.out.println(String.format("P99: %d ms (Target: < %d ms)", metrics.p99, P99_TARGET_MS));
        System.out.println("========================================\n");
    }

    /**
     * 성능 목표 검증
     *
     * @param metrics 성능 메트릭
     * @param scenarioName 시나리오 이름
     */
    private void assertPerformanceTarget(PerformanceMetrics metrics, String scenarioName) {
        assertThat(metrics.p95)
            .as("%s - P95 레이턴시는 %dms 미만이어야 합니다", scenarioName, P95_TARGET_MS)
            .isLessThan(P95_TARGET_MS);

        assertThat(metrics.p99)
            .as("%s - P99 레이턴시는 %dms 미만이어야 합니다", scenarioName, P99_TARGET_MS)
            .isLessThan(P99_TARGET_MS);
    }

    /**
     * Performance Metrics Record
     *
     * @param scenarioName 시나리오 이름
     * @param min 최소 레이턴시 (ms)
     * @param max 최대 레이턴시 (ms)
     * @param avg 평균 레이턴시 (ms)
     * @param p50 P50 레이턴시 (ms)
     * @param p95 P95 레이턴시 (ms)
     * @param p99 P99 레이턴시 (ms)
     */
    private record PerformanceMetrics(
        String scenarioName,
        long min,
        long max,
        double avg,
        long p50,
        long p95,
        long p99
    ) {
    }
}
