package com.ryuqq.fileflow.adapter.rest.iam.permission.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Evaluate Permission Request DTO (Record)
 *
 * <p>Permission 평가 API의 요청 DTO입니다.</p>
 *
 * <p><strong>Endpoint</strong>: GET /api/v1/permissions/evaluate</p>
 *
 * <p><strong>Request Parameters</strong>:</p>
 * <ul>
 *   <li>userId - 사용자 ID (필수)</li>
 *   <li>tenantId - 테넌트 ID (필수)</li>
 *   <li>organizationId - 조직 ID (필수)</li>
 *   <li>permissionCode - 권한 코드 (필수)</li>
 *   <li>scope - Scope 코드 (필수, 예: "SELF", "ORGANIZATION", "TENANT")</li>
 *   <li>roleCode - Role 코드 (선택)</li>
 *   <li>resourceAttributes - 리소스 속성 (선택, JSON 형식)</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>
 * GET /api/v1/permissions/evaluate?userId=1001&tenantId=10&organizationId=100
 *     &permissionCode=file.upload&scope=ORGANIZATION
 *     &resourceAttributes={"size_mb":15.5,"ownerId":1001}
 * </pre>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java Record 사용 - 불변성 보장</li>
 *   <li>✅ Jakarta Validation 사용</li>
 *   <li>✅ 간결한 코드</li>
 * </ul>
 *
 * @param userId 사용자 ID (필수)
 * @param tenantId 테넌트 ID (필수)
 * @param organizationId 조직 ID (필수)
 * @param permissionCode 권한 코드 (필수, 예: "file.upload", "file.delete")
 * @param scope Scope 코드 (필수, 예: "SELF", "ORGANIZATION", "TENANT")
 * @param roleCode Role 코드 (선택, 예: "UPLOADER", "ADMIN")
 * @param resourceAttributes 리소스 속성 (선택, ABAC 조건 평가용)
 * @author ryu-qqq
 * @since 2025-10-29
 */
public record EvaluatePermissionApiRequest(

    @NotNull(message = "userId는 필수입니다")
    Long userId,

    @NotNull(message = "tenantId는 필수입니다")
    Long tenantId,

    @NotNull(message = "organizationId는 필수입니다")
    Long organizationId,

    @NotBlank(message = "permissionCode는 필수입니다")
    String permissionCode,

    @NotBlank(message = "scope는 필수입니다")
    String scope,

    String roleCode,

    Map<String, Object> resourceAttributes
) {


    /**
     * Role 코드가 있는지 확인합니다
     *
     * @return Role 코드가 있으면 true
     */
    public boolean hasRoleCode() {
        return roleCode != null && !roleCode.isBlank();
    }

    /**
     * 리소스 속성이 있는지 확인합니다
     *
     * @return 리소스 속성이 있으면 true
     */
    public boolean hasResourceAttributes() {
        return resourceAttributes != null && !resourceAttributes.isEmpty();
    }
}
