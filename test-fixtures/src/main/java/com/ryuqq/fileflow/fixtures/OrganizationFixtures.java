package com.ryuqq.fileflow.fixtures;

import com.ryuqq.fileflow.domain.iam.organization.OrgCode;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationStatus;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Organization Object Mother Pattern
 *
 * <p>Organization 도메인 객체의 테스트 픽스쳐를 생성하는 팩토리 클래스입니다.</p>
 * <p>Object Mother 패턴을 사용하여 테스트에서 필요한 다양한 Organization 상태를 제공합니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 Sales 조직
 * Organization org = OrganizationFixtures.salesOrganization(1L);
 *
 * // HR 조직
 * Organization org = OrganizationFixtures.hrOrganization(1L);
 *
 * // 커스텀 조직
 * Organization org = OrganizationFixtures.organizationWithCode(1L, "IT", "IT Department");
 *
 * // 삭제된 조직
 * Organization org = OrganizationFixtures.deletedOrganization(1L);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public final class OrganizationFixtures {

    private OrganizationFixtures() {
        // Utility class - 인스턴스 생성 방지
    }

    /**
     * Sales 조직을 생성합니다 (신규, ID 없음).
     *
     * <p>DB에서 자동 증가 ID가 생성되기 전 상태의 Organization입니다.</p>
     *
     * @param tenantId Tenant ID (String - Tenant PK 타입과 일치)
     * @return ID가 null인 신규 Sales 조직 (ACTIVE)
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Organization salesOrganization(Long tenantId) {
        return Organization.of(
            null,  // ID 없음 (DB 저장 시 자동 생성)
            tenantId,
            OrgCode.of("SALES"),
            "Sales Department"
        );
    }

    /**
     * HR 조직을 생성합니다 (신규, ID 없음).
     *
     * @param tenantId Tenant ID
     * @return ID가 null인 신규 HR 조직 (ACTIVE)
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Organization hrOrganization(Long tenantId) {
        return Organization.of(
            null,  // ID 없음 (DB 저장 시 자동 생성)
            tenantId,
            OrgCode.of("HR"),
            "Human Resources"
        );
    }

    /**
     * IT 조직을 생성합니다 (신규, ID 없음).
     *
     * @param tenantId Tenant ID
     * @return ID가 null인 신규 IT 조직 (ACTIVE)
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Organization itOrganization(Long tenantId) {
        return Organization.of(
            null,  // ID 없음 (DB 저장 시 자동 생성)
            tenantId,
            OrgCode.of("IT"),
            "IT Department"
        );
    }

    /**
     * 특정 조직 코드와 이름을 가진 조직을 생성합니다 (신규, ID 없음).
     *
     * @param tenantId Tenant ID
     * @param orgCodeValue 조직 코드 값
     * @param name 조직 이름
     * @return ID가 null인 신규 조직 (ACTIVE)
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Organization organizationWithCode(Long tenantId, String orgCodeValue, String name) {
        return Organization.of(
            null,  // ID 없음 (DB 저장 시 자동 생성)
            tenantId,
            OrgCode.of(orgCodeValue),
            name
        );
    }

    /**
     * ID를 가진 Sales 조직을 생성합니다 (DB 저장 후 상태).
     *
     * @param organizationId Organization ID
     * @param tenantId Tenant ID
     * @return ID를 가진 Sales 조직 (ACTIVE)
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Organization salesOrganizationWithId(Long organizationId, Long tenantId) {
        return Organization.of(
            OrganizationId.of(organizationId),
            tenantId,
            OrgCode.of("SALES"),
            "Sales Department"
        );
    }

    /**
     * ID를 가진 HR 조직을 생성합니다 (DB 저장 후 상태).
     *
     * @param organizationId Organization ID
     * @param tenantId Tenant ID
     * @return ID를 가진 HR 조직 (ACTIVE)
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Organization hrOrganizationWithId(Long organizationId, Long tenantId) {
        return Organization.of(
            OrganizationId.of(organizationId),
            tenantId,
            OrgCode.of("HR"),
            "Human Resources"
        );
    }

    /**
     * 비활성화된 조직을 생성합니다.
     *
     * @param organizationId Organization ID
     * @param tenantId Tenant ID
     * @return INACTIVE 상태의 조직
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Organization inactiveOrganization(Long organizationId, Long tenantId) {
        return Organization.reconstitute(
            OrganizationId.of(organizationId),
            tenantId,
            OrgCode.of("INACTIVE"),
            "Inactive Organization",
            OrganizationStatus.INACTIVE,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now().minusDays(1),
            false
        );
    }

    /**
     * 삭제된 조직을 생성합니다 (Soft Delete).
     *
     * @param tenantId Tenant ID
     * @return 삭제된 상태의 조직 (deleted = true, status = INACTIVE)
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Organization deletedOrganization(Long tenantId) {
        return Organization.reconstitute(
            OrganizationId.of(999L),
            tenantId,
            OrgCode.of("DELETED"),
            "Deleted Organization",
            OrganizationStatus.INACTIVE,
            LocalDateTime.now().minusDays(60),
            LocalDateTime.now().minusDays(30),
            true  // deleted
        );
    }

    /**
     * 특정 조직 코드를 가진 삭제된 조직을 생성합니다.
     *
     * @param organizationId Organization ID
     * @param tenantId Tenant ID
     * @param orgCodeValue 조직 코드 값
     * @return 삭제된 상태의 조직
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Organization deletedOrganizationWithCode(Long organizationId, Long tenantId, String orgCodeValue) {
        return Organization.reconstitute(
            OrganizationId.of(organizationId),
            tenantId,
            OrgCode.of(orgCodeValue),
            "Deleted Organization",
            OrganizationStatus.INACTIVE,
            LocalDateTime.now().minusDays(60),
            LocalDateTime.now().minusDays(30),
            true  // deleted
        );
    }

    /**
     * 테스트용 Clock을 사용하는 Organization을 생성합니다.
     *
     * <p>시간 제어가 필요한 테스트에서 사용합니다.</p>
     * <p>reconstitute를 사용하여 시간이 제어된 Organization을 생성합니다.</p>
     *
     * @param tenantId Tenant ID
     * @param orgCodeValue 조직 코드 값
     * @param name 조직 이름
     * @param clock 시간 제공자
     * @return 생성된 Organization (신규, ID 없음)
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Organization organizationWithClock(Long tenantId, String orgCodeValue, String name, Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        return Organization.reconstitute(
            null,  // ID는 null (DB 저장 시 생성됨)
            tenantId,
            OrgCode.of(orgCodeValue),
            name,
            OrganizationStatus.ACTIVE,
            now,
            now,
            false
        );
    }

    /**
     * 완전히 커스터마이징된 Organization을 생성합니다.
     *
     * <p>모든 필드를 직접 지정할 수 있는 팩토리 메서드입니다.</p>
     *
     * @param organizationId Organization ID
     * @param tenantId Tenant ID
     * @param orgCodeValue 조직 코드 값
     * @param name 조직 이름
     * @param status Organization 상태
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deleted 삭제 여부
     * @return 생성된 Organization
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Organization customOrganization(
        Long organizationId,
        Long tenantId,
        String orgCodeValue,
        String name,
        OrganizationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return Organization.reconstitute(
            OrganizationId.of(organizationId),
            tenantId,
            OrgCode.of(orgCodeValue),
            name,
            status,
            createdAt,
            updatedAt,
            deleted
        );
    }

    /**
     * ID를 가진 ACTIVE 상태의 Organization을 생성합니다.
     *
     * <p>테스트에서 Update/Delete 작업을 테스트할 때 사용합니다.</p>
     *
     * @param organizationId Organization ID
     * @return ID를 가진 Organization (ACTIVE, tenantId: 1L, "ORG-DEFAULT")
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static Organization activeOrganizationWithId(Long organizationId) {
        return Organization.reconstitute(
            OrganizationId.of(organizationId),
            1L,
            OrgCode.of("ORG-DEFAULT"),
            "Default Organization",
            OrganizationStatus.ACTIVE,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusDays(1),
            false
        );
    }

    /**
     * INACTIVE 상태의 Organization을 생성합니다 (인자 없음 버전).
     *
     * <p>테스트에서 비활성화된 Organization이 필요할 때 사용합니다.</p>
     *
     * @return INACTIVE 상태의 Organization (ID: 999L, tenantId: 1L)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static Organization inactiveOrganization() {
        return Organization.reconstitute(
            OrganizationId.of(999L),
            1L,
            OrgCode.of("INACTIVE"),
            "Inactive Organization",
            OrganizationStatus.INACTIVE,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now().minusDays(1),
            false
        );
    }

    /**
     * 특정 ID를 가진 비활성화된 조직을 생성합니다 (WithId 접미사 버전).
     *
     * <p>EntityMapper 테스트에서 사용하는 메서드입니다.</p>
     *
     * @param organizationId Organization ID
     * @param tenantId Tenant ID
     * @return INACTIVE 상태의 조직
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static Organization inactiveOrganizationWithId(Long organizationId, Long tenantId) {
        return inactiveOrganization(organizationId, tenantId);
    }

    /**
     * 특정 ID를 가진 삭제된 조직을 생성합니다 (WithId 접미사 버전).
     *
     * <p>EntityMapper 테스트에서 사용하는 메서드입니다.</p>
     *
     * @param organizationId Organization ID
     * @param tenantId Tenant ID
     * @return 삭제된 상태의 조직
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static Organization deletedOrganizationWithId(Long organizationId, Long tenantId) {
        return Organization.reconstitute(
            OrganizationId.of(organizationId),
            tenantId,
            OrgCode.of("DELETED"),
            "Deleted Organization",
            OrganizationStatus.INACTIVE,
            LocalDateTime.now().minusDays(60),
            LocalDateTime.now().minusDays(30),
            true  // deleted
        );
    }
}
