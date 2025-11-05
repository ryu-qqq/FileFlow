package com.ryuqq.fileflow.application.download.fixture;

import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;

/**
 * ExternalDownloadResponse Test Fixture
 *
 * <p>테스트에서 ExternalDownloadResponse 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class ExternalDownloadResponseFixture {

    private static final String DEFAULT_IDEMPOTENCY_KEY = "test-idem-key-001";
    private static final Long DEFAULT_DOWNLOAD_ID = 67890L;
    private static final Long DEFAULT_UPLOAD_SESSION_ID = 12345L;
    private static final String DEFAULT_SOURCE_URL = "https://example.com/files/document.pdf";
    private static final String DEFAULT_STATUS = "PENDING";

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     */
    private ExternalDownloadResponseFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 기본 Response 생성
     *
     * @return ExternalDownloadResponse
     */
    public static ExternalDownloadResponse create() {
        return ExternalDownloadResponse.of(
            DEFAULT_IDEMPOTENCY_KEY,
            DEFAULT_DOWNLOAD_ID,
            DEFAULT_UPLOAD_SESSION_ID,
            DEFAULT_SOURCE_URL,
            DEFAULT_STATUS
        );
    }

    /**
     * 특정 상태로 Response 생성
     *
     * @param status 다운로드 상태
     * @return ExternalDownloadResponse
     */
    public static ExternalDownloadResponse createWithStatus(String status) {
        return ExternalDownloadResponse.of(
            DEFAULT_IDEMPOTENCY_KEY,
            DEFAULT_DOWNLOAD_ID,
            DEFAULT_UPLOAD_SESSION_ID,
            DEFAULT_SOURCE_URL,
            status
        );
    }

    /**
     * PENDING 상태 Response 생성
     *
     * @return ExternalDownloadResponse
     */
    public static ExternalDownloadResponse createPending() {
        return createWithStatus("PENDING");
    }

    /**
     * DOWNLOADING 상태 Response 생성
     *
     * @return ExternalDownloadResponse
     */
    public static ExternalDownloadResponse createDownloading() {
        return createWithStatus("DOWNLOADING");
    }

    /**
     * COMPLETED 상태 Response 생성
     *
     * @return ExternalDownloadResponse
     */
    public static ExternalDownloadResponse createCompleted() {
        return createWithStatus("COMPLETED");
    }

    /**
     * FAILED 상태 Response 생성
     *
     * @return ExternalDownloadResponse
     */
    public static ExternalDownloadResponse createFailed() {
        return createWithStatus("FAILED");
    }

    /**
     * 특정 값으로 Response 생성
     *
     * @param idempotencyKey 멱등키
     * @param downloadId 다운로드 ID
     * @param uploadSessionId 업로드 세션 ID
     * @param sourceUrl 소스 URL
     * @param status 상태
     * @return ExternalDownloadResponse
     */
    public static ExternalDownloadResponse create(
        String idempotencyKey,
        Long downloadId,
        Long uploadSessionId,
        String sourceUrl,
        String status
    ) {
        return ExternalDownloadResponse.of(
            idempotencyKey,
            downloadId,
            uploadSessionId,
            sourceUrl,
            status
        );
    }
}

