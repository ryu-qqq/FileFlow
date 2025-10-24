package com.ryuqq.fileflow.adapter.out.abac.cel.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CelEngineTest - CelEngine 단위 테스트
 *
 * <p>CEL 라이브러리 래퍼의 표현식 평가 및 검증 로직을 검증합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ {@code evaluateBoolean()}: CEL 표현식 평가 검증</li>
 *   <li>✅ {@code validateExpression()}: 표현식 유효성 검증</li>
 *   <li>✅ 변수 바인딩 검증 (ctx.*, res.*)</li>
 *   <li>✅ 보수적 거부 정책 검증 (Deny by Default)</li>
 *   <li>✅ 타입 불일치 오류 처리</li>
 *   <li>✅ 컴파일 오류 처리</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Tag("unit")
@Tag("adapter")
@Tag("fast")
@DisplayName("CelEngine 테스트")
class CelEngineTest {

    private final CelEngine celEngine = new CelEngine();

    @Nested
    @DisplayName("evaluateBoolean() - 정상 평가")
    class EvaluateBooleanSuccessTests {

        /**
         * 정상: res.size_mb <= 20 표현식 평가 (true)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("res.size_mb <= 20 표현식이 true로 평가된다")
        void evaluateBoolean_FileSizeUnderLimit_ReturnsTrue() {
            // Given - 파일 크기 제한 표현식
            String expression = "res.size_mb <= 20";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 15L);
            variables.put("res", res);

            // When - 표현식 평가
            boolean result = celEngine.evaluateBoolean(expression, variables);

            // Then - true 반환 (15MB <= 20MB)
            assertThat(result).isTrue();
        }

        /**
         * 정상: res.size_mb <= 20 표현식 평가 (false)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("res.size_mb <= 20 표현식이 false로 평가된다")
        void evaluateBoolean_FileSizeOverLimit_ReturnsFalse() {
            // Given - 파일 크기 제한 표현식
            String expression = "res.size_mb <= 20";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 25L);
            variables.put("res", res);

            // When - 표현식 평가
            boolean result = celEngine.evaluateBoolean(expression, variables);

            // Then - false 반환 (25MB > 20MB)
            assertThat(result).isFalse();
        }

        /**
         * 정상: ctx.role == "admin" 표현식 평가 (true)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("ctx.role == \"admin\" 표현식이 true로 평가된다")
        void evaluateBoolean_AdminRole_ReturnsTrue() {
            // Given - 역할 검증 표현식
            String expression = "ctx.role == \"admin\"";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "admin");
            variables.put("ctx", ctx);

            // When - 표현식 평가
            boolean result = celEngine.evaluateBoolean(expression, variables);

            // Then - true 반환
            assertThat(result).isTrue();
        }

        /**
         * 정상: ctx.role == "admin" 표현식 평가 (false)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("ctx.role == \"admin\" 표현식이 false로 평가된다")
        void evaluateBoolean_NonAdminRole_ReturnsFalse() {
            // Given - 역할 검증 표현식
            String expression = "ctx.role == \"admin\"";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "user");
            variables.put("ctx", ctx);

            // When - 표현식 평가
            boolean result = celEngine.evaluateBoolean(expression, variables);

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
        @DisplayName("복합 조건 (role AND size) 표현식이 true로 평가된다")
        void evaluateBoolean_ComplexConditionWithAnd_ReturnsTrue() {
            // Given - 복합 조건 표현식
            String expression = "ctx.role == \"admin\" && res.size_mb <= 20";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "admin");
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 15L);
            variables.put("ctx", ctx);
            variables.put("res", res);

            // When - 표현식 평가
            boolean result = celEngine.evaluateBoolean(expression, variables);

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
        @DisplayName("복합 조건 (role OR size) 표현식이 true로 평가된다")
        void evaluateBoolean_ComplexConditionWithOr_ReturnsTrue() {
            // Given - OR 조건 표현식
            String expression = "ctx.role == \"admin\" || res.size_mb <= 20";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "user");
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 15L);
            variables.put("ctx", ctx);
            variables.put("res", res);

            // When - 표현식 평가
            boolean result = celEngine.evaluateBoolean(expression, variables);

            // Then - true 반환 (size 조건 만족)
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("evaluateBoolean() - 보수적 거부 (Deny by Default)")
    class EvaluateBooleanDenyByDefaultTests {

        /**
         * 거부: null 표현식 → false 반환
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("null 표현식 전달 시 false를 반환한다")
        void evaluateBoolean_NullExpression_ReturnsFalse() {
            // Given - null 표현식
            Map<String, Object> variables = new HashMap<>();

            // When - 표현식 평가
            boolean result = celEngine.evaluateBoolean(null, variables);

            // Then - false 반환 (거부)
            assertThat(result).isFalse();
        }

        /**
         * 거부: 빈 표현식 → false 반환
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("빈 표현식 전달 시 false를 반환한다")
        void evaluateBoolean_BlankExpression_ReturnsFalse() {
            // Given - 빈 표현식
            String expression = "   ";
            Map<String, Object> variables = new HashMap<>();

            // When - 표현식 평가
            boolean result = celEngine.evaluateBoolean(expression, variables);

            // Then - false 반환 (거부)
            assertThat(result).isFalse();
        }

        /**
         * 거부: null 변수 맵 → false 반환
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("null 변수 맵 전달 시 false를 반환한다")
        void evaluateBoolean_NullVariables_ReturnsFalse() {
            // Given - null 변수 맵
            String expression = "true";

            // When - 표현식 평가
            boolean result = celEngine.evaluateBoolean(expression, null);

            // Then - false 반환 (거부)
            assertThat(result).isFalse();
        }

        /**
         * 거부: 컴파일 오류 표현식 → false 반환
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("잘못된 문법의 표현식 전달 시 false를 반환한다")
        void evaluateBoolean_InvalidSyntax_ReturnsFalse() {
            // Given - 잘못된 문법
            String expression = "res.size_mb <<= 20";  // 잘못된 연산자
            Map<String, Object> variables = new HashMap<>();

            // When - 표현식 평가
            boolean result = celEngine.evaluateBoolean(expression, variables);

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
        @DisplayName("Boolean이 아닌 결과 반환 시 false를 반환한다")
        void evaluateBoolean_NonBooleanResult_ReturnsFalse() {
            // Given - 숫자를 반환하는 표현식
            String expression = "res.size_mb";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 15L);
            variables.put("res", res);

            // When - 표현식 평가
            boolean result = celEngine.evaluateBoolean(expression, variables);

            // Then - false 반환 (Boolean이 아님)
            assertThat(result).isFalse();
        }

        /**
         * 거부: 런타임 평가 오류 → false 반환
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("런타임 평가 오류 발생 시 false를 반환한다")
        void evaluateBoolean_RuntimeEvaluationError_ReturnsFalse() {
            // Given - 존재하지 않는 변수 참조
            String expression = "ctx.non_existent_field == \"value\"";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            variables.put("ctx", ctx);

            // When - 표현식 평가
            boolean result = celEngine.evaluateBoolean(expression, variables);

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
        @DisplayName("올바른 문법의 표현식 검증 시 true를 반환한다")
        void validateExpression_ValidExpression_ReturnsTrue() {
            // Given - 올바른 표현식
            String expression = "res.size_mb <= 20";

            // When - 표현식 검증
            boolean result = celEngine.validateExpression(expression);

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
            boolean result = celEngine.validateExpression(expression);

            // Then - true 반환
            assertThat(result).isTrue();
        }

        /**
         * 무효: null 표현식 → false 반환
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("null 표현식 검증 시 false를 반환한다")
        void validateExpression_NullExpression_ReturnsFalse() {
            // When - null 검증
            boolean result = celEngine.validateExpression(null);

            // Then - false 반환
            assertThat(result).isFalse();
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
            boolean result = celEngine.validateExpression(expression);

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
            boolean result = celEngine.validateExpression(expression);

            // Then - false 반환
            assertThat(result).isFalse();
        }

        /**
         * 무효: 존재하지 않는 변수는 컴파일 시 통과
         *
         * <p>CEL은 동적 타입 언어처럼 작동하므로, 존재하지 않는 변수도 컴파일 시에는 통과합니다.
         * 런타임에 평가할 때만 오류가 발생합니다.</p>
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("존재하지 않는 변수 참조도 컴파일 시에는 통과한다")
        void validateExpression_UndefinedVariable_ReturnsTrue() {
            // Given - 존재하지 않는 변수 (ctx, res는 선언되어 있음)
            String expression = "ctx.undefined_field == \"value\"";

            // When - 표현식 검증
            boolean result = celEngine.validateExpression(expression);

            // Then - true 반환 (컴파일 시에는 통과)
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("변수 바인딩 테스트")
    class VariableBindingTests {

        /**
         * 정상: ctx 변수 바인딩 테스트
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("ctx 변수가 올바르게 바인딩된다")
        void variableBinding_CtxVariable_Works() {
            // Given - ctx 변수 사용 표현식
            String expression = "ctx.user_id == 123 && ctx.tenant_id == \"tenant-1\"";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("user_id", 123L);
            ctx.put("tenant_id", "tenant-1");
            variables.put("ctx", ctx);

            // When - 표현식 평가
            boolean result = celEngine.evaluateBoolean(expression, variables);

            // Then - true 반환
            assertThat(result).isTrue();
        }

        /**
         * 정상: res 변수 바인딩 테스트
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("res 변수가 올바르게 바인딩된다")
        void variableBinding_ResVariable_Works() {
            // Given - res 변수 사용 표현식
            String expression = "res.file_type == \"pdf\" && res.size_mb <= 50";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> res = new HashMap<>();
            res.put("file_type", "pdf");
            res.put("size_mb", 30L);
            variables.put("res", res);

            // When - 표현식 평가
            boolean result = celEngine.evaluateBoolean(expression, variables);

            // Then - true 반환
            assertThat(result).isTrue();
        }

        /**
         * 정상: ctx와 res 동시 바인딩 테스트
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("ctx와 res 변수가 동시에 올바르게 바인딩된다")
        void variableBinding_CtxAndRes_Works() {
            // Given - ctx와 res 모두 사용하는 표현식
            String expression = "ctx.role == \"admin\" || (ctx.role == \"user\" && res.size_mb <= 10)";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "user");
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 5L);
            variables.put("ctx", ctx);
            variables.put("res", res);

            // When - 표현식 평가
            boolean result = celEngine.evaluateBoolean(expression, variables);

            // Then - true 반환 (user && 5MB <= 10MB)
            assertThat(result).isTrue();
        }
    }
}
