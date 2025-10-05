package com.ryuqq.fileflow.adapter.persistence.repository;

import com.ryuqq.fileflow.adapter.persistence.entity.UploadPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UploadPolicy JPA Repository
 *
 * 비즈니스 규칙:
 * - policyKey는 "tenantId:userType:serviceType" 형식의 복합 키
 * - 활성화된 정책만 조회하는 메서드 제공
 * - Spring Data JPA의 메서드 네이밍 규칙 활용
 *
 * @author sangwon-ryu
 */
@Repository
public interface UploadPolicyJpaRepository extends JpaRepository<UploadPolicyEntity, String> {

    /**
     * policyKey로 활성화된 UploadPolicy 조회
     *
     * @param policyKey 정책 키
     * @return 활성화된 정책 (존재하지 않으면 Optional.empty())
     */
    @Query("SELECT u FROM UploadPolicyEntity u WHERE u.policyKey = :policyKey AND u.isActive = true")
    Optional<UploadPolicyEntity> findByPolicyKeyAndIsActiveTrue(@Param("policyKey") String policyKey);

    /**
     * policyKey로 UploadPolicy 존재 여부 확인
     *
     * @param policyKey 정책 키
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByPolicyKey(String policyKey);
}
