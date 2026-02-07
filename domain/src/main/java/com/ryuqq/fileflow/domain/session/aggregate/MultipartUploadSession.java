package com.ryuqq.fileflow.domain.session.aggregate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.session.event.UploadCompletedEvent;
import com.ryuqq.fileflow.domain.session.exception.SessionErrorCode;
import com.ryuqq.fileflow.domain.session.exception.SessionException;
import com.ryuqq.fileflow.domain.session.id.MultipartUploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.MultipartSessionStatus;
import com.ryuqq.fileflow.domain.session.vo.UploadTarget;

/**
 * 멀티파트 업로드 세션.
 *
 * <p>라이프사이클: INITIATED → UPLOADING → COMPLETED | ABORTED | EXPIRED
 * <p>S3 CreateMultipartUpload로 시작, 파트별 업로드 후 CompleteMultipartUpload로 완료.
 */
public class MultipartUploadSession {

    private final MultipartUploadSessionId id;
    private final UploadTarget uploadTarget;
    private final String uploadId;
    private final long partSize;
    private final String purpose;
    private final String source;
    private MultipartSessionStatus status;
    private final Instant expiresAt;
    private final Instant createdAt;
    private final List<CompletedPart> completedParts;

    private final List<DomainEvent> events = new ArrayList<>();

    private MultipartUploadSession(MultipartUploadSessionId id, UploadTarget uploadTarget,
                                    String uploadId, long partSize, String purpose, String source,
                                    MultipartSessionStatus status, Instant expiresAt, Instant createdAt,
                                    List<CompletedPart> completedParts) {
        this.id = id;
        this.uploadTarget = uploadTarget;
        this.uploadId = uploadId;
        this.partSize = partSize;
        this.purpose = purpose;
        this.source = source;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.completedParts = new ArrayList<>(completedParts);
    }

    public static MultipartUploadSession forNew(MultipartUploadSessionId id, UploadTarget uploadTarget,
                                                 String uploadId, long partSize,
                                                 String purpose, String source,
                                                 Instant expiresAt, Instant now) {
        return new MultipartUploadSession(id, uploadTarget, uploadId, partSize, purpose, source,
                MultipartSessionStatus.INITIATED, expiresAt, now, List.of());
    }

    public static MultipartUploadSession reconstitute(MultipartUploadSessionId id, UploadTarget uploadTarget,
                                                       String uploadId, long partSize,
                                                       String purpose, String source,
                                                       MultipartSessionStatus status, Instant expiresAt,
                                                       Instant createdAt, List<CompletedPart> completedParts) {
        return new MultipartUploadSession(id, uploadTarget, uploadId, partSize, purpose, source,
                status, expiresAt, createdAt, completedParts);
    }

    /**
     * 파트 업로드 완료를 기록합니다.
     */
    public void addCompletedPart(int partNumber, String etag, long size) {
        validateActive();
        validatePartNotDuplicate(partNumber);

        completedParts.add(CompletedPart.of(partNumber, etag, size));

        if (this.status == MultipartSessionStatus.INITIATED) {
            this.status = MultipartSessionStatus.UPLOADING;
        }
    }

    /**
     * 멀티파트 업로드 완료 처리.
     * S3 CompleteMultipartUpload 호출 후 검증된 결과로 이벤트를 발행합니다.
     */
    public void complete(long totalFileSize, String etag, Instant now) {
        validateActive();
        validateNotExpired(now);

        if (completedParts.isEmpty()) {
            throw new SessionException(SessionErrorCode.INVALID_SESSION_STATUS,
                    "Cannot complete multipart session without any completed parts");
        }

        this.status = MultipartSessionStatus.COMPLETED;
        registerEvent(UploadCompletedEvent.of(
                id.value(), "MULTIPART",
                uploadTarget.s3Key(), uploadTarget.bucket(), uploadTarget.accessType(),
                uploadTarget.fileName(), uploadTarget.contentType(),
                totalFileSize, etag, purpose, source, now
        ));
    }

    /**
     * 업로드를 중단합니다.
     */
    public void abort() {
        if (this.status == MultipartSessionStatus.COMPLETED) {
            throw new SessionException(SessionErrorCode.SESSION_ALREADY_COMPLETED);
        }
        this.status = MultipartSessionStatus.ABORTED;
    }

    public void expire() {
        if (this.status == MultipartSessionStatus.COMPLETED
                || this.status == MultipartSessionStatus.ABORTED) {
            return;
        }
        this.status = MultipartSessionStatus.EXPIRED;
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    // -- query methods --

    public MultipartUploadSessionId id() {
        return id;
    }

    public UploadTarget uploadTarget() {
        return uploadTarget;
    }

    public String s3Key() {
        return uploadTarget.s3Key();
    }

    public String bucket() {
        return uploadTarget.bucket();
    }

    public AccessType accessType() {
        return uploadTarget.accessType();
    }

    public String fileName() {
        return uploadTarget.fileName();
    }

    public String contentType() {
        return uploadTarget.contentType();
    }

    public String uploadId() {
        return uploadId;
    }

    public long partSize() {
        return partSize;
    }

    public String purpose() {
        return purpose;
    }

    public String source() {
        return source;
    }

    public MultipartSessionStatus status() {
        return status;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public List<CompletedPart> completedParts() {
        return Collections.unmodifiableList(completedParts);
    }

    public int completedPartCount() {
        return completedParts.size();
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

    // -- invariant validation --

    private void validateActive() {
        if (this.status == MultipartSessionStatus.COMPLETED) {
            throw new SessionException(SessionErrorCode.SESSION_ALREADY_COMPLETED);
        }
        if (this.status == MultipartSessionStatus.ABORTED) {
            throw new SessionException(SessionErrorCode.SESSION_ALREADY_ABORTED);
        }
        if (this.status == MultipartSessionStatus.EXPIRED) {
            throw new SessionException(SessionErrorCode.SESSION_EXPIRED);
        }
    }

    private void validateNotExpired(Instant now) {
        if (isExpired(now)) {
            throw new SessionException(SessionErrorCode.SESSION_EXPIRED);
        }
    }

    private void validatePartNotDuplicate(int partNumber) {
        boolean exists = completedParts.stream()
                .anyMatch(p -> p.partNumber() == partNumber);
        if (exists) {
            throw new SessionException(SessionErrorCode.PART_NUMBER_DUPLICATE,
                    "Part number already exists: " + partNumber);
        }
    }

    // -- equals/hashCode ID 기반 --

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultipartUploadSession that = (MultipartUploadSession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
