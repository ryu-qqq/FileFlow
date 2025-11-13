package com.ryuqq.fileflow.adapter.rest.iam.organization.fixture;

import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.response.OrganizationApiResponse;

import java.time.LocalDateTime;

/**
 * OrganizationApiResponse 테스트 Fixture
 *
 * @author Claude Code
 * @since 1.0.0
 * @see OrganizationApiResponse
 */
public class OrganizationApiResponseFixture {

    /**
     * 기본값으로 OrganizationApiResponse 생성
     *
     * @return 기본값을 가진 OrganizationApiResponse
     */
    public static OrganizationApiResponse create() {
        LocalDateTime now = LocalDateTime.now();
        return new OrganizationApiResponse(
            1L,              // organizationId
            1L,              // tenantId
            "ORG001",        // orgCode
            "Test Organization",  // name
            false,           // deleted
            now,             // createdAt
            now              // updatedAt
        );
    }

    /**
     * 특정 ID로 OrganizationApiResponse 생성
     *
     * @param organizationId Organization ID
     * @return 지정된 ID를 가진 OrganizationApiResponse
     */
    public static OrganizationApiResponse createWithId(Long organizationId) {
        LocalDateTime now = LocalDateTime.now();
        return new OrganizationApiResponse(
            organizationId,
            1L,
            "ORG001",
            "Test Organization",
            false,
            now,
            now
        );
    }

    /**
     * 특정 값으로 OrganizationApiResponse 생성
     *
     * @param organizationId Organization ID
     * @param tenantId Tenant ID
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @return 지정된 값을 가진 OrganizationApiResponse
     */
    public static OrganizationApiResponse createWith(
        Long organizationId,
        Long tenantId,
        String orgCode,
        String name
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new OrganizationApiResponse(
            organizationId,
            tenantId,
            orgCode,
            name,
            false,
            now,
            now
        );
    }

    // Private 생성자
    private OrganizationApiResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
