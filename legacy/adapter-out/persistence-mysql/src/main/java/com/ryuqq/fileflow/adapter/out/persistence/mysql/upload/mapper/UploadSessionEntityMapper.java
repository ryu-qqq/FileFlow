package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.upload.FailureReason;
import com.ryuqq.fileflow.domain.upload.FileName;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.StorageKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;

/**
 * Upload Session Entity Mapper
 *
 * <p>Domain {@link UploadSession} ↔ JPA {@link UploadSessionJpaEntity} 변환을 담당하는 Stateless Utility 클래스입니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>Stateless: 상태를 저장하지 않는 순수 변환 로직</li>
 *   <li>Pure Function: 동일한 입력에 항상 동일한 출력</li>
 *   <li>Law of Demeter 준수: Value Object의 {@code value()} 메서드 사용</li>
 *   <li>Final 클래스, Private 생성자: 인스턴스화 방지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class UploadSessionEntityMapper {

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @throws AssertionError 인스턴스 생성 시도 시
     */
    private UploadSessionEntityMapper() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * JPA Entity → Domain 변환
     *
     * <p><strong>변환 과정:</strong></p>
     * <ol>
     *   <li>Entity getter로 원시 타입 추출 (Long, String, Enum)</li>
     *   <li>Value Object Static Factory Method 호출</li>
     *   <li>Domain reconstitute() 호출 (기존 데이터 복원)</li>
     * </ol>
     *
     * @param entity JPA Entity ({@link UploadSessionJpaEntity})
     * @return Domain Aggregate ({@link UploadSession})
     * @throws IllegalArgumentException entity가 null인 경우
     */
    public static UploadSession toDomain(UploadSessionJpaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("UploadSessionJpaEntity must not be null");
        }

        // 1. Value Object 생성
        SessionKey sessionKey = SessionKey.of(entity.getSessionKey());
        TenantId tenantId = TenantId.of(entity.getTenantId());
        FileName fileName = FileName.of(entity.getFileName());
        FileSize fileSize = FileSize.of(entity.getFileSize());

        StorageKey storageKey = entity.getStorageKey() != null
            ? StorageKey.of(entity.getStorageKey())
            : null;

        FailureReason failureReason = entity.getFailureReason() != null
            ? FailureReason.of(entity.getFailureReason())
            : null;

        // 2. Domain reconstitute
        return UploadSession.reconstitute(
            UploadSessionId.of(entity.getId()),
            sessionKey,
            tenantId,
            fileName,
            fileSize,
            entity.getUploadType(),
            storageKey,
            entity.getStatus(),
            entity.getFileId(),
            failureReason,
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getCompletedAt(),
            entity.getFailedAt()
        );
    }

    /**
     * Domain → JPA Entity 변환
     *
     * <p><strong>변환 과정:</strong></p>
     * <ol>
     *   <li>Domain getter로 Value Object 추출</li>
     *   <li>Value Object의 {@code value()} 메서드로 원시 타입 추출</li>
     *   <li>Entity create() 또는 reconstitute() 호출</li>
     * </ol>
     *
     * @param domain Domain Aggregate ({@link UploadSession})
     * @return JPA Entity ({@link UploadSessionJpaEntity})
     * @throws IllegalArgumentException domain이 null인 경우
     */
    public static UploadSessionJpaEntity toEntity(UploadSession domain) {
        if (domain == null) {
            throw new IllegalArgumentException("UploadSession must not be null");
        }

        // ID가 있으면 reconstitute, 없으면 create
        if (domain.getIdValue() != null) {
            return UploadSessionJpaEntity.reconstitute(
                domain.getIdValue(),
                domain.getSessionKey().value(),
                domain.getTenantId().value(),
                domain.getFileName().value(),
                domain.getFileSize().bytes(),
                domain.getUploadType(),
                domain.getStorageKey() != null ? domain.getStorageKey().value() : null,
                domain.getStatus(),
                domain.getFileId(),
                domain.getFailureReason() != null ? domain.getFailureReason().value() : null,
                domain.getCompletedAt(),
                domain.getFailedAt(),
                domain.getCreatedAt(),
                domain.getCreatedAt()  // updatedAt은 createdAt으로 초기화
            );
        } else {
            return UploadSessionJpaEntity.create(
                domain.getSessionKey().value(),
                domain.getTenantId().value(),
                domain.getFileName().value(),
                domain.getFileSize().bytes(),
                domain.getUploadType(),
                domain.getStorageKey() != null ? domain.getStorageKey().value() : null,
                domain.getStatus()
            );
        }
    }
}
