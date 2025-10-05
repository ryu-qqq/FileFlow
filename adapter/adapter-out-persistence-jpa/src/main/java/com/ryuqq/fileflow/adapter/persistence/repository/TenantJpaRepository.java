package com.ryuqq.fileflow.adapter.persistence.repository;

import com.ryuqq.fileflow.adapter.persistence.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Tenant JPA Repository
 *
 * 비즈니스 규칙:
 * - tenantId를 기본 키로 사용
 * - 기본 CRUD 작업만 제공
 *
 * @author sangwon-ryu
 */
@Repository
public interface TenantJpaRepository extends JpaRepository<TenantEntity, String> {

    /**
     * tenantId로 Tenant 존재 여부 확인
     *
     * @param tenantId 테넌트 ID
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByTenantId(String tenantId);
}
