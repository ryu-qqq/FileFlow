package com.ryuqq.fileflow.adapter.rest.iam.tenant.fixture;

import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.request.CreateTenantApiRequest;

/**
 * CreateTenantApiRequest 테스트 Fixture
 *
 * @author Claude Code
 * @since 1.0.0
 * @see CreateTenantApiRequest
 */
public class CreateTenantApiRequestFixture {

    /**
     * 기본값으로 CreateTenantApiRequest 생성
     *
     * @return 기본값을 가진 CreateTenantApiRequest
     */
    public static CreateTenantApiRequest create() {
        return new CreateTenantApiRequest("Test Tenant");
    }

    /**
     * 특정 이름으로 CreateTenantApiRequest 생성
     *
     * @param name Tenant 이름
     * @return 지정된 이름을 가진 CreateTenantApiRequest
     */
    public static CreateTenantApiRequest createWith(String name) {
        return new CreateTenantApiRequest(name);
    }

    // Private 생성자
    private CreateTenantApiRequestFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
