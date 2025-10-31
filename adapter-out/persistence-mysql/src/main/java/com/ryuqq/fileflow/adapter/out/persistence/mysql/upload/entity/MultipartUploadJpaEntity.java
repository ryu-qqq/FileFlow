package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.entity.BaseAuditEntity;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
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
 * Multipart Upload JPA Entity (Persistence Layer)
 *
 * <p><strong>역할</strong>: DB 매핑만 담당 (비즈니스 로직 없음)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/upload/entity/</p>
 * <p><strong>변환</strong>: {@code MultipartUploadMapper}를 통해 Domain {@code MultipartUpload}와 상호 변환</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ JPA 어노테이션만 사용 (비즈니스 로직 없음)</li>
 *   <li>✅ Long FK 전략 (JPA 관계 어노테이션 금지)</li>
 *   <li>✅ Setter 제공 (JPA 전용, 외부 노출 금지)</li>
 *   <li>✅ Static Factory Methods: {@code create()}, {@code reconstitute()}</li>
 *   <li>❌ Lombok 금지</li>
 *   <li>❌ JPA 관계 어노테이션 금지 (@ManyToOne, @OneToMany 등)</li>
 * </ul>
 *
 * <h3>테이블 스키마</h3>
 * <pre>
 * CREATE TABLE upload_multipart (
 *   id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   upload_session_id BIGINT NOT NULL,
 *   provider_upload_id VARCHAR(500),
 *   status VARCHAR(20) NOT NULL,
 *   total_parts INT,
 *   started_at DATETIME NOT NULL,
 *   completed_at DATETIME,
 *   aborted_at DATETIME,
 *   created_at DATETIME NOT NULL,
 *   updated_at DATETIME NOT NULL,
 *   INDEX idx_upload_session_id (upload_session_id),
 *   INDEX idx_status (status)
 * );
 * </pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see com.ryuqq.fileflow.domain.upload.MultipartUpload Domain Model
 */
@Entity
@Table(name = "upload_multipart")
public class MultipartUploadJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Upload Session ID (Long FK Strategy)
     * ❌ @ManyToOne 사용 안함!
     */
    @Column(name = "upload_session_id", nullable = false)
    private Long uploadSessionId;

    @Column(name = "provider_upload_id", length = 500)
    private String providerUploadId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MultipartUpload.MultipartStatus status;

    @Column(name = "total_parts")
    private Integer totalParts;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "aborted_at")
    private LocalDateTime abortedAt;

    /**
     * 기본 생성자 (JPA 스펙 요구사항)
     */
    protected MultipartUploadJpaEntity() {
        super();
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id Multipart Upload ID
     * @param uploadSessionId Upload Session ID
     * @param providerUploadId Provider Upload ID
     * @param status 상태
     * @param totalParts 총 파트 수
     * @param startedAt 시작 시간
     * @param completedAt 완료 시간
     * @param abortedAt 중단 시간
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     */
    private MultipartUploadJpaEntity(
        Long id,
        Long uploadSessionId,
        String providerUploadId,
        MultipartUpload.MultipartStatus status,
        Integer totalParts,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime abortedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        super(createdAt, updatedAt);
        this.id = id;
        this.uploadSessionId = uploadSessionId;
        this.providerUploadId = providerUploadId;
        this.status = status;
        this.totalParts = totalParts;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.abortedAt = abortedAt;
    }

    /**
     * Static Factory Method - 신규 엔티티 생성
     *
     * @param uploadSessionId Upload Session ID
     * @param providerUploadId Provider Upload ID
     * @param status 상태
     * @param totalParts 총 파트 수
     * @param startedAt 시작 시간
     * @return 생성된 MultipartUploadJpaEntity
     */
    public static MultipartUploadJpaEntity create(
        Long uploadSessionId,
        String providerUploadId,
        MultipartUpload.MultipartStatus status,
        Integer totalParts,
        LocalDateTime startedAt
    ) {
        MultipartUploadJpaEntity entity = new MultipartUploadJpaEntity();
        entity.uploadSessionId = uploadSessionId;
        entity.providerUploadId = providerUploadId;
        entity.status = status;
        entity.totalParts = totalParts;
        entity.startedAt = startedAt;
        entity.initializeAuditFields();
        return entity;
    }

    /**
     * Static Factory Method - DB 조회 데이터로 재구성
     *
     * @param id Multipart Upload ID
     * @param uploadSessionId Upload Session ID
     * @param providerUploadId Provider Upload ID
     * @param status 상태
     * @param totalParts 총 파트 수
     * @param startedAt 시작 시간
     * @param completedAt 완료 시간
     * @param abortedAt 중단 시간
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     * @return 재구성된 MultipartUploadJpaEntity
     */
    public static MultipartUploadJpaEntity reconstitute(
        Long id,
        Long uploadSessionId,
        String providerUploadId,
        MultipartUpload.MultipartStatus status,
        Integer totalParts,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime abortedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new MultipartUploadJpaEntity(
            id,
            uploadSessionId,
            providerUploadId,
            status,
            totalParts,
            startedAt,
            completedAt,
            abortedAt,
            createdAt,
            updatedAt
        );
    }

    /**
     * Multipart Upload ID를 반환합니다.
     *
     * @return Multipart Upload ID
     */
    public Long getId() {
        return id;
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
     * Provider Upload ID를 반환합니다.
     *
     * @return Provider Upload ID
     */
    public String getProviderUploadId() {
        return providerUploadId;
    }

    /**
     * 상태를 반환합니다.
     *
     * @return 상태
     */
    public MultipartUpload.MultipartStatus getStatus() {
        return status;
    }

    /**
     * 총 파트 수를 반환합니다.
     *
     * @return 총 파트 수
     */
    public Integer getTotalParts() {
        return totalParts;
    }

    /**
     * 시작 시간을 반환합니다.
     *
     * @return 시작 시간
     */
    public LocalDateTime getStartedAt() {
        return startedAt;
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
     * 중단 시간을 반환합니다.
     *
     * @return 중단 시간
     */
    public LocalDateTime getAbortedAt() {
        return abortedAt;
    }

    /**
     * ID를 설정합니다 (JPA 전용).
     *
     * @param id Multipart Upload ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Upload Session ID를 설정합니다 (JPA 전용).
     *
     * @param uploadSessionId Upload Session ID
     */
    public void setUploadSessionId(Long uploadSessionId) {
        this.uploadSessionId = uploadSessionId;
    }

    /**
     * Provider Upload ID를 설정합니다 (JPA 전용).
     *
     * @param providerUploadId Provider Upload ID
     */
    public void setProviderUploadId(String providerUploadId) {
        this.providerUploadId = providerUploadId;
    }

    /**
     * 상태를 설정합니다 (JPA 전용).
     *
     * @param status 상태
     */
    public void setStatus(MultipartUpload.MultipartStatus status) {
        this.status = status;
        this.markAsUpdated();
    }

    /**
     * 총 파트 수를 설정합니다 (JPA 전용).
     *
     * @param totalParts 총 파트 수
     */
    public void setTotalParts(Integer totalParts) {
        this.totalParts = totalParts;
        this.markAsUpdated();
    }

    /**
     * 시작 시간을 설정합니다 (JPA 전용).
     *
     * @param startedAt 시작 시간
     */
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * 완료 시간을 설정합니다 (JPA 전용).
     *
     * @param completedAt 완료 시간
     */
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
        this.markAsUpdated();
    }

    /**
     * 중단 시간을 설정합니다 (JPA 전용).
     *
     * @param abortedAt 중단 시간
     */
    public void setAbortedAt(LocalDateTime abortedAt) {
        this.abortedAt = abortedAt;
        this.markAsUpdated();
    }
}
