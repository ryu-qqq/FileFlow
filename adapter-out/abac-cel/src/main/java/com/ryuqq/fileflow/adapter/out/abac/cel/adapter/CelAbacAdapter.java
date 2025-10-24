package com.ryuqq.fileflow.adapter.out.abac.cel.adapter;

import com.ryuqq.fileflow.adapter.out.abac.cel.evaluator.ConditionEvaluator;
import com.ryuqq.fileflow.application.iam.abac.port.out.AbacEvaluatorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * CEL 기반 ABAC 어댑터 (Hexagonal Architecture - Driven Adapter)
 *
 * <p>{@link AbacEvaluatorPort} 인터페이스를 구현하여
 * Application Layer와 CEL 엔진 사이의 어댑터 역할을 수행합니다.</p>
 *
 * <p><strong>책임</strong>:
 * <ul>
 *   <li>Port 인터페이스 구현 (의존성 역전)</li>
 *   <li>ConditionEvaluator에 평가 위임</li>
 *   <li>Spring Bean으로 등록</li>
 *   <li>로깅 및 모니터링</li>
 * </ul>
 * </p>
 *
 * <p><strong>성능 요구사항</strong>: P95 Latency < 10ms</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Component
public class CelAbacAdapter implements AbacEvaluatorPort {

    private static final Logger log = LoggerFactory.getLogger(CelAbacAdapter.class);

    private final ConditionEvaluator conditionEvaluator;

    /**
     * CelAbacAdapter 생성자
     *
     * <p>Spring이 ConditionEvaluator를 자동 주입합니다.</p>
     *
     * @param conditionEvaluator 조건 평가기
     * @throws IllegalArgumentException conditionEvaluator가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public CelAbacAdapter(ConditionEvaluator conditionEvaluator) {
        if (conditionEvaluator == null) {
            throw new IllegalArgumentException("ConditionEvaluator must not be null");
        }
        this.conditionEvaluator = conditionEvaluator;
        log.info("CelAbacAdapter initialized");
    }

    /**
     * ABAC 조건식 평가
     *
     * <p>{@link AbacEvaluatorPort#evaluateCondition(String, Map)} 구현</p>
     *
     * <p><strong>보수적 거부 (Deny by Default)</strong>:
     * <ul>
     *   <li>표현식 평가 실패 → {@code false}</li>
     *   <li>런타임 에러 → {@code false}</li>
     *   <li>null 입력 → IllegalArgumentException</li>
     * </ul>
     * </p>
     *
     * @param expression CEL 조건식 (예: "res.size_mb <= 20")
     * @param variables 변수 바인딩 맵 (key: 변수명, value: 값)
     * @return 평가 결과 {@code true} (허용) 또는 {@code false} (거부)
     * @throws IllegalArgumentException expression 또는 variables가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public boolean evaluateCondition(String expression, Map<String, Object> variables) {
        if (expression == null) {
            throw new IllegalArgumentException("Expression must not be null");
        }

        if (variables == null) {
            throw new IllegalArgumentException("Variables map must not be null");
        }

        long startTime = System.nanoTime();

        try {
            boolean result = conditionEvaluator.evaluate(expression, variables);
            long durationNanos = System.nanoTime() - startTime;
            long durationMillis = durationNanos / 1_000_000;

            log.debug("ABAC evaluation completed in {}ms. Expression: '{}', Result: {}",
                    durationMillis, expression, result);

            if (durationMillis > 10) {
                log.warn("ABAC evaluation exceeded 10ms threshold: {}ms for expression '{}'",
                        durationMillis, expression);
            }

            return result;

        } catch (Exception e) {
            long durationNanos = System.nanoTime() - startTime;
            long durationMillis = durationNanos / 1_000_000;

            log.error("ABAC evaluation failed after {}ms. Expression: '{}', Error: {}",
                    durationMillis, expression, e.getMessage(), e);

            return false;
        }
    }

    /**
     * ABAC 조건식 유효성 검증
     *
     * <p>{@link AbacEvaluatorPort#validateExpression(String)} 구현</p>
     *
     * @param expression 검증할 CEL 표현식
     * @return 유효한 표현식이면 {@code true}, 그렇지 않으면 {@code false}
     * @throws IllegalArgumentException expression이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public boolean validateExpression(String expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Expression must not be null");
        }

        try {
            return conditionEvaluator.validate(expression);
        } catch (Exception e) {
            log.error("Expression validation failed for '{}': {}", expression, e.getMessage());
            return false;
        }
    }
}
