package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.MultipartUploadJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadPartJpaEntity;
import com.ryuqq.fileflow.domain.upload.Checksum;
import com.ryuqq.fileflow.domain.upload.ETag;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.PartNumber;
import com.ryuqq.fileflow.domain.upload.ProviderUploadId;
import com.ryuqq.fileflow.domain.upload.TotalParts;
import com.ryuqq.fileflow.domain.upload.UploadPart;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Multipart Upload Entity Mapper
 *
 * <p>Domain {@link MultipartUpload} ↔ JPA {@link MultipartUploadJpaEntity} 변환을 담당하는 Stateless Utility 클래스입니다.</p>
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
public final class MultipartUploadEntityMapper {

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @throws AssertionError 인스턴스 생성 시도 시
     */
    private MultipartUploadEntityMapper() {
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
     * @param entity JPA Entity ({@link MultipartUploadJpaEntity})
     * @param parts 업로드된 파트 목록 (JPA Entity)
     * @return Domain Aggregate ({@link MultipartUpload})
     * @throws IllegalArgumentException entity가 null인 경우
     */
    public static MultipartUpload toDomain(
        MultipartUploadJpaEntity entity,
        List<UploadPartJpaEntity> parts
    ) {
        if (entity == null) {
            throw new IllegalArgumentException("MultipartUploadJpaEntity must not be null");
        }

        // 1. Value Object 생성
        UploadSessionId uploadSessionId = UploadSessionId.of(entity.getUploadSessionId());
        
        ProviderUploadId providerUploadId = entity.getProviderUploadId() != null
            ? ProviderUploadId.of(entity.getProviderUploadId())
            : null;
        
        TotalParts totalParts = entity.getTotalParts() != null
            ? TotalParts.of(entity.getTotalParts())
            : null;

        // 2. UploadPart 목록 변환
        List<UploadPart> uploadedParts = parts != null
            ? parts.stream()
                .map(MultipartUploadEntityMapper::partToDomain)
                .collect(Collectors.toList())
            : Collections.emptyList();

        // 3. Domain reconstitute
        return MultipartUpload.reconstitute(
            entity.getId(),
            uploadSessionId,
            providerUploadId,
            entity.getStatus(),
            totalParts,
            uploadedParts,
            entity.getStartedAt(),
            entity.getCompletedAt(),
            entity.getAbortedAt()
        );
    }

    /**
     * Domain → JPA Entity 변환
     *
     * <p><strong>변환 과정:</strong></p>
     * <ol>
     *   <li>Domain getter로 Value Object 추출</li>
     *   <li>Value Object의 {@code value()} 메서드로 원시 타입 추출</li>
     *   <li>Entity Static Factory Method 호출</li>
     * </ol>
     *
     * @param multipart Domain Aggregate ({@link MultipartUpload})
     * @return JPA Entity ({@link MultipartUploadJpaEntity})
     * @throws IllegalArgumentException multipart가 null인 경우
     */
    public static MultipartUploadJpaEntity toEntity(MultipartUpload multipart) {
        if (multipart == null) {
            throw new IllegalArgumentException("MultipartUpload must not be null");
        }

        // 신규 생성 vs 기존 데이터 업데이트 구분
        if (multipart.getId() == null) {
            // 신규 생성
            return MultipartUploadJpaEntity.create(
                multipart.getUploadSessionId().value(),
                multipart.getProviderUploadId() != null
                    ? multipart.getProviderUploadId().value()
                    : null,
                multipart.getStatus(),
                multipart.getTotalParts() != null
                    ? multipart.getTotalParts().value()
                    : null,
                multipart.getStartedAt()
            );
        } else {
            // 기존 데이터 reconstitute
            return MultipartUploadJpaEntity.reconstitute(
                multipart.getId(),
                multipart.getUploadSessionId().value(),
                multipart.getProviderUploadId() != null
                    ? multipart.getProviderUploadId().value()
                    : null,
                multipart.getStatus(),
                multipart.getTotalParts() != null
                    ? multipart.getTotalParts().value()
                    : null,
                multipart.getStartedAt(),
                multipart.getCompletedAt(),
                multipart.getAbortedAt(),
                multipart.getCreatedAt(),
                multipart.getUpdatedAt()
            );
        }
    }

    /**
     * UploadPart JPA Entity → Domain 변환
     *
     * @param entity UploadPart JPA Entity
     * @return UploadPart Domain
     * @throws IllegalArgumentException entity가 null인 경우
     */
    public static UploadPart partToDomain(UploadPartJpaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("UploadPartJpaEntity must not be null");
        }

        PartNumber partNumber = PartNumber.of(entity.getPartNumber());
        ETag etag = ETag.of(entity.getEtag());
        FileSize size = FileSize.of(entity.getSize());
        
        Checksum checksum = entity.getChecksum() != null
            ? Checksum.of(entity.getChecksum())
            : null;

        return checksum != null
            ? UploadPart.of(partNumber, etag, size, checksum)
            : UploadPart.of(partNumber, etag, size);
    }

    /**
     * UploadPart Domain → JPA Entity 변환
     *
     * @param part UploadPart Domain
     * @param multipartUploadId Multipart Upload ID (FK)
     * @return UploadPart JPA Entity
     * @throws IllegalArgumentException part가 null인 경우
     */
    public static UploadPartJpaEntity partToEntity(UploadPart part, Long multipartUploadId) {
        if (part == null) {
            throw new IllegalArgumentException("UploadPart must not be null");
        }
        if (multipartUploadId == null) {
            throw new IllegalArgumentException("Multipart Upload ID must not be null");
        }

        return UploadPartJpaEntity.create(
            multipartUploadId,
            part.getPartNumber().value(),
            part.getEtag().value(),
            part.getSize().bytes(),
            part.getChecksum() != null ? part.getChecksum().value() : null,
            part.getUploadedAt()
        );
    }

    /**
     * UploadPart 목록 Domain → JPA Entity 변환
     *
     * @param parts UploadPart Domain 목록
     * @param multipartUploadId Multipart Upload ID (FK)
     * @return UploadPart JPA Entity 목록
     */
    public static List<UploadPartJpaEntity> partsToEntities(
        List<UploadPart> parts,
        Long multipartUploadId
    ) {
        if (parts == null || parts.isEmpty()) {
            return Collections.emptyList();
        }

        return parts.stream()
            .map(part -> partToEntity(part, multipartUploadId))
            .collect(Collectors.toList());
    }
}
