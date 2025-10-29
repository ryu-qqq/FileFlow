package com.ryuqq.fileflow.application.iam.usercontext.service;

import com.ryuqq.fileflow.application.iam.permission.port.out.GrantsCachePort;
import com.ryuqq.fileflow.application.iam.usercontext.dto.command.RevokeRoleCommand;
import com.ryuqq.fileflow.application.iam.usercontext.event.RoleRevokedEvent;
import com.ryuqq.fileflow.application.iam.usercontext.port.out.UserContextRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.usercontext.*;
import com.ryuqq.fileflow.domain.iam.usercontext.exception.UserContextNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * RoleRevocationService Unit Test
 *
 * <p>RoleRevocationService의 단위 테스트입니다.</p>
 * <p>주요 검증 항목: Domain 로직 실행 + 영속화 + 캐시 무효화 + 이벤트 발행</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ Mockito를 사용한 Port 인터페이스 Mocking</li>
 *   <li>✅ Domain 로직 실행 검증 (UserContext.revokeMembership 호출)</li>
 *   <li>✅ 캐시 무효화 호출 검증 (GrantsCachePort.invalidateUser)</li>
 *   <li>✅ 이벤트 발행 검증 (ApplicationEventPublisher.publishEvent)</li>
 *   <li>✅ 예외 처리 검증 (UserContext 미존재, Membership 미존재 등)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleRevocationService 단위 테스트")
class RoleRevocationServiceTest {

    @Mock
    private UserContextRepositoryPort userContextRepository;

    @Mock
    private GrantsCachePort grantsCachePort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private Clock clock;

    @InjectMocks
    private RoleRevocationService roleRevocationService;

    private UserContext userContext;
    private RevokeRoleCommand command;
    private LocalDateTime fixedTime;

    /**
     * 테스트 전 공통 설정
     *
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @BeforeEach
    void setUp() {
        // Given: 고정된 시간 설정 (테스트 용이성)
        fixedTime = LocalDateTime.of(2025, 10, 26, 15, 30, 0);
        Instant fixedInstant = fixedTime.atZone(ZoneId.systemDefault()).toInstant();
        lenient().when(clock.instant()).thenReturn(fixedInstant);
        lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        // Given: UserContext 생성 + Membership 추가
        userContext = UserContext.of(
            UserContextId.of(123L),
            ExternalUserId.of("test-external-user-123"),
            Email.of("test@example.com")
        );
        Membership membership = Membership.of(
            TenantId.of("tenant-1"),
            OrganizationId.of(456L),
            MembershipType.EMPLOYEE
        );
        userContext.addMembership(membership);

        // Given: RevokeRoleCommand 생성
        command = RevokeRoleCommand.of(
            123L,
            "tenant-1",
            456L
        );
    }

    /**
     * 성공 시나리오: Role 철회 + 캐시 무효화 + 이벤트 발행
     *
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Test
    @DisplayName("성공: Role 철회 시 Domain 로직 실행 + 캐시 무효화 + 이벤트 발행")
    void shouldRevokeRoleAndInvalidateCacheAndPublishEvent() {
        // Given
        when(userContextRepository.findById(UserContextId.of(123L))).thenReturn(Optional.of(userContext));

        // When
        roleRevocationService.execute(command);

        // Then: UserContext에서 Membership 제거됨
        assertThat(userContext.getMemberships()).isEmpty();

        // Then: Repository save 호출됨
        verify(userContextRepository, times(1)).save(userContext);

        // Then: 캐시 무효화 호출됨 (KAN-262 통합)
        verify(grantsCachePort, times(1)).invalidateUser(123L);

        // Then: 이벤트 발행됨
        ArgumentCaptor<RoleRevokedEvent> eventCaptor = ArgumentCaptor.forClass(RoleRevokedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        RoleRevokedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.userId()).isEqualTo(123L);
        assertThat(publishedEvent.tenantId()).isEqualTo("tenant-1");
        assertThat(publishedEvent.organizationId()).isEqualTo(456L);
        assertThat(publishedEvent.occurredAt()).isEqualTo(fixedTime);
    }

    /**
     * 예외 시나리오: UserContext 미존재
     *
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Test
    @DisplayName("예외: UserContext가 존재하지 않으면 UserContextNotFoundException 발생")
    void shouldThrowExceptionWhenUserContextNotFound() {
        // Given
        when(userContextRepository.findById(UserContextId.of(123L))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roleRevocationService.execute(command))
            .isInstanceOf(UserContextNotFoundException.class)
            .hasMessageContaining("사용자를 찾을 수 없습니다");

        // Then: 캐시 무효화 호출 안 됨
        verify(grantsCachePort, never()).invalidateUser(any());

        // Then: 이벤트 발행 안 됨
        verify(eventPublisher, never()).publishEvent(any());
    }

    /**
     * Cache Fallback: 캐시 무효화 실패 시에도 서비스는 정상 동작
     *
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Test
    @DisplayName("Cache Fallback: 캐시 무효화 실패 시에도 서비스는 정상 동작")
    void shouldContinueServiceEvenWhenCacheInvalidationFails() {
        // Given
        when(userContextRepository.findById(UserContextId.of(123L))).thenReturn(Optional.of(userContext));
        doThrow(new RuntimeException("Redis 연결 실패")).when(grantsCachePort).invalidateUser(123L);

        // When
        roleRevocationService.execute(command);

        // Then: Domain 로직은 정상 실행됨
        assertThat(userContext.getMemberships()).isEmpty();

        // Then: Repository save 호출됨
        verify(userContextRepository, times(1)).save(userContext);

        // Then: 이벤트 발행됨 (캐시 실패해도 발행)
        verify(eventPublisher, times(1)).publishEvent(any(RoleRevokedEvent.class));
    }

    /**
     * Domain 검증: 존재하지 않는 Membership 철회 시 Domain 예외 발생
     *
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Test
    @DisplayName("Domain 검증: 존재하지 않는 Membership 철회 시 IllegalStateException 발생")
    void shouldThrowExceptionWhenMembershipNotExists() {
        // Given: 다른 tenant/org의 Command
        RevokeRoleCommand nonExistentCommand = RevokeRoleCommand.of(
            123L,
            "tenant-2",  // 존재하지 않는 tenant
            999L         // 존재하지 않는 org
        );

        when(userContextRepository.findById(UserContextId.of(123L))).thenReturn(Optional.of(userContext));

        // When & Then: Domain 내부에서 존재 검사하여 예외 발생
        assertThatThrownBy(() -> roleRevocationService.execute(nonExistentCommand))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("해당 테넌트와 조직의 멤버십이 존재하지 않습니다");

        // Then: 캐시 무효화 호출 안 됨 (트랜잭션 롤백)
        verify(grantsCachePort, never()).invalidateUser(any());

        // Then: 이벤트 발행 안 됨 (트랜잭션 롤백으로 부수효과 없음)
        verify(eventPublisher, never()).publishEvent(any());
    }
}
