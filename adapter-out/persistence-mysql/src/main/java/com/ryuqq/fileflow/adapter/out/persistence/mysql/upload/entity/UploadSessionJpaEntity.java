package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.BaseAuditEntity;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import com.ryuqq.fileflow.domain.upload.UploadType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Upload Session JPA Entity (Persistence Layer)
 *
 * <p><strong>역할</strong>: DB 매핑만 담당 (비즈니스 로직 없음)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/upload/entity/</p>
 * <p><strong>변환</strong>: {@code UploadSessionMapper}를 통해 Domain {@code UploadSession}와 상호 변환</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ JPA 어노테이션만 사용 (비즈니스 로직 없음)</li>
 *   <li>✅ Long FK 전략 (JPA 관계 어노테이션 금지)</li>
 *   <li>✅ Setter 제공 (JPA 전용, 외부 노출 금지)</li>
 *   <li>✅ Static Factory Methods: {@code create()}, {@code reconstitute()}</li>
 *   <li>❌ Lombok 금지</li>
 *   <li>❌ JPA 관계 어노테이션 금지 (ManyToOne, OneToMany 등)</li>
 * </ul>
 *
 * <h3>테이블 스키마</h3>
 * <pre>
 * CREATE TABLE upload_session (
 *   id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   session_key VARCHAR(100) NOT NULL UNIQUE,
 *   tenant_id BIGINT NOT NULL,
 *   file_name VARCHAR(500) NOT NULL,
 *   file_size BIGINT NOT NULL,
 *   upload_type VARCHAR(20) NOT NULL,
 *   storage_key VARCHAR(500),
 *   status VARCHAR(20) NOT NULL,
 *   file_id BIGINT,
 *   failure_reason TEXT,
 *   created_at DATETIME NOT NULL,
 *   completed_at DATETIME,
 *   failed_at DATETIME,
 *   updated_at DATETIME NOT NULL,
 *   INDEX idx_session_key (session_key),
 *   INDEX idx_tenant_id (tenant_id),
 *   INDEX idx_status (status)
 * );
 * </pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see com.ryuqq.fileflow.domain.upload.UploadSession Domain Model
 */
@Entity
@Table(name = "upload_session")
public class UploadSessionJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "session_key", nullable = false, unique = true, length = 100)
    private String sessionKey;

    /**
     * Tenant ID (Long FK Strategy)
     * ❌ ManyToOne 관계 어노테이션 사용 안함!
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "file_name", nullable = false, length = 500)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "upload_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UploadType uploadType;

    @Column(name = "storage_key", length = 500)
    private String storageKey;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    /**
     * File ID (Long FK Strategy)
     * ❌ ManyToOne 관계 어노테이션 사용 안함!
     */
    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    /**
     * 기본 생성자 (JPA 스펙 요구사항)
     */
    protected UploadSessionJpaEntity() {
        super();
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     */
    private UploadSessionJpaEntity(
        Long id,
        String sessionKey,
        Long tenantId,
        String fileName,
        Long fileSize,
        UploadType uploadType,
        String storageKey,
        SessionStatus status,
        Long fileId,
        String failureReason,
        LocalDateTime completedAt,
        LocalDateTime failedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        super(createdAt, updatedAt);
        this.id = id;
        this.sessionKey = sessionKey;
        this.tenantId = tenantId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.uploadType = uploadType;
        this.storageKey = storageKey;
        this.status = status;
        this.fileId = fileId;
        this.failureReason = failureReason;
        this.completedAt = completedAt;
        this.failedAt = failedAt;
    }

    /**
     * Static Factory Method - 신규 엔티티 생성
     */
    public static UploadSessionJpaEntity create(
        String sessionKey,
        Long tenantId,
        String fileName,
        Long fileSize,
        UploadType uploadType,
        String storageKey,
        SessionStatus status
    ) {
        UploadSessionJpaEntity entity = new UploadSessionJpaEntity();
        entity.sessionKey = sessionKey;
        entity.tenantId = tenantId;
        entity.fileName = fileName;
        entity.fileSize = fileSize;
        entity.uploadType = uploadType;
        entity.storageKey = storageKey;
        entity.status = status;
        entity.initializeAuditFields();
        return entity;
    }

    /**
     * Static Factory Method - DB 조회 데이터로 재구성
     */
    public static UploadSessionJpaEntity reconstitute(
        Long id,
        String sessionKey,
        Long tenantId,
        String fileName,
        Long fileSize,
        UploadType uploadType,
        String storageKey,
        SessionStatus status,
        Long fileId,
        String failureReason,
        LocalDateTime completedAt,
        LocalDateTime failedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new UploadSessionJpaEntity(
            id,
            sessionKey,
            tenantId,
            fileName,
            fileSize,
            uploadType,
            storageKey,
            status,
            fileId,
            failureReason,
            completedAt,
            failedAt,
            createdAt,
            updatedAt
        );
    }

    // ===== Getters =====

    public Long getId() {
        return id;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public UploadType getUploadType() {
        return uploadType;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public Long getFileId() {
        return fileId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

}
