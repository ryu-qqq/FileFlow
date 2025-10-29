package com.ryuqq.fileflow.application.iam.organization.fixture;

import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationQuery;

/**
 * GetOrganizationQuery 테스트 Fixture
 *
 * <p>테스트에서 GetOrganizationQuery 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Claude Code
 * @since 1.0.0
 * @see GetOrganizationQuery
 */
public class GetOrganizationQueryFixture {

    /**
     * 기본값으로 GetOrganizationQuery 생성
     *
     * @return 기본값을 가진 GetOrganizationQuery (organizationId = 1L)
     */
    public static GetOrganizationQuery create() {
        return new GetOrganizationQuery(1L);
    }

    /**
     * 특정 organizationId로 GetOrganizationQuery 생성
     *
     * @param organizationId Organization ID
     * @return 지정된 ID를 가진 GetOrganizationQuery
     */
    public static GetOrganizationQuery createWith(Long organizationId) {
        return new GetOrganizationQuery(organizationId);
    }

    // Private 생성자 - Utility 클래스이므로 인스턴스화 방지
    private GetOrganizationQueryFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
