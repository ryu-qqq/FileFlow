package com.ryuqq.fileflow.application.iam.permission.port.in;

import com.ryuqq.fileflow.application.iam.permission.dto.command.EvaluatePermissionCommand;
import com.ryuqq.fileflow.application.iam.permission.dto.response.EvaluatePermissionResponse;

/**
 * EvaluatePermissionUseCase - 권한 평가 UseCase 인터페이스
 *
 * <p>Hexagonal Architecture의 Driving Port (Inbound Port)입니다.
 * REST Controller 또는 다른 Application Service에서 이 인터페이스를 의존하여
 * 권한 평가 로직을 실행합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>사용자의 권한 평가 (4단계 파이프라인)</li>
 *   <li>ABAC (Attribute-Based Access Control) 조건 평가</li>
 *   <li>Scope 기반 권한 검증</li>
 * </ul>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>Query UseCase - 데이터 조회 (권한 평가 결과 반환)</li>
 *   <li>구현체: EvaluatePermissionService</li>
 * </ul>
 *
 * <p><strong>4단계 평가 파이프라인:</strong></p>
 * <ol>
 *   <li>Cache Lookup - user:tenant:org 키로 Grants 조회</li>
 *   <li>Permission 필터링 - 요청된 permissionCode와 일치하는 Grant 추출</li>
 *   <li>Scope 매칭 - Grant의 Scope가 요청 Scope를 포함하는지 검증</li>
 *   <li>ABAC 평가 - CEL 조건식 평가 (조건이 있는 경우)</li>
 * </ol>
 *
 * <p><strong>성능 요구사항:</strong></p>
 * <ul>
 *   <li>P95 Latency < 50ms</li>
 *   <li>Cache Hit 시 P95 < 20ms 목표</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * EvaluatePermissionCommand command = EvaluatePermissionCommand.builder()
 *     .userContext(EvaluationContext.withRole(1001L, 10L, 100L, "UPLOADER"))
 *     .permissionCode("file.upload")
 *     .scope(Scope.ORGANIZATION)
 *     .resourceAttributes(ResourceAttributes.builder()
 *         .attribute("size_mb", 15.5)
 *         .build())
 *     .build();
 *
 * EvaluatePermissionResponse response = evaluatePermissionUseCase.execute(command);
 *
 * if (response.allowed()) {
 *     // 권한 허용 - 작업 수행
 * } else {
 *     // 권한 거부 - 403 Forbidden 반환
 *     throw new PermissionDeniedException(...);
 * }
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public interface EvaluatePermissionUseCase {

    /**
     * 권한 평가 실행
     *
     * <p>주어진 사용자 컨텍스트, 권한 코드, Scope, 리소스 속성을 기반으로
     * 4단계 파이프라인을 통해 권한을 평가합니다.</p>
     *
     * <p><strong>평가 프로세스:</strong></p>
     * <ol>
     *   <li>Cache에서 Grants 조회 (user:tenant:org 키)</li>
     *   <li>permissionCode와 일치하는 Grant 필터링</li>
     *   <li>Grant의 Scope가 요청 Scope를 포함하는지 검증</li>
     *   <li>Grant에 조건이 있으면 CEL 평가 수행</li>
     * </ol>
     *
     * <p><strong>보수적 거부 (Deny by Default):</strong></p>
     * <ul>
     *   <li>Grant가 없으면 거부 (NO_GRANT)</li>
     *   <li>Scope 불일치 시 거부 (SCOPE_MISMATCH)</li>
     *   <li>ABAC 조건 불충족 시 거부 (CONDITION_NOT_MET)</li>
     *   <li>ABAC 평가 실패 시 거부 (CONDITION_EVALUATION_FAILED)</li>
     * </ul>
     *
     * <p><strong>성능 최적화:</strong></p>
     * <ul>
     *   <li>Grant 조회는 Cache 우선 (Redis/Caffeine)</li>
     *   <li>조건 없는 Grant는 즉시 허용 (ABAC 평가 생략)</li>
     *   <li>Short-circuit 평가 (첫 번째 일치 시 즉시 반환)</li>
     * </ul>
     *
     * @param command 권한 평가 Command (Not null)
     * @return EvaluatePermissionResponse (허용/거부 결과)
     * @throws IllegalArgumentException command가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    EvaluatePermissionResponse execute(EvaluatePermissionCommand command);
}
