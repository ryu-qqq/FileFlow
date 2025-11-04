package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.BaseAuditEntity;
import com.ryuqq.fileflow.domain.download.ExternalDownloadStatus;

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
 * External Download JPA Entity (Persistence Layer)
 *
 * <p><strong>역할</strong>: DB 매핑만 담당 (비즈니스 로직 없음)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/download/entity/</p>
 * <p><strong>변환</strong>: {@code ExternalDownloadMapper}를 통해 Domain {@code ExternalDownload}와 상호 변환</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ JPA 어노테이션만 사용 (비즈니스 로직 없음)</li>
 *   <li>✅ Long FK 전략 (JPA 관계 어노테이션 금지)</li>
 *   <li>✅ 불변성 (Setter 금지, Mapper에서 reconstitute() 사용)</li>
 *   <li>✅ Static Factory Methods: {@code create()}, {@code reconstitute()}</li>
 *   <li>❌ Lombok 금지</li>
 *   <li>❌ JPA 관계 어노테이션 금지 (ManyToOne 등)</li>
 *   <li>❌ Setter 금지 (Domain → Entity 변환은 Mapper의 reconstitute() 사용)</li>
 * </ul>
 *
 * <h3>테이블 스키마</h3>
 * <pre>
 * CREATE TABLE external_download (
 *   id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   upload_session_id BIGINT NOT NULL,
 *   source_url VARCHAR(2000) NOT NULL,
 *   bytes_transferred BIGINT DEFAULT 0,
 *   total_bytes BIGINT,
 *   status VARCHAR(20) NOT NULL,
 *   retry_count INT DEFAULT 0,
 *   last_retry_at DATETIME,
 *   error_code VARCHAR(50),
 *   error_message TEXT,
 *   created_at DATETIME NOT NULL,
 *   updated_at DATETIME NOT NULL,
 *   INDEX idx_upload_session_id (upload_session_id),
 *   INDEX idx_status (status)
 * );
 * </pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see com.ryuqq.fileflow.domain.download.ExternalDownload Domain Model
 */
@Entity
@Table(name = "external_download")
public class ExternalDownloadJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Upload Session ID (Long FK Strategy)
     * ❌ ManyToOne 관계 어노테이션 사용 안함!
     */
    @Column(name = "upload_session_id", nullable = false)
    private Long uploadSessionId;

    @Column(name = "source_url", nullable = false, length = 2000)
    private String sourceUrl;

    @Column(name = "bytes_transferred")
    private Long bytesTransferred;

    @Column(name = "total_bytes")
    private Long totalBytes;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ExternalDownloadStatus status;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 기본 생성자 (JPA 스펙 요구사항)
     */
    protected ExternalDownloadJpaEntity() {
        super();
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id External Download ID
     * @param uploadSessionId Upload Session ID
     * @param sourceUrl 소스 URL
     * @param bytesTransferred 전송된 바이트 수
     * @param totalBytes 총 바이트 수
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @param lastRetryAt 마지막 재시도 시간
     * @param errorCode 오류 코드
     * @param errorMessage 오류 메시지
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     */
    private ExternalDownloadJpaEntity(
        Long id,
        Long uploadSessionId,
        String sourceUrl,
        Long bytesTransferred,
        Long totalBytes,
        ExternalDownloadStatus status,
        Integer retryCount,
        LocalDateTime lastRetryAt,
        String errorCode,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        super(createdAt, updatedAt);
        this.id = id;
        this.uploadSessionId = uploadSessionId;
        this.sourceUrl = sourceUrl;
        this.bytesTransferred = bytesTransferred;
        this.totalBytes = totalBytes;
        this.status = status;
        this.retryCount = retryCount;
        this.lastRetryAt = lastRetryAt;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Static Factory Method - 신규 엔티티 생성
     *
     * @param uploadSessionId Upload Session ID
     * @param sourceUrl 소스 URL
     * @param status 상태
     * @return 생성된 ExternalDownloadJpaEntity
     */
    public static ExternalDownloadJpaEntity create(
        Long uploadSessionId,
        String sourceUrl,
        ExternalDownloadStatus status
    ) {
        ExternalDownloadJpaEntity entity = new ExternalDownloadJpaEntity();
        entity.uploadSessionId = uploadSessionId;
        entity.sourceUrl = sourceUrl;
        entity.status = status;
        entity.bytesTransferred = 0L;
        entity.retryCount = 0;
        entity.initializeAuditFields();
        return entity;
    }

    /**
     * Static Factory Method - DB 조회 데이터로 재구성
     *
     * @param id External Download ID
     * @param uploadSessionId Upload Session ID
     * @param sourceUrl 소스 URL
     * @param bytesTransferred 전송된 바이트 수
     * @param totalBytes 총 바이트 수
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @param lastRetryAt 마지막 재시도 시간
     * @param errorCode 오류 코드
     * @param errorMessage 오류 메시지
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     * @return 재구성된 ExternalDownloadJpaEntity
     */
    public static ExternalDownloadJpaEntity reconstitute(
        Long id,
        Long uploadSessionId,
        String sourceUrl,
        Long bytesTransferred,
        Long totalBytes,
        ExternalDownloadStatus status,
        Integer retryCount,
        LocalDateTime lastRetryAt,
        String errorCode,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ExternalDownloadJpaEntity(
            id,
            uploadSessionId,
            sourceUrl,
            bytesTransferred,
            totalBytes,
            status,
            retryCount,
            lastRetryAt,
            errorCode,
            errorMessage,
            createdAt,
            updatedAt
        );
    }

    /**
     * External Download ID를 반환합니다.
     *
     * @return External Download ID
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
     * 소스 URL을 반환합니다.
     *
     * @return 소스 URL
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * 전송된 바이트 수를 반환합니다.
     *
     * @return 전송된 바이트 수
     */
    public Long getBytesTransferred() {
        return bytesTransferred;
    }

    /**
     * 총 바이트 수를 반환합니다.
     *
     * @return 총 바이트 수
     */
    public Long getTotalBytes() {
        return totalBytes;
    }

    /**
     * 상태를 반환합니다.
     *
     * @return 상태
     */
    public ExternalDownloadStatus getStatus() {
        return status;
    }

    /**
     * 재시도 횟수를 반환합니다.
     *
     * @return 재시도 횟수
     */
    public Integer getRetryCount() {
        return retryCount;
    }

    /**
     * 마지막 재시도 시간을 반환합니다.
     *
     * @return 마지막 재시도 시간
     */
    public LocalDateTime getLastRetryAt() {
        return lastRetryAt;
    }

    /**
     * 오류 코드를 반환합니다.
     *
     * @return 오류 코드
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 오류 메시지를 반환합니다.
     *
     * @return 오류 메시지
     */
    public String getErrorMessage() {
        return errorMessage;
    }

}
