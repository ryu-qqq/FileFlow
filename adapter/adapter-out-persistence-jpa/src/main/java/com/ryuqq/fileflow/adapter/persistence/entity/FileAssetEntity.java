package com.ryuqq.fileflow.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * 파일 자산 엔티티
 *
 * 비즈니스 규칙:
 * - 업로드된 파일의 기본 정보를 저장합니다
 * - S3 메타데이터, CDN URL, 파일 정보를 추적합니다
 * - Soft delete를 지원하여 논리적 삭제를 수행합니다
 * - Entity는 데이터 저장을 위한 매핑만 담당합니다
 * - Lombok을 사용하지 않으므로 모든 메서드를 수동으로 구현합니다
 *
 * @author sangwon-ryu
 */
@Entity
@Table(name = "file_asset", indexes = {
        @Index(name = "idx_session_id", columnList = "session_id"),
        @Index(name = "idx_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_s3_key", columnList = "s3_key"),
        @Index(name = "idx_deleted_at", columnList = "deleted_at"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
public class FileAssetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "file_id", nullable = false, unique = true, length = 36)
    private String fileId;

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    @Column(name = "s3_bucket", nullable = false, length = 100)
    private String s3Bucket;

    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;

    @Column(name = "s3_region", nullable = false, length = 50)
    private String s3Region;

    @Column(name = "cdn_url", length = 500)
    private String cdnUrl;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_extension", length = 20)
    private String fileExtension;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * JPA 기본 생성자 (protected)
     */
    protected FileAssetEntity() {
    }

    /**
     * 파일 자산 엔티티 생성자 (protected - factory method 사용 권장)
     *
     * @param fileId 파일 고유 식별자
     * @param sessionId 업로드 세션 ID
     * @param tenantId 테넌트 ID
     * @param originalFileName 원본 파일 이름
     * @param storedFileName 저장된 파일 이름
     * @param s3Bucket S3 버킷 이름
     * @param s3Key S3 객체 키
     * @param s3Region S3 리전
     * @param cdnUrl CDN URL
     * @param fileSize 파일 크기
     * @param contentType MIME 타입
     * @param fileExtension 파일 확장자
     * @param checksum 파일 체크섬
     * @param isPublic 공개 여부
     */
    protected FileAssetEntity(
            String fileId,
            String sessionId,
            String tenantId,
            String originalFileName,
            String storedFileName,
            String s3Bucket,
            String s3Key,
            String s3Region,
            String cdnUrl,
            Long fileSize,
            String contentType,
            String fileExtension,
            String checksum,
            Boolean isPublic
    ) {
        this.fileId = fileId;
        this.sessionId = sessionId;
        this.tenantId = tenantId;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.s3Bucket = s3Bucket;
        this.s3Key = s3Key;
        this.s3Region = s3Region;
        this.cdnUrl = cdnUrl;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.fileExtension = fileExtension;
        this.checksum = checksum;
        this.isPublic = isPublic;
    }

    /**
     * 새로운 파일 자산 엔티티를 생성하는 factory method
     *
     * @param fileId 파일 고유 식별자
     * @param sessionId 업로드 세션 ID
     * @param tenantId 테넌트 ID
     * @param originalFileName 원본 파일 이름
     * @param storedFileName 저장된 파일 이름
     * @param s3Bucket S3 버킷 이름
     * @param s3Key S3 객체 키
     * @param s3Region S3 리전
     * @param cdnUrl CDN URL
     * @param fileSize 파일 크기
     * @param contentType MIME 타입
     * @param fileExtension 파일 확장자
     * @param checksum 파일 체크섬
     * @param isPublic 공개 여부
     * @return 생성된 FileAssetEntity
     */
    public static FileAssetEntity of(
            String fileId,
            String sessionId,
            String tenantId,
            String originalFileName,
            String storedFileName,
            String s3Bucket,
            String s3Key,
            String s3Region,
            String cdnUrl,
            Long fileSize,
            String contentType,
            String fileExtension,
            String checksum,
            Boolean isPublic
    ) {
        return new FileAssetEntity(
                fileId,
                sessionId,
                tenantId,
                originalFileName,
                storedFileName,
                s3Bucket,
                s3Key,
                s3Region,
                cdnUrl,
                fileSize,
                contentType,
                fileExtension,
                checksum,
                isPublic
        );
    }

    /**
     * 파일을 논리적으로 삭제합니다 (soft delete)
     */
    public void delete() {
        if (this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
        }
    }

    /**
     * 파일이 삭제되었는지 확인합니다
     *
     * @return 삭제 여부
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 삭제를 취소합니다
     */
    public void restore() {
        this.deletedAt = null;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.isPublic == null) {
            this.isPublic = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========== Getters ==========

    public Long getId() {
        return id;
    }

    public String getFileId() {
        return fileId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public String getS3Key() {
        return s3Key;
    }

    public String getS3Region() {
        return s3Region;
    }

    public String getCdnUrl() {
        return cdnUrl;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getChecksum() {
        return checksum;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileAssetEntity that = (FileAssetEntity) o;
        return Objects.equals(fileId, that.fileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId);
    }

    @Override
    public String toString() {
        return "FileAssetEntity{" +
                "id=" + id +
                ", fileId='" + fileId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", originalFileName='" + originalFileName + '\'' +
                ", storedFileName='" + storedFileName + '\'' +
                ", s3Bucket='" + s3Bucket + '\'' +
                ", s3Key='" + s3Key + '\'' +
                ", s3Region='" + s3Region + '\'' +
                ", cdnUrl='" + cdnUrl + '\'' +
                ", fileSize=" + fileSize +
                ", contentType='" + contentType + '\'' +
                ", fileExtension='" + fileExtension + '\'' +
                ", checksum='" + checksum + '\'' +
                ", isPublic=" + isPublic +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deletedAt=" + deletedAt +
                '}';
    }
}
