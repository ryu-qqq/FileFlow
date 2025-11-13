package com.ryuqq.fileflow.adapter.rest.iam.usercontext.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Role 할당 API 요청
 *
 * <p><strong>Request Body 예시</strong>:</p>
 * <pre>{@code
 * {
 *   "tenantId": 1,
 *   "organizationId": 10,
 *   "membershipType": "ADMIN"
 * }
 * }</pre>
 *
 * <p><strong>Validation 규칙</strong>:</p>
 * <ul>
 *   <li>{@code tenantId}: 필수 (Not null)</li>
 *   <li>{@code organizationId}: 필수 (Not null)</li>
 *   <li>{@code membershipType}: 필수 (Not blank, ADMIN/MEMBER/VIEWER)</li>
 * </ul>
 *
 * @param tenantId Tenant ID
 * @param organizationId Organization ID
 * @param membershipType Membership Type (ADMIN/MEMBER/VIEWER)
 * @author ryu-qqq
 * @since 2025-11-03
 */
public record AssignRoleApiRequest(
    @NotNull(message = "tenantId는 필수입니다")
    Long tenantId,

    @NotNull(message = "organizationId는 필수입니다")
    Long organizationId,

    @NotBlank(message = "membershipType은 필수입니다")
    String membershipType
) {
    /**
     * Compact Constructor - Validation
     *
     * @throws IllegalArgumentException 필드 유효성 검증 실패
     */
    public AssignRoleApiRequest {
        if (tenantId != null && tenantId <= 0) {
            throw new IllegalArgumentException("tenantId는 양수여야 합니다");
        }
        if (organizationId != null && organizationId <= 0) {
            throw new IllegalArgumentException("organizationId는 양수여야 합니다");
        }
    }
}
