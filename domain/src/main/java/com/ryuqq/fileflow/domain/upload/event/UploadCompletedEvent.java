package com.ryuqq.fileflow.domain.upload.event;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Upload Completed Domain Event
 * 업로드 완료 도메인 이벤트 (불변 객체)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class UploadCompletedEvent {

    private final Long uploadSessionId;
    private final String sessionKey;
    private final Long fileId;
    private final LocalDateTime completedAt;
    private final LocalDateTime occurredAt;

    /**
     * Private 생성자
     *
     * @param uploadSessionId Upload Session ID
     * @param sessionKey Session Key
     * @param fileId File ID
     * @param completedAt 완료 시간
     */
    private UploadCompletedEvent(
        Long uploadSessionId,
        String sessionKey,
        Long fileId,
        LocalDateTime completedAt
    ) {
        this.uploadSessionId = uploadSessionId;
        this.sessionKey = sessionKey;
        this.fileId = fileId;
        this.completedAt = completedAt;
        this.occurredAt = LocalDateTime.now();
    }

    /**
     * Static Factory Method
     *
     * @param uploadSessionId Upload Session ID
     * @param sessionKey Session Key
     * @param fileId File ID
     * @param completedAt 완료 시간
     * @return UploadCompletedEvent 인스턴스
     */
    public static UploadCompletedEvent of(
        Long uploadSessionId,
        String sessionKey,
        Long fileId,
        LocalDateTime completedAt
    ) {
        return new UploadCompletedEvent(
            uploadSessionId,
            sessionKey,
            fileId,
            completedAt
        );
    }

    /**
     * Upload Session ID를 반환합니다.
     *
     * @return Upload Session ID
     */
    public Long getUploadSessionId() {
        return uploadSessionId;
    }

    /**
     * Session Key를 반환합니다.
     *
     * @return Session Key
     */
    public String getSessionKey() {
        return sessionKey;
    }

    /**
     * File ID를 반환합니다.
     *
     * @return File ID
     */
    public Long getFileId() {
        return fileId;
    }

    /**
     * 완료 시간을 반환합니다.
     *
     * @return 완료 시간
     */
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    /**
     * 이벤트 발생 시간을 반환합니다.
     *
     * @return 이벤트 발생 시간
     */
    public LocalDateTime getOccurredAt() {
        return occurredAt;
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
        if (!(o instanceof UploadCompletedEvent)) {
            return false;
        }
        UploadCompletedEvent that = (UploadCompletedEvent) o;
        return Objects.equals(uploadSessionId, that.uploadSessionId) &&
               Objects.equals(occurredAt, that.occurredAt);
    }

    /**
     * 해시코드를 반환합니다.
     *
     * @return 해시코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(uploadSessionId, occurredAt);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * @return UploadCompletedEvent 정보 문자열
     */
    @Override
    public String toString() {
        return "UploadCompletedEvent{" +
            "uploadSessionId=" + uploadSessionId +
            ", sessionKey='" + sessionKey + '\'' +
            ", fileId=" + fileId +
            ", completedAt=" + completedAt +
            ", occurredAt=" + occurredAt +
            '}';
    }
}
