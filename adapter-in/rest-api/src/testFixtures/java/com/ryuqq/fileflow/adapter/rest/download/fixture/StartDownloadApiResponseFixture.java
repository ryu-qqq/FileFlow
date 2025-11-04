package com.ryuqq.fileflow.adapter.rest.download.fixture;

import com.ryuqq.fileflow.adapter.rest.download.dto.response.StartDownloadApiResponse;

/**
 * StartDownloadApiResponse 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see StartDownloadApiResponse
 */
public class StartDownloadApiResponseFixture {

    /**
     * 기본값으로 StartDownloadApiResponse 생성
     *
     * @return 기본값을 가진 StartDownloadApiResponse
     */
    public static StartDownloadApiResponse create() {
        return StartDownloadApiResponse.of(
            67890L,
            12345L,
            "PENDING"
        );
    }

    /**
     * 특정 다운로드 ID로 StartDownloadApiResponse 생성
     *
     * @param downloadId 다운로드 ID
     * @return 지정된 다운로드 ID를 가진 StartDownloadApiResponse
     */
    public static StartDownloadApiResponse createWithDownloadId(Long downloadId) {
        return StartDownloadApiResponse.of(
            downloadId,
            12345L,
            "PENDING"
        );
    }

    /**
     * 특정 업로드 세션 ID로 StartDownloadApiResponse 생성
     *
     * @param uploadSessionId 업로드 세션 ID
     * @return 지정된 업로드 세션 ID를 가진 StartDownloadApiResponse
     */
    public static StartDownloadApiResponse createWithUploadSessionId(Long uploadSessionId) {
        return StartDownloadApiResponse.of(
            67890L,
            uploadSessionId,
            "PENDING"
        );
    }

    /**
     * 특정 상태로 StartDownloadApiResponse 생성
     *
     * @param status 다운로드 상태
     * @return 지정된 상태를 가진 StartDownloadApiResponse
     */
    public static StartDownloadApiResponse createWithStatus(String status) {
        return StartDownloadApiResponse.of(
            67890L,
            12345L,
            status
        );
    }

    /**
     * 모든 필드를 지정하여 StartDownloadApiResponse 생성
     *
     * @param downloadId 다운로드 ID
     * @param uploadSessionId 업로드 세션 ID
     * @param status 다운로드 상태
     * @return StartDownloadApiResponse
     */
    public static StartDownloadApiResponse createWith(
        Long downloadId,
        Long uploadSessionId,
        String status
    ) {
        return StartDownloadApiResponse.of(downloadId, uploadSessionId, status);
    }

    // Private 생성자
    private StartDownloadApiResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
