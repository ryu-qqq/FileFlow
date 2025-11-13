package com.ryuqq.fileflow.adapter.rest.iam.tenant.fixture;

import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.response.TenantApiResponse;

import java.time.LocalDateTime;

/**
 * TenantApiResponse 테스트 Fixture
 *
 * @author Claude Code
 * @since 1.0.0
 * @see TenantApiResponse
 */
public class TenantApiResponseFixture {

    /**
     * 기본값으로 TenantApiResponse 생성
     *
     * @return 기본값을 가진 TenantApiResponse
     */
    public static TenantApiResponse create() {
        LocalDateTime now = LocalDateTime.now();
        return new TenantApiResponse(
            1L,              // tenantId
            "Test Tenant",   // name
            "ACTIVE",        // status
            false,           // deleted
            now,             // createdAt
            now              // updatedAt
        );
    }

    /**
     * 특정 ID로 TenantApiResponse 생성
     *
     * @param tenantId Tenant ID
     * @return 지정된 ID를 가진 TenantApiResponse
     */
    public static TenantApiResponse createWithId(Long tenantId) {
        LocalDateTime now = LocalDateTime.now();
        return new TenantApiResponse(
            tenantId,
            "Test Tenant",
            "ACTIVE",
            false,
            now,
            now
        );
    }

    /**
     * 특정 값으로 TenantApiResponse 생성
     *
     * @param tenantId Tenant ID
     * @param name Tenant 이름
     * @param status Tenant 상태
     * @return 지정된 값을 가진 TenantApiResponse
     */
    public static TenantApiResponse createWith(Long tenantId, String name, String status) {
        LocalDateTime now = LocalDateTime.now();
        return new TenantApiResponse(
            tenantId,
            name,
            status,
            false,
            now,
            now
        );
    }

    // Private 생성자
    private TenantApiResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
