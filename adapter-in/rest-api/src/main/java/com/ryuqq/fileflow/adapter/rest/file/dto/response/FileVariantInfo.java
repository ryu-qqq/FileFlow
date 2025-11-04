package com.ryuqq.fileflow.adapter.rest.file.dto.response;

/**
 * File Variant Information
 *
 * <p>파일 변형본 정보 (원본 또는 생성된 변형본)</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <ul>
 *   <li>원본 이미지: type=ORIGINAL, width=2000, height=1500</li>
 *   <li>썸네일: type=THUMBNAIL, width=200, height=150</li>
 *   <li>중간 크기: type=MEDIUM, width=800, height=600</li>
 * </ul>
 *
 * <p><strong>JSON 예시:</strong></p>
 * <pre>{@code
 * {
 *   "type": "THUMBNAIL",
 *   "storageKey": "tenant-1/org-2/thumbnail/profile.jpg",
 *   "url": "https://cdn.example.com/thumbnail/profile.jpg",
 *   "width": 200,
 *   "height": 150,
 *   "fileSize": 51200
 * }
 * }</pre>
 *
 * @param type 변형본 타입 (ORIGINAL, THUMBNAIL, SMALL, MEDIUM, LARGE)
 * @param storageKey S3 저장 키
 * @param url 접근 URL (CDN 또는 Presigned URL)
 * @param width 이미지 너비 (px, 이미지가 아니면 null)
 * @param height 이미지 높이 (px, 이미지가 아니면 null)
 * @param fileSize 파일 크기 (bytes)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record FileVariantInfo(
    String type,
    String storageKey,
    String url,
    Integer width,
    Integer height,
    Long fileSize
) {
    /**
     * Static Factory Method
     *
     * @param type 변형본 타입
     * @param storageKey S3 저장 키
     * @param url 접근 URL
     * @param width 이미지 너비 (px)
     * @param height 이미지 높이 (px)
     * @param fileSize 파일 크기 (bytes)
     * @return FileVariantInfo
     */
    public static FileVariantInfo of(
        String type,
        String storageKey,
        String url,
        Integer width,
        Integer height,
        Long fileSize
    ) {
        return new FileVariantInfo(type, storageKey, url, width, height, fileSize);
    }
}
