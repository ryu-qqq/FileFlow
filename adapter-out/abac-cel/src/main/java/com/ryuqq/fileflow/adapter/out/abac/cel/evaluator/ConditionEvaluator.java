package com.ryuqq.fileflow.adapter.out.abac.cel.evaluator;

import com.ryuqq.fileflow.adapter.out.abac.cel.engine.CelEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * ABAC 조건 평가기
 *
 * <p>CelEngine을 사용하여 ABAC 조건식을 평가합니다.
 * 비즈니스 로직과 CEL 엔진 사이의 중간 계층 역할을 수행합니다.</p>
 *
 * <p><strong>책임</strong>:
 * <ul>
 *   <li>입력 검증 (표현식, 변수)</li>
 *   <li>변수 바인딩 형식 검증</li>
 *   <li>CEL 엔진 호출 및 결과 반환</li>
 *   <li>보수적 거부 정책 적용</li>
 * </ul>
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public class ConditionEvaluator {

    private static final Logger log = LoggerFactory.getLogger(ConditionEvaluator.class);

    private final CelEngine celEngine;

    /**
     * ConditionEvaluator 생성자
     *
     * @param celEngine CEL 엔진 인스턴스
     * @throws IllegalArgumentException celEngine이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public ConditionEvaluator(CelEngine celEngine) {
        if (celEngine == null) {
            throw new IllegalArgumentException("CelEngine must not be null");
        }
        this.celEngine = celEngine;
        log.info("ConditionEvaluator initialized");
    }

    /**
     * ABAC 조건 평가
     *
     * <p>주어진 표현식과 변수를 사용하여 ABAC 조건을 평가합니다.
     * 입력 검증 실패 시 {@code false}를 반환합니다 (보수적 거부).</p>
     *
     * <p><strong>입력 검증</strong>:
     * <ul>
     *   <li>표현식이 null 또는 blank → {@code false}</li>
     *   <li>변수 맵이 null → {@code false}</li>
     *   <li>CEL 평가 실패 → {@code false}</li>
     * </ul>
     * </p>
     *
     * @param expression CEL 조건식
     * @param variables 변수 바인딩 맵
     * @return 평가 결과 (true: 허용, false: 거부)
     * @throws IllegalArgumentException expression 또는 variables가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean evaluate(String expression, Map<String, Object> variables) {
        if (expression == null) {
            throw new IllegalArgumentException("Expression must not be null");
        }

        if (variables == null) {
            throw new IllegalArgumentException("Variables map must not be null");
        }

        if (expression.isBlank()) {
            log.warn("Expression is blank, denying by default");
            return false;
        }

        try {
            boolean result = celEngine.evaluateBoolean(expression, variables);
            log.debug("Condition evaluation result for '{}': {}", expression, result);
            return result;
        } catch (Exception e) {
            log.error("Unexpected error during condition evaluation: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 표현식 유효성 검증
     *
     * <p>주어진 CEL 표현식의 문법이 올바른지 검증합니다.</p>
     *
     * @param expression 검증할 CEL 표현식
     * @return 유효한 표현식이면 {@code true}, 그렇지 않으면 {@code false}
     * @throws IllegalArgumentException expression이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean validate(String expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Expression must not be null");
        }

        if (expression.isBlank()) {
            return false;
        }

        return celEngine.validateExpression(expression);
    }
}
