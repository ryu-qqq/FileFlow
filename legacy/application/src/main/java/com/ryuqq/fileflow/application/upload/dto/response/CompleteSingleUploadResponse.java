package com.ryuqq.fileflow.application.upload.dto.response;

/**
 * Single 업로드 완료 Response
 *
 * <p>Single Upload 완료 후 생성된 FileAsset 정보를 담는 Response DTO입니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Record 패턴 사용 (Java 21)</li>
 *   <li>✅ Lombok 금지</li>
 *   <li>✅ Static Factory Method 제공</li>
 * </ul>
 *
 * @param fileId 생성된 FileAsset ID
 * @param etag S3 ETag (파일 무결성 확인용)
 * @param fileSize 파일 크기 (bytes)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record CompleteSingleUploadResponse(
    Long fileId,
    String etag,
    Long fileSize
) {
    /**
     * Static Factory Method
     *
     * @param fileId 파일 ID
     * @param etag ETag
     * @param fileSize 파일 크기
     * @return CompleteSingleUploadResponse 인스턴스
     */
    public static CompleteSingleUploadResponse of(Long fileId, String etag, Long fileSize) {
        return new CompleteSingleUploadResponse(fileId, etag, fileSize);
    }
}
