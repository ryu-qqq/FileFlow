package com.ryuqq.fileflow.application.iam.usercontext.event;

import java.time.LocalDateTime;

/**
 * Role 할당 이벤트
 *
 * <p>사용자에게 새로운 Role(Membership)이 할당되었을 때 발행되는 도메인 이벤트입니다.</p>
 * <p>이 이벤트는 감사 로그, 알림, 캐시 무효화 등 다양한 목적으로 사용될 수 있습니다.</p>
 *
 * <p><strong>이벤트 발행 시점:</strong></p>
 * <ul>
 *   <li>UserContext에 Membership이 추가된 직후</li>
 *   <li>DB 영속화 완료 후</li>
 *   <li>캐시 무효화 후</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>
 * RoleAssignedEvent event = RoleAssignedEvent.of(
 *     123L,                          // userId
 *     "tenant-1",                    // tenantId
 *     456L,                          // organizationId
 *     MembershipType.EMPLOYEE        // membershipType (Role)
 * );
 * eventPublisher.publishEvent(event);
 * </pre>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용</li>
 *   <li>✅ 불변 객체 (Immutable)</li>
 *   <li>✅ null 검증</li>
 *   <li>❌ Lombok 사용 안함</li>
 * </ul>
 *
 * @param userId 사용자 ID
 * @param tenantId 테넌트 ID
 * @param organizationId 조직 ID
 * @param membershipType Role 타입 (EMPLOYEE, MANAGER, ADMIN 등)
 * @param occurredAt 이벤트 발생 시각
 * @author ryu-qqq
 * @since 2025-10-26
 */
public record RoleAssignedEvent(
    Long userId,
    String tenantId,
    Long organizationId,
    String membershipType,
    LocalDateTime occurredAt
) {

    /**
     * Compact Constructor - null 검증
     *
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public RoleAssignedEvent {
        if (userId == null || userId < 0) {
            throw new IllegalArgumentException("userId는 필수이며 0 이상이어야 합니다");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId는 필수입니다");
        }
        if (organizationId == null || organizationId < 0) {
            throw new IllegalArgumentException("organizationId는 필수이며 0 이상이어야 합니다");
        }
        if (membershipType == null || membershipType.isBlank()) {
            throw new IllegalArgumentException("membershipType은 필수입니다");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt은 필수입니다");
        }
    }

    /**
     * Static Factory Method - 현재 시각으로 이벤트 생성
     *
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param membershipType Role 타입
     * @return RoleAssignedEvent 인스턴스
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public static RoleAssignedEvent of(
        Long userId,
        String tenantId,
        Long organizationId,
        String membershipType
    ) {
        return new RoleAssignedEvent(
            userId,
            tenantId,
            organizationId,
            membershipType,
            LocalDateTime.now()
        );
    }
}
