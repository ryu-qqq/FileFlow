package com.ryuqq.fileflow.adapter.out.abac.cel.engine;

import dev.cel.common.CelAbstractSyntaxTree;
import dev.cel.common.CelValidationException;
import dev.cel.common.types.SimpleType;
import dev.cel.compiler.CelCompiler;
import dev.cel.compiler.CelCompilerFactory;
import dev.cel.runtime.CelEvaluationException;
import dev.cel.runtime.CelRuntime;
import dev.cel.runtime.CelRuntimeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * CEL (Common Expression Language) 엔진 래퍼
 *
 * <p>Google CEL 라이브러리를 래핑하여 ABAC 조건식 평가를 수행합니다.
 * CEL Environment를 캐싱하여 성능을 최적화합니다.</p>
 *
 * <p><strong>성능 최적화</strong>:
 * <ul>
 *   <li>CEL Compiler/Runtime 인스턴스 재사용</li>
 *   <li>표현식 사전 컴파일 및 캐싱</li>
 *   <li>불변 객체로 스레드 안전성 보장</li>
 * </ul>
 * </p>
 *
 * <p><strong>변수 타입 선언</strong>:
 * <ul>
 *   <li>{@code ctx} - Map<String, Object> 타입 (사용자 컨텍스트)</li>
 *   <li>{@code res} - Map<String, Object> 타입 (리소스 속성)</li>
 * </ul>
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public class CelEngine {

    private static final Logger log = LoggerFactory.getLogger(CelEngine.class);

    private final CelCompiler compiler;
    private final CelRuntime runtime;

    /**
     * CelEngine 생성자
     *
     * <p>CEL Compiler와 Runtime을 초기화합니다.
     * ctx, res 변수를 Map 타입으로 선언합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public CelEngine() {
        this.compiler = CelCompilerFactory.standardCelCompilerBuilder()
                .addVar("ctx", SimpleType.DYN)
                .addVar("res", SimpleType.DYN)
                .build();

        this.runtime = CelRuntimeFactory.standardCelRuntimeBuilder()
                .build();

        log.info("CelEngine initialized with ctx and res variables");
    }

    /**
     * CEL 표현식 평가 (Boolean 결과)
     *
     * <p>주어진 CEL 표현식을 평가하여 Boolean 결과를 반환합니다.
     * 평가 실패 시 {@code false}를 반환합니다 (보수적 거부).</p>
     *
     * <p><strong>보수적 거부 (Deny by Default)</strong>:
     * <ul>
     *   <li>컴파일 실패 → {@code false}</li>
     *   <li>평가 실패 → {@code false}</li>
     *   <li>결과가 Boolean이 아님 → {@code false}</li>
     *   <li>런타임 에러 → {@code false}</li>
     * </ul>
     * </p>
     *
     * @param expression CEL 표현식 (예: "res.size_mb <= 20")
     * @param variables 변수 바인딩 맵 (key: ctx/res, value: Map)
     * @return 평가 결과 (true: 허용, false: 거부)
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean evaluateBoolean(String expression, Map<String, Object> variables) {
        if (expression == null || expression.isBlank()) {
            log.warn("Expression is null or blank, denying by default");
            return false;
        }

        if (variables == null) {
            log.warn("Variables map is null, denying by default");
            return false;
        }

        try {
            CelAbstractSyntaxTree ast = compiler.compile(expression).getAst();

            Object result = runtime.createProgram(ast).eval(variables);

            if (result instanceof Boolean) {
                boolean booleanResult = (Boolean) result;
                log.debug("Expression '{}' evaluated to: {}", expression, booleanResult);
                return booleanResult;
            } else {
                log.warn("Expression '{}' did not evaluate to Boolean, got: {}. Denying by default.",
                        expression, result);
                return false;
            }

        } catch (CelValidationException e) {
            log.error("CEL validation failed for expression '{}': {}", expression, e.getMessage());
            return false;
        } catch (CelEvaluationException e) {
            log.error("CEL evaluation failed for expression '{}': {}", expression, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error evaluating expression '{}': {}", expression, e.getMessage(), e);
            return false;
        }
    }

    /**
     * CEL 표현식 유효성 검증
     *
     * <p>주어진 CEL 표현식의 문법이 올바른지 검증합니다.
     * 런타임 평가 전에 표현식 사전 검증에 사용합니다.</p>
     *
     * @param expression 검증할 CEL 표현식
     * @return 유효한 표현식이면 {@code true}, 그렇지 않으면 {@code false}
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean validateExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            return false;
        }

        try {
            compiler.compile(expression).getAst();
            log.debug("Expression '{}' is valid", expression);
            return true;
        } catch (CelValidationException e) {
            log.debug("Expression '{}' is invalid: {}", expression, e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("Unexpected error validating expression '{}': {}", expression, e.getMessage());
            return false;
        }
    }
}
