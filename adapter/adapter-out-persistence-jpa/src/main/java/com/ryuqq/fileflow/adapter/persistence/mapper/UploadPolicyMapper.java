package com.ryuqq.fileflow.adapter.persistence.mapper;

import com.ryuqq.fileflow.adapter.persistence.entity.UploadPolicyEntity;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import org.springframework.stereotype.Component;

/**
 * UploadPolicy Entity ↔ Domain 양방향 Mapper
 *
 * 변환 규칙:
 * - PolicyKey: Domain VO ↔ Entity String ("tenantId:userType:serviceType")
 * - FileTypePolicies, RateLimiting: AttributeConverter에서 자동 변환
 * - Domain의 reconstitute() 메서드로 불변 객체 재구성
 *
 * @author sangwon-ryu
 */
@Component
public class UploadPolicyMapper {

    /**
     * Domain → Entity 변환
     *
     * @param domain UploadPolicy 도메인 객체
     * @return UploadPolicyEntity
     */
    public UploadPolicyEntity toEntity(UploadPolicy domain) {
        if (domain == null) {
            return null;
        }

        // Entity의 @PrePersist가 createdAt/updatedAt을 자동 설정
        return UploadPolicyEntity.of(
                domain.getPolicyKey().getValue(),
                domain.getFileTypePolicies(),
                domain.getRateLimiting(),
                domain.getVersion(),
                domain.isActive(),
                domain.getEffectiveFrom(),
                domain.getEffectiveUntil()
        );
    }

    /**
     * Entity → Domain 변환
     *
     * @param entity UploadPolicyEntity
     * @return UploadPolicy 도메인 객체
     */
    public UploadPolicy toDomain(UploadPolicyEntity entity) {
        if (entity == null) {
            return null;
        }

        PolicyKey policyKey = parsePolicyKey(entity.getPolicyKey());

        return UploadPolicy.reconstitute(
                policyKey,
                entity.getFileTypePolicies(),
                entity.getRateLimiting(),
                entity.getVersion(),
                entity.isActive(),
                entity.getEffectiveFrom(),
                entity.getEffectiveUntil()
        );
    }

    /**
     * PolicyKey 문자열을 파싱하여 PolicyKey VO로 변환
     *
     * @param policyKeyString "tenantId:userType:serviceType" 형식
     * @return PolicyKey VO
     */
    private PolicyKey parsePolicyKey(String policyKeyString) {
        if (policyKeyString == null || policyKeyString.trim().isEmpty()) {
            throw new IllegalArgumentException("PolicyKey string cannot be null or empty");
        }

        String[] parts = policyKeyString.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "Invalid PolicyKey format. Expected 'tenantId:userType:serviceType', got: " + policyKeyString
            );
        }

        return PolicyKey.of(parts[0], parts[1], parts[2]);
    }
}
