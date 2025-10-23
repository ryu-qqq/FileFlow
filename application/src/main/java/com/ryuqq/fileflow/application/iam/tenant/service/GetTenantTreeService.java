package com.ryuqq.fileflow.application.iam.tenant.service;

import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationQueryRepositoryPort;
import com.ryuqq.fileflow.application.iam.tenant.assembler.TenantAssembler;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantTreeQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantTreeResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.in.GetTenantTreeUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.out.TenantQueryRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.exception.TenantNotFoundException;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * GetTenantTreeService - Tenant 트리 조회 Service
 *
 * <p>Tenant와 하위 Organization 목록을 트리 구조로 조회하는 UseCase 구현체입니다.</p>
 *
 * <p><strong>실행 흐름:</strong></p>
 * <ol>
 *   <li>Tenant 조회 (존재하지 않으면 IllegalStateException)</li>
 *   <li>해당 Tenant의 Organization 목록 조회</li>
 *   <li>TenantTreeResponse로 조합하여 반환</li>
 * </ol>
 *
 * <p><strong>Application Service 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ {@code @Transactional(readOnly = true)} 적용 (조회 최적화)</li>
 *   <li>✅ UseCase 인터페이스 구현</li>
 *   <li>✅ Assembler를 통한 DTO 변환</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Service
@Transactional(readOnly = true)
public class GetTenantTreeService implements GetTenantTreeUseCase {

    private final TenantQueryRepositoryPort tenantQueryRepositoryPort;
    private final OrganizationQueryRepositoryPort organizationQueryRepositoryPort;
    private final int maxOrganizationsPerTree;

    /**
     * Constructor - 의존성 주입
     *
     * <p>Spring의 Constructor Injection 사용 (Field Injection 금지)</p>
     *
     * @param tenantQueryRepositoryPort Tenant 조회 Repository Port
     * @param organizationQueryRepositoryPort Organization 조회 Repository Port
     * @param maxOrganizationsPerTree Tenant 트리에서 조회할 최대 Organization 개수 (application.yml 설정)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public GetTenantTreeService(
        TenantQueryRepositoryPort tenantQueryRepositoryPort,
        OrganizationQueryRepositoryPort organizationQueryRepositoryPort,
        @Value("${application.tenant.max-organizations-per-tree:1000}") int maxOrganizationsPerTree
    ) {
        this.tenantQueryRepositoryPort = tenantQueryRepositoryPort;
        this.organizationQueryRepositoryPort = organizationQueryRepositoryPort;
        this.maxOrganizationsPerTree = maxOrganizationsPerTree;
    }

    /**
     * Tenant 트리 조회 실행
     *
     * <p>Tenant와 하위 Organization 목록을 트리 구조로 반환합니다.</p>
     *
     * <p><strong>실행 단계:</strong></p>
     * <ol>
     *   <li>Tenant 조회 (존재하지 않으면 TenantNotFoundException)</li>
     *   <li>해당 Tenant의 Organization 목록 조회</li>
     *   <li>TenantTreeResponse로 조합하여 반환</li>
     * </ol>
     *
     * @param query Tenant 트리 조회 Query
     * @return TenantTreeResponse
     * @throws IllegalArgumentException query가 null인 경우
     * @throws TenantNotFoundException Tenant가 존재하지 않는 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public TenantTreeResponse execute(GetTenantTreeQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("GetTenantTreeQuery는 필수입니다");
        }

        // 1. Tenant 조회 (존재하지 않으면 Domain Exception 발생)
        TenantId tenantIdVo = TenantId.of(query.tenantId());

        Tenant tenant = tenantQueryRepositoryPort.findById(tenantIdVo)
            .orElseThrow(() -> new TenantNotFoundException(query.tenantId()));

        // 2. Tenant에 속한 Organization 목록 조회
        String tenantId = tenant.getIdValue();
        Boolean deletedFilter = query.includeDeleted() ? null : false;

        List<Organization> organizations = organizationQueryRepositoryPort.findAllWithOffset(
            tenantId,                    // tenantId filter (String - Tenant PK 타입과 일치)
            null,                        // orgCodeContains (전체 조회)
            null,                        // nameContains (전체 조회)
            deletedFilter,               // deleted (null = 전체, false = 활성만)
            0,                           // offset (처음부터)
            maxOrganizationsPerTree      // limit (application.yml에서 설정, 기본값 1000)
        );

        // 3. Domain → DTO 변환 (Assembler 책임)
        return TenantAssembler.toTreeResponse(tenant, organizations);
    }

}
