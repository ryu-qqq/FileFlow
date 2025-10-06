package com.ryuqq.fileflow.domain.upload.event;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.policy.FileType;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Presigned URL 요청 이벤트
 * 파일 업로드를 위한 Presigned URL이 요청되었음을 알립니다.
 */
public final class PresignedUrlRequested implements DomainEvent {

    private final String sessionId;
    private final String uploaderId;
    private final String fileName;
    private final FileType fileType;
    private final long fileSizeBytes;
    private final String policyKey;
    private final LocalDateTime occurredOn;

    private PresignedUrlRequested(
            String sessionId,
            String uploaderId,
            String fileName,
            FileType fileType,
            long fileSizeBytes,
            String policyKey,
            LocalDateTime occurredOn
    ) {
        this.sessionId = sessionId;
        this.uploaderId = uploaderId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSizeBytes = fileSizeBytes;
        this.policyKey = policyKey;
        this.occurredOn = occurredOn;
    }

    public static PresignedUrlRequested of(
            String sessionId,
            String uploaderId,
            String fileName,
            FileType fileType,
            long fileSizeBytes,
            String policyKey
    ) {
        return new PresignedUrlRequested(
                sessionId,
                uploaderId,
                fileName,
                fileType,
                fileSizeBytes,
                policyKey,
                LocalDateTime.now()
        );
    }

    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventType() {
        return "PresignedUrlRequested";
    }

    // ========== Getters ==========

    public String getSessionId() {
        return sessionId;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public String getFileName() {
        return fileName;
    }

    public FileType getFileType() {
        return fileType;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getPolicyKey() {
        return policyKey;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PresignedUrlRequested that = (PresignedUrlRequested) o;
        return fileSizeBytes == that.fileSizeBytes &&
               Objects.equals(sessionId, that.sessionId) &&
               Objects.equals(uploaderId, that.uploaderId) &&
               Objects.equals(fileName, that.fileName) &&
               fileType == that.fileType &&
               Objects.equals(policyKey, that.policyKey) &&
               Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, uploaderId, fileName, fileType, fileSizeBytes, policyKey, occurredOn);
    }

    @Override
    public String toString() {
        return "PresignedUrlRequested{" +
                "sessionId='" + sessionId + '\'' +
                ", uploaderId='" + uploaderId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileType=" + fileType +
                ", fileSizeBytes=" + fileSizeBytes +
                ", policyKey='" + policyKey + '\'' +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
