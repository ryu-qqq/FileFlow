package com.ryuqq.fileflow.fixtures;

import com.ryuqq.fileflow.application.iam.organization.dto.command.CreateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.SoftDeleteOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationStatusCommand;

import java.util.UUID;

/**
 * OrganizationCommandFixtures - Organization Command Object Mother Pattern
 *
 * <p>Organization Command DTO의 테스트 픽스쳐를 생성하는 팩토리 클래스입니다.</p>
 * <p>Object Mother 패턴을 사용하여 테스트에서 필요한 다양한 Command를 제공합니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 CreateOrganizationCommand
 * CreateOrganizationCommand command = OrganizationCommandFixtures.createOrganizationCommand("tenant-id");
 *
 * // Sales 조직 생성 Command
 * CreateOrganizationCommand command = OrganizationCommandFixtures.createSalesOrganizationCommand("tenant-id");
 *
 * // UpdateOrganizationCommand
 * UpdateOrganizationCommand command = OrganizationCommandFixtures.updateOrganizationCommand(1L, "New Name");
 *
 * // UpdateOrganizationStatusCommand
 * UpdateOrganizationStatusCommand command = OrganizationCommandFixtures.inactivateOrganizationCommand(1L);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
public final class OrganizationCommandFixtures {

    private OrganizationCommandFixtures() {
        // Utility class - 인스턴스 생성 방지
    }

    /**
     * 기본 CreateOrganizationCommand를 생성합니다.
     *
     * <p>"ORG-DEFAULT" 조직 코드와 "Default Organization" 이름으로 생성합니다.</p>
     *
     * @param tenantId Tenant ID (String - Tenant PK 타입과 일치)
     * @return CreateOrganizationCommand
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static CreateOrganizationCommand createOrganizationCommand(String tenantId) {
        return new CreateOrganizationCommand(tenantId, "ORG-DEFAULT", "Default Organization");
    }

    /**
     * 특정 조직 코드와 이름을 가진 CreateOrganizationCommand를 생성합니다.
     *
     * @param tenantId Tenant ID
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @return CreateOrganizationCommand
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static CreateOrganizationCommand createOrganizationCommand(String tenantId, String orgCode, String name) {
        return new CreateOrganizationCommand(tenantId, orgCode, name);
    }

    /**
     * Sales 조직을 생성하는 CreateOrganizationCommand를 생성합니다.
     *
     * @param tenantId Tenant ID
     * @return CreateOrganizationCommand (SALES, "Sales Department")
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static CreateOrganizationCommand createSalesOrganizationCommand(String tenantId) {
        return new CreateOrganizationCommand(tenantId, "SALES", "Sales Department");
    }

    /**
     * HR 조직을 생성하는 CreateOrganizationCommand를 생성합니다.
     *
     * @param tenantId Tenant ID
     * @return CreateOrganizationCommand (HR, "Human Resources")
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static CreateOrganizationCommand createHrOrganizationCommand(String tenantId) {
        return new CreateOrganizationCommand(tenantId, "HR", "Human Resources");
    }

    /**
     * IT 조직을 생성하는 CreateOrganizationCommand를 생성합니다.
     *
     * @param tenantId Tenant ID
     * @return CreateOrganizationCommand (IT, "IT Department")
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static CreateOrganizationCommand createItOrganizationCommand(String tenantId) {
        return new CreateOrganizationCommand(tenantId, "IT", "IT Department");
    }

    /**
     * 기본 UpdateOrganizationCommand를 생성합니다.
     *
     * <p>ID 1L과 "Updated Organization" 이름으로 생성합니다.</p>
     *
     * @return UpdateOrganizationCommand
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static UpdateOrganizationCommand updateOrganizationCommand() {
        return new UpdateOrganizationCommand(1L, "Updated Organization");
    }

    /**
     * 특정 ID와 이름을 가진 UpdateOrganizationCommand를 생성합니다.
     *
     * @param organizationId Organization ID
     * @param newName 새로운 조직 이름
     * @return UpdateOrganizationCommand
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static UpdateOrganizationCommand updateOrganizationCommand(Long organizationId, String newName) {
        return new UpdateOrganizationCommand(organizationId, newName);
    }

    /**
     * 기본 UpdateOrganizationStatusCommand를 생성합니다 (INACTIVE 상태로 변경).
     *
     * <p>ID 1L과 INACTIVE 상태로 변경하는 Command를 생성합니다.</p>
     *
     * @return UpdateOrganizationStatusCommand (INACTIVE)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static UpdateOrganizationStatusCommand updateOrganizationStatusCommand() {
        return new UpdateOrganizationStatusCommand(1L, "INACTIVE");
    }

    /**
     * 특정 ID와 상태를 가진 UpdateOrganizationStatusCommand를 생성합니다.
     *
     * @param organizationId Organization ID
     * @param status 변경할 상태 (ACTIVE, INACTIVE)
     * @return UpdateOrganizationStatusCommand
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static UpdateOrganizationStatusCommand updateOrganizationStatusCommand(Long organizationId, String status) {
        return new UpdateOrganizationStatusCommand(organizationId, status);
    }

    /**
     * INACTIVE 상태로 변경하는 UpdateOrganizationStatusCommand를 생성합니다.
     *
     * @param organizationId Organization ID
     * @return UpdateOrganizationStatusCommand (INACTIVE)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static UpdateOrganizationStatusCommand inactivateOrganizationCommand(Long organizationId) {
        return new UpdateOrganizationStatusCommand(organizationId, "INACTIVE");
    }

    /**
     * 기본 SoftDeleteOrganizationCommand를 생성합니다.
     *
     * <p>ID 1L로 삭제 Command를 생성합니다.</p>
     *
     * @return SoftDeleteOrganizationCommand
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static SoftDeleteOrganizationCommand softDeleteOrganizationCommand() {
        return new SoftDeleteOrganizationCommand(1L);
    }

    /**
     * 특정 ID를 가진 SoftDeleteOrganizationCommand를 생성합니다.
     *
     * @param organizationId Organization ID
     * @return SoftDeleteOrganizationCommand
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static SoftDeleteOrganizationCommand softDeleteOrganizationCommand(Long organizationId) {
        return new SoftDeleteOrganizationCommand(organizationId);
    }
}
