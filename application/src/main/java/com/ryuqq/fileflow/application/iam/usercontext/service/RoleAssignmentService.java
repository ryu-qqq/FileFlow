package com.ryuqq.fileflow.application.iam.usercontext.service;

import com.ryuqq.fileflow.application.iam.permission.port.out.GrantsCachePort;
import com.ryuqq.fileflow.application.iam.usercontext.dto.command.AssignRoleCommand;
import com.ryuqq.fileflow.application.iam.usercontext.event.RoleAssignedEvent;
import com.ryuqq.fileflow.application.iam.usercontext.port.out.UserContextRepositoryPort;
import com.ryuqq.fileflow.application.iam.usercontext.port.in.AssignRoleUseCase;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.usercontext.Membership;
import com.ryuqq.fileflow.domain.iam.usercontext.MembershipType;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContext;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContextId;
import com.ryuqq.fileflow.domain.iam.usercontext.exception.UserContextNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Role 할당 Service 구현
 *
 * <p>{@link AssignRoleUseCase}의 구현체로, 사용자에게 새로운 Role(Membership)을 할당합니다.</p>
 * <p>Domain 로직 실행 + 영속화 + 캐시 무효화 + 이벤트 발행을 하나의 트랜잭션으로 처리합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>✅ Command → Domain 변환 (AssignRoleCommand → Membership)</li>
 *   <li>✅ UserContext Aggregate 조회</li>
 *   <li>✅ Domain 로직 실행 (UserContext.addMembership)</li>
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
 *    - MembershipType.valueOf(command.membershipType())
 * 4. Domain 로직 실행
 *    - userContext.addMembership(membership)
 *    - 중복 검사, 삭제 상태 검사 등 Domain 내부에서 처리
 * 5. 영속화
 *    - userContextRepository.save(userContext)
 * 6. 캐시 무효화
 *    - grantsCachePort.invalidateUser(userId)
 *    - KAN-262에서 구현한 Redis 캐시 무효화
 * 7. 이벤트 발행
 *    - eventPublisher.publishEvent(new RoleAssignedEvent(...))
 *    - 감사 로그, 알림 등 비동기 처리
 * </pre>
 *
 * <p><strong>예외 처리:</strong></p>
 * <ul>
 *   <li>{@code IllegalArgumentException}: Command 필드 null 또는 유효하지 않음</li>
 *   <li>{@code IllegalStateException}: UserContext 삭제됨 또는 중복 Membership</li>
 *   <li>{@code UserContextNotFoundException}: UserContext 미존재 (userId 불일치)</li>
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
public class RoleAssignmentService implements AssignRoleUseCase {

    private static final Logger log = LoggerFactory.getLogger(RoleAssignmentService.class);

    private final UserContextRepositoryPort userContextRepository;
    private final GrantsCachePort grantsCachePort;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    /**
     * Constructor
     *
     * @param userContextRepository UserContext 저장소 Port
     * @param grantsCachePort Grants 캐시 Port (KAN-262에서 구현)
     * @param eventPublisher Spring 이벤트 발행자
     * @param clock 시간 제어를 위한 Clock (테스트 용이성)
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public RoleAssignmentService(
        UserContextRepositoryPort userContextRepository,
        GrantsCachePort grantsCachePort,
        ApplicationEventPublisher eventPublisher,
        Clock clock
    ) {
        this.userContextRepository = userContextRepository;
        this.grantsCachePort = grantsCachePort;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    /**
     * Role 할당 실행
     *
     * <p>주어진 Command에 따라 사용자에게 새로운 Role(Membership)을 할당합니다.</p>
     * <p>Domain 로직 + 영속화 + 캐시 무효화 + 이벤트 발행을 하나의 트랜잭션으로 처리합니다.</p>
     *
     * @param command Role 할당 Command (Not null)
     * @throws IllegalArgumentException Command 필드 유효성 검증 실패
     * @throws IllegalStateException UserContext가 삭제됨 또는 중복 Membership 존재
     * @throws UserContextNotFoundException UserContext 미존재 (userId 불일치)
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Override
    @Transactional
    public void execute(AssignRoleCommand command) {
        log.debug("Role 할당 시작: userId={}, tenantId={}, orgId={}, type={}",
            command.userId(), command.tenantId(), command.organizationId(), command.membershipType());

        // 1. UserContext 조회
        UserContext userContext = userContextRepository.findById(UserContextId.of(command.userId()))
            .orElseThrow(() -> UserContextNotFoundException.withUserId(command.userId()));

        // 2. Command → Domain 변환
        Membership membership = Membership.of(
            TenantId.of(command.tenantId()),
            OrganizationId.of(command.organizationId()),
            MembershipType.valueOf(command.membershipType())
        );

        // 3. Domain 로직 실행 (기존 UserContext Domain 활용!)
        userContext.addMembership(membership);

        // 4. 영속화
        userContextRepository.save(userContext);

        log.info("Role 할당 성공 (DB 저장 완료): userId={}, tenantId={}, orgId={}, type={}",
            command.userId(), command.tenantId(), command.organizationId(), command.membershipType());

        // 5. 캐시 무효화 (KAN-262 통합)
        try {
            grantsCachePort.invalidateUser(command.userId());
            log.debug("캐시 무효화 성공: userId={}", command.userId());
        } catch (RuntimeException e) {
            // Cache Fallback: 캐시 무효화 실패 시에도 서비스는 정상 동작
            // TTL(5분)로 자동 만료되므로 최대 5분 후 정상화
            // RuntimeException으로 제한하여 예상치 못한 Checked Exception은 전파
            log.error("캐시 무효화 실패 (TTL로 자동 만료 예정): userId={}", command.userId(), e);
        }

        // 6. 이벤트 발행 (감사 로그, 알림 등)
        RoleAssignedEvent event = new RoleAssignedEvent(
            command.userId(),
            command.tenantId(),
            command.organizationId(),
            command.membershipType(),
            LocalDateTime.now(clock)
        );
        eventPublisher.publishEvent(event);

        log.debug("Role 할당 이벤트 발행 완료: userId={}", command.userId());
    }
}
