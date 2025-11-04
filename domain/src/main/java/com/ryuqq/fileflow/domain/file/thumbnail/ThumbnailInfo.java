package com.ryuqq.fileflow.domain.file.thumbnail;

import com.ryuqq.fileflow.domain.upload.StorageKey;

/**
 * Thumbnail Information Value Object
 *
 * <p>파일의 썸네일 정보를 나타내는 불변 Value Object입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>썸네일 저장 위치 (S3 Key)</li>
 *   <li>썸네일 크기 (Width, Height)</li>
 *   <li>썸네일 파일 크기 (Bytes)</li>
 *   <li>썸네일 Content Type</li>
 * </ul>
 *
 * <p><strong>생성 과정:</strong></p>
 * <ol>
 *   <li>원본 이미지를 S3에서 다운로드</li>
 *   <li>리사이징 (예: 4000x3000 → 300x300)</li>
 *   <li>압축 (예: 5MB PNG → 50KB JPEG, 품질 85%)</li>
 *   <li>S3에 썸네일 업로드</li>
 *   <li>ThumbnailInfo 생성 및 반환</li>
 * </ol>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>
 * ThumbnailInfo thumbnail = new ThumbnailInfo(
 *     new StorageKey("thumbnails/2025/01/uuid_300x300.jpg"),
 *     300,
 *     300,
 *     51200L,
 *     "image/jpeg"
 * );
 * </pre>
 *
 * <p><strong>불변성:</strong></p>
 * <ul>
 *   <li>Record 패턴 사용 (Java 21)</li>
 *   <li>모든 필드는 final</li>
 *   <li>Compact Constructor로 유효성 검증</li>
 * </ul>
 *
 * <p><strong>검증 규칙:</strong></p>
 * <ul>
 *   <li>storageKey는 null 불가</li>
 *   <li>width, height는 1 이상</li>
 *   <li>size는 0 이상</li>
 *   <li>contentType은 null 또는 빈 문자열 불가</li>
 * </ul>
 *
 * @param storageKey  썸네일 저장 위치 (S3 Key)
 * @param width       썸네일 너비 (픽셀)
 * @param height      썸네일 높이 (픽셀)
 * @param size        썸네일 파일 크기 (Bytes)
 * @param contentType 썸네일 Content Type (예: "image/jpeg")
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record ThumbnailInfo(
    StorageKey storageKey,
    int width,
    int height,
    long size,
    String contentType
) {

    /**
     * Compact Constructor - 유효성 검증
     *
     * <p><strong>검증 규칙:</strong></p>
     * <ul>
     *   <li>storageKey는 null 불가</li>
     *   <li>width는 1 이상 (최소 1픽셀)</li>
     *   <li>height는 1 이상 (최소 1픽셀)</li>
     *   <li>size는 0 이상 (빈 파일도 허용)</li>
     *   <li>contentType은 null 또는 빈 문자열 불가</li>
     * </ul>
     *
     * @throws IllegalArgumentException 검증 실패 시
     */
    public ThumbnailInfo {
        if (storageKey == null) {
            throw new IllegalArgumentException("StorageKey는 null일 수 없습니다");
        }

        if (width < 1) {
            throw new IllegalArgumentException(
                "Width는 1 이상이어야 합니다: " + width
            );
        }

        if (height < 1) {
            throw new IllegalArgumentException(
                "Height는 1 이상이어야 합니다: " + height
            );
        }

        if (size < 0) {
            throw new IllegalArgumentException(
                "Size는 0 이상이어야 합니다: " + size
            );
        }

        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException(
                "ContentType은 null 또는 빈 문자열일 수 없습니다"
            );
        }
    }

    /**
     * 썸네일 저장 위치 (문자열)
     *
     * @return S3 Key 문자열
     */
    public String getStorageKeyValue() {
        return storageKey.value();
    }

    /**
     * 썸네일이 이미지인지 확인
     *
     * @return 이미지 여부
     */
    public boolean isImage() {
        return contentType.startsWith("image/");
    }

    /**
     * 썸네일 크기 (KB)
     *
     * @return 크기 (KB)
     */
    public double getSizeInKB() {
        return size / 1024.0;
    }

    /**
     * 썸네일 크기 (MB)
     *
     * @return 크기 (MB)
     */
    public double getSizeInMB() {
        return size / (1024.0 * 1024.0);
    }
}
