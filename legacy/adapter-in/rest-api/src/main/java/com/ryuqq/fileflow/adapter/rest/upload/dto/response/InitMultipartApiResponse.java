package com.ryuqq.fileflow.adapter.rest.upload.dto.response;

/**
 * Multipart Upload 초기화 API Response
 *
 * <p>Multipart Upload 초기화 성공 시 반환되는 응답 DTO입니다.</p>
 *
 * <p><strong>Response Body 예시:</strong></p>
 * <pre>{@code
 * {
 *   "sessionKey": "mpu_abc123def456",
 *   "uploadId": "upload-xyz789",
 *   "totalParts": 10,
 *   "storageKey": "uploads/2024/10/31/large-video.mp4"
 * }
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class InitMultipartApiResponse {

    private String sessionKey;
    private String uploadId;
    private Integer totalParts;
    private String storageKey;

    /**
     * Private 생성자
     */
    private InitMultipartApiResponse() {
    }

    /**
     * 전체 생성자
     *
     * @param sessionKey 세션 키
     * @param uploadId S3 Upload ID
     * @param totalParts 총 파트 수
     * @param storageKey 스토리지 키
     */
    private InitMultipartApiResponse(String sessionKey, String uploadId, Integer totalParts, String storageKey) {
        this.sessionKey = sessionKey;
        this.uploadId = uploadId;
        this.totalParts = totalParts;
        this.storageKey = storageKey;
    }

    /**
     * Static Factory Method
     *
     * @param sessionKey 세션 키
     * @param uploadId S3 Upload ID
     * @param totalParts 총 파트 수
     * @param storageKey 스토리지 키
     * @return InitMultipartApiResponse 인스턴스
     */
    public static InitMultipartApiResponse of(String sessionKey, String uploadId, Integer totalParts, String storageKey) {
        return new InitMultipartApiResponse(sessionKey, uploadId, totalParts, storageKey);
    }

    /**
     * 세션 키를 반환합니다.
     *
     * @return 세션 키
     */
    public String getSessionKey() {
        return sessionKey;
    }

    /**
     * S3 Upload ID를 반환합니다.
     *
     * @return S3 Upload ID
     */
    public String getUploadId() {
        return uploadId;
    }

    /**
     * 총 파트 수를 반환합니다.
     *
     * @return 총 파트 수
     */
    public Integer getTotalParts() {
        return totalParts;
    }

    /**
     * 스토리지 키를 반환합니다.
     *
     * @return 스토리지 키
     */
    public String getStorageKey() {
        return storageKey;
    }
}
