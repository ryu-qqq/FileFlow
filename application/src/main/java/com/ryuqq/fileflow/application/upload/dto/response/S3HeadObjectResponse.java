package com.ryuqq.fileflow.application.upload.dto.response;

/**
 * S3 HeadObject Response
 *
 * <p>S3 Object의 메타데이터 조회 결과를 담는 Response DTO입니다.</p>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>Single Upload 완료 시 S3에 파일이 실제로 존재하는지 확인</li>
 *   <li>파일 크기, ETag 등 메타데이터 조회</li>
 * </ul>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Record 패턴 사용 (Java 21)</li>
 *   <li>✅ Lombok 금지</li>
 *   <li>✅ Static Factory Method 제공</li>
 * </ul>
 *
 * @param contentLength 파일 크기 (bytes)
 * @param etag ETag (파일 무결성 확인용)
 * @param contentType Content-Type
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record S3HeadObjectResponse(
    Long contentLength,
    String etag,
    String contentType
) {
    /**
     * Static Factory Method
     *
     * @param contentLength 파일 크기
     * @param etag ETag
     * @param contentType Content-Type
     * @return S3HeadObjectResponse 인스턴스
     */
    public static S3HeadObjectResponse of(Long contentLength, String etag, String contentType) {
        return new S3HeadObjectResponse(contentLength, etag, contentType);
    }
}
