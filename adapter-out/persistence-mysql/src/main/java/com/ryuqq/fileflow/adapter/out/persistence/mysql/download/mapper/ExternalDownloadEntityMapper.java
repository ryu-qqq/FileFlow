package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.ExternalDownloadJpaEntity;
import com.ryuqq.fileflow.domain.download.ErrorCode;
import com.ryuqq.fileflow.domain.download.ErrorMessage;
import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * External Download Entity Mapper
 *
 * <p>Domain {@link ExternalDownload} ↔ JPA {@link ExternalDownloadJpaEntity} 변환을 담당하는 Stateless Utility 클래스입니다.</p>
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
public final class ExternalDownloadEntityMapper {

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @throws AssertionError 인스턴스 생성 시도 시
     */
    private ExternalDownloadEntityMapper() {
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
     * @param entity JPA Entity ({@link ExternalDownloadJpaEntity})
     * @return Domain Aggregate ({@link ExternalDownload})
     * @throws IllegalArgumentException entity가 null인 경우
     */
    public static ExternalDownload toDomain(ExternalDownloadJpaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("ExternalDownloadJpaEntity must not be null");
        }

        // 1. Value Object 생성
        UploadSessionId uploadSessionId = UploadSessionId.of(entity.getUploadSessionId());
        
        URL sourceUrl;
        try {
            sourceUrl = new URL(entity.getSourceUrl());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid source URL: " + entity.getSourceUrl(), e);
        }

        FileSize bytesTransferred = entity.getBytesTransferred() != null
            ? FileSize.of(entity.getBytesTransferred())
            : FileSize.of(0L);

        FileSize totalBytes = entity.getTotalBytes() != null
            ? FileSize.of(entity.getTotalBytes())
            : null;

        ErrorCode errorCode = entity.getErrorCode() != null
            ? ErrorCode.of(entity.getErrorCode())
            : null;

        ErrorMessage errorMessage = entity.getErrorMessage() != null
            ? ErrorMessage.of(entity.getErrorMessage())
            : null;

        // 2. Domain reconstitute
        return ExternalDownload.reconstitute(
            entity.getId(),
            uploadSessionId,
            sourceUrl,
            bytesTransferred,
            totalBytes,
            entity.getStatus(),
            entity.getRetryCount(),
            entity.getLastRetryAt(),
            errorCode,
            errorMessage
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
     * @param download Domain Aggregate ({@link ExternalDownload})
     * @return JPA Entity ({@link ExternalDownloadJpaEntity})
     * @throws IllegalArgumentException download가 null인 경우
     */
    public static ExternalDownloadJpaEntity toEntity(ExternalDownload download) {
        if (download == null) {
            throw new IllegalArgumentException("ExternalDownload must not be null");
        }

        // 신규 생성 vs 기존 데이터 업데이트 구분
        if (download.getId() == null) {
            // 신규 생성
            return ExternalDownloadJpaEntity.create(
                download.getUploadSessionId().value(),
                download.getSourceUrl().toString(),
                download.getStatus()
            );
        } else {
            // 기존 데이터 reconstitute
            return ExternalDownloadJpaEntity.reconstitute(
                download.getId(),
                download.getUploadSessionId().value(),
                download.getSourceUrl().toString(),
                download.getBytesTransferred() != null
                    ? download.getBytesTransferred().bytes()
                    : 0L,
                download.getTotalBytes() != null
                    ? download.getTotalBytes().bytes()
                    : null,
                download.getStatus(),
                download.getRetryCount(),
                download.getLastRetryAt(),
                download.getErrorCode() != null
                    ? download.getErrorCode().value()
                    : null,
                download.getErrorMessage() != null
                    ? download.getErrorMessage().value()
                    : null,
                download.getCreatedAt(),
                download.getUpdatedAt()
            );
        }
    }
}
