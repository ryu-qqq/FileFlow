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
import com.ryuqq.fileflow.domain.session.id.SingleUploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.SingleSessionStatus;
import com.ryuqq.fileflow.domain.session.vo.UploadTarget;

/**
 * 단건 Presigned URL 업로드 세션.
 *
 * <p>라이프사이클: CREATED → COMPLETED | EXPIRED
 * <p>클라이언트가 presigned URL로 S3에 직접 업로드 후 complete 호출.
 */
public class SingleUploadSession {

    private final SingleUploadSessionId id;
    private final UploadTarget uploadTarget;
    private final String presignedUrl;
    private final String purpose;
    private final String source;
    private SingleSessionStatus status;
    private final Instant expiresAt;
    private final Instant createdAt;

    private final List<DomainEvent> events = new ArrayList<>();

    private SingleUploadSession(SingleUploadSessionId id, UploadTarget uploadTarget,
                                String presignedUrl, String purpose, String source,
                                SingleSessionStatus status, Instant expiresAt, Instant createdAt) {
        this.id = id;
        this.uploadTarget = uploadTarget;
        this.presignedUrl = presignedUrl;
        this.purpose = purpose;
        this.source = source;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public static SingleUploadSession forNew(SingleUploadSessionId id, UploadTarget uploadTarget,
                                              String presignedUrl, String purpose, String source,
                                              Instant expiresAt, Instant now) {
        return new SingleUploadSession(id, uploadTarget, presignedUrl, purpose, source,
                SingleSessionStatus.CREATED, expiresAt, now);
    }

    public static SingleUploadSession reconstitute(SingleUploadSessionId id, UploadTarget uploadTarget,
                                                    String presignedUrl, String purpose, String source,
                                                    SingleSessionStatus status, Instant expiresAt,
                                                    Instant createdAt) {
        return new SingleUploadSession(id, uploadTarget, presignedUrl, purpose, source,
                status, expiresAt, createdAt);
    }

    /**
     * 업로드 완료 처리.
     * S3 HeadObject로 검증된 fileSize, etag를 받아 이벤트를 발행합니다.
     */
    public void complete(long fileSize, String etag, Instant now) {
        validateNotCompleted();
        validateNotExpired(now);

        this.status = SingleSessionStatus.COMPLETED;
        registerEvent(UploadCompletedEvent.of(
                id.value(), "SINGLE",
                uploadTarget.s3Key(), uploadTarget.bucket(), uploadTarget.accessType(),
                uploadTarget.fileName(), uploadTarget.contentType(),
                fileSize, etag, purpose, source, now
        ));
    }

    public void expire() {
        if (this.status == SingleSessionStatus.COMPLETED) {
            return;
        }
        this.status = SingleSessionStatus.EXPIRED;
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    // -- query methods --

    public SingleUploadSessionId id() {
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

    public String presignedUrl() {
        return presignedUrl;
    }

    public String purpose() {
        return purpose;
    }

    public String source() {
        return source;
    }

    public SingleSessionStatus status() {
        return status;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant createdAt() {
        return createdAt;
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

    private void validateNotCompleted() {
        if (this.status == SingleSessionStatus.COMPLETED) {
            throw new SessionException(SessionErrorCode.SESSION_ALREADY_COMPLETED);
        }
    }

    private void validateNotExpired(Instant now) {
        if (this.status == SingleSessionStatus.EXPIRED || isExpired(now)) {
            throw new SessionException(SessionErrorCode.SESSION_EXPIRED);
        }
    }

    // -- equals/hashCode ID 기반 --

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingleUploadSession that = (SingleUploadSession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
