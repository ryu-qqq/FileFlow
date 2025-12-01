package com.ryuqq.fileflow.adapter.out.persistence.session.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.iam.vo.Organization;
import com.ryuqq.fileflow.domain.iam.vo.Tenant;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.iam.vo.UserRole;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.ExpirationTime;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.PartNumber;
import com.ryuqq.fileflow.domain.session.vo.PartSize;
import com.ryuqq.fileflow.domain.session.vo.PresignedUrl;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.S3UploadId;
import com.ryuqq.fileflow.domain.session.vo.TotalParts;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * MultipartUploadSession Domain ↔ JPA Entity 매퍼.
 *
 * <p>Domain 모델과 JPA Entity 간 변환을 담당합니다.
 */
@Component
public class MultipartUploadSessionJpaMapper {

    private final ClockHolder clockHolder;

    public MultipartUploadSessionJpaMapper(ClockHolder clockHolder) {
        this.clockHolder = clockHolder;
    }

    /**
     * Domain → JPA Entity 변환.
     *
     * @param domain MultipartUploadSession Domain 객체
     * @return MultipartUploadSessionJpaEntity
     */
    public MultipartUploadSessionJpaEntity toEntity(MultipartUploadSession domain) {
        LocalDateTime now = LocalDateTime.now();
        UserContext userContext = domain.getUserContext();

        return MultipartUploadSessionJpaEntity.of(
                domain.getId().getValue(),
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
                domain.getS3UploadIdValue(),
                domain.getTotalPartsValue(),
                domain.getPartSizeValue(),
                domain.getExpiresAt(),
                domain.getStatus(),
                domain.getMergedETag() != null ? domain.getMergedETag().value() : null,
                domain.getCompletedAt(),
                domain.getVersion(),
                domain.getCreatedAt(),
                now);
    }

    /**
     * CompletedPart Domain → JPA Entity 변환.
     *
     * @param sessionId 세션 ID
     * @param domain CompletedPart Domain 객체
     * @return CompletedPartJpaEntity
     */
    public CompletedPartJpaEntity toPartEntity(String sessionId, CompletedPart domain) {
        LocalDateTime now = LocalDateTime.now();

        // id가 있으면 reconstitute (업데이트), 없으면 of (신규 생성)
        if (domain.getId() != null) {
            return CompletedPartJpaEntity.reconstitute(
                    domain.getId(),
                    sessionId,
                    domain.getPartNumberValue(),
                    domain.getPresignedUrlValue(),
                    domain.getETagValue(),
                    domain.getSize(),
                    domain.getUploadedAt(),
                    now,
                    now);
        }

        return CompletedPartJpaEntity.of(
                sessionId,
                domain.getPartNumberValue(),
                domain.getPresignedUrlValue(),
                domain.getETagValue(),
                domain.getSize(),
                domain.getUploadedAt(),
                now,
                now);
    }

    /**
     * JPA Entity → Domain 변환.
     *
     * <p>CompletedParts는 별도 Aggregate이므로 세션 재구성 시 포함하지 않습니다.
     *
     * @param entity MultipartUploadSessionJpaEntity
     * @return MultipartUploadSession Domain 객체
     */
    public MultipartUploadSession toDomain(MultipartUploadSessionJpaEntity entity) {
        UserContext userContext = reconstructUserContext(entity);

        return MultipartUploadSession.reconstitute(
                UploadSessionId.of(UUID.fromString(entity.getId())),
                userContext,
                FileName.of(entity.getFileName()),
                FileSize.of(entity.getFileSize()),
                ContentType.of(entity.getContentType()),
                S3Bucket.of(entity.getBucket()),
                S3Key.of(entity.getS3Key()),
                S3UploadId.of(entity.getS3UploadId()),
                TotalParts.of(entity.getTotalParts()),
                PartSize.of(entity.getPartSize()),
                ExpirationTime.of(entity.getExpiresAt()),
                entity.getCreatedAt(),
                entity.getStatus(),
                entity.getCompletedAt(),
                entity.getVersion(),
                clockHolder.getClock());
    }

    /**
     * CompletedPartJpaEntity → CompletedPart Domain 변환.
     *
     * @param entity CompletedPartJpaEntity
     * @return CompletedPart Domain 객체
     */
    public CompletedPart toCompletedPart(CompletedPartJpaEntity entity) {
        return CompletedPart.of(
                entity.getId(),
                UploadSessionId.of(entity.getSessionId()),
                PartNumber.of(entity.getPartNumber()),
                PresignedUrl.of(entity.getPresignedUrl()),
                ETag.of(entity.getEtag()),
                entity.getSize(),
                entity.getUploadedAt(),
                clockHolder.getClock());
    }

    private UserContext reconstructUserContext(MultipartUploadSessionJpaEntity entity) {
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
