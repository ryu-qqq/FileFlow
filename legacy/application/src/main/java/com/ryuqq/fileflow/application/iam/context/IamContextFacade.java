package com.ryuqq.fileflow.application.iam.context;

import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationQueryRepositoryPort;
import com.ryuqq.fileflow.application.iam.tenant.port.out.TenantQueryRepositoryPort;
import com.ryuqq.fileflow.application.iam.usercontext.port.out.UserContextRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContext;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContextId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * IAM Context Facade
 *
 * <p>Tenant, Organization, UserContext 조회를 통합 관리하는 Facade입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>IAM 컨텍스트 통합 조회 (Tenant, Organization, UserContext)</li>
 *   <li>Null 체크 및 예외 처리 통합</li>
 *   <li>Upload Service에서 중복 코드 제거</li>
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>InitSingleUploadService: Single upload 초기화</li>
 *   <li>InitMultipartUploadService: Multipart upload 초기화</li>
 *   <li>기타 Multi-tenancy 기반 스토리지 작업</li>
 * </ul>
 *
 * <p><strong>트랜잭션:</strong></p>
 * <ul>
 *   <li>조회 전용 작업이므로 readOnly=true</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class IamContextFacade {

    private final TenantQueryRepositoryPort tenantQueryRepositoryPort;
    private final OrganizationQueryRepositoryPort organizationQueryRepositoryPort;
    private final UserContextRepositoryPort userContextRepositoryPort;

    /**
     * 생성자
     *
     * @param tenantQueryRepositoryPort Tenant Query Repository Port
     * @param organizationQueryRepositoryPort Organization Query Repository Port
     * @param userContextRepositoryPort User Context Repository Port
     */
    public IamContextFacade(
        TenantQueryRepositoryPort tenantQueryRepositoryPort,
        OrganizationQueryRepositoryPort organizationQueryRepositoryPort,
        UserContextRepositoryPort userContextRepositoryPort
    ) {
        this.tenantQueryRepositoryPort = tenantQueryRepositoryPort;
        this.organizationQueryRepositoryPort = organizationQueryRepositoryPort;
        this.userContextRepositoryPort = userContextRepositoryPort;
    }

    /**
     * IAM Context 통합 조회
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Tenant 조회 (필수 - 없으면 예외)</li>
     *   <li>Organization 조회 (선택 - organizationId가 null이면 skip)</li>
     *   <li>UserContext 조회 (선택 - userContextId가 null이면 skip)</li>
     *   <li>IamContext DTO 생성 및 반환</li>
     * </ol>
     *
     * <p><strong>예외 처리:</strong></p>
     * <ul>
     *   <li>Tenant가 없으면 IllegalArgumentException</li>
     *   <li>Organization, UserContext는 없으면 null 허용</li>
     * </ul>
     *
     * @param tenantId Tenant ID (필수)
     * @param organizationId Organization ID (Optional, null 가능)
     * @param userContextId UserContext ID (Optional, null 가능)
     * @return IamContext (Tenant + Organization + UserContext)
     * @throws IllegalArgumentException Tenant가 존재하지 않는 경우
     */
    public IamContext loadContext(
        TenantId tenantId,
        Long organizationId,
        Long userContextId
    ) {
        // 1. Tenant 조회 (필수)
        Tenant tenant = tenantQueryRepositoryPort.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Tenant not found: " + tenantId.value()
            ));

        // 2. Organization 조회 (선택)
        Organization organization = null;
        if (organizationId != null) {
            OrganizationId orgId = OrganizationId.of(organizationId);
            organization = organizationQueryRepositoryPort.findById(orgId)
                .orElse(null);
        }

        // 3. UserContext 조회 (선택)
        UserContext userContext = null;
        if (userContextId != null) {
            UserContextId userCtxId = UserContextId.of(userContextId);
            userContext = userContextRepositoryPort.findById(userCtxId)
                .orElse(null);
        }

        // 4. IamContext 생성
        return IamContext.of(tenant, organization, userContext);
    }
}
