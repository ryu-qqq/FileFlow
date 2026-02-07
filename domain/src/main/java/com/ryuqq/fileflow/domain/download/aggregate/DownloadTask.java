package com.ryuqq.fileflow.domain.download.aggregate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.download.event.DownloadCompletedEvent;
import com.ryuqq.fileflow.domain.download.exception.DownloadErrorCode;
import com.ryuqq.fileflow.domain.download.exception.DownloadException;
import com.ryuqq.fileflow.domain.download.id.DownloadTaskId;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;

/**
 * 외부 다운로드 작업 Aggregate Root.
 *
 * <p>외부 URL에서 파일을 가져와 S3에 저장하는 비동기 작업을 표현합니다.
 * <p>라이프사이클: QUEUED → DOWNLOADING → COMPLETED | FAILED → QUEUED (재시도)
 */
public class DownloadTask {

    private static final int DEFAULT_MAX_RETRIES = 3;

    private final DownloadTaskId id;
    private final String sourceUrl;
    private final String s3Key;
    private final String bucket;
    private final AccessType accessType;
    private final String purpose;
    private final String source;
    private DownloadTaskStatus status;
    private int retryCount;
    private final int maxRetries;
    private final String callbackUrl;
    private String lastError;
    private final Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;

    private final List<DomainEvent> events = new ArrayList<>();

    private DownloadTask(DownloadTaskId id, String sourceUrl, String s3Key, String bucket,
                         AccessType accessType, String purpose, String source,
                         DownloadTaskStatus status, int retryCount, int maxRetries,
                         String callbackUrl, String lastError,
                         Instant createdAt, Instant startedAt, Instant completedAt) {
        this.id = id;
        this.sourceUrl = sourceUrl;
        this.s3Key = s3Key;
        this.bucket = bucket;
        this.accessType = accessType;
        this.purpose = purpose;
        this.source = source;
        this.status = status;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.callbackUrl = callbackUrl;
        this.lastError = lastError;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public static DownloadTask forNew(DownloadTaskId id, String sourceUrl, String s3Key,
                                       String bucket, AccessType accessType,
                                       String purpose, String source,
                                       String callbackUrl, Instant now) {
        return new DownloadTask(id, sourceUrl, s3Key, bucket, accessType, purpose, source,
                DownloadTaskStatus.QUEUED, 0, DEFAULT_MAX_RETRIES,
                callbackUrl, null, now, null, null);
    }

    public static DownloadTask reconstitute(DownloadTaskId id, String sourceUrl, String s3Key,
                                             String bucket, AccessType accessType,
                                             String purpose, String source,
                                             DownloadTaskStatus status, int retryCount, int maxRetries,
                                             String callbackUrl, String lastError,
                                             Instant createdAt, Instant startedAt, Instant completedAt) {
        return new DownloadTask(id, sourceUrl, s3Key, bucket, accessType, purpose, source,
                status, retryCount, maxRetries, callbackUrl, lastError,
                createdAt, startedAt, completedAt);
    }

    /**
     * 다운로드 시작.
     */
    public void start(Instant now) {
        if (this.status != DownloadTaskStatus.QUEUED) {
            throw new DownloadException(DownloadErrorCode.INVALID_DOWNLOAD_STATUS,
                    "Cannot start download in status: " + this.status);
        }
        this.status = DownloadTaskStatus.DOWNLOADING;
        this.startedAt = now;
    }

    /**
     * 다운로드 완료 처리.
     */
    public void complete(String fileName, String contentType, long fileSize, String etag, Instant now) {
        if (this.status != DownloadTaskStatus.DOWNLOADING) {
            throw new DownloadException(DownloadErrorCode.INVALID_DOWNLOAD_STATUS,
                    "Cannot complete download in status: " + this.status);
        }
        this.status = DownloadTaskStatus.COMPLETED;
        this.completedAt = now;
        this.lastError = null;

        registerEvent(DownloadCompletedEvent.of(
                id.value(), s3Key, bucket, accessType,
                fileName, contentType, fileSize, etag, purpose, source, now
        ));
    }

    /**
     * 다운로드 실패 처리. 재시도 가능하면 QUEUED로 복원.
     */
    public void fail(String errorMessage, Instant now) {
        this.lastError = errorMessage;
        this.retryCount++;

        if (this.retryCount >= this.maxRetries) {
            this.status = DownloadTaskStatus.FAILED;
            this.completedAt = now;
        } else {
            this.status = DownloadTaskStatus.QUEUED;
            this.startedAt = null;
        }
    }

    public boolean canRetry() {
        return retryCount < maxRetries;
    }

    public boolean hasCallback() {
        return callbackUrl != null && !callbackUrl.isBlank();
    }

    // -- query methods --

    public DownloadTaskId id() {
        return id;
    }

    public String sourceUrl() {
        return sourceUrl;
    }

    public String s3Key() {
        return s3Key;
    }

    public String bucket() {
        return bucket;
    }

    public AccessType accessType() {
        return accessType;
    }

    public String purpose() {
        return purpose;
    }

    public String source() {
        return source;
    }

    public DownloadTaskStatus status() {
        return status;
    }

    public int retryCount() {
        return retryCount;
    }

    public int maxRetries() {
        return maxRetries;
    }

    public String callbackUrl() {
        return callbackUrl;
    }

    public String lastError() {
        return lastError;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant completedAt() {
        return completedAt;
    }

    // -- event management --

    protected void registerEvent(DomainEvent event) {
        events.add(event);
    }

    public List<DomainEvent> pollEvents() {
        List<DomainEvent> snapshot = Collections.unmodifiableList(new ArrayList<>(events));
        events.clear();
        return snapshot;
    }

    // -- equals/hashCode ID 기반 --

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DownloadTask that = (DownloadTask) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
