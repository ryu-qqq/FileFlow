package com.ryuqq.fileflow.application.iam.usercontext.port.in;

import com.ryuqq.fileflow.application.iam.usercontext.dto.command.RevokeRoleCommand;

/**
 * Role 철회 UseCase 인터페이스
 *
 * <p>사용자의 Role(Membership)을 철회하는 비즈니스 유스케이스입니다.</p>
 * <p>Application Layer의 진입점으로, Domain 로직 실행 + 캐시 무효화 + 이벤트 발행을 담당합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>✅ Command DTO 검증 및 Domain 객체 변환</li>
 *   <li>✅ UserContext Aggregate 조회</li>
 *   <li>✅ Domain 로직 실행 (UserContext.revokeMembership)</li>
 *   <li>✅ 영속화 (Repository save)</li>
 *   <li>✅ 캐시 무효화 (GrantsCachePort.invalidateUser)</li>
 *   <li>✅ 도메인 이벤트 발행 (RoleRevokedEvent)</li>
 * </ul>
 *
 * <p><strong>트랜잭션 경계:</strong></p>
 * <ul>
 *   <li>구현체에서 {@code @Transactional} 적용</li>
 *   <li>트랜잭션 내: Domain 로직 + Repository save + 캐시 무효화</li>
 *   <li>트랜잭션 커밋 후: 이벤트 발행 ({@code @TransactionalEventListener})</li>
 * </ul>
 *
 * <p><strong>실행 흐름:</strong></p>
 * <pre>
 * RevokeRoleUseCase
 *   1. Command → Domain 변환 (TenantId, OrganizationId)
 *   2. UserContext 조회 (Repository)
 *   3. userContext.revokeMembership(tenantId, orgId)  ← Domain 로직
 *   4. userContextRepository.save()                   ← 영속화
 *   5. grantsCachePort.invalidateUser()               ← 캐시 무효화 (KAN-262)
 *   6. eventPublisher.publishEvent()                  ← 이벤트 발행
 * </pre>
 *
 * <p><strong>예외 처리:</strong></p>
 * <ul>
 *   <li>{@code IllegalArgumentException}: Command 필드 유효성 검증 실패</li>
 *   <li>{@code IllegalStateException}: UserContext가 삭제됨 또는 Membership 미존재</li>
 *   <li>{@code EntityNotFoundException}: UserContext 미존재</li>
 * </ul>
 *
 * <p><strong>캐시 무효화 전략:</strong></p>
 * <ul>
 *   <li>Membership 변경 시 해당 userId의 모든 Grants 캐시 무효화</li>
 *   <li>패턴: {@code grants:user:{userId}:*}</li>
 *   <li>실패 시 로그만 남기고 서비스는 정상 동작 (Cache Fallback)</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>
 * {@code @Autowired}
 * private RevokeRoleUseCase revokeRoleUseCase;
 *
 * RevokeRoleCommand command = RevokeRoleCommand.of(
 *     123L,        // userId
 *     "tenant-1",  // tenantId
 *     456L         // organizationId
 * );
 *
 * revokeRoleUseCase.execute(command);
 * // → UserContext에서 Membership 제거 + 캐시 무효화 + 이벤트 발행
 * </pre>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
public interface RevokeRoleUseCase {

    /**
     * Role 철회 실행
     *
     * <p>주어진 Command에 따라 사용자의 Role(Membership)을 철회합니다.</p>
     *
     * @param command Role 철회 Command (Not null)
     * @throws IllegalArgumentException Command 필드 유효성 검증 실패
     * @throws IllegalStateException UserContext가 삭제됨 또는 Membership 미존재
     * @throws EntityNotFoundException UserContext 미존재 (userId 불일치)
     * @author ryu-qqq
     * @since 2025-10-26
     */
    void execute(RevokeRoleCommand command);
}
