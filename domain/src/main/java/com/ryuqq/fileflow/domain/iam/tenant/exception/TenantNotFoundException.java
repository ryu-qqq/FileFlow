package com.ryuqq.fileflow.domain.iam.tenant.exception;

import com.ryuqq.fileflow.domain.shared.exception.BusinessException;

/**
 * TenantNotFoundException - Tenant를 찾을 수 없을 때 발생하는 예외
 *
 * <p>Tenant 조회 시 해당 ID의 Tenant가 존재하지 않을 때 발생합니다.</p>
 *
 * <p><strong>HTTP 응답:</strong></p>
 * <ul>
 *   <li>Status Code: 404 NOT FOUND</li>
 *   <li>Error Code: TENANT-001</li>
 *   <li>Message: "Tenant not found: {tenantId}"</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * Tenant tenant = tenantRepository.findById(tenantId)
 *     .orElseThrow(() -> new TenantNotFoundException(tenantId.value()));
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
public class TenantNotFoundException extends BusinessException {

    /**
     * Constructor - Tenant ID를 포함한 예외 생성
     *
     * <p>에러 메시지에 찾지 못한 Tenant ID를 포함시킵니다.</p>
     *
     * @param tenantId 찾지 못한 Tenant ID
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public TenantNotFoundException(String tenantId) {
        super(
            TenantErrorCode.TENANT_NOT_FOUND,
            "Tenant not found: " + tenantId
        );
    }

    /**
     * Constructor - 기본 에러 메시지 사용
     *
     * <p>TenantErrorCode의 기본 메시지를 사용합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public TenantNotFoundException() {
        super(TenantErrorCode.TENANT_NOT_FOUND);
    }
}
