package com.ryuqq.fileflow.adapter.out.persistence.redis.iam.permission.dto;

import com.ryuqq.fileflow.domain.iam.permission.Grant;
import com.ryuqq.fileflow.domain.iam.permission.Scope;

/**
 * Redis 캐시용 Grant DTO
 *
 * <p>Domain Grant Record를 Redis에 직접 저장하지 않고 별도 DTO로 변환하여 저장합니다.</p>
 * <p>Jackson이 자유롭게 직렬화/역직렬화할 수 있도록 모든 필드를 public으로 노출합니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Jackson 친화적 구조 (Public getter/setter)</li>
 *   <li>✅ Record 직렬화 문제 회피 (Record → DTO 변환)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
public class CachedGrant {

    private String roleCode;
    private String permissionCode;
    private String scope;
    private String conditionExpr;

    /**
     * 기본 생성자 (Jackson 역직렬화용)
     */
    public CachedGrant() {
    }

    /**
     * Domain Grant를 CachedGrant로 변환
     *
     * @param domain Domain Grant Record
     * @return CachedGrant
     */
    public static CachedGrant from(Grant domain) {
        CachedGrant cached = new CachedGrant();
        cached.setRoleCode(domain.roleCode());
        cached.setPermissionCode(domain.permissionCode());
        cached.setScope(domain.scope().name());
        cached.setConditionExpr(domain.conditionExpr());
        return cached;
    }

    /**
     * CachedGrant를 Domain Grant로 변환
     *
     * @return Domain Grant Record
     */
    public Grant toDomain() {
        return new Grant(
            roleCode,
            permissionCode,
            Scope.valueOf(scope),
            conditionExpr
        );
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getConditionExpr() {
        return conditionExpr;
    }

    public void setConditionExpr(String conditionExpr) {
        this.conditionExpr = conditionExpr;
    }
}
