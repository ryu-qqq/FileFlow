package com.ryuqq.fileflow.application.iam.organization.fixture;

import com.ryuqq.fileflow.application.iam.organization.dto.command.CreateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationStatusCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.SoftDeleteOrganizationCommand;

/**
 * Organization Command Test Fixture
 *
 * <p>테스트에서 Organization Command 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class OrganizationCommandFixture {

    private static final Long DEFAULT_TENANT_ID = 1L;
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private OrganizationCommandFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final Long DEFAULT_ORG_ID = 1L;
    private static final String DEFAULT_ORG_CODE = "ORG001";
    private static final String DEFAULT_NAME = "Engineering Department";

    // CreateOrganizationCommand
    public static CreateOrganizationCommand createCommand() {
        return new CreateOrganizationCommand(
            DEFAULT_TENANT_ID,
            DEFAULT_ORG_CODE,
            DEFAULT_NAME
        );
    }

    public static CreateOrganizationCommand createCommand(Long tenantId, String orgCode, String name) {
        return new CreateOrganizationCommand(tenantId, orgCode, name);
    }

    public static CreateOrganizationCommand createEngineeringDept() {
        return new CreateOrganizationCommand(
            1L,
            "ENG001",
            "Engineering Department"
        );
    }

    public static CreateOrganizationCommand createSalesDept() {
        return new CreateOrganizationCommand(
            1L,
            "SALES001",
            "Sales Department"
        );
    }

    public static CreateOrganizationCommand createHRDept() {
        return new CreateOrganizationCommand(
            1L,
            "HR001",
            "Human Resources"
        );
    }

    public static java.util.List<CreateOrganizationCommand> createMultipleCommands(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> new CreateOrganizationCommand(
                DEFAULT_TENANT_ID,
                "ORG" + String.format("%03d", i),
                "Department " + i
            ))
            .toList();
    }

    // UpdateOrganizationCommand
    public static UpdateOrganizationCommand updateCommand() {
        return new UpdateOrganizationCommand(
            DEFAULT_ORG_ID,
            "Updated Department"
        );
    }

    public static UpdateOrganizationCommand updateCommand(Long organizationId, String name) {
        return new UpdateOrganizationCommand(organizationId, name);
    }

    // UpdateOrganizationStatusCommand
    public static UpdateOrganizationStatusCommand updateStatusCommand() {
        return new UpdateOrganizationStatusCommand(
            DEFAULT_ORG_ID,
            "ACTIVE"
        );
    }

    public static UpdateOrganizationStatusCommand updateStatusCommand(Long organizationId, String status) {
        return new UpdateOrganizationStatusCommand(organizationId, status);
    }

    public static UpdateOrganizationStatusCommand activateCommand(Long organizationId) {
        return new UpdateOrganizationStatusCommand(organizationId, "ACTIVE");
    }

    public static UpdateOrganizationStatusCommand inactivateCommand(Long organizationId) {
        return new UpdateOrganizationStatusCommand(organizationId, "INACTIVE");
    }

    // SoftDeleteOrganizationCommand
    public static SoftDeleteOrganizationCommand softDeleteCommand() {
        return new SoftDeleteOrganizationCommand(DEFAULT_ORG_ID);
    }

    public static SoftDeleteOrganizationCommand softDeleteCommand(Long organizationId) {
        return new SoftDeleteOrganizationCommand(organizationId);
    }

    // Builder
    public static CreateCommandBuilder createBuilder() {
        return new CreateCommandBuilder();
    }

    public static class CreateCommandBuilder {
        private Long tenantId = DEFAULT_TENANT_ID;
        private String orgCode = DEFAULT_ORG_CODE;
        private String name = DEFAULT_NAME;

        public CreateCommandBuilder tenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public CreateCommandBuilder orgCode(String orgCode) {
            this.orgCode = orgCode;
            return this;
        }

        public CreateCommandBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CreateOrganizationCommand build() {
            return new CreateOrganizationCommand(tenantId, orgCode, name);
        }
    }
}
