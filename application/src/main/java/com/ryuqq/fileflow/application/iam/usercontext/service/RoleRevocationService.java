package com.ryuqq.fileflow.application.iam.usercontext.service;

import com.ryuqq.fileflow.application.iam.permission.port.out.GrantsCachePort;
import com.ryuqq.fileflow.application.iam.usercontext.dto.RevokeRoleCommand;
import com.ryuqq.fileflow.application.iam.usercontext.event.RoleRevokedEvent;
import com.ryuqq.fileflow.application.iam.usercontext.port.out.UserContextRepositoryPort;
import com.ryuqq.fileflow.application.iam.usercontext.usecase.RevokeRoleUseCase;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContext;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContextId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Role 철회 Service 구현
 *
 * <p>{@link RevokeRoleUseCase}의 구현체로, 사용자의 Role(Membership)을 철회합니다.</p>
 * <p>Domain 로직 실행 + 영속화 + 캐시 무효화 + 이벤트 발행을 하나의 트랜잭션으로 처리합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>✅ Command → Domain 변환 (RevokeRoleCommand → TenantId, OrganizationId)</li>
 *   <li>✅ UserContext Aggregate 조회</li>
 *   <li>✅ Domain 로직 실행 (UserContext.revokeMembership)</li>
 *   <li>✅ 영속화 (UserContextRepositoryPort.save)</li>
 *   <li>✅ 캐시 무효화 (GrantsCachePort.invalidateUser) - KAN-262 통합</li>
 *   <li>✅ 이벤트 발행 (ApplicationEventPublisher.publishEvent)</li>
 * </ul>
 *
 * <p><strong>트랜잭션 전략:</strong></p>
 * <ul>
 *   <li>{@code @Transactional}: 읽기-쓰기 트랜잭션</li>
 *   <li>트랜잭션 내: Domain 로직 + Repository save + 캐시 무효화</li>
 *   <li>트랜잭션 커밋 후: 이벤트 리스너가 감사 로그 기록 (비동기 처리 가능)</li>
 * </ul>
 *
 * <p><strong>캐시 무효화 전략 (KAN-262):</strong></p>
 * <ul>
 *   <li>Membership 변경 시 해당 userId의 모든 Grants 캐시 무효화</li>
 *   <li>패턴: {@code grants:user:{userId}:*}</li>
 *   <li>Redis SCAN 사용 (non-blocking)</li>
 *   <li>실패 시 로그만 남기고 서비스는 정상 동작 (Cache Fallback)</li>
 * </ul>
 *
 * <p><strong>실행 흐름:</strong></p>
 * <pre>
 * 1. Command 검증 (Record compact constructor에서 자동)
 * 2. UserContext 조회 (userId로 검색)
 * 3. Command → Domain 변환
 *    - TenantId.of(command.tenantId())
 *    - OrganizationId.of(command.organizationId())
 * 4. Domain 로직 실행
 *    - userContext.revokeMembership(tenantId, organizationId)
 *    - 존재 검사, 삭제 상태 검사 등 Domain 내부에서 처리
 * 5. 영속화
 *    - userContextRepository.save(userContext)
 * 6. 캐시 무효화
 *    - grantsCachePort.invalidateUser(userId)
 *    - KAN-262에서 구현한 Redis 캐시 무효화
 * 7. 이벤트 발행
 *    - eventPublisher.publishEvent(new RoleRevokedEvent(...))
 *    - 감사 로그, 알림 등 비동기 처리
 * </pre>
 *
 * <p><strong>예외 처리:</strong></p>
 * <ul>
 *   <li>{@code IllegalArgumentException}: Command 필드 null 또는 유효하지 않음</li>
 *   <li>{@code IllegalStateException}: UserContext 삭제됨 또는 Membership 미존재</li>
 *   <li>{@code EntityNotFoundException}: UserContext 미존재 (userId 불일치)</li>
 * </ul>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ {@code @Service} + {@code @Transactional}</li>
 *   <li>✅ Port 인터페이스 의존 (Hexagonal Architecture)</li>
 *   <li>✅ Law of Demeter (Tell, Don't Ask)</li>
 *   <li>✅ Javadoc 필수 (@author, @since)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@Service
public class RoleRevocationService implements RevokeRoleUseCase {

    private static final Logger log = LoggerFactory.getLogger(RoleRevocationService.class);

    private final UserContextRepositoryPort userContextRepository;
    private final GrantsCachePort grantsCachePort;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructor
     *
     * @param userContextRepository UserContext 저장소 Port
     * @param grantsCachePort Grants 캐시 Port (KAN-262에서 구현)
     * @param eventPublisher Spring 이벤트 발행자
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public RoleRevocationService(
        UserContextRepositoryPort userContextRepository,
        GrantsCachePort grantsCachePort,
        ApplicationEventPublisher eventPublisher
    ) {
        this.userContextRepository = userContextRepository;
        this.grantsCachePort = grantsCachePort;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Role 철회 실행
     *
     * <p>주어진 Command에 따라 사용자의 Role(Membership)을 철회합니다.</p>
     * <p>Domain 로직 + 영속화 + 캐시 무효화 + 이벤트 발행을 하나의 트랜잭션으로 처리합니다.</p>
     *
     * @param command Role 철회 Command (Not null)
     * @throws IllegalArgumentException Command 필드 유효성 검증 실패
     * @throws IllegalStateException UserContext가 삭제됨 또는 Membership 미존재
     * @throws EntityNotFoundException UserContext 미존재 (userId 불일치)
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Override
    @Transactional
    public void execute(RevokeRoleCommand command) {
        log.debug("Role 철회 시작: userId={}, tenantId={}, orgId={}",
            command.userId(), command.tenantId(), command.organizationId());

        // 1. UserContext 조회
        UserContext userContext = userContextRepository.findById(UserContextId.of(command.userId()))
            .orElseThrow(() -> new IllegalStateException(
                String.format("사용자를 찾을 수 없습니다: userId=%d", command.userId())
            ));

        // 2. Command → Domain 변환
        TenantId tenantId = TenantId.of(command.tenantId());
        OrganizationId organizationId = OrganizationId.of(command.organizationId());

        // 3. Domain 로직 실행 (기존 UserContext Domain 활용!)
        userContext.revokeMembership(tenantId, organizationId);

        // 4. 영속화
        userContextRepository.save(userContext);

        log.info("Role 철회 성공 (DB 저장 완료): userId={}, tenantId={}, orgId={}",
            command.userId(), command.tenantId(), command.organizationId());

        // 5. 캐시 무효화 (KAN-262 통합)
        try {
            grantsCachePort.invalidateUser(command.userId());
            log.debug("캐시 무효화 성공: userId={}", command.userId());
        } catch (Exception e) {
            // Cache Fallback: 캐시 무효화 실패 시에도 서비스는 정상 동작
            // TTL(5분)로 자동 만료되므로 최대 5분 후 정상화
            log.error("캐시 무효화 실패 (TTL로 자동 만료 예정): userId={}", command.userId(), e);
        }

        // 6. 이벤트 발행 (감사 로그, 알림 등)
        RoleRevokedEvent event = RoleRevokedEvent.of(
            command.userId(),
            command.tenantId(),
            command.organizationId()
        );
        eventPublisher.publishEvent(event);

        log.debug("Role 철회 이벤트 발행 완료: userId={}", command.userId());
    }
}
