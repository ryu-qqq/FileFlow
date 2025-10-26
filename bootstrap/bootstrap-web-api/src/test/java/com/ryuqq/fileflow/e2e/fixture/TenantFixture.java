package com.ryuqq.fileflow.e2e.fixture;

import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.CreateTenantRequest;

/**
 * TenantFixture - Tenant 테스트 데이터 생성 유틸리티
 *
 * <p>E2E 테스트에서 사용할 Tenant 관련 테스트 데이터를 생성하는 Fixture 클래스입니다.</p>
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
public class TenantFixture {

    /**
     * 기본 Tenant 생성 요청 생성
     *
     * @param name Tenant 이름
     * @return CreateTenantRequest
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public static CreateTenantRequest createRequest(String name) {
        return new CreateTenantRequest(name);
    }

    /**
     * 기본 Tenant 생성 요청 생성 (이름 자동 생성)
     *
     * @return CreateTenantRequest
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public static CreateTenantRequest createRequest() {
        return createRequest("test-tenant-" + System.currentTimeMillis());
    }

    /**
     * 여러 Tenant 생성 요청 배열 생성
     *
     * @param count 생성할 Tenant 개수
     * @return CreateTenantRequest 배열
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public static CreateTenantRequest[] createRequests(int count) {
        CreateTenantRequest[] requests = new CreateTenantRequest[count];
        for (int i = 0; i < count; i++) {
            requests[i] = createRequest("test-tenant-" + System.currentTimeMillis() + "-" + i);
        }
        return requests;
    }
}
