package com.ryuqq.fileflow.domain.download;

import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * External Download Aggregate Root
 * 외부 URL로부터 파일을 다운로드하여 S3에 저장
 *
 * <p>비즈니스 규칙:</p>
 * <ul>
 *   <li>HTTP/HTTPS만 지원</li>
 *   <li>최대 3회 재시도 (지수 백오프)</li>
 *   <li>5xx, Timeout 오류만 재시도</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class ExternalDownload {

    private final Long id;
    private final UploadSessionId uploadSessionId;
    private final URL sourceUrl;
    private FileSize bytesTransferred;
    private FileSize totalBytes;
    private ExternalDownloadStatus status;
    private Integer retryCount;
    private final Integer maxRetry = 3;
    private LocalDateTime lastRetryAt;
    private ErrorCode errorCode;
    private ErrorMessage errorMessage;

    /**
     * External Download 상태 Enum
     */
    public enum ExternalDownloadStatus {
        INIT,
        DOWNLOADING,
        COMPLETED,
        FAILED,
        ABORTED
    }

    /**
     * Private 생성자
     *
     * @param id External Download ID (null 가능 - 신규 생성 시)
     * @param uploadSessionId Upload Session ID
     * @param url 소스 URL
     */
    private ExternalDownload(Long id, UploadSessionId uploadSessionId, String url) {
        this.id = id;
        this.uploadSessionId = uploadSessionId;
        this.sourceUrl = validateAndParseUrl(url);
        this.status = ExternalDownloadStatus.INIT;
        this.bytesTransferred = FileSize.of(0L);
        this.retryCount = 0;
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id External Download ID
     * @param uploadSessionId Upload Session ID
     * @param sourceUrl 소스 URL
     * @param bytesTransferred 전송된 바이트 수
     * @param totalBytes 총 바이트 수
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @param lastRetryAt 마지막 재시도 시간
     * @param errorCode 오류 코드
     * @param errorMessage 오류 메시지
     */
    private ExternalDownload(
        Long id,
        UploadSessionId uploadSessionId,
        URL sourceUrl,
        FileSize bytesTransferred,
        FileSize totalBytes,
        ExternalDownloadStatus status,
        Integer retryCount,
        LocalDateTime lastRetryAt,
        ErrorCode errorCode,
        ErrorMessage errorMessage
    ) {
        this.id = id;
        this.uploadSessionId = uploadSessionId;
        this.sourceUrl = sourceUrl;
        this.bytesTransferred = bytesTransferred;
        this.totalBytes = totalBytes;
        this.status = status;
        this.retryCount = retryCount;
        this.lastRetryAt = lastRetryAt;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Static Factory Method - 신규 External Download 생성
     *
     * @param sourceUrl 소스 URL
     * @param uploadSessionId Upload Session ID
     * @return 생성된 ExternalDownload (ID = null)
     * @throws IllegalArgumentException URL이 유효하지 않은 경우
     */
    public static ExternalDownload create(String sourceUrl, UploadSessionId uploadSessionId) {
        return new ExternalDownload(null, uploadSessionId, sourceUrl);
    }

    /**
     * DB에서 조회한 데이터로 ExternalDownload 재구성 (Static Factory Method)
     *
     * @param id External Download ID (필수)
     * @param uploadSessionId Upload Session ID
     * @param sourceUrl 소스 URL
     * @param bytesTransferred 전송된 바이트 수
     * @param totalBytes 총 바이트 수
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @param lastRetryAt 마지막 재시도 시간
     * @param errorCode 오류 코드
     * @param errorMessage 오류 메시지
     * @return 재구성된 ExternalDownload
     * @throws IllegalArgumentException id가 null인 경우
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
        ErrorCode errorCode,
        ErrorMessage errorMessage
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new ExternalDownload(
            id,
            uploadSessionId,
            sourceUrl,
            bytesTransferred,
            totalBytes,
            status,
            retryCount,
            lastRetryAt,
            errorCode,
            errorMessage
        );
    }

    /**
     * 다운로드 시작
     * 상태: INIT → DOWNLOADING
     *
     * @throws IllegalStateException INIT 상태가 아닌 경우
     */
    public void start() {
        if (this.status != ExternalDownloadStatus.INIT) {
            throw new IllegalStateException(
                "Can only start from INIT state: " + status
            );
        }
        this.status = ExternalDownloadStatus.DOWNLOADING;
    }

    /**
     * 진행률 업데이트
     *
     * @param transferred 전송된 바이트 수
     * @param total 총 바이트 수
     * @throws IllegalStateException 다운로드 중 상태가 아닌 경우
     */
    public void updateProgress(FileSize transferred, FileSize total) {
        if (this.status != ExternalDownloadStatus.DOWNLOADING) {
            throw new IllegalStateException("Not downloading: " + status);
        }
        this.bytesTransferred = transferred;
        this.totalBytes = total;
    }

    /**
     * 다운로드 완료
     * 상태: DOWNLOADING → COMPLETED
     *
     * @throws IllegalStateException 다운로드 중 상태가 아닌 경우
     */
    public void complete() {
        if (this.status != ExternalDownloadStatus.DOWNLOADING) {
            throw new IllegalStateException("Not downloading: " + status);
        }
        this.status = ExternalDownloadStatus.COMPLETED;
    }

    /**
     * 다운로드 실패 처리
     * 재시도 가능한 경우 상태 유지, 불가능한 경우 FAILED
     *
     * @param errorCode 오류 코드
     * @param errorMessage 오류 메시지
     */
    public void fail(ErrorCode errorCode, ErrorMessage errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;

        if (canRetry(errorCode)) {
            this.retryCount++;
            this.lastRetryAt = LocalDateTime.now();
        } else {
            this.status = ExternalDownloadStatus.FAILED;
        }
    }

    /**
     * 다운로드 중단
     *
     * @throws IllegalStateException 이미 완료된 경우
     */
    public void abort() {
        if (this.status == ExternalDownloadStatus.COMPLETED) {
            throw new IllegalStateException("Cannot abort completed download");
        }
        this.status = ExternalDownloadStatus.ABORTED;
    }

    /**
     * 재시도 가능 여부
     *
     * @param errorCode 오류 코드
     * @return 재시도 가능하면 true
     */
    public boolean canRetry(ErrorCode errorCode) {
        return isRetryableError(errorCode) && retryCount < maxRetry;
    }

    /**
     * 진행률 계산 (%)
     *
     * @return 진행률 (0-100)
     */
    public int getProgressPercentage() {
        if (totalBytes == null || totalBytes.bytes() == 0) {
            return 0;
        }
        return (int) ((bytesTransferred.bytes() * 100) / totalBytes.bytes());
    }

    /**
     * 다음 재시도까지의 대기 시간 계산 (지수 백오프)
     *
     * @return 대기 시간
     */
    public Duration getNextRetryDelay() {
        if (retryCount >= maxRetry) {
            return Duration.ZERO;
        }
        return Duration.ofSeconds((long) Math.pow(2, retryCount));
    }

    /**
     * URL 검증 및 파싱
     *
     * @param url URL 문자열
     * @return 파싱된 URL
     * @throws IllegalArgumentException URL이 유효하지 않은 경우
     */
    private static URL validateAndParseUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }

        try {
            URL parsedUrl = new URL(url);
            String protocol = parsedUrl.getProtocol();

            if (!protocol.matches("https?")) {
                throw new IllegalArgumentException(
                    "Only HTTP/HTTPS protocols are supported: " + protocol
                );
            }

            return parsedUrl;

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }

    /**
     * 재시도 가능한 오류인지 판단
     * 5xx 서버 오류, Timeout만 재시도
     *
     * @param errorCode 오류 코드
     * @return 재시도 가능하면 true
     */
    private boolean isRetryableError(ErrorCode errorCode) {
        if (errorCode == null) {
            return false;
        }

        String code = errorCode.value();

        if (code.startsWith("5")) {
            return true;
        }

        if ("TIMEOUT".equals(code) || "READ_TIMEOUT".equals(code)) {
            return true;
        }

        return false;
    }

    /**
     * External Download ID를 반환합니다.
     *
     * @return External Download ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Upload Session ID를 반환합니다.
     *
     * @return Upload Session ID
     */
    public UploadSessionId getUploadSessionId() {
        return uploadSessionId;
    }

    /**
     * 소스 URL을 반환합니다.
     *
     * @return 소스 URL
     */
    public URL getSourceUrl() {
        return sourceUrl;
    }

    /**
     * 상태를 반환합니다.
     *
     * @return 상태
     */
    public ExternalDownloadStatus getStatus() {
        return status;
    }

    /**
     * 전송된 바이트 수를 반환합니다.
     *
     * @return 전송된 바이트 수
     */
    public FileSize getBytesTransferred() {
        return bytesTransferred;
    }

    /**
     * 총 바이트 수를 반환합니다.
     *
     * @return 총 바이트 수
     */
    public FileSize getTotalBytes() {
        return totalBytes;
    }

    /**
     * 재시도 횟수를 반환합니다.
     *
     * @return 재시도 횟수
     */
    public Integer getRetryCount() {
        return retryCount;
    }

    /**
     * 오류 코드를 반환합니다.
     *
     * @return 오류 코드
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 오류 메시지를 반환합니다.
     *
     * @return 오류 메시지
     */
    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    /**
     * 동등성을 비교합니다.
     *
     * @param o 비교 대상 객체
     * @return 동등 여부
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExternalDownload that = (ExternalDownload) o;
        return Objects.equals(id, that.id);
    }

    /**
     * 해시코드를 반환합니다.
     *
     * @return 해시코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * @return ExternalDownload 정보 문자열
     */
    @Override
    public String toString() {
        return "ExternalDownload{" +
            "id=" + id +
            ", uploadSessionId=" + uploadSessionId +
            ", sourceUrl=" + sourceUrl +
            ", status=" + status +
            ", retryCount=" + retryCount +
            '}';
    }
}
