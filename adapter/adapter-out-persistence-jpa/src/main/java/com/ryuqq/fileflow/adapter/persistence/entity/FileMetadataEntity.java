package com.ryuqq.fileflow.adapter.persistence.entity;

import com.ryuqq.fileflow.domain.file.MetadataType;
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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 파일 메타데이터 엔티티
 *
 * 비즈니스 규칙:
 * - 파일별로 메타데이터 키-값 쌍을 저장하는 키-값 저장소
 * - file_id와 metadata_key의 조합이 유일해야 함
 * - 메타데이터 값은 문자열로 저장되며, value_type으로 타입 구분
 * - Entity는 데이터 저장을 위한 매핑만 담당합니다
 * - Lombok을 사용하지 않으므로 모든 메서드를 수동으로 구현합니다
 *
 * @author sangwon-ryu
 */
@Entity
@Table(name = "file_metadata",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_file_metadata", columnNames = {"file_id", "metadata_key"})
        },
        indexes = {
                @Index(name = "idx_file_id", columnList = "file_id"),
                @Index(name = "idx_metadata_key", columnList = "metadata_key")
        })
public class FileMetadataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "file_id", nullable = false, length = 36)
    private String fileId;

    @Column(name = "metadata_key", nullable = false, length = 100)
    private String metadataKey;

    @Column(name = "metadata_value", columnDefinition = "TEXT", nullable = false)
    private String metadataValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 20)
    private MetadataType valueType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * JPA 기본 생성자 (protected)
     */
    protected FileMetadataEntity() {
    }

    /**
     * 파일 메타데이터 엔티티 생성자 (protected - factory method 사용 권장)
     *
     * @param fileId 파일 ID (UUID)
     * @param metadataKey 메타데이터 키
     * @param metadataValue 메타데이터 값
     * @param valueType 값의 데이터 타입
     */
    protected FileMetadataEntity(
            String fileId,
            String metadataKey,
            String metadataValue,
            MetadataType valueType
    ) {
        this.fileId = fileId;
        this.metadataKey = metadataKey;
        this.metadataValue = metadataValue;
        this.valueType = valueType;
    }

    /**
     * 새로운 파일 메타데이터 엔티티를 생성하는 factory method
     *
     * @param fileId 파일 ID (UUID)
     * @param metadataKey 메타데이터 키
     * @param metadataValue 메타데이터 값
     * @param valueType 값의 데이터 타입
     * @return 생성된 FileMetadataEntity
     */
    public static FileMetadataEntity of(
            String fileId,
            String metadataKey,
            String metadataValue,
            MetadataType valueType
    ) {
        return new FileMetadataEntity(
                fileId,
                metadataKey,
                metadataValue,
                valueType
        );
    }

    /**
     * 메타데이터 값을 업데이트합니다.
     *
     * @param newValue 새로운 값
     */
    public void updateValue(String newValue) {
        this.metadataValue = newValue;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
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

    public String getMetadataKey() {
        return metadataKey;
    }

    public String getMetadataValue() {
        return metadataValue;
    }

    public MetadataType getValueType() {
        return valueType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ========== Object Methods ==========

    /**
     * 비즈니스 키 기반 equals: fileId + metadataKey 조합으로 동등성 판단
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileMetadataEntity that = (FileMetadataEntity) o;
        return Objects.equals(fileId, that.fileId)
                && Objects.equals(metadataKey, that.metadataKey);
    }

    /**
     * 비즈니스 키 기반 hashCode: fileId + metadataKey 조합
     */
    @Override
    public int hashCode() {
        return Objects.hash(fileId, metadataKey);
    }

    @Override
    public String toString() {
        return "FileMetadataEntity{" +
                "id=" + id +
                ", fileId='" + fileId + '\'' +
                ", metadataKey='" + metadataKey + '\'' +
                ", metadataValue='" + metadataValue + '\'' +
                ", valueType=" + valueType +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
