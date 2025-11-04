package com.ryuqq.fileflow.application.upload.dto.response;

/**
 * Single Upload Response
 *
 * <p>단일 업로드 초기화 응답 DTO입니다.</p>
 *
 * <p><strong>응답 필드:</strong></p>
 * <ul>
 *   <li>sessionKey: 업로드 세션 식별 키</li>
 *   <li>uploadUrl: S3 Presigned URL (단일 PUT 업로드용)</li>
 *   <li>storageKey: S3 저장 경로</li>
 * </ul>
 *
 * <p><strong>불변성 (Immutability):</strong></p>
 * <ul>
 *   <li>Java Record 사용으로 모든 필드 final</li>
 *   <li>생성 후 상태 변경 불가</li>
 * </ul>
 *
 * @param sessionKey 세션 키
 * @param uploadUrl Presigned URL
 * @param storageKey 저장 경로
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record SingleUploadResponse(
    String sessionKey,
    String uploadUrl,
    String storageKey
) {

    /**
     * Static Factory Method
     *
     * @param sessionKey 세션 키
     * @param uploadUrl Presigned URL
     * @param storageKey 저장 경로
     * @return SingleUploadResponse 인스턴스
     */
    public static SingleUploadResponse of(
        String sessionKey,
        String uploadUrl,
        String storageKey
    ) {
        return new SingleUploadResponse(
            sessionKey,
            uploadUrl,
            storageKey
        );
    }
}
