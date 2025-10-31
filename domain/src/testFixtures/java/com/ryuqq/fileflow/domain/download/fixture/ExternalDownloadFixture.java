package com.ryuqq.fileflow.domain.download.fixture;

import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.download.ExternalDownload.DownloadStatus;
import com.ryuqq.fileflow.domain.download.ExternalDownload.ErrorType;

import java.time.LocalDateTime;

/**
 * ExternalDownload Test Fixture
 *
 * <p>테스트에서 ExternalDownload 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Sangwon Ryu
 * @since 2025-10-31
 */
public class ExternalDownloadFixture {

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     */
    private ExternalDownloadFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final Long DEFAULT_TENANT_ID = 1L;
    private static final String DEFAULT_URL = "https://example.com/files/test-file.pdf";
    private static final String DEFAULT_TARGET_PATH = "downloads/test-file.pdf";

    /**
     * 기본 ExternalDownload 생성 (PENDING 상태)
     *
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createNew() {
        return ExternalDownload.create(DEFAULT_TENANT_ID, DEFAULT_URL, DEFAULT_TARGET_PATH);
    }

    /**
     * 특정 값으로 ExternalDownload 생성
     *
     * @param tenantId 테넌트 ID
     * @param url 다운로드 URL
     * @param targetPath 대상 경로
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload create(Long tenantId, String url, String targetPath) {
        return ExternalDownload.create(tenantId, url, targetPath);
    }

    /**
     * HTTP URL로 ExternalDownload 생성
     *
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createWithHttpUrl() {
        return ExternalDownload.create(
            DEFAULT_TENANT_ID,
            "http://example.com/files/document.pdf",
            DEFAULT_TARGET_PATH
        );
    }

    /**
     * 진행 중인 ExternalDownload 생성 (IN_PROGRESS 상태)
     *
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createInProgress() {
        ExternalDownload download = ExternalDownload.create(DEFAULT_TENANT_ID, DEFAULT_URL, DEFAULT_TARGET_PATH);
        download.start();
        return download;
    }

    /**
     * 진행률이 있는 ExternalDownload 생성
     *
     * @param bytesDownloaded 다운로드된 바이트
     * @param totalBytes 전체 바이트
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createWithProgress(Long bytesDownloaded, Long totalBytes) {
        ExternalDownload download = ExternalDownload.create(DEFAULT_TENANT_ID, DEFAULT_URL, DEFAULT_TARGET_PATH);
        download.start();
        download.updateProgress(bytesDownloaded, totalBytes);
        return download;
    }

    /**
     * 완료된 ExternalDownload 생성 (COMPLETED 상태)
     *
     * @param fileId 생성된 파일 ID
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createCompleted(Long fileId) {
        ExternalDownload download = ExternalDownload.create(DEFAULT_TENANT_ID, DEFAULT_URL, DEFAULT_TARGET_PATH);
        download.start();
        download.complete(fileId, "downloaded-file.pdf", 1024L);
        return download;
    }

    /**
     * 실패한 ExternalDownload 생성 (FAILED 상태)
     *
     * @param errorType 에러 타입
     * @param errorMessage 에러 메시지
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createFailed(ErrorType errorType, String errorMessage) {
        ExternalDownload download = ExternalDownload.create(DEFAULT_TENANT_ID, DEFAULT_URL, DEFAULT_TARGET_PATH);
        download.start();
        download.fail(errorType, errorMessage);
        return download;
    }

    /**
     * 재시도 가능한 실패 ExternalDownload 생성
     *
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createRetryableFailed() {
        ExternalDownload download = ExternalDownload.create(DEFAULT_TENANT_ID, DEFAULT_URL, DEFAULT_TARGET_PATH);
        download.start();
        download.fail(ErrorType.HTTP_5XX, "Internal Server Error");
        return download;
    }

    /**
     * 재시도 불가능한 실패 ExternalDownload 생성
     *
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createNonRetryableFailed() {
        ExternalDownload download = ExternalDownload.create(DEFAULT_TENANT_ID, DEFAULT_URL, DEFAULT_TARGET_PATH);
        download.start();
        download.fail(ErrorType.HTTP_4XX, "Not Found");
        return download;
    }

    /**
     * 최대 재시도 횟수에 도달한 ExternalDownload 생성
     *
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createMaxRetriesReached() {
        ExternalDownload download = ExternalDownload.create(DEFAULT_TENANT_ID, DEFAULT_URL, DEFAULT_TARGET_PATH);

        for (int i = 0; i < 5; i++) {
            download.start();
            download.fail(ErrorType.HTTP_5XX, "Server Error");
        }

        return download;
    }

    /**
     * DB에서 복원한 ExternalDownload 생성 (Reconstitute)
     *
     * @param id Download ID
     * @param tenantId 테넌트 ID
     * @param url 다운로드 URL
     * @param targetPath 대상 경로
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @param lastErrorType 마지막 에러 타입
     * @param lastErrorMessage 마지막 에러 메시지
     * @param fileId 파일 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param bytesDownloaded 다운로드된 바이트
     * @param totalBytes 전체 바이트
     * @param startedAt 시작 시간
     * @param completedAt 완료 시간
     * @param failedAt 실패 시간
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload reconstitute(
        Long id,
        Long tenantId,
        String url,
        String targetPath,
        DownloadStatus status,
        Integer retryCount,
        ErrorType lastErrorType,
        String lastErrorMessage,
        Long fileId,
        String fileName,
        Long fileSize,
        Long bytesDownloaded,
        Long totalBytes,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime failedAt
    ) {
        return ExternalDownload.reconstitute(
            id,
            tenantId,
            url,
            targetPath,
            status,
            retryCount,
            lastErrorType,
            lastErrorMessage,
            fileId,
            fileName,
            fileSize,
            bytesDownloaded,
            totalBytes,
            startedAt,
            completedAt,
            failedAt
        );
    }

    /**
     * 기본값으로 Reconstitute된 ExternalDownload 생성
     *
     * @param id Download ID
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload reconstituteDefault(Long id) {
        return ExternalDownload.reconstitute(
            id,
            DEFAULT_TENANT_ID,
            DEFAULT_URL,
            DEFAULT_TARGET_PATH,
            DownloadStatus.PENDING,
            0,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    /**
     * Builder 패턴으로 ExternalDownload 생성
     *
     * @return ExternalDownloadBuilder 인스턴스
     */
    public static ExternalDownloadBuilder builder() {
        return new ExternalDownloadBuilder();
    }

    /**
     * ExternalDownload Builder
     */
    public static class ExternalDownloadBuilder {
        private Long tenantId = DEFAULT_TENANT_ID;
        private String url = DEFAULT_URL;
        private String targetPath = DEFAULT_TARGET_PATH;
        private boolean shouldStart = false;
        private boolean shouldComplete = false;
        private Long fileId = null;
        private String fileName = null;
        private Long fileSize = null;
        private boolean shouldFail = false;
        private ErrorType errorType = null;
        private String errorMessage = null;
        private Long bytesDownloaded = null;
        private Long totalBytes = null;

        public ExternalDownloadBuilder tenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public ExternalDownloadBuilder url(String url) {
            this.url = url;
            return this;
        }

        public ExternalDownloadBuilder targetPath(String targetPath) {
            this.targetPath = targetPath;
            return this;
        }

        public ExternalDownloadBuilder start() {
            this.shouldStart = true;
            return this;
        }

        public ExternalDownloadBuilder complete(Long fileId, String fileName, Long fileSize) {
            this.shouldComplete = true;
            this.fileId = fileId;
            this.fileName = fileName;
            this.fileSize = fileSize;
            return this;
        }

        public ExternalDownloadBuilder fail(ErrorType errorType, String errorMessage) {
            this.shouldFail = true;
            this.errorType = errorType;
            this.errorMessage = errorMessage;
            return this;
        }

        public ExternalDownloadBuilder progress(Long bytesDownloaded, Long totalBytes) {
            this.bytesDownloaded = bytesDownloaded;
            this.totalBytes = totalBytes;
            return this;
        }

        public ExternalDownload build() {
            ExternalDownload download = ExternalDownload.create(tenantId, url, targetPath);

            if (shouldStart) {
                download.start();
            }

            if (bytesDownloaded != null && totalBytes != null) {
                download.updateProgress(bytesDownloaded, totalBytes);
            }

            if (shouldComplete && fileId != null) {
                download.complete(fileId, fileName, fileSize);
            } else if (shouldFail) {
                download.fail(errorType, errorMessage);
            }

            return download;
        }
    }
}
