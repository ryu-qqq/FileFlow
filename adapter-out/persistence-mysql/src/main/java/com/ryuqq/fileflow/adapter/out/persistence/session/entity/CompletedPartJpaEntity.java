package com.ryuqq.fileflow.adapter.out.persistence.session.entity;

import com.ryuqq.fileflow.adapter.out.persistence.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * CompletedPart JPA Entity.
 *
 * <p>멀티파트 업로드의 완료된 Part 정보를 저장합니다.
 *
 * <p><strong>설계 원칙:</strong>
 *
 * <ul>
 *   <li>No Lombok - Plain Java 사용
 *   <li>Long FK 전략 - sessionId로 MultipartUploadSession 참조
 *   <li>Protected default constructor - JPA 스펙 요구
 *   <li>Private full constructor - of() 팩토리 메서드로만 생성
 *   <li>Getter only - Setter 미제공 (불변성)
 * </ul>
 */
@Entity
@Table(name = "completed_part")
public class CompletedPartJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(name = "part_number", nullable = false)
    private Integer partNumber;

    @Column(name = "presigned_url", nullable = false, length = 2048)
    private String presignedUrl;

    @Column(name = "etag", nullable = false, length = 64)
    private String etag;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    /** JPA 스펙 요구 기본 생성자 */
    protected CompletedPartJpaEntity() {
        super();
    }

    /** Private 전체 생성자 (신규 생성용 - id 없음) */
    private CompletedPartJpaEntity(
            String sessionId,
            Integer partNumber,
            String presignedUrl,
            String etag,
            Long size,
            LocalDateTime uploadedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        super(createdAt, updatedAt);
        this.sessionId = sessionId;
        this.partNumber = partNumber;
        this.presignedUrl = presignedUrl;
        this.etag = etag;
        this.size = size;
        this.uploadedAt = uploadedAt;
    }

    /** Private 전체 생성자 (DB 조회용 - id 포함) */
    private CompletedPartJpaEntity(
            Long id,
            String sessionId,
            Integer partNumber,
            String presignedUrl,
            String etag,
            Long size,
            LocalDateTime uploadedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.sessionId = sessionId;
        this.partNumber = partNumber;
        this.presignedUrl = presignedUrl;
        this.etag = etag;
        this.size = size;
        this.uploadedAt = uploadedAt;
    }

    /**
     * 신규 Entity 생성 팩토리 메서드 (id 자동 생성).
     *
     * @return CompletedPartJpaEntity
     */
    public static CompletedPartJpaEntity of(
            String sessionId,
            Integer partNumber,
            String presignedUrl,
            String etag,
            Long size,
            LocalDateTime uploadedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new CompletedPartJpaEntity(
                sessionId, partNumber, presignedUrl, etag, size, uploadedAt, createdAt, updatedAt);
    }

    /**
     * 기존 Entity 복원 팩토리 메서드 (id 포함).
     *
     * @return CompletedPartJpaEntity
     */
    public static CompletedPartJpaEntity reconstitute(
            Long id,
            String sessionId,
            Integer partNumber,
            String presignedUrl,
            String etag,
            Long size,
            LocalDateTime uploadedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new CompletedPartJpaEntity(
                id,
                sessionId,
                partNumber,
                presignedUrl,
                etag,
                size,
                uploadedAt,
                createdAt,
                updatedAt);
    }

    // ==================== Getter ====================

    public Long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Integer getPartNumber() {
        return partNumber;
    }

    public String getPresignedUrl() {
        return presignedUrl;
    }

    public String getEtag() {
        return etag;
    }

    public Long getSize() {
        return size;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
