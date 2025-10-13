package com.ryuqq.fileflow.adapter.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.persistence.dto.MultipartUploadInfoDto;
import com.ryuqq.fileflow.adapter.persistence.dto.PartUploadInfoDto;
import com.ryuqq.fileflow.adapter.persistence.entity.UploadSessionEntity;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.vo.CheckSum;
import com.ryuqq.fileflow.domain.upload.vo.MultipartUploadInfo;
import com.ryuqq.fileflow.domain.upload.vo.PartUploadInfo;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * UploadSession Entity ↔ Domain 양방향 Mapper
 *
 * 변환 규칙:
 * - PolicyKey: Domain VO ↔ Entity String
 * - UploadRequest: Domain VO ↔ Entity fields (fileName, fileType, contentType, fileSize, checksum, idempotencyKey)
 * - IdempotencyKey: Domain VO ↔ Entity String (nullable)
 * - CheckSum: Domain VO ↔ Entity String (nullable)
 * - MultipartUploadInfo: Domain VO ↔ Entity JSON String (nullable)
 *
 * @author sangwon-ryu
 */
@Component
public class UploadSessionMapper {

    private static final Logger logger = LoggerFactory.getLogger(UploadSessionMapper.class);

    private final ObjectMapper objectMapper;

    public UploadSessionMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Domain → Entity 변환
     *
     * @param domain UploadSession 도메인 객체
     * @param tenantId 테넌트 ID (PolicyKey에서 추출)
     * @return UploadSessionEntity
     */
    public UploadSessionEntity toEntity(UploadSession domain, String tenantId) {
        logger.debug("========== [MAPPER-TO-ENTITY] UploadSessionMapper.toEntity() called ==========");
        if (domain == null) {
            logger.debug("[MAPPER-TO-ENTITY] domain is null");
            return null;
        }

        logger.debug("[MAPPER-TO-ENTITY] SessionId: {}", domain.getSessionId());
        logger.debug("[MAPPER-TO-ENTITY] HasMultipartInfo: {}", domain.getMultipartUploadInfo().isPresent());

        UploadRequest request = domain.getUploadRequest();

        // IdempotencyKey는 nullable이므로 null 체크
        String idempotencyKeyValue = request.idempotencyKey() != null
                ? request.idempotencyKey().value()
                : null;

        // MultipartUploadInfo를 JSON으로 직렬화 (nullable)
        logger.debug("[MAPPER-TO-ENTITY] Calling serializeMultipartUploadInfo");
        String multipartUploadInfoJson = serializeMultipartUploadInfo(
                domain.getMultipartUploadInfo().orElse(null)
        );
        logger.debug("[MAPPER-TO-ENTITY] Serialization result - JSON is null: {}", (multipartUploadInfoJson == null));

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
                multipartUploadInfoJson,
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
        logger.debug("========== [MAPPER-TO-DOMAIN] UploadSessionMapper.toDomain() called ==========");
        if (entity == null) {
            logger.debug("[MAPPER-TO-DOMAIN] entity is null");
            return null;
        }

        logger.debug("[MAPPER-TO-DOMAIN] SessionId: {}", entity.getSessionId());
        logger.debug("[MAPPER-TO-DOMAIN] Entity has JSON: {}", (entity.getMultipartUploadInfoJson() != null));

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

        // JSON에서 MultipartUploadInfo 역직렬화 (nullable)
        logger.debug("[MAPPER-TO-DOMAIN] Calling deserializeMultipartUploadInfo");
        MultipartUploadInfo multipartUploadInfo = deserializeMultipartUploadInfo(
                entity.getMultipartUploadInfoJson()
        );
        logger.debug("[MAPPER-TO-DOMAIN] Deserialization result - multipartUploadInfo is null: {}", (multipartUploadInfo == null));

        // MultipartUploadInfo가 있으면 reconstituteWithMultipart() 사용
        if (multipartUploadInfo != null) {
            logger.debug("[MAPPER-TO-DOMAIN] Using reconstituteWithMultipart()");
            return UploadSession.reconstituteWithMultipart(
                    entity.getSessionId(),
                    policyKey,
                    uploadRequest,
                    entity.getTenantId(), // use tenantId as uploaderId temporarily
                    entity.getStatus(),
                    entity.getCreatedAt(),
                    entity.getExpiresAt(),
                    multipartUploadInfo
            );
        }

        // 일반 업로드는 기존 reconstitute() 사용
        logger.debug("[MAPPER-TO-DOMAIN] Using reconstitute()");
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

    /**
     * MultipartUploadInfo를 JSON 문자열로 직렬화
     *
     * @param multipartUploadInfo 멀티파트 업로드 정보 (nullable)
     * @return JSON 문자열 (null이면 null 반환)
     */
    private String serializeMultipartUploadInfo(MultipartUploadInfo multipartUploadInfo) {
        logger.debug("========== [JSON-SERIAL] serializeMultipartUploadInfo called ==========");
        if (multipartUploadInfo == null) {
            logger.debug("[JSON-SERIAL] MultipartUploadInfo is null");
            return null;
        }

        try {
            logger.debug("[JSON-SERIAL] Serializing with {} parts", multipartUploadInfo.totalParts());

            // Domain VO → DTO 변환
            MultipartUploadInfoDto dto = toDtoFromDomain(multipartUploadInfo);

            // DTO → JSON
            String json = objectMapper.writeValueAsString(dto);
            logger.debug("[JSON-SERIAL] SUCCESS! JSON length: {}", json.length());
            logger.debug("[JSON-SERIAL] JSON content: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize MultipartUploadInfo to JSON", e);
            throw new IllegalStateException(
                    "Failed to serialize MultipartUploadInfo to JSON: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * JSON 문자열을 MultipartUploadInfo로 역직렬화
     *
     * @param json JSON 문자열 (nullable)
     * @return MultipartUploadInfo (null이면 null 반환)
     */
    private MultipartUploadInfo deserializeMultipartUploadInfo(String json) {
        logger.debug("========== [JSON-DESERIAL] deserializeMultipartUploadInfo called ==========");
        if (json == null || json.trim().isEmpty()) {
            logger.debug("[JSON-DESERIAL] JSON is null or empty");
            return null;
        }

        try {
            logger.debug("[JSON-DESERIAL] JSON length: {}", json.length());
            logger.debug("[JSON-DESERIAL] JSON content: {}", json);

            // JSON → DTO
            MultipartUploadInfoDto dto = objectMapper.readValue(json, MultipartUploadInfoDto.class);

            // DTO → Domain VO
            MultipartUploadInfo result = toDomainFromDto(dto);
            logger.debug("[JSON-DESERIAL] SUCCESS! Parts count: {}", (result != null ? result.totalParts() : 0));
            return result;
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize MultipartUploadInfo from JSON", e);
            throw new IllegalStateException(
                    "Failed to deserialize MultipartUploadInfo from JSON: " + e.getMessage(),
                    e
            );
        }
    }

    // ========== Domain VO ↔ DTO Conversion Methods ==========

    /**
     * Domain VO → DTO 변환
     *
     * @param domain MultipartUploadInfo Domain VO
     * @return MultipartUploadInfoDto
     */
    private MultipartUploadInfoDto toDtoFromDomain(MultipartUploadInfo domain) {
        if (domain == null) {
            return null;
        }

        var partDtos = domain.parts().stream()
                .map(this::toDtoFromDomain)
                .toList();

        return new MultipartUploadInfoDto(
                domain.uploadId(),
                domain.uploadPath(),
                partDtos
        );
    }

    /**
     * Domain VO → DTO 변환 (Part)
     *
     * @param domain PartUploadInfo Domain VO
     * @return PartUploadInfoDto
     */
    private PartUploadInfoDto toDtoFromDomain(PartUploadInfo domain) {
        if (domain == null) {
            return null;
        }

        return new PartUploadInfoDto(
                domain.partNumber(),
                domain.presignedUrl(),
                domain.startByte(),
                domain.endByte(),
                domain.expiresAt()
        );
    }

    /**
     * DTO → Domain VO 변환
     *
     * @param dto MultipartUploadInfoDto
     * @return MultipartUploadInfo Domain VO
     */
    private MultipartUploadInfo toDomainFromDto(MultipartUploadInfoDto dto) {
        if (dto == null) {
            return null;
        }

        var parts = dto.parts().stream()
                .map(this::toDomainFromDto)
                .toList();

        return MultipartUploadInfo.of(
                dto.uploadId(),
                dto.uploadPath(),
                parts
        );
    }

    /**
     * DTO → Domain VO 변환 (Part)
     *
     * @param dto PartUploadInfoDto
     * @return PartUploadInfo Domain VO
     */
    private PartUploadInfo toDomainFromDto(PartUploadInfoDto dto) {
        if (dto == null) {
            return null;
        }

        return PartUploadInfo.of(
                dto.partNumber(),
                dto.presignedUrl(),
                dto.startByte(),
                dto.endByte(),
                dto.expiresAt()
        );
    }

}
