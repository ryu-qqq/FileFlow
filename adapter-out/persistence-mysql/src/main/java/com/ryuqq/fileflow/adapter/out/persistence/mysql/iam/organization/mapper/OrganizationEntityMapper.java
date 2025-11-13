package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.organization.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.organization.entity.OrganizationJpaEntity;
import com.ryuqq.fileflow.domain.iam.organization.OrgCode;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

/**
 * Organization Entity Mapper
 *
 * <p><strong>역할</strong>: Domain Model {@code Organization} ↔ JPA Entity {@code OrganizationJpaEntity} 상호 변환</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/organization/mapper/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ 상태 없는(Stateless) 유틸리티 클래스</li>
 *   <li>✅ {@code toDomain()}: Entity → Domain 변환</li>
 *   <li>✅ {@code toEntity()}: Domain → Entity 변환</li>
 *   <li>✅ Value Object 변환 포함 (OrganizationId, OrgCode, TenantId, OrganizationStatus)</li>
 *   <li>✅ Long FK 전략 + Value Object 래핑 (tenantId는 TenantId로 래핑하여 Type Safety 보장)</li>
 *   <li>❌ Lombok 금지 (Pure Java)</li>
 *   <li>❌ 비즈니스 로직 금지 (단순 변환만)</li>
 * </ul>
 *
 * @since 1.0.0
 */
public final class OrganizationEntityMapper {

    /**
     * Private 생성자 - 인스턴스화 방지
     */
    private OrganizationEntityMapper() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * JPA Entity → Domain Model 변환
     *
     * <p>DB에서 조회한 {@code OrganizationJpaEntity}를 Domain {@code Organization}으로 변환합니다.</p>
     *
     * <h4>변환 과정</h4>
     * <ol>
     *   <li>Value Object 생성: {@code OrganizationId}, {@code OrgCode}, {@code TenantId}</li>
     *   <li>Domain Enum 그대로 사용: {@code OrganizationStatus}</li>
     *   <li>Long tenantId를 TenantId로 래핑 (Long FK 전략 + Type Safety)</li>
     *   <li>Domain Aggregate 재구성</li>
     * </ol>
     *
     * @param entity JPA Entity
     * @return Domain Organization
     * @throws IllegalArgumentException entity가 null인 경우
     */
    public static Organization toDomain(OrganizationJpaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("OrganizationJpaEntity must not be null");
        }

        // Value Object 변환 (Static Factory Method 사용)
        OrganizationId organizationId = OrganizationId.of(entity.getId());
        OrgCode orgCode = OrgCode.of(entity.getOrgCode());
        TenantId tenantId = TenantId.of(entity.getTenantId());

        // Domain Aggregate 재구성 (Status 그대로 사용)
        return Organization.reconstitute(
            organizationId,
            tenantId,           // TenantId Value Object (Long FK 전략 + Type Safety)
            orgCode,
            entity.getName(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.isDeleted()
        );
    }

    /**
     * Domain Model → JPA Entity 변환
     *
     * <p>Domain {@code Organization}을 JPA {@code OrganizationJpaEntity}로 변환합니다.</p>
     *
     * <h4>변환 과정</h4>
     * <ol>
     *   <li>Value Object 원시 타입 추출: {@code id.value()}, {@code orgCode.value()}, {@code tenantId.value()}</li>
     *   <li>Domain Enum 그대로 사용: {@code OrganizationStatus}</li>
     *   <li>TenantId → Long 변환 (Long FK 전략 - Tenant PK 타입과 일치)</li>
     *   <li>JPA Entity 생성 (reconstitute 또는 create)</li>
     * </ol>
     *
     * @param organization Domain Organization
     * @return JPA Entity
     * @throws IllegalArgumentException organization이 null인 경우
     */
    public static OrganizationJpaEntity toEntity(Organization organization) {
        if (organization == null) {
            throw new IllegalArgumentException("Organization must not be null");
        }

        // Value Object → 원시 타입 (Law of Demeter 준수)
        Long id = organization.getId() != null ? organization.getIdValue() : null;
        Long tenantId = organization.getTenantIdValue();  // TenantId.value() (Law of Demeter)
        String orgCode = organization.getOrgCodeValue();
        String name = organization.getName();

        // 신규 Organization인 경우 (ID가 아직 없음)
        if (id == null) {
            return OrganizationJpaEntity.create(
                tenantId,
                orgCode,
                name,
                organization.getCreatedAt()
            );
        }

        // 기존 Organization인 경우 (재구성) - Status 그대로 전달
        return OrganizationJpaEntity.reconstitute(
            id,
            tenantId,
            orgCode,
            name,
            organization.getStatus(),
            organization.getCreatedAt(),
            organization.getUpdatedAt(),
            organization.isDeleted()
        );
    }
}
