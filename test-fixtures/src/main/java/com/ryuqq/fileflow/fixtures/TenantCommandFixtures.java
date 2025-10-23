package com.ryuqq.fileflow.fixtures;

import com.ryuqq.fileflow.application.iam.tenant.dto.command.CreateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantStatusCommand;

import java.util.UUID;

/**
 * TenantCommandFixtures - Tenant Command Object Mother Pattern
 *
 * <p>Tenant Command DTO의 테스트 픽스쳐를 생성하는 팩토리 클래스입니다.</p>
 * <p>Object Mother 패턴을 사용하여 테스트에서 필요한 다양한 Command를 제공합니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 CreateTenantCommand
 * CreateTenantCommand command = TenantCommandFixtures.createTenantCommand();
 *
 * // 특정 이름으로 생성
 * CreateTenantCommand command = TenantCommandFixtures.createTenantCommand("My Company");
 *
 * // UpdateTenantCommand
 * UpdateTenantCommand command = TenantCommandFixtures.updateTenantCommand("tenant-id-123", "New Name");
 *
 * // UpdateTenantStatusCommand
 * UpdateTenantStatusCommand command = TenantCommandFixtures.updateTenantStatusCommand("tenant-id-123", "SUSPENDED");
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
public final class TenantCommandFixtures {

    private TenantCommandFixtures() {
        // Utility class - 인스턴스 생성 방지
    }

    /**
     * 기본 CreateTenantCommand를 생성합니다.
     *
     * <p>"Test Company" 이름으로 Tenant 생성 Command를 만듭니다.</p>
     *
     * @return CreateTenantCommand
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static CreateTenantCommand createTenantCommand() {
        return new CreateTenantCommand("Test Company");
    }

    /**
     * 특정 이름을 가진 CreateTenantCommand를 생성합니다.
     *
     * @param name Tenant 이름
     * @return CreateTenantCommand
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static CreateTenantCommand createTenantCommand(String name) {
        return new CreateTenantCommand(name);
    }

    /**
     * 기본 UpdateTenantCommand를 생성합니다.
     *
     * <p>랜덤 UUID를 ID로 사용하고, "Updated Company"를 이름으로 사용합니다.</p>
     *
     * @return UpdateTenantCommand
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static UpdateTenantCommand updateTenantCommand() {
        return new UpdateTenantCommand(
            UUID.randomUUID().toString(),
            "Updated Company"
        );
    }

    /**
     * 특정 ID와 이름을 가진 UpdateTenantCommand를 생성합니다.
     *
     * @param tenantId Tenant ID
     * @param newName 새로운 Tenant 이름
     * @return UpdateTenantCommand
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static UpdateTenantCommand updateTenantCommand(String tenantId, String newName) {
        return new UpdateTenantCommand(tenantId, newName);
    }

    /**
     * 기본 UpdateTenantStatusCommand를 생성합니다 (SUSPENDED 상태로 변경).
     *
     * <p>랜덤 UUID를 ID로 사용하고, SUSPENDED 상태로 변경하는 Command를 생성합니다.</p>
     *
     * @return UpdateTenantStatusCommand (SUSPENDED)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static UpdateTenantStatusCommand updateTenantStatusCommand() {
        return new UpdateTenantStatusCommand(
            UUID.randomUUID().toString(),
            "SUSPENDED"
        );
    }

    /**
     * 특정 ID와 상태를 가진 UpdateTenantStatusCommand를 생성합니다.
     *
     * @param tenantId Tenant ID
     * @param status 변경할 상태 (ACTIVE, SUSPENDED)
     * @return UpdateTenantStatusCommand
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static UpdateTenantStatusCommand updateTenantStatusCommand(String tenantId, String status) {
        return new UpdateTenantStatusCommand(tenantId, status);
    }

    /**
     * ACTIVE 상태로 변경하는 UpdateTenantStatusCommand를 생성합니다.
     *
     * @param tenantId Tenant ID
     * @return UpdateTenantStatusCommand (ACTIVE)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static UpdateTenantStatusCommand activateTenantCommand(String tenantId) {
        return new UpdateTenantStatusCommand(tenantId, "ACTIVE");
    }

    /**
     * SUSPENDED 상태로 변경하는 UpdateTenantStatusCommand를 생성합니다.
     *
     * @param tenantId Tenant ID
     * @return UpdateTenantStatusCommand (SUSPENDED)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static UpdateTenantStatusCommand suspendTenantCommand(String tenantId) {
        return new UpdateTenantStatusCommand(tenantId, "SUSPENDED");
    }
}
