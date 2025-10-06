package com.ryuqq.fileflow.domain.upload.event;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 업로드 세션 생성 이벤트
 * 새로운 업로드 세션이 생성되었음을 알립니다.
 */
public final class UploadSessionCreated implements DomainEvent {

    private final String sessionId;
    private final String uploaderId;
    private final String policyKey;
    private final LocalDateTime expiresAt;
    private final LocalDateTime occurredOn;

    private UploadSessionCreated(
            String sessionId,
            String uploaderId,
            String policyKey,
            LocalDateTime expiresAt,
            LocalDateTime occurredOn
    ) {
        this.sessionId = sessionId;
        this.uploaderId = uploaderId;
        this.policyKey = policyKey;
        this.expiresAt = expiresAt;
        this.occurredOn = occurredOn;
    }

    public static UploadSessionCreated of(
            String sessionId,
            String uploaderId,
            String policyKey,
            LocalDateTime expiresAt
    ) {
        return new UploadSessionCreated(
                sessionId,
                uploaderId,
                policyKey,
                expiresAt,
                LocalDateTime.now()
        );
    }

    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventType() {
        return "UploadSessionCreated";
    }

    // ========== Getters ==========

    public String getSessionId() {
        return sessionId;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public String getPolicyKey() {
        return policyKey;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadSessionCreated that = (UploadSessionCreated) o;
        return Objects.equals(sessionId, that.sessionId) &&
               Objects.equals(uploaderId, that.uploaderId) &&
               Objects.equals(policyKey, that.policyKey) &&
               Objects.equals(expiresAt, that.expiresAt) &&
               Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, uploaderId, policyKey, expiresAt, occurredOn);
    }

    @Override
    public String toString() {
        return "UploadSessionCreated{" +
                "sessionId='" + sessionId + '\'' +
                ", uploaderId='" + uploaderId + '\'' +
                ", policyKey='" + policyKey + '\'' +
                ", expiresAt=" + expiresAt +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
