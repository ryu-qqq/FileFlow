package com.ryuqq.fileflow.application.iam.tenant.dto.query;

/**
 * GetTenantQuery - Tenant 단건 조회 Query
 *
 * <p>CQRS 패턴의 Query DTO입니다.
 * Tenant ID를 기반으로 단건 조회를 요청합니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * GetTenantQuery query = new GetTenantQuery("tenant-id-123");
 * TenantResponse tenant = getTenantUseCase.execute(query);
 * }</pre>
 *
 * @param tenantId Tenant ID (UUID String)
 * @author ryu-qqq
 * @since 2025-10-23
 */
public record GetTenantQuery(
    String tenantId
) {
    /**
     * Compact Constructor - 검증
     *
     * @throws IllegalArgumentException tenantId가 null이거나 빈 값인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public GetTenantQuery {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId는 필수입니다");
        }
    }

    public static GetTenantQuery of(String tenantId){
        return new GetTenantQuery(tenantId);
    }

}
