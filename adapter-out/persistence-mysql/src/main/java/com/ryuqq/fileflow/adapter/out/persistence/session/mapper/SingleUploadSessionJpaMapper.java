package com.ryuqq.fileflow.adapter.out.persistence.session.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.id.SingleUploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.UploadTarget;
import org.springframework.stereotype.Component;

@Component
public class SingleUploadSessionJpaMapper {

    public SingleUploadSessionJpaEntity toEntity(SingleUploadSession domain) {
        return SingleUploadSessionJpaEntity.create(
                domain.idValue(),
                domain.s3Key(),
                domain.bucket(),
                domain.accessType(),
                domain.fileName(),
                domain.contentType(),
                domain.presignedUrlValue(),
                domain.purposeValue(),
                domain.sourceValue(),
                domain.status(),
                domain.expiresAt(),
                domain.createdAt(),
                domain.updatedAt());
    }

    public SingleUploadSession toDomain(SingleUploadSessionJpaEntity entity) {
        return SingleUploadSession.reconstitute(
                SingleUploadSessionId.of(entity.getId()),
                UploadTarget.of(
                        entity.getS3Key(),
                        entity.getBucket(),
                        entity.getAccessType(),
                        entity.getFileName(),
                        entity.getContentType()),
                entity.getPresignedUrl(),
                entity.getPurpose(),
                entity.getSource(),
                entity.getStatus(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
