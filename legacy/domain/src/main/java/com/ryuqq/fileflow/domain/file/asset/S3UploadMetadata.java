package com.ryuqq.fileflow.domain.file.asset;

import com.ryuqq.fileflow.domain.upload.ETag;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MimeType;
import com.ryuqq.fileflow.domain.upload.StorageKey;

/**
 * S3 업로드 메타데이터 (Domain Value Object)
 *
 * <p><strong>목적:</strong></p>
 * <ul>
 *   <li>Domain Layer의 외부 의존성 제거</li>
 *   <li>S3 업로드 결과를 Domain 관점에서 표현</li>
 *   <li>Application Layer DTO로부터 Domain 격리</li>
 * </ul>
 *
 * <p><strong>사용 시점:</strong></p>
 * <ul>
 *   <li>Single Upload 완료 후 FileAsset 생성</li>
 *   <li>Multipart Upload 완료 후 FileAsset 생성</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Value Object 사용 (FileSize, ETag, MimeType, StorageKey)</li>
 *   <li>✅ Type Safety 보장</li>
 *   <li>✅ Pure Java Record 패턴</li>
 * </ul>
 *
 * @param fileSize 파일 크기 (FileSize VO)
 * @param etag S3 ETag (ETag VO)
 * @param mimeType MIME Type (MimeType VO, nullable)
 * @param storageKey S3 저장 경로 (StorageKey VO)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record S3UploadMetadata(
    FileSize fileSize,
    ETag etag,
    MimeType mimeType,
    StorageKey storageKey
) {
    /**
     * Compact Constructor - 유효성 검증
     *
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     */
    public S3UploadMetadata {
        if (fileSize == null) {
            throw new IllegalArgumentException("FileSize는 필수입니다");
        }
        if (etag == null) {
            throw new IllegalArgumentException("ETag는 필수입니다");
        }
        if (storageKey == null) {
            throw new IllegalArgumentException("StorageKey는 필수입니다");
        }
        // mimeType은 null 허용 (기본값: application/octet-stream)
    }

    /**
     * Static Factory Method
     *
     * @param fileSize 파일 크기
     * @param etag ETag
     * @param mimeType MIME Type (nullable)
     * @param storageKey 저장 경로
     * @return S3UploadMetadata
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static S3UploadMetadata of(
        FileSize fileSize,
        ETag etag,
        MimeType mimeType,
        StorageKey storageKey
    ) {
        return new S3UploadMetadata(fileSize, etag, mimeType, storageKey);
    }

    /**
     * MimeType이 없을 때 기본값으로 생성
     *
     * @param fileSize 파일 크기
     * @param etag ETag
     * @param storageKey 저장 경로
     * @return S3UploadMetadata (mimeType = null)
     */
    public static S3UploadMetadata withoutMimeType(
        FileSize fileSize,
        ETag etag,
        StorageKey storageKey
    ) {
        return new S3UploadMetadata(fileSize, etag, null, storageKey);
    }

    /**
     * MimeType 반환 (null이면 기본값)
     *
     * @return MimeType (null이면 application/octet-stream)
     */
    public MimeType getMimeTypeOrDefault() {
        return mimeType != null ? mimeType : MimeType.defaultType();
    }
}
