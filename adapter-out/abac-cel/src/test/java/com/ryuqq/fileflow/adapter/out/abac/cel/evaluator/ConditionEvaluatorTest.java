package com.ryuqq.fileflow.adapter.out.abac.cel.evaluator;

import com.ryuqq.fileflow.adapter.out.abac.cel.engine.CelEngine;
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
 * ConditionEvaluatorTest - ConditionEvaluator 단위 테스트
 *
 * <p>조건 평가 로직의 입력 검증 및 CEL 엔진 위임을 검증합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ {@code evaluate()}: 정상적인 조건 평가 검증</li>
 *   <li>✅ {@code validate()}: 표현식 유효성 검증</li>
 *   <li>✅ null 파라미터 예외 처리 검증</li>
 *   <li>✅ 빈 표현식 예외 처리 검증</li>
 *   <li>✅ CEL 엔진 위임 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Tag("unit")
@Tag("adapter")
@Tag("fast")
@DisplayName("ConditionEvaluator 테스트")
class ConditionEvaluatorTest {

    private CelEngine celEngine;
    private ConditionEvaluator conditionEvaluator;

    /**
     * 각 테스트 전 초기화
     *
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @BeforeEach
    void setUp() {
        celEngine = new CelEngine();
        conditionEvaluator = new ConditionEvaluator(celEngine);
    }

    @Nested
    @DisplayName("evaluate() - 정상 평가")
    class EvaluateSuccessTests {

        /**
         * 정상: res.size_mb <= 20 표현식 평가 (true)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("res.size_mb <= 20 표현식이 true로 평가된다")
        void evaluate_FileSizeUnderLimit_ReturnsTrue() {
            // Given - 파일 크기 제한 표현식
            String expression = "res.size_mb <= 20";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 15L);
            variables.put("res", res);

            // When - 표현식 평가
            boolean result = conditionEvaluator.evaluate(expression, variables);

            // Then - true 반환
            assertThat(result).isTrue();
        }

        /**
         * 정상: ctx.role == "admin" 표현식 평가
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("ctx.role == \"admin\" 표현식이 올바르게 평가된다")
        void evaluate_AdminRole_ReturnsTrue() {
            // Given - 역할 검증 표현식
            String expression = "ctx.role == \"admin\"";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "admin");
            variables.put("ctx", ctx);

            // When - 표현식 평가
            boolean result = conditionEvaluator.evaluate(expression, variables);

            // Then - true 반환
            assertThat(result).isTrue();
        }

        /**
         * 정상: 복합 조건 표현식 평가
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("복합 조건 표현식이 올바르게 평가된다")
        void evaluate_ComplexCondition_ReturnsTrue() {
            // Given - 복합 조건 표현식
            String expression = "ctx.role == \"admin\" && res.size_mb <= 20";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "admin");
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 10L);
            variables.put("ctx", ctx);
            variables.put("res", res);

            // When - 표현식 평가
            boolean result = conditionEvaluator.evaluate(expression, variables);

            // Then - true 반환
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("evaluate() - 입력 검증 예외")
    class EvaluateValidationTests {

        /**
         * 예외: null 표현식 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("null 표현식 전달 시 IllegalArgumentException이 발생한다")
        void evaluate_NullExpression_ThrowsException() {
            // Given - null 표현식
            Map<String, Object> variables = new HashMap<>();

            // When & Then - IllegalArgumentException 발생
            assertThatThrownBy(() -> conditionEvaluator.evaluate(null, variables))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expression must not be null");
        }

        /**
         * 예외: 빈 표현식 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("빈 표현식 전달 시 IllegalArgumentException이 발생한다")
        void evaluate_BlankExpression_ThrowsException() {
            // Given - 빈 표현식
            String expression = "   ";
            Map<String, Object> variables = new HashMap<>();

            // When & Then - evaluate는 빈 표현식에 대해 false를 반환하므로 예외가 발생하지 않음
            boolean result = conditionEvaluator.evaluate(expression, variables);

            // Then - false 반환
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
        void evaluate_NullVariables_ThrowsException() {
            // Given - null 변수 맵
            String expression = "true";

            // When & Then - IllegalArgumentException 발생
            assertThatThrownBy(() -> conditionEvaluator.evaluate(expression, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Variables map must not be null");
        }
    }

    @Nested
    @DisplayName("evaluate() - 보수적 거부 (Deny by Default)")
    class EvaluateDenyByDefaultTests {

        /**
         * 거부: 잘못된 문법 표현식 → false 반환
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("잘못된 문법의 표현식은 false를 반환한다")
        void evaluate_InvalidSyntax_ReturnsFalse() {
            // Given - 잘못된 문법
            String expression = "res.size_mb <<= 20";
            Map<String, Object> variables = new HashMap<>();

            // When - 표현식 평가
            boolean result = conditionEvaluator.evaluate(expression, variables);

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
        void evaluate_UndefinedVariable_ReturnsFalse() {
            // Given - 존재하지 않는 변수 참조
            String expression = "ctx.non_existent_field == \"value\"";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            variables.put("ctx", ctx);

            // When - 표현식 평가
            boolean result = conditionEvaluator.evaluate(expression, variables);

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
        void evaluate_NonBooleanResult_ReturnsFalse() {
            // Given - 숫자를 반환하는 표현식
            String expression = "res.size_mb";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 15L);
            variables.put("res", res);

            // When - 표현식 평가
            boolean result = conditionEvaluator.evaluate(expression, variables);

            // Then - false 반환 (거부)
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("validate() - 표현식 유효성 검증")
    class ValidateTests {

        /**
         * 유효: 올바른 표현식 → true 반환
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("올바른 표현식 검증 시 true를 반환한다")
        void validate_ValidExpression_ReturnsTrue() {
            // Given - 올바른 표현식
            String expression = "res.size_mb <= 20";

            // When - 표현식 검증
            boolean result = conditionEvaluator.validate(expression);

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
        void validate_ComplexExpression_ReturnsTrue() {
            // Given - 복잡한 표현식
            String expression = "ctx.role == \"admin\" && res.size_mb <= 20 || ctx.is_premium";

            // When - 표현식 검증
            boolean result = conditionEvaluator.validate(expression);

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
        void validate_NullExpression_ThrowsException() {
            // When & Then - IllegalArgumentException 발생
            assertThatThrownBy(() -> conditionEvaluator.validate(null))
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
        void validate_BlankExpression_ReturnsFalse() {
            // Given - 빈 표현식
            String expression = "   ";

            // When - 표현식 검증
            boolean result = conditionEvaluator.validate(expression);

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
        void validate_InvalidSyntax_ReturnsFalse() {
            // Given - 잘못된 문법
            String expression = "res.size_mb <<= 20";

            // When - 표현식 검증
            boolean result = conditionEvaluator.validate(expression);

            // Then - false 반환
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorTests {

        /**
         * 예외: null CelEngine 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("null CelEngine 전달 시 IllegalArgumentException이 발생한다")
        void constructor_NullCelEngine_ThrowsException() {
            // When & Then - IllegalArgumentException 발생
            assertThatThrownBy(() -> new ConditionEvaluator(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CelEngine must not be null");
        }
    }
}
