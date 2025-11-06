package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.BaseAuditEntity;
import com.ryuqq.fileflow.domain.common.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * External Download Outbox JPA Entity (Persistence Layer)
 *
 * <p><strong>역할</strong>: DB 매핑만 담당 (비즈니스 로직 없음)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/download/entity/</p>
 * <p><strong>패턴</strong>: Transactional Outbox Pattern</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ JPA 어노테이션만 사용 (비즈니스 로직 없음)</li>
 *   <li>✅ Long FK 전략 (JPA 관계 어노테이션 금지)</li>
 *   <li>✅ Getter만 제공 (Setter 금지)</li>
 *   <li>✅ Static Factory Methods: {@code create()}, {@code reconstitute()}</li>
 *   <li>❌ Lombok 금지</li>
 *   <li>❌ JPA 관계 어노테이션 금지 (ManyToOne, OneToMany 등)</li>
 * </ul>
 *
 * <h3>Outbox 패턴</h3>
 * <ul>
 *   <li>External Download 이벤트를 안전하게 발행하기 위한 Outbox 테이블</li>
 *   <li>트랜잭션 내에서 이벤트를 저장하고, 별도 프로세스에서 비동기 처리</li>
 *   <li>멱등성 보장을 위한 idempotencyKey 사용</li>
 * </ul>
 *
 * <h3>테이블 구조</h3>
 * <pre>
 * CREATE TABLE external_download_outbox (
 *   id BIGINT AUTO_INCREMENT PRIMARY KEY,
 *   idempotency_key VARCHAR(255) NOT NULL UNIQUE,
 *   download_id BIGINT NOT NULL,
 *   upload_session_id BIGINT NOT NULL,
 *   status VARCHAR(50) NOT NULL,
 *   payload TEXT NOT NULL,
 *   retry_count INT NOT NULL DEFAULT 0,
 *   created_at DATETIME(6) NOT NULL,
 *   INDEX idx_status_created (status, created_at),
 *   INDEX idx_download_id (download_id)
 * );
 * </pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Entity
@Table(
    name = "external_download_outbox",
    indexes = {
        @Index(name = "idx_status_created", columnList = "status, created_at"),
        @Index(name = "idx_download_id", columnList = "download_id"),
        @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true)
    }
)
public class ExternalDownloadOutboxJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 멱등성 키 (Unique 제약)
     * 중복 이벤트 발행 방지
     */
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    /**
     * External Download ID (Long FK)
     */
    @Column(name = "download_id", nullable = false)
    private Long downloadId;

    /**
     * Upload Session ID (Long FK)
     */
    @Column(name = "upload_session_id", nullable = false)
    private Long uploadSessionId;

    /**
     * Outbox 처리 상태
     * PENDING, PROCESSING, COMPLETED, FAILED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OutboxStatus status;


    /**
     * 재시도 횟수
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    // createdAt/updatedAt are provided by BaseAuditEntity

    /**
     * JPA 기본 생성자 (protected)
     */
    protected ExternalDownloadOutboxJpaEntity() {
    }

    /**
     * Private 전체 생성자
     *
     * @param id               Outbox ID
     * @param idempotencyKey   멱등성 키
     * @param downloadId       External Download ID
     * @param uploadSessionId  Upload Session ID
     * @param status           Outbox 상태
     * @param retryCount       재시도 횟수
     * @param createdAt        생성 시간
     */
    private ExternalDownloadOutboxJpaEntity(
        Long id,
        String idempotencyKey,
        Long downloadId,
        Long uploadSessionId,
        OutboxStatus status,
        Integer retryCount,
        LocalDateTime createdAt
    ) {
        super(createdAt, createdAt);
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.downloadId = downloadId;
        this.uploadSessionId = uploadSessionId;
        this.status = status;
        this.retryCount = retryCount;
    }

    /**
     * Static Factory Method - 신규 Outbox 메시지 생성
     *
     * <p><strong>사용 시기</strong>: External Download 생성 시 Outbox 메시지 저장</p>
     *
     * <p><strong>초기 상태</strong>:</p>
     * <ul>
     *   <li>status = PENDING</li>
     *   <li>retryCount = 0</li>
     *   <li>createdAt = 현재 시간</li>
     * </ul>
     *
     * @param idempotencyKey  멱등성 키 (중복 방지)
     * @param downloadId      External Download ID
     * @param uploadSessionId Upload Session ID
     * @return ExternalDownloadOutboxJpaEntity 인스턴스
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static ExternalDownloadOutboxJpaEntity create(
        String idempotencyKey,
        Long downloadId,
        Long uploadSessionId
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency Key는 필수입니다");
        }
        if (downloadId == null) {
            throw new IllegalArgumentException("Download ID는 필수입니다");
        }
        if (uploadSessionId == null) {
            throw new IllegalArgumentException("Upload Session ID는 필수입니다");
        }


        ExternalDownloadOutboxJpaEntity entity = new ExternalDownloadOutboxJpaEntity(
            null,                      // id (DB에서 생성)
            idempotencyKey,
            downloadId,
            uploadSessionId,
            OutboxStatus.PENDING,      // 초기 상태
            0,                         // 초기 재시도 횟수
            LocalDateTime.now()
        );
        // initialize audit timestamps
        entity.initializeAuditFields();
        return entity;
    }

    /**
     * Static Factory Method - DB 조회 데이터로 재구성
     *
     * <p><strong>사용 시기</strong>: JPA Repository에서 조회한 데이터를 Entity로 변환</p>
     *
     * @param id               Outbox ID
     * @param idempotencyKey   멱등성 키
     * @param downloadId       External Download ID
     * @param uploadSessionId  Upload Session ID
     * @param status           Outbox 상태
     * @param retryCount       재시도 횟수
     * @param createdAt        생성 시간
     * @return ExternalDownloadOutboxJpaEntity 인스턴스
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static ExternalDownloadOutboxJpaEntity reconstitute(
        Long id,
        String idempotencyKey,
        Long downloadId,
        Long uploadSessionId,
        OutboxStatus status,
        Integer retryCount,
        LocalDateTime createdAt
    ) {
        return new ExternalDownloadOutboxJpaEntity(
            id,
            idempotencyKey,
            downloadId,
            uploadSessionId,
            status,
            retryCount,
            createdAt
        );
    }

    // ===== Getter Methods (NO Setter) =====

    /**
     * Outbox ID를 반환합니다.
     *
     * @return Outbox ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 멱등성 키를 반환합니다.
     *
     * @return 멱등성 키
     */
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    /**
     * External Download ID를 반환합니다.
     *
     * @return External Download ID
     */
    public Long getDownloadId() {
        return downloadId;
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
     * Outbox 상태를 반환합니다.
     *
     * @return Outbox 상태
     */
    public OutboxStatus getStatus() {
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
     * 생성 시간을 반환합니다.
     *
     * @return 생성 시간
     */
    // getCreatedAt() inherited from BaseAuditEntity
}
