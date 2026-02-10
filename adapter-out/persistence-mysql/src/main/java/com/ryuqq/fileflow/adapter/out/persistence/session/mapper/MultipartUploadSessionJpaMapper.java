package com.ryuqq.fileflow.adapter.out.persistence.session.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.id.MultipartUploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.UploadTarget;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MultipartUploadSessionJpaMapper {

    public MultipartUploadSessionJpaEntity toEntity(MultipartUploadSession domain) {
        return MultipartUploadSessionJpaEntity.create(
                domain.idValue(),
                domain.s3Key(),
                domain.bucket(),
                domain.accessType(),
                domain.fileName(),
                domain.contentType(),
                domain.uploadId(),
                domain.partSize(),
                domain.purposeValue(),
                domain.sourceValue(),
                domain.status(),
                domain.expiresAt(),
                domain.createdAt(),
                domain.updatedAt());
    }

    public MultipartUploadSession toDomain(
            MultipartUploadSessionJpaEntity entity, List<CompletedPartJpaEntity> partEntities) {
        List<CompletedPart> parts =
                partEntities.stream()
                        .map(
                                p ->
                                        CompletedPart.of(
                                                p.getPartNumber(),
                                                p.getEtag(),
                                                p.getSize(),
                                                p.getCreatedAt()))
                        .toList();

        return MultipartUploadSession.reconstitute(
                MultipartUploadSessionId.of(entity.getId()),
                UploadTarget.of(
                        entity.getS3Key(),
                        entity.getBucket(),
                        entity.getAccessType(),
                        entity.getFileName(),
                        entity.getContentType()),
                entity.getUploadId(),
                entity.getPartSize(),
                entity.getPurpose(),
                entity.getSource(),
                entity.getStatus(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                parts);
    }

    public List<CompletedPartJpaEntity> toPartEntities(
            String sessionId, List<CompletedPart> parts) {
        return parts.stream()
                .map(
                        part ->
                                CompletedPartJpaEntity.create(
                                        sessionId,
                                        part.partNumber(),
                                        part.etag(),
                                        part.size(),
                                        part.createdAt()))
                .toList();
    }
}
