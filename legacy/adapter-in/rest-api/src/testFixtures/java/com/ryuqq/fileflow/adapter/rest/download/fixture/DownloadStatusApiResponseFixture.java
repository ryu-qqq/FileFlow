package com.ryuqq.fileflow.adapter.rest.download.fixture;

import com.ryuqq.fileflow.adapter.rest.download.dto.response.DownloadStatusApiResponse;

/**
 * DownloadStatusApiResponse 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see DownloadStatusApiResponse
 */
public class DownloadStatusApiResponseFixture {

    /**
     * 기본값으로 DownloadStatusApiResponse 생성 (DOWNLOADING 상태)
     *
     * @return 기본값을 가진 DownloadStatusApiResponse
     */
    public static DownloadStatusApiResponse create() {
        return DownloadStatusApiResponse.of(
            67890L,
            "DOWNLOADING",
            "https://example.com/file.pdf",
            12345L
        );
    }

    /**
     * PENDING 상태로 DownloadStatusApiResponse 생성
     *
     * @return PENDING 상태의 DownloadStatusApiResponse
     */
    public static DownloadStatusApiResponse createPending() {
        return DownloadStatusApiResponse.of(
            67890L,
            "PENDING",
            "https://example.com/file.pdf",
            12345L
        );
    }

    /**
     * COMPLETED 상태로 DownloadStatusApiResponse 생성
     *
     * @return COMPLETED 상태의 DownloadStatusApiResponse
     */
    public static DownloadStatusApiResponse createCompleted() {
        return DownloadStatusApiResponse.of(
            67890L,
            "COMPLETED",
            "https://example.com/file.pdf",
            12345L
        );
    }

    /**
     * FAILED 상태로 DownloadStatusApiResponse 생성
     *
     * @return FAILED 상태의 DownloadStatusApiResponse
     */
    public static DownloadStatusApiResponse createFailed() {
        return DownloadStatusApiResponse.of(
            67890L,
            "FAILED",
            "https://example.com/file.pdf",
            12345L
        );
    }

    /**
     * 특정 다운로드 ID로 DownloadStatusApiResponse 생성
     *
     * @param downloadId 다운로드 ID
     * @return 지정된 다운로드 ID를 가진 DownloadStatusApiResponse
     */
    public static DownloadStatusApiResponse createWithDownloadId(Long downloadId) {
        return DownloadStatusApiResponse.of(
            downloadId,
            "DOWNLOADING",
            "https://example.com/file.pdf",
            12345L
        );
    }

    /**
     * 모든 필드를 지정하여 DownloadStatusApiResponse 생성
     *
     * @param downloadId 다운로드 ID
     * @param status 다운로드 상태
     * @param sourceUrl 소스 URL
     * @param uploadSessionId 업로드 세션 ID
     * @return DownloadStatusApiResponse
     */
    public static DownloadStatusApiResponse createWith(
        Long downloadId,
        String status,
        String sourceUrl,
        Long uploadSessionId
    ) {
        return DownloadStatusApiResponse.of(downloadId, status, sourceUrl, uploadSessionId);
    }

    // Private 생성자
    private DownloadStatusApiResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
