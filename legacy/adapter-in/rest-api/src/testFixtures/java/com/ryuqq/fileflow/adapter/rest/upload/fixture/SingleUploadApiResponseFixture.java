package com.ryuqq.fileflow.adapter.rest.upload.fixture;

import com.ryuqq.fileflow.adapter.rest.upload.dto.response.SingleUploadApiResponse;

/**
 * SingleUploadApiResponse 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see SingleUploadApiResponse
 */
public class SingleUploadApiResponseFixture {

    /**
     * 기본값으로 SingleUploadApiResponse 생성
     *
     * @return 기본값을 가진 SingleUploadApiResponse
     */
    public static SingleUploadApiResponse create() {
        return SingleUploadApiResponse.of(
            "session_abc123",
            "https://s3.amazonaws.com/bucket/key?X-Amz-Signature=...",
            "uploads/2024/01/01/document.pdf"
        );
    }

    /**
     * 특정 세션 키로 SingleUploadApiResponse 생성
     *
     * @param sessionKey 세션 키
     * @return 지정된 세션 키를 가진 SingleUploadApiResponse
     */
    public static SingleUploadApiResponse createWithSessionKey(String sessionKey) {
        return SingleUploadApiResponse.of(
            sessionKey,
            "https://s3.amazonaws.com/bucket/key?X-Amz-Signature=...",
            "uploads/2024/01/01/document.pdf"
        );
    }

    /**
     * 모든 필드를 지정하여 SingleUploadApiResponse 생성
     *
     * @param sessionKey 세션 키
     * @param uploadUrl Presigned URL
     * @param storageKey 저장 경로
     * @return SingleUploadApiResponse
     */
    public static SingleUploadApiResponse createWith(
        String sessionKey,
        String uploadUrl,
        String storageKey
    ) {
        return SingleUploadApiResponse.of(sessionKey, uploadUrl, storageKey);
    }

    // Private 생성자
    private SingleUploadApiResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
