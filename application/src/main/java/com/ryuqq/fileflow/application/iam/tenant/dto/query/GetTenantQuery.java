package com.ryuqq.fileflow.application.iam.tenant.dto.query;

/**
 * GetTenantQuery - Tenant 단건 조회 Query
 *
 * <p>CQRS 패턴의 Query DTO입니다.
 * Tenant ID를 기반으로 단건 조회를 요청합니다.</p>
 *
 * <p><strong>사용 예시 (Option B 변경):</strong></p>
 * <pre>{@code
 * GetTenantQuery query = new GetTenantQuery(123L);
 * TenantResponse tenant = getTenantUseCase.execute(query);
 * }</pre>
 *
 * <p><strong>Option B 변경:</strong></p>
 * <ul>
 *   <li>변경 전: tenantId는 String (UUID)</li>
 *   <li>변경 후: tenantId는 Long (AUTO_INCREMENT)</li>
 *   <li>이유: Settings.contextId (BIGINT)와 타입 일관성 확보</li>
 * </ul>
 *
 * @param tenantId Tenant ID (Long - AUTO_INCREMENT)
 * @author ryu-qqq
 * @since 2025-10-23
 */
public record GetTenantQuery(
    Long tenantId
) {
    /**
     * Compact Constructor - 검증
     *
     * @throws IllegalArgumentException tenantId가 null이거나 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public GetTenantQuery {
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("tenantId는 필수이며 양수여야 합니다");
        }
    }

    public static GetTenantQuery of(Long tenantId){
        return new GetTenantQuery(tenantId);
    }

}
