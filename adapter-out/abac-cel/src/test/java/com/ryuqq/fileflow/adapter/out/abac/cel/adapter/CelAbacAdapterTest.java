package com.ryuqq.fileflow.adapter.out.abac.cel.adapter;

import com.ryuqq.fileflow.adapter.out.abac.cel.engine.CelEngine;
import com.ryuqq.fileflow.adapter.out.abac.cel.evaluator.ConditionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CelAbacAdapterTest - CelAbacAdapter 단위 테스트
 *
 * <p>AbacEvaluatorPort 구현체의 조건 평가 및 성능 모니터링을 검증합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ {@code evaluateCondition()}: 조건 평가 위임 검증</li>
 *   <li>✅ {@code validateExpression()}: 표현식 검증 위임 검증</li>
 *   <li>✅ 성능 모니터링 로직 검증</li>
 *   <li>✅ 입력 검증 예외 처리</li>
 *   <li>✅ 보수적 거부 정책 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Tag("unit")
@Tag("adapter")
@Tag("fast")
@DisplayName("CelAbacAdapter 테스트")
class CelAbacAdapterTest {

    private CelAbacAdapter celAbacAdapter;
    private ConditionEvaluator conditionEvaluator;

    /**
     * 각 테스트 전 초기화
     *
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @BeforeEach
    void setUp() {
        CelEngine celEngine = new CelEngine();
        conditionEvaluator = new ConditionEvaluator(celEngine);
        celAbacAdapter = new CelAbacAdapter(conditionEvaluator);
    }

    @Nested
    @DisplayName("evaluateCondition() - 정상 평가")
    class EvaluateConditionSuccessTests {

        /**
         * 정상: res.size_mb <= 20 표현식 평가 (true)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("res.size_mb <= 20 표현식이 true로 평가된다")
        void evaluateCondition_FileSizeUnderLimit_ReturnsTrue() {
            // Given - 파일 크기 제한 표현식
            String expression = "res.size_mb <= 20";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 15L);
            variables.put("res", res);

            // When - 조건 평가
            boolean result = celAbacAdapter.evaluateCondition(expression, variables);

            // Then - true 반환
            assertThat(result).isTrue();
        }

        /**
         * 정상: ctx.role == "admin" 표현식 평가 (true)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("ctx.role == \"admin\" 표현식이 true로 평가된다")
        void evaluateCondition_AdminRole_ReturnsTrue() {
            // Given - 역할 검증 표현식
            String expression = "ctx.role == \"admin\"";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "admin");
            variables.put("ctx", ctx);

            // When - 조건 평가
            boolean result = celAbacAdapter.evaluateCondition(expression, variables);

            // Then - true 반환
            assertThat(result).isTrue();
        }

        /**
         * 정상: ctx.role == "user" 표현식 평가 (false)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("ctx.role == \"admin\" 표현식이 false로 평가된다 (role=user)")
        void evaluateCondition_NonAdminRole_ReturnsFalse() {
            // Given - 역할 검증 표현식
            String expression = "ctx.role == \"admin\"";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "user");
            variables.put("ctx", ctx);

            // When - 조건 평가
            boolean result = celAbacAdapter.evaluateCondition(expression, variables);

            // Then - false 반환
            assertThat(result).isFalse();
        }

        /**
         * 정상: 복합 조건 표현식 평가 (AND)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("복합 조건 (role AND size) 표현식이 올바르게 평가된다")
        void evaluateCondition_ComplexConditionWithAnd_ReturnsTrue() {
            // Given - 복합 조건 표현식
            String expression = "ctx.role == \"admin\" && res.size_mb <= 20";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "admin");
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 15L);
            variables.put("ctx", ctx);
            variables.put("res", res);

            // When - 조건 평가
            boolean result = celAbacAdapter.evaluateCondition(expression, variables);

            // Then - true 반환 (둘 다 만족)
            assertThat(result).isTrue();
        }

        /**
         * 정상: 복합 조건 표현식 평가 (OR)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("복합 조건 (role OR size) 표현식이 올바르게 평가된다")
        void evaluateCondition_ComplexConditionWithOr_ReturnsTrue() {
            // Given - OR 조건 표현식
            String expression = "ctx.role == \"admin\" || res.size_mb <= 20";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "user");
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 5L);
            variables.put("ctx", ctx);
            variables.put("res", res);

            // When - 조건 평가
            boolean result = celAbacAdapter.evaluateCondition(expression, variables);

            // Then - true 반환 (size 조건 만족)
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("evaluateCondition() - 입력 검증 예외")
    class EvaluateConditionValidationTests {

        /**
         * 예외: null 표현식 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("null 표현식 전달 시 IllegalArgumentException이 발생한다")
        void evaluateCondition_NullExpression_ThrowsException() {
            // Given - null 표현식
            Map<String, Object> variables = new HashMap<>();

            // When & Then - IllegalArgumentException 발생
            assertThatThrownBy(() -> celAbacAdapter.evaluateCondition(null, variables))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expression must not be null");
        }

        /**
         * 거부: 빈 표현식 전달 시 false 반환
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("빈 표현식 전달 시 false를 반환한다")
        void evaluateCondition_BlankExpression_ReturnsFalse() {
            // Given - 빈 표현식
            String expression = "   ";
            Map<String, Object> variables = new HashMap<>();

            // When - 조건 평가
            boolean result = celAbacAdapter.evaluateCondition(expression, variables);

            // Then - false 반환 (거부)
            assertThat(result).isFalse();
        }

        /**
         * 예외: null 변수 맵 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("null 변수 맵 전달 시 IllegalArgumentException이 발생한다")
        void evaluateCondition_NullVariables_ThrowsException() {
            // Given - null 변수 맵
            String expression = "true";

            // When & Then - IllegalArgumentException 발생
            assertThatThrownBy(() -> celAbacAdapter.evaluateCondition(expression, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Variables map must not be null");
        }
    }

    @Nested
    @DisplayName("evaluateCondition() - 보수적 거부 (Deny by Default)")
    class EvaluateConditionDenyByDefaultTests {

        /**
         * 거부: 잘못된 문법 표현식 → false 반환
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("잘못된 문법의 표현식은 false를 반환한다")
        void evaluateCondition_InvalidSyntax_ReturnsFalse() {
            // Given - 잘못된 문법
            String expression = "res.size_mb <<= 20";
            Map<String, Object> variables = new HashMap<>();

            // When - 조건 평가
            boolean result = celAbacAdapter.evaluateCondition(expression, variables);

            // Then - false 반환 (거부)
            assertThat(result).isFalse();
        }

        /**
         * 거부: 존재하지 않는 변수 참조 → false 반환
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("존재하지 않는 변수 참조 시 false를 반환한다")
        void evaluateCondition_UndefinedVariable_ReturnsFalse() {
            // Given - 존재하지 않는 변수 참조
            String expression = "ctx.non_existent_field == \"value\"";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            variables.put("ctx", ctx);

            // When - 조건 평가
            boolean result = celAbacAdapter.evaluateCondition(expression, variables);

            // Then - false 반환 (거부)
            assertThat(result).isFalse();
        }

        /**
         * 거부: Boolean이 아닌 결과 → false 반환
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("Boolean이 아닌 결과는 false를 반환한다")
        void evaluateCondition_NonBooleanResult_ReturnsFalse() {
            // Given - 숫자를 반환하는 표현식
            String expression = "res.size_mb";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 15L);
            variables.put("res", res);

            // When - 조건 평가
            boolean result = celAbacAdapter.evaluateCondition(expression, variables);

            // Then - false 반환 (거부)
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("validateExpression() - 표현식 유효성 검증")
    class ValidateExpressionTests {

        /**
         * 유효: 올바른 표현식 → true 반환
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("올바른 표현식 검증 시 true를 반환한다")
        void validateExpression_ValidExpression_ReturnsTrue() {
            // Given - 올바른 표현식
            String expression = "res.size_mb <= 20";

            // When - 표현식 검증
            boolean result = celAbacAdapter.validateExpression(expression);

            // Then - true 반환
            assertThat(result).isTrue();
        }

        /**
         * 유효: 복잡한 표현식 → true 반환
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("복잡한 표현식 검증 시 true를 반환한다")
        void validateExpression_ComplexExpression_ReturnsTrue() {
            // Given - 복잡한 표현식
            String expression = "ctx.role == \"admin\" && res.size_mb <= 20 || ctx.is_premium";

            // When - 표현식 검증
            boolean result = celAbacAdapter.validateExpression(expression);

            // Then - true 반환
            assertThat(result).isTrue();
        }

        /**
         * 무효: null 표현식 → IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("null 표현식 검증 시 IllegalArgumentException이 발생한다")
        void validateExpression_NullExpression_ThrowsException() {
            // When & Then - IllegalArgumentException 발생
            assertThatThrownBy(() -> celAbacAdapter.validateExpression(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expression must not be null");
        }

        /**
         * 무효: 빈 표현식 → false 반환
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("빈 표현식 검증 시 false를 반환한다")
        void validateExpression_BlankExpression_ReturnsFalse() {
            // Given - 빈 표현식
            String expression = "   ";

            // When - 표현식 검증
            boolean result = celAbacAdapter.validateExpression(expression);

            // Then - false 반환
            assertThat(result).isFalse();
        }

        /**
         * 무효: 잘못된 문법 → false 반환
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("잘못된 문법의 표현식 검증 시 false를 반환한다")
        void validateExpression_InvalidSyntax_ReturnsFalse() {
            // Given - 잘못된 문법
            String expression = "res.size_mb <<= 20";

            // When - 표현식 검증
            boolean result = celAbacAdapter.validateExpression(expression);

            // Then - false 반환
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("성능 테스트 - P95 < 10ms")
    class PerformanceTests {

        /**
         * 성능: 1,000회 평가 후 P95 < 10ms 검증
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("1,000회 평가 후 P95 < 10ms를 만족한다")
        void performance_1000Evaluations_P95LessThan10ms() {
            // Given - 테스트 표현식과 변수
            String expression = "ctx.role == \"admin\" && res.size_mb <= 20";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "admin");
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 15L);
            variables.put("ctx", ctx);
            variables.put("res", res);

            int iterations = 1000;
            long[] durations = new long[iterations];

            // When - 1,000회 평가
            for (int i = 0; i < iterations; i++) {
                long startTime = System.nanoTime();
                celAbacAdapter.evaluateCondition(expression, variables);
                long endTime = System.nanoTime();
                durations[i] = endTime - startTime;
            }

            // Then - P95 < 10ms 검증
            java.util.Arrays.sort(durations);
            int p95Index = (int) Math.ceil(iterations * 0.95) - 1;
            long p95DurationNanos = durations[p95Index];
            long p95DurationMillis = p95DurationNanos / 1_000_000;

            assertThat(p95DurationMillis)
                .as("P95 레이턴시는 10ms 이하여야 합니다")
                .isLessThanOrEqualTo(10L);
        }

        /**
         * 성능: 10,000회 평가 후 P95 < 10ms 검증
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("10,000회 평가 후에도 P95 < 10ms를 만족한다")
        void performance_10000Evaluations_P95LessThan10ms() {
            // Given - 테스트 표현식과 변수
            String expression = "res.size_mb <= 20";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 15L);
            variables.put("res", res);

            int iterations = 10000;
            long[] durations = new long[iterations];

            // When - 10,000회 평가 (워밍업 포함)
            for (int i = 0; i < iterations; i++) {
                long startTime = System.nanoTime();
                celAbacAdapter.evaluateCondition(expression, variables);
                long endTime = System.nanoTime();
                durations[i] = endTime - startTime;
            }

            // Then - P95 < 10ms 검증
            java.util.Arrays.sort(durations);
            int p95Index = (int) Math.ceil(iterations * 0.95) - 1;
            long p95DurationNanos = durations[p95Index];
            long p95DurationMillis = p95DurationNanos / 1_000_000;

            assertThat(p95DurationMillis)
                .as("P95 레이턴시는 10ms 이하여야 합니다")
                .isLessThanOrEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorTests {

        /**
         * 예외: null ConditionEvaluator 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("null ConditionEvaluator 전달 시 IllegalArgumentException이 발생한다")
        void constructor_NullConditionEvaluator_ThrowsException() {
            // When & Then - IllegalArgumentException 발생
            assertThatThrownBy(() -> new CelAbacAdapter(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ConditionEvaluator must not be null");
        }
    }
}
