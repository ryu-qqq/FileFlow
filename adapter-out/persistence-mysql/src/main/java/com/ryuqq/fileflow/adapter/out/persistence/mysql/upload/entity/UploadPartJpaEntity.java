package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Upload Part JPA Entity (Persistence Layer)
 *
 * <p><strong>역할</strong>: DB 매핑만 담당 (비즈니스 로직 없음)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/upload/entity/</p>
 * <p><strong>변환</strong>: {@code MultipartUploadMapper}를 통해 Domain {@code UploadPart}와 상호 변환</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ JPA 어노테이션만 사용 (비즈니스 로직 없음)</li>
 *   <li>✅ Long FK 전략 (JPA 관계 어노테이션 금지)</li>
 *   <li>✅ Setter 제공 (JPA 전용, 외부 노출 금지)</li>
 *   <li>✅ Static Factory Methods: {@code create()}, {@code reconstitute()}</li>
 *   <li>❌ Lombok 금지</li>
 *   <li>❌ JPA 관계 어노테이션 금지 (@ManyToOne 등)</li>
 * </ul>
 *
 * <h3>테이블 스키마</h3>
 * <pre>
 * CREATE TABLE upload_part (
 *   id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   multipart_upload_id BIGINT NOT NULL,
 *   part_number INT NOT NULL,
 *   etag VARCHAR(255) NOT NULL,
 *   size BIGINT NOT NULL,
 *   checksum VARCHAR(255),
 *   uploaded_at DATETIME NOT NULL,
 *   created_at DATETIME NOT NULL,
 *   INDEX idx_multipart_upload_id (multipart_upload_id),
 *   UNIQUE KEY uk_multipart_part (multipart_upload_id, part_number)
 * );
 * </pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see com.ryuqq.fileflow.domain.upload.UploadPart Domain Model
 */
@Entity
@Table(name = "upload_part")
public class UploadPartJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Multipart Upload ID (Long FK Strategy)
     * ❌ @ManyToOne 사용 안함!
     */
    @Column(name = "multipart_upload_id", nullable = false)
    private Long multipartUploadId;

    @Column(name = "part_number", nullable = false)
    private Integer partNumber;

    @Column(name = "etag", nullable = false, length = 255)
    private String etag;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "checksum", length = 255)
    private String checksum;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    // createdAt/updatedAt are provided by BaseAuditEntity

    /**
     * 기본 생성자 (JPA 스펙 요구사항)
     */
    protected UploadPartJpaEntity() {
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id Upload Part ID
     * @param multipartUploadId Multipart Upload ID
     * @param partNumber 파트 번호
     * @param etag ETag
     * @param size 파트 크기
     * @param checksum 체크섬
     * @param uploadedAt 업로드 완료 시간
     * @param createdAt 생성 시간
     */
    private UploadPartJpaEntity(
        Long id,
        Long multipartUploadId,
        Integer partNumber,
        String etag,
        Long size,
        String checksum,
        LocalDateTime uploadedAt,
        LocalDateTime createdAt
    ) {
        super(createdAt, createdAt);
        this.id = id;
        this.multipartUploadId = multipartUploadId;
        this.partNumber = partNumber;
        this.etag = etag;
        this.size = size;
        this.checksum = checksum;
        this.uploadedAt = uploadedAt;
    }

    /**
     * Static Factory Method - 신규 엔티티 생성
     *
     * @param multipartUploadId Multipart Upload ID
     * @param partNumber 파트 번호
     * @param etag ETag
     * @param size 파트 크기
     * @param checksum 체크섬
     * @param uploadedAt 업로드 완료 시간
     * @return 생성된 UploadPartJpaEntity
     */
    public static UploadPartJpaEntity create(
        Long multipartUploadId,
        Integer partNumber,
        String etag,
        Long size,
        String checksum,
        LocalDateTime uploadedAt
    ) {
        UploadPartJpaEntity entity = new UploadPartJpaEntity();
        entity.multipartUploadId = multipartUploadId;
        entity.partNumber = partNumber;
        entity.etag = etag;
        entity.size = size;
        entity.checksum = checksum;
        entity.uploadedAt = uploadedAt;
        entity.initializeAuditFields();
        return entity;
    }

    /**
     * Static Factory Method - DB 조회 데이터로 재구성
     *
     * @param id Upload Part ID
     * @param multipartUploadId Multipart Upload ID
     * @param partNumber 파트 번호
     * @param etag ETag
     * @param size 파트 크기
     * @param checksum 체크섬
     * @param uploadedAt 업로드 완료 시간
     * @param createdAt 생성 시간
     * @return 재구성된 UploadPartJpaEntity
     */
    public static UploadPartJpaEntity reconstitute(
        Long id,
        Long multipartUploadId,
        Integer partNumber,
        String etag,
        Long size,
        String checksum,
        LocalDateTime uploadedAt,
        LocalDateTime createdAt
    ) {
        return new UploadPartJpaEntity(
            id,
            multipartUploadId,
            partNumber,
            etag,
            size,
            checksum,
            uploadedAt,
            createdAt
        );
    }

    /**
     * Upload Part ID를 반환합니다.
     *
     * @return Upload Part ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Multipart Upload ID를 반환합니다.
     *
     * @return Multipart Upload ID
     */
    public Long getMultipartUploadId() {
        return multipartUploadId;
    }

    /**
     * 파트 번호를 반환합니다.
     *
     * @return 파트 번호
     */
    public Integer getPartNumber() {
        return partNumber;
    }

    /**
     * ETag를 반환합니다.
     *
     * @return ETag
     */
    public String getEtag() {
        return etag;
    }

    /**
     * 파트 크기를 반환합니다.
     *
     * @return 파트 크기 (bytes)
     */
    public Long getSize() {
        return size;
    }

    /**
     * 체크섬을 반환합니다.
     *
     * @return 체크섬
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * 업로드 완료 시간을 반환합니다.
     *
     * @return 업로드 완료 시간
     */
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }


}
