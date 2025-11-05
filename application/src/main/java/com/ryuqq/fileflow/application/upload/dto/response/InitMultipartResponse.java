package com.ryuqq.fileflow.application.upload.dto.response;

/**
 * Multipart 업로드 초기화 Response
 *
 * @param sessionKey 세션 키
 * @param uploadId S3 Upload ID
 * @param totalParts 전체 파트 수
 * @param storageKey S3 Object Key
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record InitMultipartResponse(
    String sessionKey,
    String uploadId,
    Integer totalParts,
    String storageKey
) {
    /**
     * Static Factory Method
     *
     * @param sessionKey 세션 키
     * @param uploadId S3 Upload ID
     * @param totalParts 전체 파트 수
     * @param storageKey S3 Object Key
     * @return InitMultipartResponse 인스턴스
     */
    public static InitMultipartResponse of(
        String sessionKey,
        String uploadId,
        Integer totalParts,
        String storageKey
    ) {
        return new InitMultipartResponse(
            sessionKey,
            uploadId,
            totalParts,
            storageKey
        );
    }
}
