package com.ryuqq.fileflow.domain.download.aggregate;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.common.vo.StorageInfo;
import com.ryuqq.fileflow.domain.download.exception.DownloadErrorCode;
import com.ryuqq.fileflow.domain.download.exception.DownloadException;
import com.ryuqq.fileflow.domain.download.id.DownloadTaskId;
import com.ryuqq.fileflow.domain.download.vo.CallbackInfo;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import com.ryuqq.fileflow.domain.download.vo.DownloadedFileInfo;
import com.ryuqq.fileflow.domain.download.vo.RetryPolicy;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 외부 다운로드 작업 Aggregate Root.
 *
 * <p>외부 URL에서 파일을 가져와 S3에 저장하는 비동기 작업을 표현합니다.
 *
 * <p>라이프사이클: QUEUED → DOWNLOADING → COMPLETED | FAILED → QUEUED (재시도)
 */
public class DownloadTask {

    private static final int DEFAULT_MAX_RETRIES = 3;

    private final DownloadTaskId id;
    private final SourceUrl sourceUrl;
    private final StorageInfo storageInfo;
    private final String purpose;
    private final String source;
    private DownloadTaskStatus status;
    private RetryPolicy retryPolicy;
    private final CallbackInfo callbackInfo;
    private String lastError;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant startedAt;
    private Instant completedAt;

    private final List<DomainEvent> events = new ArrayList<>();

    private DownloadTask(
            DownloadTaskId id,
            SourceUrl sourceUrl,
            StorageInfo storageInfo,
            String purpose,
            String source,
            DownloadTaskStatus status,
            RetryPolicy retryPolicy,
            CallbackInfo callbackInfo,
            String lastError,
            Instant createdAt,
            Instant updatedAt,
            Instant startedAt,
            Instant completedAt) {
        this.id = id;
        this.sourceUrl = sourceUrl;
        this.storageInfo = storageInfo;
        this.purpose = purpose;
        this.source = source;
        this.status = status;
        this.retryPolicy = retryPolicy;
        this.callbackInfo = callbackInfo;
        this.lastError = lastError;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public static DownloadTask forNew(
            DownloadTaskId id,
            SourceUrl sourceUrl,
            StorageInfo storageInfo,
            String purpose,
            String source,
            CallbackInfo callbackInfo,
            Instant now) {
        return new DownloadTask(
                id,
                sourceUrl,
                storageInfo,
                purpose,
                source,
                DownloadTaskStatus.QUEUED,
                RetryPolicy.ofDefault(DEFAULT_MAX_RETRIES),
                callbackInfo,
                null,
                now,
                now,
                null,
                null);
    }

    public static DownloadTask reconstitute(
            DownloadTaskId id,
            SourceUrl sourceUrl,
            StorageInfo storageInfo,
            String purpose,
            String source,
            DownloadTaskStatus status,
            RetryPolicy retryPolicy,
            CallbackInfo callbackInfo,
            String lastError,
            Instant createdAt,
            Instant updatedAt,
            Instant startedAt,
            Instant completedAt) {
        return new DownloadTask(
                id,
                sourceUrl,
                storageInfo,
                purpose,
                source,
                status,
                retryPolicy,
                callbackInfo,
                lastError,
                createdAt,
                updatedAt,
                startedAt,
                completedAt);
    }

    /** 다운로드 시작. */
    public void start(Instant now) {
        if (this.status != DownloadTaskStatus.QUEUED) {
            throw new DownloadException(
                    DownloadErrorCode.INVALID_DOWNLOAD_STATUS,
                    "Cannot start download in status: " + this.status);
        }
        this.status = DownloadTaskStatus.DOWNLOADING;
        this.startedAt = now;
        this.updatedAt = now;
    }

    /** 다운로드 완료 처리. */
    public void complete(DownloadedFileInfo fileInfo) {
        Instant now = fileInfo.completedAt();
        if (this.status != DownloadTaskStatus.DOWNLOADING) {
            throw new DownloadException(
                    DownloadErrorCode.INVALID_DOWNLOAD_STATUS,
                    "Cannot complete download in status: " + this.status);
        }
        this.status = DownloadTaskStatus.COMPLETED;
        this.completedAt = now;
        this.updatedAt = now;
        this.lastError = null;
    }

    /** 다운로드 실패 처리. 재시도 가능하면 QUEUED로 복원. */
    public void fail(String errorMessage, Instant now) {
        this.lastError = errorMessage;
        this.retryPolicy = retryPolicy.increment();
        this.updatedAt = now;

        if (this.retryPolicy.isExhausted()) {
            this.status = DownloadTaskStatus.FAILED;
            this.completedAt = now;
        } else {
            this.status = DownloadTaskStatus.QUEUED;
            this.startedAt = null;
        }
    }

    public boolean canRetry() {
        return retryPolicy.canRetry();
    }

    public boolean hasCallback() {
        return callbackInfo.hasCallback();
    }

    // -- query methods --

    public DownloadTaskId id() {
        return id;
    }

    public String idValue() {
        return id.value();
    }

    public SourceUrl sourceUrl() {
        return sourceUrl;
    }

    public String sourceUrlValue() {
        return sourceUrl.value();
    }

    public StorageInfo storageInfo() {
        return storageInfo;
    }

    public String s3Key() {
        return storageInfo.s3Key();
    }

    public String bucket() {
        return storageInfo.bucket();
    }

    public com.ryuqq.fileflow.domain.common.vo.AccessType accessType() {
        return storageInfo.accessType();
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

    public RetryPolicy retryPolicy() {
        return retryPolicy;
    }

    public int retryCount() {
        return retryPolicy.retryCount();
    }

    public int maxRetries() {
        return retryPolicy.maxRetries();
    }

    public CallbackInfo callbackInfo() {
        return callbackInfo;
    }

    public String callbackUrl() {
        return callbackInfo.callbackUrl();
    }

    public String lastError() {
        return lastError;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
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
