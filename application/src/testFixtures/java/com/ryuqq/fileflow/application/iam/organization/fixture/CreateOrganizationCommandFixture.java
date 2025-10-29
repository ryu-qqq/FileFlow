package com.ryuqq.fileflow.application.iam.organization.fixture;

import com.ryuqq.fileflow.application.iam.organization.dto.command.CreateOrganizationCommand;

/**
 * CreateOrganizationCommand 테스트 Fixture
 *
 * <p>테스트에서 CreateOrganizationCommand 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Claude Code
 * @since 1.0.0
 * @see CreateOrganizationCommand
 */
public class CreateOrganizationCommandFixture {

    /**
     * 기본값으로 CreateOrganizationCommand 생성
     *
     * @return 기본값을 가진 CreateOrganizationCommand
     */
    public static CreateOrganizationCommand create() {
        return new CreateOrganizationCommand(
            1L,          // tenantId
            "ORG001",    // orgCode
            "Test Organization"  // name
        );
    }

    /**
     * 특정 값으로 CreateOrganizationCommand 생성
     *
     * @param tenantId Tenant ID
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @return 지정된 값을 가진 CreateOrganizationCommand
     */
    public static CreateOrganizationCommand createWith(Long tenantId, String orgCode, String name) {
        return new CreateOrganizationCommand(tenantId, orgCode, name);
    }

    /**
     * 특정 tenantId로 CreateOrganizationCommand 생성
     *
     * @param tenantId Tenant ID
     * @return 지정된 tenantId를 가진 CreateOrganizationCommand
     */
    public static CreateOrganizationCommand createWithTenantId(Long tenantId) {
        return new CreateOrganizationCommand(
            tenantId,
            "ORG001",
            "Test Organization"
        );
    }

    /**
     * 특정 이름으로 CreateOrganizationCommand 생성
     *
     * @param name 조직 이름
     * @return 지정된 이름을 가진 CreateOrganizationCommand
     */
    public static CreateOrganizationCommand createWithName(String name) {
        return new CreateOrganizationCommand(
            1L,
            "ORG001",
            name
        );
    }

    // Private 생성자 - Utility 클래스이므로 인스턴스화 방지
    private CreateOrganizationCommandFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
