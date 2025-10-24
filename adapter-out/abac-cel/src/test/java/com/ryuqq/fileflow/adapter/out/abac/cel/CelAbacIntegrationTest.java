package com.ryuqq.fileflow.adapter.out.abac.cel;

import com.ryuqq.fileflow.adapter.out.abac.cel.adapter.CelAbacAdapter;
import com.ryuqq.fileflow.adapter.out.abac.cel.config.CelConfig;
import com.ryuqq.fileflow.adapter.out.abac.cel.engine.CelEngine;
import com.ryuqq.fileflow.adapter.out.abac.cel.evaluator.ConditionEvaluator;
import com.ryuqq.fileflow.application.iam.abac.port.out.AbacEvaluatorPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CelAbacIntegrationTest - CEL ABAC 통합 테스트
 *
 * <p>Spring Context 로딩, Bean 의존성 주입, E2E 조건 평가를 검증합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ Spring Context 로딩 검증</li>
 *   <li>✅ Bean 의존성 주입 검증 (CelEngine, ConditionEvaluator, CelAbacAdapter)</li>
 *   <li>✅ AbacEvaluatorPort 인터페이스로 접근 가능 검증</li>
 *   <li>✅ E2E 조건 평가 시나리오 검증</li>
 *   <li>✅ 변수 바인딩 통합 검증 (ctx.*, res.*)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Tag("integration")
@Tag("adapter")
@Tag("slow")
@SpringBootTest(classes = {TestApplication.class, CelConfig.class})
@DisplayName("CEL ABAC 통합 테스트")
class CelAbacIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private CelEngine celEngine;

    @Autowired(required = false)
    private ConditionEvaluator conditionEvaluator;

    @Autowired(required = false)
    private CelAbacAdapter celAbacAdapter;

    @Autowired(required = false)
    private AbacEvaluatorPort abacEvaluatorPort;

    @Nested
    @DisplayName("Spring Context 및 Bean 검증")
    class SpringContextTests {

        /**
         * 정상: Spring Context가 정상적으로 로드된다
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("Spring Context가 정상적으로 로드된다")
        void contextLoads() {
            // Then - Context 로드 성공
            assertThat(applicationContext).isNotNull();
        }

        /**
         * 정상: CelEngine Bean이 등록되어 있다
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("CelEngine Bean이 등록되어 있다")
        void celEngineBean_IsRegistered() {
            // Then - CelEngine Bean 존재
            assertThat(celEngine).isNotNull();
            assertThat(applicationContext.getBean(CelEngine.class)).isNotNull();
        }

        /**
         * 정상: ConditionEvaluator Bean이 등록되어 있다
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("ConditionEvaluator Bean이 등록되어 있다")
        void conditionEvaluatorBean_IsRegistered() {
            // Then - ConditionEvaluator Bean 존재
            assertThat(conditionEvaluator).isNotNull();
            assertThat(applicationContext.getBean(ConditionEvaluator.class)).isNotNull();
        }

        /**
         * 정상: CelAbacAdapter Bean이 등록되어 있다
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("CelAbacAdapter Bean이 등록되어 있다")
        void celAbacAdapterBean_IsRegistered() {
            // Then - CelAbacAdapter Bean 존재
            assertThat(celAbacAdapter).isNotNull();
            assertThat(applicationContext.getBean(CelAbacAdapter.class)).isNotNull();
        }

        /**
         * 정상: AbacEvaluatorPort 인터페이스로 접근 가능하다
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("AbacEvaluatorPort 인터페이스로 접근 가능하다")
        void abacEvaluatorPort_IsAccessible() {
            // Then - AbacEvaluatorPort로 접근 가능
            assertThat(abacEvaluatorPort).isNotNull();
            assertThat(abacEvaluatorPort).isInstanceOf(CelAbacAdapter.class);
            assertThat(applicationContext.getBean(AbacEvaluatorPort.class)).isNotNull();
        }

        /**
         * 정상: ConditionEvaluator가 CelEngine을 주입받는다
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("ConditionEvaluator가 CelEngine을 주입받는다")
        void conditionEvaluator_HasCelEngineDependency() {
            // Then - ConditionEvaluator가 동작함 (CelEngine 주입됨)
            assertThat(conditionEvaluator).isNotNull();

            String expression = "true";
            boolean result = conditionEvaluator.validate(expression);

            assertThat(result).isTrue();
        }

        /**
         * 정상: CelAbacAdapter가 ConditionEvaluator를 주입받는다
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("CelAbacAdapter가 ConditionEvaluator를 주입받는다")
        void celAbacAdapter_HasConditionEvaluatorDependency() {
            // Then - CelAbacAdapter가 동작함 (ConditionEvaluator 주입됨)
            assertThat(celAbacAdapter).isNotNull();

            String expression = "true";
            boolean result = celAbacAdapter.validateExpression(expression);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("E2E 조건 평가 시나리오")
    class EndToEndScenarioTests {

        /**
         * E2E: 파일 업로드 크기 제한 검증 (허용)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("파일 업로드 크기 제한 (15MB <= 20MB) 검증 성공")
        void e2e_FileUploadSizeLimit_Allowed() {
            // Given - 파일 업로드 시나리오
            String expression = "res.size_mb <= 20";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 15L);
            variables.put("res", res);

            // When - Port를 통한 조건 평가
            boolean result = abacEvaluatorPort.evaluateCondition(expression, variables);

            // Then - 허용됨
            assertThat(result).isTrue();
        }

        /**
         * E2E: 파일 업로드 크기 제한 검증 (거부)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("파일 업로드 크기 제한 (25MB > 20MB) 검증 실패")
        void e2e_FileUploadSizeLimit_Denied() {
            // Given - 파일 업로드 시나리오 (크기 초과)
            String expression = "res.size_mb <= 20";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 25L);
            variables.put("res", res);

            // When - Port를 통한 조건 평가
            boolean result = abacEvaluatorPort.evaluateCondition(expression, variables);

            // Then - 거부됨
            assertThat(result).isFalse();
        }

        /**
         * E2E: 관리자 역할 검증 (허용)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("관리자 역할 (role=admin) 검증 성공")
        void e2e_AdminRoleCheck_Allowed() {
            // Given - 관리자 역할 시나리오
            String expression = "ctx.role == \"admin\"";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "admin");
            variables.put("ctx", ctx);

            // When - Port를 통한 조건 평가
            boolean result = abacEvaluatorPort.evaluateCondition(expression, variables);

            // Then - 허용됨
            assertThat(result).isTrue();
        }

        /**
         * E2E: 일반 사용자 역할 검증 (거부)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("일반 사용자 역할 (role=user) 검증 실패")
        void e2e_UserRoleCheck_Denied() {
            // Given - 일반 사용자 역할 시나리오
            String expression = "ctx.role == \"admin\"";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "user");
            variables.put("ctx", ctx);

            // When - Port를 통한 조건 평가
            boolean result = abacEvaluatorPort.evaluateCondition(expression, variables);

            // Then - 거부됨
            assertThat(result).isFalse();
        }

        /**
         * E2E: 복합 조건 (관리자 AND 파일 크기) 검증 (허용)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("복합 조건 (admin AND size<=20) 검증 성공")
        void e2e_ComplexConditionAdminAndSize_Allowed() {
            // Given - 복합 조건 시나리오
            String expression = "ctx.role == \"admin\" && res.size_mb <= 20";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "admin");
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 10L);
            variables.put("ctx", ctx);
            variables.put("res", res);

            // When - Port를 통한 조건 평가
            boolean result = abacEvaluatorPort.evaluateCondition(expression, variables);

            // Then - 허용됨
            assertThat(result).isTrue();
        }

        /**
         * E2E: 복합 조건 (일반 사용자 AND 파일 크기) 검증 (거부)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("복합 조건 (user AND size<=20) 검증 실패")
        void e2e_ComplexConditionUserAndSize_Denied() {
            // Given - 복합 조건 시나리오 (역할 불일치)
            String expression = "ctx.role == \"admin\" && res.size_mb <= 20";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "user");
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 10L);
            variables.put("ctx", ctx);
            variables.put("res", res);

            // When - Port를 통한 조건 평가
            boolean result = abacEvaluatorPort.evaluateCondition(expression, variables);

            // Then - 거부됨
            assertThat(result).isFalse();
        }

        /**
         * E2E: 복합 조건 (OR) 검증 (허용)
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("복합 조건 (admin OR size<=10) 검증 성공")
        void e2e_ComplexConditionWithOr_Allowed() {
            // Given - OR 조건 시나리오 (한 조건만 만족)
            String expression = "ctx.role == \"admin\" || res.size_mb <= 10";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "user");
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 5L);
            variables.put("ctx", ctx);
            variables.put("res", res);

            // When - Port를 통한 조건 평가
            boolean result = abacEvaluatorPort.evaluateCondition(expression, variables);

            // Then - 허용됨 (size 조건 만족)
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("변수 바인딩 통합 검증")
    class VariableBindingIntegrationTests {

        /**
         * 통합: ctx 변수 바인딩 검증
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("ctx 변수 (user_id, tenant_id, role)가 올바르게 바인딩된다")
        void integration_CtxVariableBinding_Works() {
            // Given - ctx 변수 사용 표현식
            String expression = "ctx.user_id == 123 && ctx.tenant_id == \"tenant-1\" && ctx.role == \"admin\"";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("user_id", 123L);
            ctx.put("tenant_id", "tenant-1");
            ctx.put("role", "admin");
            variables.put("ctx", ctx);

            // When - Port를 통한 조건 평가
            boolean result = abacEvaluatorPort.evaluateCondition(expression, variables);

            // Then - 성공
            assertThat(result).isTrue();
        }

        /**
         * 통합: res 변수 바인딩 검증
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("res 변수 (file_type, size_mb)가 올바르게 바인딩된다")
        void integration_ResVariableBinding_Works() {
            // Given - res 변수 사용 표현식
            String expression = "res.file_type == \"pdf\" && res.size_mb <= 50";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> res = new HashMap<>();
            res.put("file_type", "pdf");
            res.put("size_mb", 30L);
            variables.put("res", res);

            // When - Port를 통한 조건 평가
            boolean result = abacEvaluatorPort.evaluateCondition(expression, variables);

            // Then - 성공
            assertThat(result).isTrue();
        }

        /**
         * 통합: ctx와 res 동시 바인딩 검증
         *
         * @author ryu-qqq
         * @since 2025-10-24
         */
        @Test
        @DisplayName("ctx와 res 변수가 동시에 올바르게 바인딩된다")
        void integration_CtxAndResVariableBinding_Works() {
            // Given - ctx와 res 모두 사용하는 표현식
            String expression = "ctx.role == \"admin\" || (ctx.role == \"user\" && res.size_mb <= 10)";
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "user");
            Map<String, Object> res = new HashMap<>();
            res.put("size_mb", 5L);
            variables.put("ctx", ctx);
            variables.put("res", res);

            // When - Port를 통한 조건 평가
            boolean result = abacEvaluatorPort.evaluateCondition(expression, variables);

            // Then - 성공 (user && 5MB <= 10MB)
            assertThat(result).isTrue();
        }
    }
}
