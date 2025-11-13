package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.BaseAuditEntity;
import com.ryuqq.fileflow.domain.file.variant.VariantType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * FileVariant JPA Entity (Persistence Layer)
 *
 * <p><strong>역할</strong>: DB 매핑만 담당 (비즈니스 로직 없음)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/file/entity/</p>
 * <p><strong>변환</strong>: {@code FileVariantEntityMapper}를 통해 Domain {@code FileVariant}와 상호 변환</p>
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
 * CREATE TABLE file_variants (
 *   id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   parent_file_asset_id BIGINT NOT NULL,
 *   variant_type VARCHAR(20) NOT NULL,
 *   storage_key VARCHAR(512) NOT NULL,
 *   file_size BIGINT NOT NULL,
 *   mime_type VARCHAR(150) NOT NULL,
 *   created_at DATETIME NOT NULL,
 *   updated_at DATETIME NOT NULL,
 *   INDEX idx_parent (parent_file_asset_id),
 *   UNIQUE KEY uk_parent_variant (parent_file_asset_id, variant_type)
 * );
 * </pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see com.ryuqq.fileflow.domain.file.variant.FileVariant Domain Model
 */
@Entity
@Table(
    name = "file_variants",
    indexes = {
        @Index(name = "idx_parent", columnList = "parent_file_asset_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_parent_variant",
            columnNames = {"parent_file_asset_id", "variant_type"}
        )
    }
)
public class FileVariantJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Parent FileAsset ID (Long FK Strategy)
     * ❌ ManyToOne 관계 어노테이션 사용 안함!
     */
    @Column(name = "parent_file_asset_id", nullable = false)
    private Long parentFileAssetId;

    /**
     * Variant Type (THUMBNAIL, PREVIEW, COMPRESSED, etc.)
     */
    @Column(name = "variant_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private VariantType variantType;

    /**
     * Storage Key (S3 저장 위치)
     */
    @Column(name = "storage_key", nullable = false, length = 512)
    private String storageKey;

    /**
     * File Size (Bytes)
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * MIME Type (예: image/jpeg, image/webp)
     */
    @Column(name = "mime_type", nullable = false, length = 150)
    private String mimeType;

    /**
     * 기본 생성자 (JPA 스펙 요구사항)
     */
    protected FileVariantJpaEntity() {
        super();
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id File Variant ID
     * @param parentFileAssetId Parent FileAsset ID
     * @param variantType Variant Type
     * @param storageKey Storage Key
     * @param fileSize File Size
     * @param mimeType MIME Type
     * @param createdAt Created At
     * @param updatedAt Updated At
     */
    private FileVariantJpaEntity(
        Long id,
        Long parentFileAssetId,
        VariantType variantType,
        String storageKey,
        Long fileSize,
        String mimeType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        super(createdAt, updatedAt);
        this.id = id;
        this.parentFileAssetId = parentFileAssetId;
        this.variantType = variantType;
        this.storageKey = storageKey;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

    /**
     * Static Factory Method - 신규 엔티티 생성
     *
     * @param parentFileAssetId Parent FileAsset ID
     * @param variantType Variant Type
     * @param storageKey Storage Key
     * @param fileSize File Size
     * @param mimeType MIME Type
     * @return 생성된 FileVariantJpaEntity
     */
    public static FileVariantJpaEntity create(
        Long parentFileAssetId,
        VariantType variantType,
        String storageKey,
        Long fileSize,
        String mimeType
    ) {
        FileVariantJpaEntity entity = new FileVariantJpaEntity();
        entity.parentFileAssetId = parentFileAssetId;
        entity.variantType = variantType;
        entity.storageKey = storageKey;
        entity.fileSize = fileSize;
        entity.mimeType = mimeType;
        entity.initializeAuditFields();
        return entity;
    }

    /**
     * Static Factory Method - DB 조회 데이터로 재구성
     *
     * @param id File Variant ID
     * @param parentFileAssetId Parent FileAsset ID
     * @param variantType Variant Type
     * @param storageKey Storage Key
     * @param fileSize File Size
     * @param mimeType MIME Type
     * @param createdAt Created At
     * @param updatedAt Updated At
     * @return 재구성된 FileVariantJpaEntity
     */
    public static FileVariantJpaEntity reconstitute(
        Long id,
        Long parentFileAssetId,
        VariantType variantType,
        String storageKey,
        Long fileSize,
        String mimeType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new FileVariantJpaEntity(
            id,
            parentFileAssetId,
            variantType,
            storageKey,
            fileSize,
            mimeType,
            createdAt,
            updatedAt
        );
    }

    // ===== Getters =====

    public Long getId() {
        return id;
    }

    public Long getParentFileAssetId() {
        return parentFileAssetId;
    }

    public VariantType getVariantType() {
        return variantType;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }
}
