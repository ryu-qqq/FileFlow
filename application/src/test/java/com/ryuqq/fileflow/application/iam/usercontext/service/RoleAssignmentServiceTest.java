package com.ryuqq.fileflow.application.iam.usercontext.service;

import com.ryuqq.fileflow.application.iam.permission.port.out.GrantsCachePort;
import com.ryuqq.fileflow.application.iam.usercontext.dto.command.AssignRoleCommand;
import com.ryuqq.fileflow.application.iam.usercontext.event.RoleAssignedEvent;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * RoleAssignmentService Unit Test
 *
 * <p>RoleAssignmentService의 단위 테스트입니다.</p>
 * <p>주요 검증 항목: Domain 로직 실행 + 영속화 + 캐시 무효화 + 이벤트 발행</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ Mockito를 사용한 Port 인터페이스 Mocking</li>
 *   <li>✅ Domain 로직 실행 검증 (UserContext.addMembership 호출)</li>
 *   <li>✅ 캐시 무효화 호출 검증 (GrantsCachePort.invalidateUser)</li>
 *   <li>✅ 이벤트 발행 검증 (ApplicationEventPublisher.publishEvent)</li>
 *   <li>✅ 예외 처리 검증 (UserContext 미존재 등)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleAssignmentService 단위 테스트")
class RoleAssignmentServiceTest {

    @Mock
    private UserContextRepositoryPort userContextRepository;

    @Mock
    private GrantsCachePort grantsCachePort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private Clock clock;

    @InjectMocks
    private RoleAssignmentService roleAssignmentService;

    private UserContext userContext;
    private AssignRoleCommand command;
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

        // Given: UserContext 생성 (Membership 없는 초기 상태)
        userContext = UserContext.of(
            UserContextId.of(123L),
            ExternalUserId.of("test-external-user-123"),
            Email.of("test@example.com")
        );

        // Given: AssignRoleCommand 생성
        command = AssignRoleCommand.of(
            123L,
            1L,  // Long FK 전략: tenant-1 → 1L
            456L,
            "EMPLOYEE"
        );
    }

    /**
     * 성공 시나리오: Role 할당 + 캐시 무효화 + 이벤트 발행
     *
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Test
    @DisplayName("성공: Role 할당 시 Domain 로직 실행 + 캐시 무효화 + 이벤트 발행")
    void shouldAssignRoleAndInvalidateCacheAndPublishEvent() {
        // Given
        when(userContextRepository.findById(UserContextId.of(123L))).thenReturn(Optional.of(userContext));

        // When
        roleAssignmentService.execute(command);

        // Then: UserContext에 Membership 추가됨
        assertThat(userContext.getMemberships()).hasSize(1);
        Membership addedMembership = userContext.getMemberships().iterator().next();
        assertThat(addedMembership.getTenantIdValue()).isEqualTo("tenant-1");
        assertThat(addedMembership.getOrganizationIdValue()).isEqualTo(456L);

        // Then: Repository save 호출됨
        verify(userContextRepository, times(1)).save(userContext);

        // Then: 캐시 무효화 호출됨 (KAN-262 통합)
        verify(grantsCachePort, times(1)).invalidateUser(123L);

        // Then: 이벤트 발행됨
        ArgumentCaptor<RoleAssignedEvent> eventCaptor = ArgumentCaptor.forClass(RoleAssignedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        RoleAssignedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.userId()).isEqualTo(123L);
        assertThat(publishedEvent.tenantId()).isEqualTo("tenant-1");
        assertThat(publishedEvent.organizationId()).isEqualTo(456L);
        assertThat(publishedEvent.membershipType()).isEqualTo("EMPLOYEE");
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
        assertThatThrownBy(() -> roleAssignmentService.execute(command))
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
        roleAssignmentService.execute(command);

        // Then: Domain 로직은 정상 실행됨
        assertThat(userContext.getMemberships()).hasSize(1);

        // Then: Repository save 호출됨
        verify(userContextRepository, times(1)).save(userContext);

        // Then: 이벤트 발행됨 (캐시 실패해도 발행)
        verify(eventPublisher, times(1)).publishEvent(any(RoleAssignedEvent.class));
    }

    /**
     * Domain 검증: 중복 Membership 추가 시 Domain 예외 발생
     *
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Test
    @DisplayName("Domain 검증: 중복 Membership 추가 시 IllegalStateException 발생")
    void shouldThrowExceptionWhenDuplicateMembershipExists() {
        // Given: 이미 Membership 존재
        Membership existingMembership = Membership.of(
            TenantId.of(1L),
            OrganizationId.of(456L),
            MembershipType.EMPLOYEE
        );
        userContext.addMembership(existingMembership);

        when(userContextRepository.findById(UserContextId.of(123L))).thenReturn(Optional.of(userContext));

        // When & Then: Domain 내부에서 중복 검사하여 예외 발생
        assertThatThrownBy(() -> roleAssignmentService.execute(command))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("이미 해당 테넌트와 조직에 멤버십이 존재합니다");

        // Then: 캐시 무효화 호출 안 됨 (트랜잭션 롤백)
        verify(grantsCachePort, never()).invalidateUser(any());

        // Then: 이벤트 발행 안 됨 (트랜잭션 롤백으로 부수효과 없음)
        verify(eventPublisher, never()).publishEvent(any());
    }
}
