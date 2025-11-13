package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.BaseAuditEntity;
import com.ryuqq.fileflow.domain.file.extraction.ExtractionMethod;
import com.ryuqq.fileflow.domain.file.extraction.ExtractionType;
import com.ryuqq.fileflow.domain.file.extraction.ValidationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * ExtractedData JPA Entity
 *
 * <p><strong>역할</strong>: extracted_data 테이블 매핑 Entity</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/file/entity/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ No Lombok: Pure Java (Domain 규칙 준수)</li>
 *   <li>✅ Long FK Strategy: Long fileId (JPA 관계 금지)</li>
 *   <li>✅ Static Factory Method: create() 패턴</li>
 *   <li>✅ Multi-tenant: tenantId, organizationId</li>
 *   <li>✅ Business Unique Key: (fileId, extractionType, extractionMethod, version)</li>
 * </ul>
 *
 * <h3>Cold/Hot 분리 (간소화)</h3>
 * <ul>
 *   <li>✅ textData, structuredData, previewData를 TEXT 컬럼으로 직접 저장</li>
 *   <li>❌ *_ref 필드 제거 (S3 분리 안함)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Entity
@Table(
    name = "extracted_data",
    indexes = {
        @Index(name = "idx_file", columnList = "file_id"),
        @Index(name = "idx_tenant", columnList = "tenant_id, organization_id"),
        @Index(name = "idx_trace", columnList = "trace_id"),
        @Index(name = "idx_extracted_uuid", columnList = "extracted_uuid")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_business_key",
            columnNames = {"file_id", "extraction_type", "extraction_method", "version"}
        )
    }
)
public class ExtractedDataJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "extracted_uuid", nullable = false, length = 36)
    private String extractedUuid;

    @Column(name = "file_id", nullable = false)
    private Long fileId; // ⭐ Long FK Strategy

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId; // ⭐ Multi-tenant

    @Column(name = "organization_id", nullable = false)
    private Long organizationId; // ⭐ Multi-tenant

    @Column(name = "extraction_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ExtractionType extractionType;

    @Column(name = "extraction_method", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ExtractionMethod extractionMethod;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "trace_id", length = 100)
    private String traceId;

    @Column(name = "text_data", columnDefinition = "TEXT")
    private String textData; // JSON 문자열

    @Column(name = "structured_data", columnDefinition = "TEXT")
    private String structuredData; // JSON 문자열

    @Column(name = "preview_data", columnDefinition = "TEXT")
    private String previewData; // JSON 문자열

    @Column(name = "confidence_score")
    private Double confidenceScore; // 0.0 ~ 1.0

    @Column(name = "quality_score")
    private Double qualityScore; // 0.0 ~ 1.0

    @Column(name = "validation_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ValidationStatus validationStatus;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "extracted_at", nullable = false)
    private LocalDateTime extractedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // Soft Delete

    /**
     * JPA Default Constructor (Protected)
     */
    protected ExtractedDataJpaEntity() {
        // JPA 전용
    }

    /**
     * Static Factory Method - 신규 생성 시
     *
     * @param extractedUuid Business UUID
     * @param fileId File ID
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @param extractionType 추출 유형
     * @param extractionMethod 추출 방법
     * @param version 버전
     * @param traceId Trace ID
     * @param textData 텍스트 데이터
     * @param structuredData 구조화 데이터
     * @param previewData 미리보기 데이터
     * @param confidenceScore 신뢰도 점수
     * @param qualityScore 품질 점수
     * @param validationStatus 검증 상태
     * @param notes 노트
     * @param extractedAt 추출 시간
     * @return ExtractedDataJpaEntity
     */
    public static ExtractedDataJpaEntity create(
        String extractedUuid,
        Long fileId,
        Long tenantId,
        Long organizationId,
        ExtractionType extractionType,
        ExtractionMethod extractionMethod,
        Integer version,
        String traceId,
        String textData,
        String structuredData,
        String previewData,
        Double confidenceScore,
        Double qualityScore,
        ValidationStatus validationStatus,
        String notes,
        LocalDateTime extractedAt
    ) {
        ExtractedDataJpaEntity entity = new ExtractedDataJpaEntity();
        entity.extractedUuid = extractedUuid;
        entity.fileId = fileId;
        entity.tenantId = tenantId;
        entity.organizationId = organizationId;
        entity.extractionType = extractionType;
        entity.extractionMethod = extractionMethod;
        entity.version = version;
        entity.traceId = traceId;
        entity.textData = textData;
        entity.structuredData = structuredData;
        entity.previewData = previewData;
        entity.confidenceScore = confidenceScore;
        entity.qualityScore = qualityScore;
        entity.validationStatus = validationStatus;
        entity.notes = notes;
        entity.extractedAt = extractedAt;
        entity.initializeAuditFields(); // BaseAuditEntity에서 상속
        return entity;
    }

    // Getters (Plain Java)

    public Long getId() {
        return id;
    }

    public String getExtractedUuid() {
        return extractedUuid;
    }

    public Long getFileId() {
        return fileId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public ExtractionType getExtractionType() {
        return extractionType;
    }

    public ExtractionMethod getExtractionMethod() {
        return extractionMethod;
    }

    public Integer getVersion() {
        return version;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getTextData() {
        return textData;
    }

    public String getStructuredData() {
        return structuredData;
    }

    public String getPreviewData() {
        return previewData;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public Double getQualityScore() {
        return qualityScore;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getExtractedAt() {
        return extractedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    /**
     * Soft Delete
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
