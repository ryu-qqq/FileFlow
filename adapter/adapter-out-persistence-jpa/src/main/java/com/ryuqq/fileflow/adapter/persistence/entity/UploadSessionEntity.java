package com.ryuqq.fileflow.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 업로드 세션 엔티티
 *
 * 비즈니스 규칙:
 * - 세션은 Presigned URL 발급 시 생성됩니다
 * - 세션 상태는 INITIATED → IN_PROGRESS → COMPLETED/FAILED/EXPIRED 순으로 전환됩니다
 * - 만료된 세션은 자동으로 정리될 수 있습니다
 * - Lombok을 사용하지 않으므로 모든 메서드를 수동으로 구현합니다
 *
 * @author sangwon-ryu
 */
@Entity
@Table(name = "upload_session", indexes = {
        @Index(name = "idx_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_expires_at", columnList = "expires_at"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
public class UploadSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 36)
    private String sessionId;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "policy_key", nullable = false, length = 200)
    private String policyKey;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UploadStatus status;

    @Column(name = "presigned_url", columnDefinition = "TEXT")
    private String presignedUrl;

    @Column(name = "s3_key", length = 500)
    private String s3Key;

    @Column(name = "upload_started_at")
    private LocalDateTime uploadStartedAt;

    @Column(name = "upload_completed_at")
    private LocalDateTime uploadCompletedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * JPA 기본 생성자 (protected)
     */
    protected UploadSessionEntity() {
    }

    /**
     * 업로드 세션 엔티티 생성자 (protected - factory method 사용 권장)
     *
     * @param sessionId 세션 고유 식별자
     * @param tenantId 테넌트 ID
     * @param policyKey 적용된 정책 키
     * @param fileName 원본 파일 이름
     * @param contentType MIME 타입
     * @param fileSize 파일 크기
     * @param status 업로드 상태
     * @param presignedUrl S3 Presigned URL
     * @param expiresAt 세션 만료 시각
     */
    protected UploadSessionEntity(
            String sessionId,
            String tenantId,
            String policyKey,
            String fileName,
            String contentType,
            Long fileSize,
            UploadStatus status,
            String presignedUrl,
            LocalDateTime expiresAt
    ) {
        this.sessionId = sessionId;
        this.tenantId = tenantId;
        this.policyKey = policyKey;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.status = status;
        this.presignedUrl = presignedUrl;
        this.expiresAt = expiresAt;
    }

    /**
     * 새로운 업로드 세션 엔티티를 생성하는 factory method
     *
     * @param sessionId 세션 고유 식별자
     * @param tenantId 테넌트 ID
     * @param policyKey 적용된 정책 키
     * @param fileName 원본 파일 이름
     * @param contentType MIME 타입
     * @param fileSize 파일 크기
     * @param status 업로드 상태
     * @param presignedUrl S3 Presigned URL
     * @param expiresAt 세션 만료 시각
     * @return 생성된 UploadSessionEntity
     */
    public static UploadSessionEntity of(
            String sessionId,
            String tenantId,
            String policyKey,
            String fileName,
            String contentType,
            Long fileSize,
            UploadStatus status,
            String presignedUrl,
            LocalDateTime expiresAt
    ) {
        return new UploadSessionEntity(
                sessionId,
                tenantId,
                policyKey,
                fileName,
                contentType,
                fileSize,
                status,
                presignedUrl,
                expiresAt
        );
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========== Business Methods ==========

    /**
     * 업로드 시작
     *
     * @param s3Key S3 객체 키
     */
    public void startUpload(String s3Key) {
        this.status = UploadStatus.IN_PROGRESS;
        this.s3Key = s3Key;
        this.uploadStartedAt = LocalDateTime.now();
    }

    /**
     * 업로드 완료
     */
    public void completeUpload() {
        this.status = UploadStatus.COMPLETED;
        this.uploadCompletedAt = LocalDateTime.now();
    }

    /**
     * 업로드 실패
     */
    public void failUpload() {
        this.status = UploadStatus.FAILED;
    }

    /**
     * 세션 만료
     */
    public void expireSession() {
        this.status = UploadStatus.EXPIRED;
    }

    /**
     * 세션이 만료되었는지 확인
     *
     * @return 만료 여부
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 업로드가 완료되었는지 확인
     *
     * @return 완료 여부
     */
    public boolean isCompleted() {
        return this.status == UploadStatus.COMPLETED;
    }

    // ========== Getters ==========

    public Long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getPolicyKey() {
        return policyKey;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public UploadStatus getStatus() {
        return status;
    }

    public String getPresignedUrl() {
        return presignedUrl;
    }

    public String getS3Key() {
        return s3Key;
    }

    public LocalDateTime getUploadStartedAt() {
        return uploadStartedAt;
    }

    public LocalDateTime getUploadCompletedAt() {
        return uploadCompletedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadSessionEntity that = (UploadSessionEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sessionId);
    }

    @Override
    public String toString() {
        return "UploadSessionEntity{" +
                "id=" + id +
                ", sessionId='" + sessionId + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", policyKey='" + policyKey + '\'' +
                ", fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", fileSize=" + fileSize +
                ", status=" + status +
                ", s3Key='" + s3Key + '\'' +
                ", uploadStartedAt=" + uploadStartedAt +
                ", uploadCompletedAt=" + uploadCompletedAt +
                ", expiresAt=" + expiresAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    /**
     * 업로드 세션 상태
     */
    public enum UploadStatus {
        INITIATED,      // 세션 생성됨 (Presigned URL 발급)
        IN_PROGRESS,    // 업로드 진행 중
        COMPLETED,      // 업로드 완료
        FAILED,         // 업로드 실패
        EXPIRED         // 세션 만료
    }
}
