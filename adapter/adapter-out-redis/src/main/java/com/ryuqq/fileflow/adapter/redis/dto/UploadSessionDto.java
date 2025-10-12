package com.ryuqq.fileflow.adapter.redis.dto;

import com.ryuqq.fileflow.domain.upload.UploadSession;

import java.time.LocalDateTime;

/**
 * Redis에 저장될 UploadSession DTO
 *
 * TTL 기반 만료 감지를 위한 최소 정보만 포함합니다.
 * 실제 영구 데이터는 DB에 저장되며, Redis는 만료 이벤트 발생용으로만 사용됩니다.
 *
 * @author sangwon-ryu
 */
public class UploadSessionDto {

    private String sessionId;
    private String uploaderId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    /**
     * Default Constructor (Jackson 직렬화용)
     */
    public UploadSessionDto() {
    }

    /**
     * All-Args Constructor
     */
    public UploadSessionDto(
            String sessionId,
            String uploaderId,
            String status,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
        this.sessionId = sessionId;
        this.uploaderId = uploaderId;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    /**
     * Factory Method - Domain UploadSession에서 DTO 생성
     *
     * @param session 도메인 UploadSession 객체
     * @return UploadSessionDto
     */
    public static UploadSessionDto from(UploadSession session) {
        return new UploadSessionDto(
                session.getSessionId(),
                session.getUploaderId(),
                session.getStatus().name(),
                session.getCreatedAt(),
                session.getExpiresAt()
        );
    }

    // ========== Getters and Setters ==========

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
