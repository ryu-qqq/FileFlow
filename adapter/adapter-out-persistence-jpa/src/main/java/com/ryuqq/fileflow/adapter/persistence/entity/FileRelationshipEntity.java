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
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 파일 관계 엔티티
 *
 * 비즈니스 규칙:
 * - 파일 간 관계(원본-썸네일, 원본-변환본 등)를 저장합니다
 * - JSON 타입으로 관계별 메타데이터를 유연하게 저장합니다
 * - 동일한 source-target-type 조합은 UNIQUE 제약으로 중복 방지합니다
 * - Entity는 데이터 저장을 위한 매핑만 담당합니다
 * - Lombok을 사용하지 않으므로 모든 메서드를 수동으로 구현합니다
 *
 * @author sangwon-ryu
 */
@Entity
@Table(name = "file_relationship", indexes = {
        @Index(name = "idx_source_file_id", columnList = "source_file_id"),
        @Index(name = "idx_target_file_id", columnList = "target_file_id"),
        @Index(name = "idx_relationship_type", columnList = "relationship_type"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
public class FileRelationshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "source_file_id", nullable = false, length = 36)
    private String sourceFileId;

    @Column(name = "target_file_id", nullable = false, length = 36)
    private String targetFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 50)
    private FileRelationshipTypeEntity relationshipType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "relationship_metadata", columnDefinition = "JSON")
    private Map<String, Object> relationshipMetadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * JPA 기본 생성자 (protected)
     */
    protected FileRelationshipEntity() {
    }

    /**
     * 파일 관계 엔티티 생성자 (protected - factory method 사용 권장)
     *
     * @param sourceFileId 원본 파일 ID
     * @param targetFileId 대상 파일 ID
     * @param relationshipType 관계 유형
     * @param relationshipMetadata 관계 메타데이터
     */
    protected FileRelationshipEntity(
            String sourceFileId,
            String targetFileId,
            FileRelationshipTypeEntity relationshipType,
            Map<String, Object> relationshipMetadata
    ) {
        this.sourceFileId = sourceFileId;
        this.targetFileId = targetFileId;
        this.relationshipType = relationshipType;
        this.relationshipMetadata = relationshipMetadata != null ? new HashMap<>(relationshipMetadata) : new HashMap<>();
    }

    /**
     * 새로운 파일 관계 엔티티를 생성하는 factory method
     *
     * @param sourceFileId 원본 파일 ID
     * @param targetFileId 대상 파일 ID
     * @param relationshipType 관계 유형
     * @param relationshipMetadata 관계 메타데이터
     * @return 생성된 FileRelationshipEntity
     */
    public static FileRelationshipEntity of(
            String sourceFileId,
            String targetFileId,
            FileRelationshipTypeEntity relationshipType,
            Map<String, Object> relationshipMetadata
    ) {
        return new FileRelationshipEntity(
                sourceFileId,
                targetFileId,
                relationshipType,
                relationshipMetadata
        );
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.relationshipMetadata == null) {
            this.relationshipMetadata = new HashMap<>();
        }
    }

    // ========== Getters ==========

    public Long getId() {
        return id;
    }

    public String getSourceFileId() {
        return sourceFileId;
    }

    public String getTargetFileId() {
        return targetFileId;
    }

    public FileRelationshipTypeEntity getRelationshipType() {
        return relationshipType;
    }

    public Map<String, Object> getRelationshipMetadata() {
        return relationshipMetadata != null ? new HashMap<>(relationshipMetadata) : new HashMap<>();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileRelationshipEntity that = (FileRelationshipEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FileRelationshipEntity{" +
                "id=" + id +
                ", sourceFileId='" + sourceFileId + '\'' +
                ", targetFileId='" + targetFileId + '\'' +
                ", relationshipType=" + relationshipType +
                ", relationshipMetadata=" + relationshipMetadata +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * 파일 관계 유형 엔티티 enum
     */
    public enum FileRelationshipTypeEntity {
        THUMBNAIL,
        OPTIMIZED,
        CONVERTED,
        DERIVATIVE,
        VERSION
    }
}
