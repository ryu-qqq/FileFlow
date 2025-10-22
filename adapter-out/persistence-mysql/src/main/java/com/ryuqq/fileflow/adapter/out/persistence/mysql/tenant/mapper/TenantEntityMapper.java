package com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant.entity.TenantJpaEntity;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantName;

/**
 * Tenant Entity Mapper
 *
 * <p><strong>역할</strong>: Domain Model {@code Tenant} ↔ JPA Entity {@code TenantJpaEntity} 상호 변환</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/tenant/mapper/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ 상태 없는(Stateless) 유틸리티 클래스</li>
 *   <li>✅ {@code toDomain()}: Entity → Domain 변환</li>
 *   <li>✅ {@code toEntity()}: Domain → Entity 변환</li>
 *   <li>✅ Value Object 변환 포함 (TenantId, TenantName, TenantStatus)</li>
 *   <li>❌ Lombok 금지 (Pure Java)</li>
 *   <li>❌ 비즈니스 로직 금지 (단순 변환만)</li>
 * </ul>
 *
 * @since 1.0.0
 */
public final class TenantEntityMapper {

    /**
     * Private 생성자 - 인스턴스화 방지
     */
    private TenantEntityMapper() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * JPA Entity → Domain Model 변환
     *
     * <p>DB에서 조회한 {@code TenantJpaEntity}를 Domain {@code Tenant}로 변환합니다.</p>
     *
     * <h4>변환 과정</h4>
     * <ol>
     *   <li>Value Object 생성: {@code TenantId}, {@code TenantName}</li>
     *   <li>Domain Enum 그대로 사용: {@code TenantStatus}</li>
     *   <li>Domain Aggregate 재구성</li>
     * </ol>
     *
     * @param entity JPA Entity
     * @return Domain Tenant
     * @throws IllegalArgumentException entity가 null인 경우
     */
    public static Tenant toDomain(TenantJpaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("TenantJpaEntity must not be null");
        }

        // Value Object 변환 (Static Factory Method 사용)
        TenantId tenantId = TenantId.of(entity.getId());
        TenantName tenantName = TenantName.of(entity.getName());

        // Domain Aggregate 재구성 (Status는 그대로 사용)
        return Tenant.reconstitute(
            tenantId,
            tenantName,
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.isDeleted()
        );
    }

    /**
     * Domain Model → JPA Entity 변환
     *
     * <p>Domain {@code Tenant}를 JPA {@code TenantJpaEntity}로 변환합니다.</p>
     *
     * <h4>변환 과정</h4>
     * <ol>
     *   <li>Value Object 원시 타입 추출: {@code id.value()}, {@code name.value()}</li>
     *   <li>Domain Enum 그대로 사용: {@code TenantStatus}</li>
     *   <li>JPA Entity 생성 (reconstitute 또는 create)</li>
     * </ol>
     *
     * @param tenant Domain Tenant
     * @return JPA Entity
     * @throws IllegalArgumentException tenant가 null인 경우
     */
    public static TenantJpaEntity toEntity(Tenant tenant) {
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant must not be null");
        }

        // Value Object → 원시 타입 (Law of Demeter 준수)
        String id = tenant.getId() != null ? tenant.getIdValue() : null;
        String name = tenant.getNameValue();

        // 신규 Tenant인 경우 (ID가 아직 없음)
        if (id == null) {
            return TenantJpaEntity.create(
                id,
                name,
                tenant.getCreatedAt()
            );
        }

        // 기존 Tenant인 경우 (재구성) - Status 그대로 전달
        return TenantJpaEntity.reconstitute(
            id,
            name,
            tenant.getStatus(),
            tenant.getCreatedAt(),
            tenant.getUpdatedAt(),
            tenant.isDeleted()
        );
    }
}
