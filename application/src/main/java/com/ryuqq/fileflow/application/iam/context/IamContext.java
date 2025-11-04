package com.ryuqq.fileflow.application.iam.context;

import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContext;

/**
 * IAM Context DTO
 *
 * <p>Tenant, Organization, UserContext 3개 Aggregate를 묶어서 전달하는 DTO입니다.</p>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>Upload 초기화 시 IAM 컨텍스트 통합 조회</li>
 *   <li>StorageContext 생성 시 IAM Aggregate 전달</li>
 *   <li>Multi-tenancy 기반 스토리지 정책 적용</li>
 * </ul>
 *
 * <p><strong>불변성 (Immutability):</strong></p>
 * <ul>
 *   <li>Java Record 사용으로 모든 필드 final</li>
 *   <li>생성 후 상태 변경 불가</li>
 * </ul>
 *
 * <p><strong>Null 허용:</strong></p>
 * <ul>
 *   <li>tenant: 필수 (null 불가)</li>
 *   <li>organization: 선택 (null 허용)</li>
 *   <li>userContext: 선택 (null 허용)</li>
 * </ul>
 *
 * @param tenant Tenant Aggregate (필수)
 * @param organization Organization Aggregate (Optional, null 가능)
 * @param userContext UserContext Aggregate (Optional, null 가능)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record IamContext(
    Tenant tenant,
    Organization organization,
    UserContext userContext
) {

    /**
     * Compact Constructor (Validation)
     *
     * @throws IllegalArgumentException tenant가 null인 경우
     */
    public IamContext {
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant는 필수입니다");
        }
    }

    /**
     * Factory Method (Organization, UserContext 없이)
     *
     * @param tenant Tenant Aggregate
     * @return IamContext 인스턴스
     */
    public static IamContext of(Tenant tenant) {
        return new IamContext(tenant, null, null);
    }

    /**
     * Factory Method (Organization 포함, UserContext 없이)
     *
     * @param tenant Tenant Aggregate
     * @param organization Organization Aggregate
     * @return IamContext 인스턴스
     */
    public static IamContext of(Tenant tenant, Organization organization) {
        return new IamContext(tenant, organization, null);
    }

    /**
     * Factory Method (전체 필드)
     *
     * @param tenant Tenant Aggregate
     * @param organization Organization Aggregate
     * @param userContext UserContext Aggregate
     * @return IamContext 인스턴스
     */
    public static IamContext of(
        Tenant tenant,
        Organization organization,
        UserContext userContext
    ) {
        return new IamContext(tenant, organization, userContext);
    }
}
