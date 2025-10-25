package com.ryuqq.fileflow.application.iam.permission.service;

import com.ryuqq.fileflow.application.iam.abac.port.out.AbacEvaluatorPort;
import com.ryuqq.fileflow.application.iam.permission.dto.command.EvaluatePermissionCommand;
import com.ryuqq.fileflow.application.iam.permission.dto.context.EvaluationContext;
import com.ryuqq.fileflow.application.iam.permission.dto.context.ResourceAttributes;
import com.ryuqq.fileflow.domain.iam.permission.exception.DenialReason;
import com.ryuqq.fileflow.application.iam.permission.dto.response.EvaluatePermissionResponse;
import com.ryuqq.fileflow.application.iam.permission.port.in.EvaluatePermissionUseCase;
import com.ryuqq.fileflow.application.iam.permission.port.out.GrantRepositoryPort;
import com.ryuqq.fileflow.domain.iam.permission.Grant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Evaluate Permission Service - 권한 평가 서비스
 *
 * <p>{@link EvaluatePermissionUseCase} 인터페이스의 구현체로,
 * 4단계 권한 평가 파이프라인을 수행합니다.</p>
 *
 * <p><strong>4단계 평가 파이프라인:</strong></p>
 * <ol>
 *   <li><strong>Cache Lookup</strong> - user:tenant:org 키로 Grants 조회</li>
 *   <li><strong>Permission 필터링</strong> - 요청된 permissionCode와 일치하는 Grant 추출</li>
 *   <li><strong>Scope 매칭</strong> - Grant의 Scope가 요청 Scope를 포함하는지 검증</li>
 *   <li><strong>ABAC 평가</strong> - CEL 조건식 평가 (조건이 있는 경우)</li>
 * </ol>
 *
 * <p><strong>보수적 거부 (Deny by Default):</strong></p>
 * <ul>
 *   <li>Grant가 없으면 즉시 거부</li>
 *   <li>Scope 불일치 시 즉시 거부</li>
 *   <li>ABAC 조건 불충족 또는 평가 실패 시 거부</li>
 * </ul>
 *
 * <p><strong>성능 최적화:</strong></p>
 * <ul>
 *   <li>Short-circuit 평가 (첫 번째 일치 Grant에서 즉시 반환)</li>
 *   <li>조건 없는 Grant는 ABAC 평가 생략</li>
 *   <li>Cache 기반 Grant 조회 (P95 < 20ms 목표)</li>
 * </ul>
 *
 * <p><strong>트랜잭션 전략:</strong></p>
 * <ul>
 *   <li>@Transactional(readOnly = true) - 읽기 전용 트랜잭션</li>
 *   <li>DB 조회만 수행 (상태 변경 없음)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Service
@Transactional(readOnly = true)
public class EvaluatePermissionService implements EvaluatePermissionUseCase {

    private static final Logger log = LoggerFactory.getLogger(EvaluatePermissionService.class);

    private final GrantRepositoryPort grantRepositoryPort;
    private final AbacEvaluatorPort abacEvaluatorPort;

    /**
     * Constructor - Spring이 의존성 자동 주입
     *
     * @param grantRepositoryPort Grant 조회 Port
     * @param abacEvaluatorPort ABAC 평가 Port
     * @throws IllegalArgumentException Port가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public EvaluatePermissionService(
        GrantRepositoryPort grantRepositoryPort,
        AbacEvaluatorPort abacEvaluatorPort
    ) {
        if (grantRepositoryPort == null) {
            throw new IllegalArgumentException("GrantRepositoryPort must not be null");
        }
        if (abacEvaluatorPort == null) {
            throw new IllegalArgumentException("AbacEvaluatorPort must not be null");
        }

        this.grantRepositoryPort = grantRepositoryPort;
        this.abacEvaluatorPort = abacEvaluatorPort;

        log.info("EvaluatePermissionService initialized");
    }

    /**
     * 권한 평가 실행 (4단계 파이프라인)
     *
     * <p>{@link EvaluatePermissionUseCase#execute(EvaluatePermissionCommand)} 구현</p>
     *
     * @param command 권한 평가 Command
     * @return EvaluatePermissionResponse (허용/거부 결과)
     * @throws IllegalArgumentException command가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public EvaluatePermissionResponse execute(EvaluatePermissionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("EvaluatePermissionCommand must not be null");
        }

        long startTime = System.nanoTime();

        try {
            log.debug("권한 평가 시작 - Command: {}", command);

            // 1단계: Cache Lookup - Grants 조회
            List<Grant> grants = lookupGrantsFromCache(command.userContext());
            log.debug("1단계 완료 - Cache Lookup: {} Grants 조회됨", grants.size());

            if (grants.isEmpty()) {
                return denyNoGrant(command, startTime);
            }

            // 2+3단계: Permission 필터링 + Scope 매칭 (모든 적용 가능한 Grant 수집)
            List<Grant> applicableGrants = grants.stream()
                .filter(grant -> grant.isForPermission(command.permissionCode()))
                .filter(grant -> grant.isApplicableToScope(command.scope()))
                .sorted(java.util.Comparator.comparing((Grant g) -> g.scope().getLevel()).reversed())
                .toList();

            log.debug("2+3단계 완료 - Permission+Scope 매칭: {} 개 Grant 발견", applicableGrants.size());

            if (applicableGrants.isEmpty()) {
                // Permission은 있지만 Scope가 맞지 않는 경우와, Permission 자체가 없는 경우 구분
                Optional<Grant> anyGrantForPermission = grants.stream()
                    .filter(g -> g.isForPermission(command.permissionCode()))
                    .findFirst();

                if (anyGrantForPermission.isPresent()) {
                    return denyScopeMismatch(command, anyGrantForPermission.get(), startTime);
                } else {
                    return denyNoGrant(command, startTime);
                }
            }

            // 4단계: ABAC 평가 (모든 적용 가능한 Grant 평가, 무조건 Grant 우선)
            // 무조건 Grant가 있으면 즉시 허용 (가장 넓은 Scope의 무조건 Grant 우선)
            Optional<Grant> unconditionalGrant = applicableGrants.stream()
                .filter(grant -> !grant.hasCondition())
                .findFirst();

            if (unconditionalGrant.isPresent()) {
                log.debug("4단계 완료 - 무조건 Grant 발견 (즉시 허용)");
                return allow(command, startTime);
            }

            // 조건부 Grant만 있는 경우: 조건 평가 (순서대로, 하나라도 성공하면 허용)
            for (Grant grant : applicableGrants) {
                try {
                    boolean conditionMet = evaluateAbacCondition(
                        grant.conditionExpr(),
                        command.userContext(),
                        command.resourceAttributes()
                    );

                    log.debug("4단계 - ABAC 평가: condition='{}', result={}",
                        grant.conditionExpr(), conditionMet);

                    if (conditionMet) {
                        return allowWithCondition(command, grant, startTime);
                    }
                    // 조건 실패 시 다음 Grant 평가 계속

                } catch (IllegalArgumentException e) {
                    // ABAC 평가 실패 시 다음 Grant 평가 계속
                    log.warn("ABAC 평가 실패 (다음 Grant 계속): condition='{}', error={}",
                        grant.conditionExpr(), e.getMessage());

                    // 마지막 Grant였다면 평가 실패로 거부
                    if (grant == applicableGrants.get(applicableGrants.size() - 1)) {
                        return denyConditionEvaluationFailed(command, grant, startTime, e);
                    }
                }
            }

            // 모든 조건부 Grant가 실패한 경우
            Grant lastGrant = applicableGrants.get(applicableGrants.size() - 1);
            log.debug("4단계 완료 - 모든 조건부 Grant 실패");
            return denyConditionNotMet(command, lastGrant, startTime);

        } catch (Exception e) {
            long durationMillis = (System.nanoTime() - startTime) / 1_000_000;
            log.error("권한 평가 중 에러 발생 ({}ms) - Command: {}, Error: {}",
                durationMillis, command, e.getMessage(), e);

            return EvaluatePermissionResponse.ofDenied(
                DenialReason.SYSTEM_ERROR,
                "권한 평가 중 시스템 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }

    /**
     * 1단계: Cache Lookup - Grants 조회
     *
     * <p>user:tenant:org 키로 사용자의 유효 Grants를 조회합니다.</p>
     *
     * @param context 사용자 컨텍스트
     * @return Grant 리스트 (빈 List 가능)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private List<Grant> lookupGrantsFromCache(EvaluationContext context) {
        long stepStartTime = System.nanoTime();

        List<Grant> grants = grantRepositoryPort.findEffectiveGrants(
            context.userId(),
            context.tenantId(),
            context.organizationId()
        );

        long stepDurationMillis = (System.nanoTime() - stepStartTime) / 1_000_000;
        log.debug("[1단계] Cache Lookup 완료 ({}ms) - key: {}, grants: {}",
            stepDurationMillis, context.toCacheKey(), grants.size());

        if (stepDurationMillis > 30) {
            log.warn("[1단계] Cache Lookup이 30ms를 초과했습니다: {}ms", stepDurationMillis);
        }

        return grants;
    }

    /**
     * 4단계: ABAC 조건 평가
     *
     * <p>CEL 조건식을 평가하여 true/false를 반환합니다.</p>
     *
     * <p><strong>변수 바인딩:</strong></p>
     * <ul>
     *   <li>ctx.* - 사용자 컨텍스트 (userId, tenantId, orgId, roleCode)</li>
     *   <li>res.* - 리소스 속성 (size_mb, ownerId 등)</li>
     * </ul>
     *
     * @param conditionExpr CEL 조건식
     * @param context 사용자 컨텍스트
     * @param resourceAttributes 리소스 속성
     * @return 조건 충족 여부 (true: 충족, false: 불충족)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private boolean evaluateAbacCondition(
        String conditionExpr,
        EvaluationContext context,
        ResourceAttributes resourceAttributes
    ) {
        long stepStartTime = System.nanoTime();

        try {
            // CEL 변수 바인딩
            Map<String, Object> variables = new HashMap<>();

            // ctx.* 변수 (사용자 컨텍스트)
            Map<String, Object> ctxMap = new HashMap<>();
            ctxMap.put("userId", context.userId());
            ctxMap.put("tenantId", context.tenantId());
            ctxMap.put("orgId", context.organizationId());
            if (context.hasRoleCode()) {
                ctxMap.put("roleCode", context.roleCode());
            }
            variables.put("ctx", ctxMap);

            // res.* 변수 (리소스 속성)
            if (!resourceAttributes.isEmpty()) {
                variables.put("res", resourceAttributes.attributes());
            }

            // ABAC 평가 실행
            boolean result = abacEvaluatorPort.evaluateCondition(conditionExpr, variables);

            long stepDurationMillis = (System.nanoTime() - stepStartTime) / 1_000_000;
            log.debug("[4단계] ABAC 평가 완료 ({}ms) - condition: '{}', result: {}",
                stepDurationMillis, conditionExpr, result);

            if (stepDurationMillis > 10) {
                log.warn("[4단계] ABAC 평가가 10ms를 초과했습니다: {}ms, condition: '{}'",
                    stepDurationMillis, conditionExpr);
            }

            return result;

        } catch (Exception e) {
            long stepDurationMillis = (System.nanoTime() - stepStartTime) / 1_000_000;
            log.error("[4단계] ABAC 평가 실패 ({}ms) - condition: '{}', error: {}",
                stepDurationMillis, conditionExpr, e.getMessage(), e);

            // 평가 실패는 CONDITION_EVALUATION_FAILED로 처리하기 위해 예외를 다시 던짐
            throw new IllegalArgumentException("ABAC 조건 평가 실패: " + conditionExpr, e);
        }
    }

    /**
     * 권한 허용 (조건 없음)
     *
     * @param command Command
     * @param startTime 시작 시간
     * @return 허용 Response
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private EvaluatePermissionResponse allow(
        EvaluatePermissionCommand command,
        long startTime
    ) {
        long totalDurationMillis = (System.nanoTime() - startTime) / 1_000_000;
        log.info("권한 허용 ({}ms) - permission: {}, user: {}",
            totalDurationMillis, command.permissionCode(), command.userContext());

        return EvaluatePermissionResponse.ofAllowed();
    }

    /**
     * 권한 허용 (조건 충족)
     *
     * @param command Command
     * @param grant Grant
     * @param startTime 시작 시간
     * @return 허용 Response (조건 포함)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private EvaluatePermissionResponse allowWithCondition(
        EvaluatePermissionCommand command,
        Grant grant,
        long startTime
    ) {
        long totalDurationMillis = (System.nanoTime() - startTime) / 1_000_000;
        log.info("권한 허용 (조건 충족, {}ms) - permission: {}, user: {}, condition: '{}'",
            totalDurationMillis, command.permissionCode(), command.userContext(), grant.conditionExpr());

        return EvaluatePermissionResponse.ofAllowedWithCondition(grant.conditionExpr());
    }

    /**
     * 권한 거부 - Grant 없음
     *
     * @param command Command
     * @param startTime 시작 시간
     * @return 거부 Response
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private EvaluatePermissionResponse denyNoGrant(
        EvaluatePermissionCommand command,
        long startTime
    ) {
        long totalDurationMillis = (System.nanoTime() - startTime) / 1_000_000;
        log.warn("권한 거부 [NO_GRANT] ({}ms) - permission: {}, user: {}",
            totalDurationMillis, command.permissionCode(), command.userContext());

        return EvaluatePermissionResponse.ofDenied(
            DenialReason.NO_GRANT,
            String.format("사용자에게 %s 권한이 부여되지 않았습니다", command.permissionCode())
        );
    }

    /**
     * 권한 거부 - Scope 불일치
     *
     * @param command Command
     * @param grant Grant
     * @param startTime 시작 시간
     * @return 거부 Response
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private EvaluatePermissionResponse denyScopeMismatch(
        EvaluatePermissionCommand command,
        Grant grant,
        long startTime
    ) {
        long totalDurationMillis = (System.nanoTime() - startTime) / 1_000_000;
        log.warn("권한 거부 [SCOPE_MISMATCH] ({}ms) - permission: {}, grantScope: {}, requestedScope: {}",
            totalDurationMillis, command.permissionCode(), grant.scope(), command.scope());

        return EvaluatePermissionResponse.ofDenied(
            DenialReason.SCOPE_MISMATCH,
            String.format(
                "%s 권한 범위(%s)로 %s 범위 작업을 수행할 수 없습니다",
                command.permissionCode(), grant.scope().getCode(), command.scope().getCode()
            )
        );
    }

    /**
     * 권한 거부 - ABAC 조건 불충족
     *
     * @param command Command
     * @param grant Grant
     * @param startTime 시작 시간
     * @return 거부 Response (조건 포함)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private EvaluatePermissionResponse denyConditionNotMet(
        EvaluatePermissionCommand command,
        Grant grant,
        long startTime
    ) {
        long totalDurationMillis = (System.nanoTime() - startTime) / 1_000_000;
        log.warn("권한 거부 [CONDITION_NOT_MET] ({}ms) - permission: {}, condition: '{}'",
            totalDurationMillis, command.permissionCode(), grant.conditionExpr());

        return EvaluatePermissionResponse.ofDeniedByCondition(
            grant.conditionExpr(),
            String.format("권한 조건을 충족하지 않습니다: %s", grant.conditionExpr())
        );
    }

    /**
     * 권한 거부 - ABAC 조건 평가 실패
     *
     * @param command Command
     * @param grant Grant
     * @param startTime 시작 시간
     * @param cause 원인 예외
     * @return 거부 Response
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private EvaluatePermissionResponse denyConditionEvaluationFailed(
        EvaluatePermissionCommand command,
        Grant grant,
        long startTime,
        Exception cause
    ) {
        long totalDurationMillis = (System.nanoTime() - startTime) / 1_000_000;
        log.error("권한 거부 [CONDITION_EVALUATION_FAILED] ({}ms) - permission: {}, condition: '{}', error: {}",
            totalDurationMillis, command.permissionCode(), grant.conditionExpr(), cause.getMessage());

        return EvaluatePermissionResponse.ofDenied(
            DenialReason.CONDITION_EVALUATION_FAILED,
            String.format("ABAC 조건 평가 중 에러가 발생했습니다 - 조건: %s", grant.conditionExpr())
        );
    }
}
