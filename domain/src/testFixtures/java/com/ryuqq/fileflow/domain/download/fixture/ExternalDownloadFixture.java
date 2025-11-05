package com.ryuqq.fileflow.domain.download.fixture;

import com.ryuqq.fileflow.domain.download.ErrorCode;
import com.ryuqq.fileflow.domain.download.ErrorMessage;
import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.download.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;
import com.ryuqq.fileflow.domain.upload.fixture.UploadSessionFixture;

import java.net.URL;
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

    private static final UploadSession DEFAULT_UPLOAD_SESSION = UploadSessionFixture.createSingle();
    private static final String DEFAULT_SOURCE_URL = "https://example.com/files/test-file.pdf";

    /**
     * 기본 ExternalDownload 생성 (INIT 상태)
     *
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createNew() {
        return ExternalDownload.forNew(DEFAULT_SOURCE_URL, DEFAULT_UPLOAD_SESSION);
    }

    /**
     * 특정 값으로 ExternalDownload 생성
     *
     * @param sourceUrl 소스 URL
     * @param uploadSession Upload Session
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload create(String sourceUrl, UploadSession uploadSession) {
        return ExternalDownload.forNew(sourceUrl, uploadSession);
    }

    /**
     * HTTP URL로 ExternalDownload 생성
     *
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createWithHttpUrl() {
        return ExternalDownload.forNew(
            "http://example.com/files/document.pdf",
            DEFAULT_UPLOAD_SESSION
        );
    }

    /**
     * 진행 중인 ExternalDownload 생성 (DOWNLOADING 상태)
     *
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createInProgress() {
        ExternalDownload download = ExternalDownload.forNew(DEFAULT_SOURCE_URL, DEFAULT_UPLOAD_SESSION);
        download.start();
        return download;
    }

    /**
     * 진행률이 있는 ExternalDownload 생성
     *
     * @param bytesTransferred 전송된 바이트
     * @param totalBytes 전체 바이트
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createWithProgress(FileSize bytesTransferred, FileSize totalBytes) {
        ExternalDownload download = ExternalDownload.forNew(DEFAULT_SOURCE_URL, DEFAULT_UPLOAD_SESSION);
        download.start();
        download.updateProgress(bytesTransferred, totalBytes);
        return download;
    }

    /**
     * 완료된 ExternalDownload 생성 (COMPLETED 상태)
     *
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createCompleted() {
        ExternalDownload download = ExternalDownload.forNew(DEFAULT_SOURCE_URL, DEFAULT_UPLOAD_SESSION);
        download.start();
        download.complete();
        return download;
    }

    /**
     * 실패한 ExternalDownload 생성 (FAILED 상태)
     *
     * @param errorCode 에러 코드
     * @param errorMessage 에러 메시지
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createFailed(ErrorCode errorCode, ErrorMessage errorMessage) {
        ExternalDownload download = ExternalDownload.forNew(DEFAULT_SOURCE_URL, DEFAULT_UPLOAD_SESSION);
        download.start();
        download.fail(errorCode, errorMessage);
        return download;
    }

    /**
     * 재시도 가능한 실패 ExternalDownload 생성 (5xx 에러)
     *
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createRetryableFailed() {
        ExternalDownload download = ExternalDownload.forNew(DEFAULT_SOURCE_URL, DEFAULT_UPLOAD_SESSION);
        download.start();
        download.fail(ErrorCode.of("500"), ErrorMessage.of("Internal Server Error"));
        return download;
    }

    /**
     * 재시도 불가능한 실패 ExternalDownload 생성 (4xx 에러)
     *
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createNonRetryableFailed() {
        ExternalDownload download = ExternalDownload.forNew(DEFAULT_SOURCE_URL, DEFAULT_UPLOAD_SESSION);
        download.start();
        download.fail(ErrorCode.of("404"), ErrorMessage.of("Not Found"));
        return download;
    }

    /**
     * 최대 재시도 횟수에 도달한 ExternalDownload 생성
     *
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createMaxRetriesReached() {
        ExternalDownload download = ExternalDownload.forNew(DEFAULT_SOURCE_URL, DEFAULT_UPLOAD_SESSION);
        download.start();
        // 최대 재시도 횟수(3회)까지 실패 처리
        download.fail(ErrorCode.of("500"), ErrorMessage.of("Internal Server Error"));
        download.fail(ErrorCode.of("500"), ErrorMessage.of("Internal Server Error"));
        download.fail(ErrorCode.of("500"), ErrorMessage.of("Internal Server Error"));
        return download;
    }

    /**
     * Timeout 실패 ExternalDownload 생성 (재시도 가능)
     *
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createTimeoutFailed() {
        ExternalDownload download = ExternalDownload.forNew(DEFAULT_SOURCE_URL, DEFAULT_UPLOAD_SESSION);
        download.start();
        download.fail(ErrorCode.of("TIMEOUT"), ErrorMessage.of("Request timeout"));
        return download;
    }

    /**
     * 중단된 ExternalDownload 생성 (ABORTED 상태)
     *
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload createAborted() {
        ExternalDownload download = ExternalDownload.forNew(DEFAULT_SOURCE_URL, DEFAULT_UPLOAD_SESSION);
        download.start();
        download.abort();
        return download;
    }

    /**
     * DB에서 복원한 ExternalDownload 생성 (Reconstitute)
     *
     * @param id Download ID
     * @param uploadSessionId Upload Session ID
     * @param sourceUrl 소스 URL
     * @param bytesTransferred 전송된 바이트 수
     * @param totalBytes 총 바이트 수
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @param lastRetryAt 마지막 재시도 시간
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     * @param errorCode 오류 코드
     * @param errorMessage 오류 메시지
     * @return ExternalDownload 인스턴스
     */
    public static ExternalDownload reconstitute(
        Long id,
        UploadSessionId uploadSessionId,
        URL sourceUrl,
        FileSize bytesTransferred,
        FileSize totalBytes,
        ExternalDownloadStatus status,
        Integer retryCount,
        LocalDateTime lastRetryAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        ErrorCode errorCode,
        ErrorMessage errorMessage
    ) {
        return ExternalDownload.reconstitute(
            new ExternalDownloadId(id),
            uploadSessionId,
            sourceUrl,
            bytesTransferred,
            totalBytes,
            status,
            retryCount,
            lastRetryAt,
            createdAt,
            updatedAt,
            errorCode,
            errorMessage
        );
    }

    /**
     * 기본값으로 Reconstitute된 ExternalDownload 생성
     *
     * @param id Download ID
     * @return ExternalDownload 인스턴스
     * @throws Exception URL 파싱 실패 시
     */
    public static ExternalDownload reconstituteDefault(Long id) {
        try {
            LocalDateTime now = LocalDateTime.now();
            // UploadSessionId는 ID가 있어야 하므로 직접 생성
            UploadSessionId uploadSessionId = UploadSessionId.of(1L);
            return ExternalDownload.reconstitute(
                new ExternalDownloadId(id),
                uploadSessionId,
                new URL(DEFAULT_SOURCE_URL),
                FileSize.of(0L),
                FileSize.of(0L),
                ExternalDownloadStatus.INIT,
                0,
                null,
                now,
                now,
                null,
                null
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create default ExternalDownload", e);
        }
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
        private UploadSession uploadSession = DEFAULT_UPLOAD_SESSION;
        private String sourceUrl = DEFAULT_SOURCE_URL;
        private boolean shouldStart = false;
        private boolean shouldComplete = false;
        private boolean shouldFail = false;
        private ErrorCode errorCode = null;
        private ErrorMessage errorMessage = null;
        private FileSize bytesTransferred = null;
        private FileSize totalBytes = null;
        private boolean shouldAbort = false;

        public ExternalDownloadBuilder uploadSession(UploadSession uploadSession) {
            this.uploadSession = uploadSession;
            return this;
        }

        public ExternalDownloadBuilder sourceUrl(String sourceUrl) {
            this.sourceUrl = sourceUrl;
            return this;
        }

        public ExternalDownloadBuilder start() {
            this.shouldStart = true;
            return this;
        }

        public ExternalDownloadBuilder complete() {
            this.shouldComplete = true;
            return this;
        }

        public ExternalDownloadBuilder fail(ErrorCode errorCode, ErrorMessage errorMessage) {
            this.shouldFail = true;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            return this;
        }

        public ExternalDownloadBuilder progress(FileSize bytesTransferred, FileSize totalBytes) {
            this.bytesTransferred = bytesTransferred;
            this.totalBytes = totalBytes;
            return this;
        }

        public ExternalDownloadBuilder abort() {
            this.shouldAbort = true;
            return this;
        }

        public ExternalDownload build() {
            ExternalDownload download = ExternalDownload.forNew(sourceUrl, uploadSession);

            if (shouldStart) {
                download.start();
            }

            if (bytesTransferred != null && totalBytes != null) {
                download.updateProgress(bytesTransferred, totalBytes);
            }

            if (shouldComplete) {
                download.complete();
            } else if (shouldFail) {
                download.fail(errorCode, errorMessage);
            } else if (shouldAbort) {
                download.abort();
            }

            return download;
        }
    }
}
