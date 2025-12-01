package com.ryuqq.fileflow.adapter.out.persistence.session.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.iam.vo.Organization;
import com.ryuqq.fileflow.domain.iam.vo.Tenant;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.iam.vo.UserRole;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.ExpirationTime;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.session.vo.PresignedUrl;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * SingleUploadSession Domain ↔ JPA Entity 매퍼.
 *
 * <p>Domain 모델과 JPA Entity 간 변환을 담당합니다.
 */
@Component
public class SingleUploadSessionJpaMapper {

    private final ClockHolder clockHolder;

    public SingleUploadSessionJpaMapper(ClockHolder clockHolder) {
        this.clockHolder = clockHolder;
    }

    /**
     * Domain → JPA Entity 변환.
     *
     * @param domain SingleUploadSession Domain 객체
     * @return SingleUploadSessionJpaEntity
     */
    public SingleUploadSessionJpaEntity toEntity(SingleUploadSession domain) {
        LocalDateTime now = LocalDateTime.now();
        UserContext userContext = domain.getUserContext();

        return SingleUploadSessionJpaEntity.of(
                domain.getId().getValue(),
                domain.getIdempotencyKey().getValue(),
                userContext.userId(),
                userContext.organization().id(),
                userContext.organization().name(),
                userContext.organization().namespace(),
                userContext.tenant().id(),
                userContext.tenant().name(),
                userContext.getRole().name(),
                userContext.email(),
                domain.getFileNameValue(),
                domain.getFileSizeValue(),
                domain.getContentTypeValue(),
                domain.getBucketValue(),
                domain.getS3KeyValue(),
                domain.getExpiresAt(),
                domain.getStatus(),
                domain.getPresignedUrl() != null ? domain.getPresignedUrl().value() : null,
                domain.getETagValue(),
                domain.getCompletedAt(),
                domain.getVersion(),
                domain.getCreatedAt(),
                now);
    }

    /**
     * JPA Entity → Domain 변환.
     *
     * @param entity SingleUploadSessionJpaEntity
     * @return SingleUploadSession Domain 객체
     */
    public SingleUploadSession toDomain(SingleUploadSessionJpaEntity entity) {
        UserContext userContext = reconstructUserContext(entity);

        return SingleUploadSession.reconstitute(
                UploadSessionId.of(UUID.fromString(entity.getId())),
                IdempotencyKey.of(UUID.fromString(entity.getIdempotencyKey())),
                userContext,
                FileName.of(entity.getFileName()),
                FileSize.of(entity.getFileSize()),
                ContentType.of(entity.getContentType()),
                S3Bucket.of(entity.getBucket()),
                S3Key.of(entity.getS3Key()),
                ExpirationTime.of(entity.getExpiresAt()),
                entity.getCreatedAt(),
                entity.getStatus(),
                entity.getPresignedUrl() != null ? PresignedUrl.of(entity.getPresignedUrl()) : null,
                entity.getEtag() != null ? ETag.of(entity.getEtag()) : null,
                entity.getCompletedAt(),
                entity.getUpdatedAt(),
                entity.getVersion(),
                clockHolder.getClock());
    }

    private UserContext reconstructUserContext(SingleUploadSessionJpaEntity entity) {
        UserRole role = UserRole.valueOf(entity.getUserRole());
        Tenant tenant = Tenant.of(entity.getTenantId(), entity.getTenantName());
        Organization organization =
                Organization.of(
                        entity.getOrganizationId(),
                        entity.getOrganizationName(),
                        entity.getOrganizationNamespace(),
                        role);

        return UserContext.of(tenant, organization, entity.getEmail(), entity.getUserId());
    }
}
