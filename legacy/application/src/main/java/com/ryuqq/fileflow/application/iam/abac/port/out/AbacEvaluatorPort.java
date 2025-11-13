package com.ryuqq.fileflow.application.iam.abac.port.out;

import java.util.Map;

/**
 * ABAC (Attribute-Based Access Control) Evaluator Outbound Port
 *
 * <p>ABAC 조건 평가 엔진과의 통신을 위한 Port 인터페이스입니다.
 * Application Layer에서 정의하고 Adapter Layer에서 구현합니다.
 * (Hexagonal Architecture - Dependency Inversion Principle)</p>
 *
 * <p><strong>구현 위치</strong>: {@code adapter-out/abac-cel/adapter/CelAbacAdapter.java}</p>
 * <p><strong>테스트</strong>: Unit Test + Performance Test 필수 (P95 < 10ms)</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public interface AbacEvaluatorPort {

    /**
     * ABAC 조건식 평가
     *
     * <p>주어진 CEL 표현식과 변수 컨텍스트를 사용하여 접근 권한을 평가합니다.
     * 평가 결과가 {@code true}이면 접근 허용, {@code false}이면 거부입니다.</p>
     *
     * <p><strong>변수 바인딩 규칙</strong>:
     * <ul>
     *   <li>{@code ctx.*} - 사용자 컨텍스트 (예: ctx.userId, ctx.orgId)</li>
     *   <li>{@code res.*} - 리소스 속성 (예: res.size_mb, res.ownerId)</li>
     * </ul>
     * </p>
     *
     * <p><strong>표현식 예시</strong>:
     * <ul>
     *   <li>{@code "res.size_mb <= 20"} - 파일 크기 제한</li>
     *   <li>{@code "ctx.orgId == res.orgId"} - 조직 소유권 검증</li>
     *   <li>{@code "ctx.role == 'ADMIN' || res.ownerId == ctx.userId"} - 관리자 또는 소유자</li>
     * </ul>
     * </p>
     *
     * <p><strong>보수적 거부 (Deny by Default)</strong>:
     * <ul>
     *   <li>표현식 평가 실패 시 {@code false} 반환 (접근 거부)</li>
     *   <li>런타임 에러 발생 시 {@code false} 반환</li>
     *   <li>null 또는 invalid 입력 시 {@code false} 반환</li>
     * </ul>
     * </p>
     *
     * <p><strong>성능 요구사항</strong>: P95 Latency < 10ms</p>
     *
     * @param expression CEL 조건식 (예: "res.size_mb <= 20")
     * @param variables 변수 바인딩 맵 (key: 변수명, value: 값)
     * @return 평가 결과 {@code true} (허용) 또는 {@code false} (거부)
     * @throws IllegalArgumentException expression 또는 variables가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    boolean evaluateCondition(String expression, Map<String, Object> variables);

    /**
     * ABAC 조건식 유효성 검증
     *
     * <p>주어진 CEL 표현식의 문법이 올바른지 검증합니다.
     * 런타임 평가 전에 표현식 검증에 사용할 수 있습니다.</p>
     *
     * <p><strong>사용 예</strong>: Policy 생성 시 표현식 사전 검증</p>
     *
     * @param expression 검증할 CEL 표현식
     * @return 유효한 표현식이면 {@code true}, 그렇지 않으면 {@code false}
     * @throws IllegalArgumentException expression이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    boolean validateExpression(String expression);
}
