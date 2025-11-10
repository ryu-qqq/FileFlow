package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.domain.file.asset.FileId;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.upload.Checksum;
import com.ryuqq.fileflow.domain.upload.FileName;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MimeType;
import com.ryuqq.fileflow.domain.upload.StorageKey;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;

/**
 * FileAsset Entity Mapper
 *
 * <p>Domain FileAsset ↔ JPA FileAssetJpaEntity 변환</p>
 *
 * <p><strong>책임</strong>:</p>
 * <ul>
 *   <li>Domain → JPA Entity 변환 (toEntity)</li>
 *   <li>JPA Entity → Domain 변환 (toDomain)</li>
 *   <li>Value Object Wrapping/Unwrapping</li>
 *   <li>Static Utility Class - 인스턴스 불필요</li>
 * </ul>
 *
 * <p><strong>변환 규칙</strong>:</p>
 * <ul>
 *   <li>Domain Value Objects → Primitive 타입 (예: FileId.value() → Long)</li>
 *   <li>Primitive 타입 → Domain Value Objects (예: Long → FileId.of())</li>
 *   <li>Null 안전성 보장 (필수 필드 검증)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class FileAssetEntityMapper {

    /**
     * Private Constructor - Utility 클래스 인스턴스화 방지
     */
    private FileAssetEntityMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Domain → JPA Entity 변환
     *
     * <p>신규 저장 시: ID가 null인 상태로 변환 → JPA가 AUTO_INCREMENT로 생성</p>
     * <p>업데이트 시: 기존 ID 유지</p>
     *
     * @param fileAsset Domain FileAsset
     * @return JPA FileAssetJpaEntity
     */
    public static FileAssetJpaEntity toEntity(FileAsset fileAsset) {
        if (fileAsset == null) {
            return null;
        }

        if (fileAsset.getId() == null) {
            // 신규 저장 (ID 없음)
            return FileAssetJpaEntity.create(
                fileAsset.getTenantId().value(),
                fileAsset.getOrganizationId(),
                fileAsset.getOwnerUserId(),
                fileAsset.getFileName().value(),
                fileAsset.getFileSize().bytes(),
                fileAsset.getMimeType().value(),
                fileAsset.getStorageKey().value(),
                fileAsset.getChecksum() != null ? fileAsset.getChecksum().value() : null,
                fileAsset.getUploadSessionId().value(),
                fileAsset.getUploadedAt()
            );
        } else {
            // 업데이트 (ID 있음)
            return FileAssetJpaEntity.reconstitute(
                fileAsset.getIdValue(),
                fileAsset.getTenantId().value(),
                fileAsset.getOrganizationId(),
                fileAsset.getOwnerUserId(),
                fileAsset.getFileName().value(),
                fileAsset.getFileSize().bytes(),
                fileAsset.getMimeType().value(),
                fileAsset.getStorageKey().value(),
                fileAsset.getChecksum() != null ? fileAsset.getChecksum().value() : null,
                fileAsset.getUploadSessionId().value(),
                fileAsset.getStatus(),
                fileAsset.getVisibility(),
                fileAsset.getUploadedAt(),
                fileAsset.getProcessedAt(),
                fileAsset.getExpiresAt(),
                fileAsset.getRetentionDays(),
                fileAsset.getDeletedAt(),
                fileAsset.getUploadedAt(),  // createdAt (Domain에서는 uploadedAt 사용)
                fileAsset.getUploadedAt()   // updatedAt (Domain에서는 uploadedAt 사용)
            );
        }
    }

    /**
     * JPA Entity → Domain 변환
     *
     * <p>DB에서 조회한 데이터를 Domain 객체로 복원합니다.</p>
     *
     * @param entity JPA FileAssetJpaEntity
     * @return Domain FileAsset
     */
    public static FileAsset toDomain(FileAssetJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return FileAsset.reconstitute(
            FileId.of(entity.getId()),
            TenantId.of(entity.getTenantId()),
            entity.getOrganizationId(),
            entity.getOwnerUserId(),
            FileName.of(entity.getFileName()),
            FileSize.of(entity.getFileSize()),
            MimeType.of(entity.getMimeType()),
            StorageKey.of(entity.getStorageKey()),
            entity.getChecksumSha256() != null ? Checksum.of(entity.getChecksumSha256()) : null,
            UploadSessionId.of(entity.getUploadSessionId()),
            entity.getStatus(),
            entity.getVisibility(),
            entity.getUploadedAt(),
            entity.getProcessedAt(),
            entity.getExpiresAt(),
            entity.getRetentionDays(),
            entity.getDeletedAt()
        );
    }

}
