package com.ryuqq.fileflow.application.iam.tenant.fixture;

import com.ryuqq.fileflow.application.iam.tenant.dto.command.CreateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantStatusCommand;

/**
 * Tenant Command Test Fixture
 *
 * <p>테스트에서 Tenant Command 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class TenantCommandFixture {

    private static final Long DEFAULT_TENANT_ID = 1L;
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private TenantCommandFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_NAME = "Default Tenant";

    // CreateTenantCommand
    public static CreateTenantCommand createCommand() {
        return new CreateTenantCommand(DEFAULT_NAME);
    }

    public static CreateTenantCommand createCommand(String name) {
        return new CreateTenantCommand(name);
    }

    public static CreateTenantCommand createAcmeCorp() {
        return new CreateTenantCommand("Acme Corporation");
    }

    public static CreateTenantCommand createTechStartup() {
        return new CreateTenantCommand("Tech Startup Inc");
    }

    public static CreateTenantCommand createEnterprise() {
        return new CreateTenantCommand("Enterprise Solutions");
    }

    public static java.util.List<CreateTenantCommand> createMultipleCommands(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> new CreateTenantCommand("Tenant " + i))
            .toList();
    }

    // UpdateTenantCommand
    public static UpdateTenantCommand updateCommand() {
        return new UpdateTenantCommand(DEFAULT_TENANT_ID, "Updated Tenant");
    }

    public static UpdateTenantCommand updateCommand(Long tenantId, String name) {
        return new UpdateTenantCommand(tenantId, name);
    }

    // UpdateTenantStatusCommand
    public static UpdateTenantStatusCommand updateStatusCommand() {
        return new UpdateTenantStatusCommand(DEFAULT_TENANT_ID, "ACTIVE");
    }

    public static UpdateTenantStatusCommand updateStatusCommand(Long tenantId, String status) {
        return new UpdateTenantStatusCommand(tenantId, status);
    }

    public static UpdateTenantStatusCommand activateCommand(Long tenantId) {
        return new UpdateTenantStatusCommand(tenantId, "ACTIVE");
    }

    public static UpdateTenantStatusCommand suspendCommand(Long tenantId) {
        return new UpdateTenantStatusCommand(tenantId, "SUSPENDED");
    }
}
