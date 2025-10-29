package com.ryuqq.fileflow.application.iam.tenant.fixture;

import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantQuery;

/**
 * GetTenantQuery 테스트 Fixture
 *
 * <p>테스트에서 GetTenantQuery 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Claude Code
 * @since 1.0.0
 * @see GetTenantQuery
 */
public class GetTenantQueryFixture {

    /**
     * 기본값으로 GetTenantQuery 생성
     *
     * @return 기본값을 가진 GetTenantQuery (tenantId = 1L)
     */
    public static GetTenantQuery create() {
        return new GetTenantQuery(1L);
    }

    /**
     * 특정 tenantId로 GetTenantQuery 생성
     *
     * @param tenantId Tenant ID
     * @return 지정된 ID를 가진 GetTenantQuery
     */
    public static GetTenantQuery createWith(Long tenantId) {
        return new GetTenantQuery(tenantId);
    }

    // Private 생성자 - Utility 클래스이므로 인스턴스화 방지
    private GetTenantQueryFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
