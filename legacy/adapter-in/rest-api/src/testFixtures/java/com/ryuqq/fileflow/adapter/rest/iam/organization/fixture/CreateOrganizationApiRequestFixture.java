package com.ryuqq.fileflow.adapter.rest.iam.organization.fixture;

import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.request.CreateOrganizationApiRequest;

/**
 * CreateOrganizationApiRequest 테스트 Fixture
 *
 * @author Claude Code
 * @since 1.0.0
 * @see CreateOrganizationApiRequest
 */
public class CreateOrganizationApiRequestFixture {

    /**
     * 기본값으로 CreateOrganizationApiRequest 생성
     *
     * @return 기본값을 가진 CreateOrganizationApiRequest
     */
    public static CreateOrganizationApiRequest create() {
        return new CreateOrganizationApiRequest(
            1L,          // tenantId
            "ORG001",    // orgCode
            "Test Organization"  // name
        );
    }

    /**
     * 특정 값으로 CreateOrganizationApiRequest 생성
     *
     * @param tenantId Tenant ID
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @return 지정된 값을 가진 CreateOrganizationApiRequest
     */
    public static CreateOrganizationApiRequest createWith(Long tenantId, String orgCode, String name) {
        return new CreateOrganizationApiRequest(tenantId, orgCode, name);
    }

    /**
     * 특정 tenantId로 CreateOrganizationApiRequest 생성
     *
     * @param tenantId Tenant ID
     * @return 지정된 tenantId를 가진 CreateOrganizationApiRequest
     */
    public static CreateOrganizationApiRequest createWithTenantId(Long tenantId) {
        return new CreateOrganizationApiRequest(
            tenantId,
            "ORG001",
            "Test Organization"
        );
    }

    // Private 생성자
    private CreateOrganizationApiRequestFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
