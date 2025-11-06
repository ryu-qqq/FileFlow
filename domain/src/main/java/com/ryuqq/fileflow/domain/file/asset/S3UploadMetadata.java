package com.ryuqq.fileflow.domain.file.asset;

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
 * @param contentLength S3 객체 크기 (bytes)
 * @param etag S3 ETag (MD5 or Multipart ETag)
 * @param contentType MIME Type
 * @param storageKey S3 저장 경로 (예: "tenant-1/files/2024/11/06/uuid.jpg")
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record S3UploadMetadata(
    Long contentLength,
    String etag,
    String contentType,
    String storageKey
) {
    /**
     * Static Factory Method
     *
     * @param contentLength 파일 크기
     * @param etag ETag
     * @param contentType MIME Type
     * @param storageKey 저장 경로
     * @return S3UploadMetadata
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static S3UploadMetadata of(
        Long contentLength,
        String etag,
        String contentType,
        String storageKey
    ) {
        validateContentLength(contentLength);
        validateEtag(etag);
        validateContentType(contentType);
        validateStorageKey(storageKey);

        return new S3UploadMetadata(contentLength, etag, contentType, storageKey);
    }

    /**
     * ContentLength 검증
     *
     * @param contentLength 파일 크기
     * @throws IllegalArgumentException contentLength가 null이거나 0 이하인 경우
     */
    private static void validateContentLength(Long contentLength) {
        if (contentLength == null || contentLength <= 0) {
            throw new IllegalArgumentException("contentLength는 양수여야 합니다");
        }
    }

    /**
     * ETag 검증
     *
     * @param etag ETag
     * @throws IllegalArgumentException etag가 null이거나 비어있는 경우
     */
    private static void validateEtag(String etag) {
        if (etag == null || etag.isBlank()) {
            throw new IllegalArgumentException("etag는 필수입니다");
        }
    }

    /**
     * ContentType 검증
     *
     * <p>null 허용 (기본값: application/octet-stream)</p>
     *
     * @param contentType MIME Type
     */
    private static void validateContentType(String contentType) {
        // null 허용 (기본값: application/octet-stream)
    }

    /**
     * StorageKey 검증
     *
     * @param storageKey 저장 경로
     * @throws IllegalArgumentException storageKey가 null이거나 비어있는 경우
     */
    private static void validateStorageKey(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("storageKey는 필수입니다");
        }
    }
}

