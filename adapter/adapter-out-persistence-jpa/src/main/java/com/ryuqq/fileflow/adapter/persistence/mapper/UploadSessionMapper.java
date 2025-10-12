package com.ryuqq.fileflow.adapter.persistence.mapper;

import com.ryuqq.fileflow.adapter.persistence.entity.UploadSessionEntity;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.vo.CheckSum;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;
import org.springframework.stereotype.Component;

/**
 * UploadSession Entity ↔ Domain 양방향 Mapper
 *
 * 변환 규칙:
 * - PolicyKey: Domain VO ↔ Entity String
 * - UploadRequest: Domain VO ↔ Entity fields (fileName, fileType, contentType, fileSize, checksum, idempotencyKey)
 * - IdempotencyKey: Domain VO ↔ Entity String (nullable)
 * - CheckSum: Domain VO ↔ Entity String (nullable)
 *
 * @author sangwon-ryu
 */
@Component
public class UploadSessionMapper {

    /**
     * Domain → Entity 변환
     *
     * @param domain UploadSession 도메인 객체
     * @param tenantId 테넌트 ID (PolicyKey에서 추출)
     * @return UploadSessionEntity
     */
    public UploadSessionEntity toEntity(UploadSession domain, String tenantId) {
        if (domain == null) {
            return null;
        }

        UploadRequest request = domain.getUploadRequest();

        // IdempotencyKey는 nullable이므로 null 체크
        String idempotencyKeyValue = request.idempotencyKey() != null
                ? request.idempotencyKey().value()
                : null;

        return UploadSessionEntity.of(
                domain.getSessionId(),
                idempotencyKeyValue,
                tenantId,
                domain.getPolicyKey().getValue(),
                request.fileName(),
                request.contentType(),
                request.fileSizeBytes(),
                domain.getStatus(),
                null, // presignedUrl will be set separately
                domain.getExpiresAt()
        );
    }

    /**
     * Entity → Domain 변환
     *
     * @param entity UploadSessionEntity
     * @return UploadSession 도메인 객체
     */
    public UploadSession toDomain(UploadSessionEntity entity) {
        if (entity == null) {
            return null;
        }

        PolicyKey policyKey = parsePolicyKey(entity.getPolicyKey());

        // Determine FileType from contentType
        com.ryuqq.fileflow.domain.policy.FileType fileType =
                com.ryuqq.fileflow.domain.policy.FileType.fromContentType(entity.getContentType());

        // IdempotencyKey 변환 (nullable)
        com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey idempotencyKey =
                entity.getIdempotencyKey() != null
                        ? com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey.of(entity.getIdempotencyKey())
                        : null;

        UploadRequest uploadRequest = UploadRequest.of(
                entity.getFileName(),
                fileType,
                entity.getFileSize(),
                entity.getContentType(),
                null, // checksum not stored in current entity
                idempotencyKey
        );

        return UploadSession.reconstitute(
                entity.getSessionId(),
                policyKey,
                uploadRequest,
                entity.getTenantId(), // use tenantId as uploaderId temporarily
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getExpiresAt()
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
