package com.ryuqq.fileflow.fixtures;

import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantName;
import com.ryuqq.fileflow.domain.iam.tenant.TenantStatus;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tenant Object Mother Pattern
 *
 * <p>Tenant 도메인 객체의 테스트 픽스쳐를 생성하는 팩토리 클래스입니다.</p>
 * <p>Object Mother 패턴을 사용하여 테스트에서 필요한 다양한 Tenant 상태를 제공합니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 활성 Tenant
 * Tenant tenant = TenantFixtures.activeTenant();
 *
 * // 특정 이름을 가진 Tenant
 * Tenant tenant = TenantFixtures.activeTenantWithName("My Company");
 *
 * // 일시 정지된 Tenant
 * Tenant tenant = TenantFixtures.suspendedTenant();
 *
 * // 삭제된 Tenant
 * Tenant tenant = TenantFixtures.deletedTenant();
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public final class TenantFixtures {

    private TenantFixtures() {
        // Utility class - 인스턴스 생성 방지
    }

    /**
     * 기본 활성 Tenant를 생성합니다.
     *
     * <p>랜덤 UUID를 ID로 사용하고, "Test Company"를 이름으로 사용합니다.</p>
     *
     * @return ACTIVE 상태의 Tenant
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Tenant activeTenant() {
        return Tenant.of(
            TenantId.of(UUID.randomUUID().toString()),
            TenantName.of("Test Company")
        );
    }

    /**
     * 특정 이름을 가진 활성 Tenant를 생성합니다.
     *
     * @param name Tenant 이름
     * @return ACTIVE 상태의 Tenant
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Tenant activeTenantWithName(String name) {
        return Tenant.of(
            TenantId.of(UUID.randomUUID().toString()),
            TenantName.of(name)
        );
    }

    /**
     * 특정 ID를 가진 활성 Tenant를 생성합니다.
     *
     * @param tenantIdValue Tenant ID 값
     * @return ACTIVE 상태의 Tenant
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Tenant activeTenantWithId(String tenantIdValue) {
        return Tenant.of(
            TenantId.of(tenantIdValue),
            TenantName.of("Test Company")
        );
    }

    /**
     * 특정 ID와 이름을 가진 활성 Tenant를 생성합니다.
     *
     * @param tenantIdValue Tenant ID 값
     * @param name Tenant 이름
     * @return ACTIVE 상태의 Tenant
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Tenant activeTenantWithIdAndName(String tenantIdValue, String name) {
        return Tenant.of(
            TenantId.of(tenantIdValue),
            TenantName.of(name)
        );
    }

    /**
     * 일시 정지된 Tenant를 생성합니다.
     *
     * @return SUSPENDED 상태의 Tenant
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Tenant suspendedTenant() {
        return Tenant.reconstitute(
            TenantId.of(UUID.randomUUID().toString()),
            TenantName.of("Suspended Company"),
            TenantStatus.SUSPENDED,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now().minusDays(1),
            false
        );
    }

    /**
     * 특정 이름을 가진 일시 정지된 Tenant를 생성합니다.
     *
     * @param name Tenant 이름
     * @return SUSPENDED 상태의 Tenant
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Tenant suspendedTenantWithName(String name) {
        return Tenant.reconstitute(
            TenantId.of(UUID.randomUUID().toString()),
            TenantName.of(name),
            TenantStatus.SUSPENDED,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now().minusDays(1),
            false
        );
    }

    /**
     * 삭제된 Tenant를 생성합니다 (Soft Delete).
     *
     * @return 삭제된 상태의 Tenant (deleted = true, status = SUSPENDED)
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Tenant deletedTenant() {
        return Tenant.reconstitute(
            TenantId.of(UUID.randomUUID().toString()),
            TenantName.of("Deleted Company"),
            TenantStatus.SUSPENDED,
            LocalDateTime.now().minusDays(60),
            LocalDateTime.now().minusDays(30),
            true  // deleted
        );
    }

    /**
     * 특정 이름을 가진 삭제된 Tenant를 생성합니다.
     *
     * @param name Tenant 이름
     * @return 삭제된 상태의 Tenant
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Tenant deletedTenantWithName(String name) {
        return Tenant.reconstitute(
            TenantId.of(UUID.randomUUID().toString()),
            TenantName.of(name),
            TenantStatus.SUSPENDED,
            LocalDateTime.now().minusDays(60),
            LocalDateTime.now().minusDays(30),
            true  // deleted
        );
    }

    /**
     * 테스트용 Clock을 사용하는 Tenant를 생성합니다.
     *
     * <p>시간 제어가 필요한 테스트에서 사용합니다.</p>
     *
     * @param tenantIdValue Tenant ID 값
     * @param name Tenant 이름
     * @param clock 시간 제공자
     * @return 생성된 Tenant
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Tenant tenantWithClock(String tenantIdValue, String name, Clock clock) {
        // Package-private 생성자 접근 (같은 패키지가 아니므로 reconstitute 사용)
        return Tenant.reconstitute(
            TenantId.of(tenantIdValue),
            TenantName.of(name),
            TenantStatus.ACTIVE,
            LocalDateTime.now(clock),
            LocalDateTime.now(clock),
            false
        );
    }

    /**
     * 완전히 커스터마이징된 Tenant를 생성합니다.
     *
     * <p>모든 필드를 직접 지정할 수 있는 팩토리 메서드입니다.</p>
     *
     * @param tenantIdValue Tenant ID 값
     * @param name Tenant 이름
     * @param status Tenant 상태
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deleted 삭제 여부
     * @return 생성된 Tenant
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Tenant customTenant(
        String tenantIdValue,
        String name,
        TenantStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return Tenant.reconstitute(
            TenantId.of(tenantIdValue),
            TenantName.of(name),
            status,
            createdAt,
            updatedAt,
            deleted
        );
    }

    /**
     * 특정 ID를 가진 일시 정지된 Tenant를 생성합니다 (WithId 접미사 버전).
     *
     * <p>EntityMapper 테스트에서 사용하는 메서드입니다.</p>
     *
     * @param tenantIdValue Tenant ID 값
     * @return SUSPENDED 상태의 Tenant
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static Tenant suspendedTenantWithId(String tenantIdValue) {
        return Tenant.reconstitute(
            TenantId.of(tenantIdValue),
            TenantName.of("Suspended Company"),
            TenantStatus.SUSPENDED,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now().minusDays(1),
            false
        );
    }

    /**
     * 특정 ID를 가진 삭제된 Tenant를 생성합니다 (WithId 접미사 버전).
     *
     * <p>EntityMapper 테스트에서 사용하는 메서드입니다.</p>
     *
     * @param tenantIdValue Tenant ID 값
     * @return 삭제된 상태의 Tenant (deleted = true, status = SUSPENDED)
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static Tenant deletedTenantWithId(String tenantIdValue) {
        return Tenant.reconstitute(
            TenantId.of(tenantIdValue),
            TenantName.of("Deleted Company"),
            TenantStatus.SUSPENDED,
            LocalDateTime.now().minusDays(60),
            LocalDateTime.now().minusDays(30),
            true  // deleted
        );
    }
}
