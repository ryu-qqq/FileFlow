package com.ryuqq.fileflow.e2e.fixture;

import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.CreateOrganizationRequest;

/**
 * OrganizationFixture - Organization 테스트 데이터 생성 유틸리티
 *
 * <p>E2E 테스트에서 사용할 Organization 관련 테스트 데이터를 생성하는 Fixture 클래스입니다.</p>
 *
 * <p><strong>Mother Object 패턴:</strong></p>
 * <ul>
 *   <li>✅ 재사용 가능한 테스트 데이터 생성</li>
 *   <li>✅ 테스트 가독성 향상</li>
 *   <li>✅ 테스트 유지보수성 향상</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
public class OrganizationFixture {

    /**
     * Organization 생성 요청 생성
     *
     * @param tenantId Tenant ID
     * @param orgCode Organization 코드
     * @param name Organization 이름
     * @return CreateOrganizationRequest
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public static CreateOrganizationRequest createRequest(String tenantId, String orgCode, String name) {
        return new CreateOrganizationRequest(tenantId, orgCode, name);
    }

    /**
     * Organization 생성 요청 생성 (이름 자동 생성)
     *
     * @param tenantId Tenant ID
     * @param orgCode Organization 코드
     * @return CreateOrganizationRequest
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public static CreateOrganizationRequest createRequest(String tenantId, String orgCode) {
        return createRequest(tenantId, orgCode, "Org-" + orgCode);
    }

    /**
     * Organization 생성 요청 생성 (orgCode 자동 생성)
     *
     * @param tenantId Tenant ID
     * @return CreateOrganizationRequest
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public static CreateOrganizationRequest createRequest(String tenantId) {
        String orgCode = "ORG" + System.currentTimeMillis();
        return createRequest(tenantId, orgCode);
    }

    /**
     * 여러 Organization 생성 요청 배열 생성
     *
     * @param tenantId Tenant ID
     * @param count 생성할 Organization 개수
     * @return CreateOrganizationRequest 배열
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public static CreateOrganizationRequest[] createRequests(String tenantId, int count) {
        CreateOrganizationRequest[] requests = new CreateOrganizationRequest[count];
        long timestamp = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            String orgCode = "ORG" + timestamp + "-" + i;
            requests[i] = createRequest(tenantId, orgCode);
        }
        return requests;
    }
}
