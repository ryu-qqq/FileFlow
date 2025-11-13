package com.ryuqq.fileflow.adapter.rest.upload.fixture;

import com.ryuqq.fileflow.adapter.rest.upload.dto.response.InitMultipartApiResponse;

/**
 * InitMultipartApiResponse 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see InitMultipartApiResponse
 */
public class InitMultipartApiResponseFixture {

    /**
     * 기본값으로 InitMultipartApiResponse 생성 (10개 파트)
     *
     * @return 기본값을 가진 InitMultipartApiResponse
     */
    public static InitMultipartApiResponse create() {
        return InitMultipartApiResponse.of(
            "mpu_abc123def456",
            "upload-xyz789",
            10,
            "uploads/2024/01/01/large-video.mp4"
        );
    }

    /**
     * 특정 파트 수로 InitMultipartApiResponse 생성
     *
     * @param totalParts 총 파트 수
     * @return 지정된 파트 수를 가진 InitMultipartApiResponse
     */
    public static InitMultipartApiResponse createWithTotalParts(Integer totalParts) {
        return InitMultipartApiResponse.of(
            "mpu_abc123def456",
            "upload-xyz789",
            totalParts,
            "uploads/2024/01/01/large-video.mp4"
        );
    }

    /**
     * 특정 Upload ID로 InitMultipartApiResponse 생성
     *
     * @param uploadId S3 Upload ID
     * @return 지정된 Upload ID를 가진 InitMultipartApiResponse
     */
    public static InitMultipartApiResponse createWithUploadId(String uploadId) {
        return InitMultipartApiResponse.of(
            "mpu_abc123def456",
            uploadId,
            10,
            "uploads/2024/01/01/large-video.mp4"
        );
    }

    /**
     * 모든 필드를 지정하여 InitMultipartApiResponse 생성
     *
     * @param sessionKey 세션 키
     * @param uploadId S3 Upload ID
     * @param totalParts 총 파트 수
     * @param storageKey 스토리지 키
     * @return InitMultipartApiResponse
     */
    public static InitMultipartApiResponse createWith(
        String sessionKey,
        String uploadId,
        Integer totalParts,
        String storageKey
    ) {
        return InitMultipartApiResponse.of(sessionKey, uploadId, totalParts, storageKey);
    }

    // Private 생성자
    private InitMultipartApiResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
