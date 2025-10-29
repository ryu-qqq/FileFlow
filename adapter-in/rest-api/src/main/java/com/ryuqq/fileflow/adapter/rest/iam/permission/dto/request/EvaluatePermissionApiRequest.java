package com.ryuqq.fileflow.adapter.rest.iam.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Evaluate Permission Request DTO
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
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Jakarta Validation 사용</li>
 *   <li>✅ 불변성 보장 (모든 필드 final)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
public class EvaluatePermissionRequest {

    /**
     * 사용자 ID (필수)
     */
    @NotNull(message = "userId는 필수입니다")
    private final Long userId;

    /**
     * 테넌트 ID (필수)
     */
    @NotNull(message = "tenantId는 필수입니다")
    private final Long tenantId;

    /**
     * 조직 ID (필수)
     */
    @NotNull(message = "organizationId는 필수입니다")
    private final Long organizationId;

    /**
     * 권한 코드 (필수, 예: "file.upload", "file.delete")
     */
    @NotBlank(message = "permissionCode는 필수입니다")
    private final String permissionCode;

    /**
     * Scope 코드 (필수, 예: "SELF", "ORGANIZATION", "TENANT")
     */
    @NotBlank(message = "scope는 필수입니다")
    private final String scope;

    /**
     * Role 코드 (선택, 예: "UPLOADER", "ADMIN")
     */
    private final String roleCode;

    /**
     * 리소스 속성 (선택, ABAC 조건 평가용)
     *
     * <p>예시: {"size_mb": 15.5, "ownerId": 1001, "mimeType": "image/jpeg"}</p>
     */
    private final Map<String, Object> resourceAttributes;

    /**
     * Constructor - JSON 역직렬화 및 빌더 패턴용
     *
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param permissionCode 권한 코드
     * @param scope Scope 코드
     * @param roleCode Role 코드 (선택)
     * @param resourceAttributes 리소스 속성 (선택)
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public EvaluatePermissionRequest(
        Long userId,
        Long tenantId,
        Long organizationId,
        String permissionCode,
        String scope,
        String roleCode,
        Map<String, Object> resourceAttributes
    ) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.organizationId = organizationId;
        this.permissionCode = permissionCode;
        this.scope = scope;
        this.roleCode = roleCode;
        this.resourceAttributes = resourceAttributes;
    }

    /**
     * 사용자 ID를 반환합니다
     *
     * @return 사용자 ID
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 테넌트 ID를 반환합니다
     *
     * @return 테넌트 ID
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public Long getTenantId() {
        return tenantId;
    }

    /**
     * 조직 ID를 반환합니다
     *
     * @return 조직 ID
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public Long getOrganizationId() {
        return organizationId;
    }

    /**
     * 권한 코드를 반환합니다
     *
     * @return 권한 코드
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public String getPermissionCode() {
        return permissionCode;
    }

    /**
     * Scope 코드를 반환합니다
     *
     * @return Scope 코드
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public String getScope() {
        return scope;
    }

    /**
     * Role 코드를 반환합니다
     *
     * @return Role 코드 (null 가능)
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public String getRoleCode() {
        return roleCode;
    }

    /**
     * 리소스 속성을 반환합니다
     *
     * @return 리소스 속성 Map (null 가능)
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public Map<String, Object> getResourceAttributes() {
        return resourceAttributes;
    }

    /**
     * Role 코드가 있는지 확인합니다
     *
     * @return Role 코드가 있으면 true
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public boolean hasRoleCode() {
        return roleCode != null && !roleCode.isBlank();
    }

    /**
     * 리소스 속성이 있는지 확인합니다
     *
     * @return 리소스 속성이 있으면 true
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public boolean hasResourceAttributes() {
        return resourceAttributes != null && !resourceAttributes.isEmpty();
    }

    /**
     * EvaluatePermissionRequest의 문자열 표현을 반환합니다 (디버깅용)
     *
     * @return Request의 읽기 쉬운 문자열 표현
     * @author ryu-qqq
     * @since 2025-10-27
     */
    @Override
    public String toString() {
        return String.format(
            "EvaluatePermissionRequest[userId=%d, tenantId=%d, orgId=%d, permission='%s', scope='%s', role='%s', hasResource=%b]",
            userId, tenantId, organizationId, permissionCode, scope, roleCode, hasResourceAttributes()
        );
    }
}
