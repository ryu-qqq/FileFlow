package com.ryuqq.fileflow.adapter.rest.upload.dto.response;

/**
 * Single Upload API Response
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
 * <p><strong>클라이언트 흐름:</strong></p>
 * <ol>
 *   <li>이 응답의 uploadUrl로 HTTP PUT 요청</li>
 *   <li>파일 데이터를 Body에 포함하여 업로드</li>
 *   <li>업로드 완료 (별도 complete API 불필요)</li>
 * </ol>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class SingleUploadApiResponse {

    private String sessionKey;
    private String uploadUrl;
    private String storageKey;

    /**
     * Private Default Constructor
     */
    private SingleUploadApiResponse() {
    }

    /**
     * Private Full Constructor
     *
     * @param sessionKey 세션 키
     * @param uploadUrl Presigned URL
     * @param storageKey 저장 경로
     */
    private SingleUploadApiResponse(
        String sessionKey,
        String uploadUrl,
        String storageKey
    ) {
        this.sessionKey = sessionKey;
        this.uploadUrl = uploadUrl;
        this.storageKey = storageKey;
    }

    /**
     * Static Factory Method
     *
     * @param sessionKey 세션 키
     * @param uploadUrl Presigned URL
     * @param storageKey 저장 경로
     * @return SingleUploadApiResponse 인스턴스
     */
    public static SingleUploadApiResponse of(
        String sessionKey,
        String uploadUrl,
        String storageKey
    ) {
        return new SingleUploadApiResponse(sessionKey, uploadUrl, storageKey);
    }

    /**
     * 세션 키 조회
     *
     * @return 세션 키
     */
    public String getSessionKey() {
        return sessionKey;
    }

    /**
     * Presigned URL 조회
     *
     * @return Presigned URL
     */
    public String getUploadUrl() {
        return uploadUrl;
    }

    /**
     * 저장 경로 조회
     *
     * @return 저장 경로
     */
    public String getStorageKey() {
        return storageKey;
    }
}
