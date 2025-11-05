package com.ryuqq.fileflow.adapter.rest.upload.dto.response;

/**
 * Single Upload 완료 API Response
 *
 * <p>REST API Layer의 Single Upload 완료 응답 DTO입니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Record 패턴 사용 (Java 21)</li>
 *   <li>✅ Lombok 금지</li>
 *   <li>✅ API Layer DTO (Application Layer Response와 분리)</li>
 * </ul>
 *
 * @param fileId 생성된 FileAsset ID
 * @param etag S3 ETag
 * @param fileSize 파일 크기 (bytes)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record CompleteSingleUploadApiResponse(
    Long fileId,
    String etag,
    Long fileSize
) {
}
