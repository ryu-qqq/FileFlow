package com.ryuqq.fileflow.domain.file.extraction;

import com.ryuqq.fileflow.domain.file.asset.FileAssetId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * ExtractedData Aggregate Root
 *
 * <p>파일에서 추출된 데이터를 관리하는 Aggregate입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>추출된 메타데이터, OCR 결과, 구조화 데이터 관리</li>
 *   <li>추출 방법 및 품질 정보 관리</li>
 *   <li>멀티테넌트 지원 (tenantId, organizationId)</li>
 *   <li>추출 데이터 생성 이벤트 발행</li>
 * </ul>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>추출 데이터는 항상 원본 FileAsset을 참조함 (fileId)</li>
 *   <li>동일 (fileId, extractionType, extractionMethod, version) 조합은 유일해야 함</li>
 *   <li>Confidence Score는 0.0 ~ 1.0 범위</li>
 *   <li>Quality Score는 0.0 ~ 1.0 범위</li>
 * </ul>
 *
 * <p><strong>Long FK 전략:</strong></p>
 * <ul>
 *   <li>✅ Long fileId, tenantId, organizationId 사용 (JPA 관계 금지)</li>
 *   <li>❌ @ManyToOne FileAsset 금지</li>
 * </ul>
 *
 * <p><strong>Zero External Dependencies:</strong></p>
 * <ul>
 *   <li>Domain Event를 List로 직접 관리 (Spring Data 의존성 제거)</li>
 *   <li>Pure Java 구현</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class ExtractedData {

    private final List<Object> domainEvents = new ArrayList<>();
    private final ExtractedDataId id;
    private final String extractedUuid; // Business UUID
    private final Long fileAssetId; // Long FK Strategy
    private final Long tenantId; // Multi-tenant
    private final Long organizationId; // Multi-tenant
    private final ExtractionType extractionType;
    private final ExtractionMethod extractionMethod;
    private final Integer version;
    private final String traceId; // Pipeline correlation
    private final String textData; // 텍스트 추출 결과 (JSON 문자열)
    private final String structuredData; // 구조화 데이터 (JSON 문자열)
    private final String previewData; // 미리보기 데이터 (JSON 문자열)
    private final Double confidenceScore; // 0.0 ~ 1.0
    private final Double qualityScore; // 0.0 ~ 1.0
    private final ValidationStatus validationStatus;
    private final String notes;
    private final LocalDateTime extractedAt;
    private final LocalDateTime createdAt;

    /**
     * Private Constructor (Static Factory Method 사용)
     */
    private ExtractedData(
        ExtractedDataId id,
        String extractedUuid,
        Long fileAssetId,
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
        LocalDateTime extractedAt,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.extractedUuid = extractedUuid;
        this.fileAssetId = fileAssetId;
        this.tenantId = tenantId;
        this.organizationId = organizationId;
        this.extractionType = extractionType;
        this.extractionMethod = extractionMethod;
        this.version = version;
        this.traceId = traceId;
        this.textData = textData;
        this.structuredData = structuredData;
        this.previewData = previewData;
        this.confidenceScore = confidenceScore;
        this.qualityScore = qualityScore;
        this.validationStatus = validationStatus;
        this.notes = notes;
        this.extractedAt = extractedAt;
        this.createdAt = createdAt;
    }

    /**
     * ExtractedData 생성 (Static Factory Method)
     *
     * @param fileAssetId 원본 FileAsset ID
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @param extractionType 추출 유형
     * @param extractionMethod 추출 방법
     * @param version 버전
     * @param traceId Trace ID
     * @param structuredData 구조화 데이터 (JSON 문자열)
     * @param confidenceScore 신뢰도 점수
     * @param qualityScore 품질 점수
     * @return ExtractedData Aggregate
     */
    public static ExtractedData create(
        FileAssetId fileAssetId,
        Long tenantId,
        Long organizationId,
        ExtractionType extractionType,
        ExtractionMethod extractionMethod,
        Integer version,
        String traceId,
        String structuredData,
        Double confidenceScore,
        Double qualityScore
    ) {
        if (fileAssetId == null || fileAssetId.value() == null) {
            throw new IllegalArgumentException("FileAsset ID는 필수입니다");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID는 필수입니다");
        }
        if (extractionType == null) {
            throw new IllegalArgumentException("Extraction Type은 필수입니다");
        }
        if (extractionMethod == null) {
            throw new IllegalArgumentException("Extraction Method는 필수입니다");
        }
        if (version == null || version < 1) {
            throw new IllegalArgumentException("Version은 1 이상이어야 합니다");
        }
        if (confidenceScore != null && (confidenceScore < 0.0 || confidenceScore > 1.0)) {
            throw new IllegalArgumentException("Confidence Score는 0.0 ~ 1.0 범위여야 합니다");
        }
        if (qualityScore != null && (qualityScore < 0.0 || qualityScore > 1.0)) {
            throw new IllegalArgumentException("Quality Score는 0.0 ~ 1.0 범위여야 합니다");
        }

        LocalDateTime now = LocalDateTime.now();

        ExtractedData extractedData = new ExtractedData(
            null, // ID는 Persistence Layer에서 생성
            UUID.randomUUID().toString(), // Business UUID
            fileAssetId.value(), // ⭐ Long FK 사용
            tenantId,
            organizationId,
            extractionType,
            extractionMethod,
            version,
            traceId,
            null, // textData는 나중에 설정 가능
            structuredData,
            null, // previewData는 나중에 설정 가능
            confidenceScore,
            qualityScore,
            ValidationStatus.PENDING, // 초기 상태
            null, // notes는 옵셔널
            now, // extractedAt
            now  // createdAt
        );

        // Domain Event 발행
        extractedData.registerEvent(new ExtractedDataCreatedEvent(
            null, // ID는 아직 할당되지 않음
            fileAssetId,
            extractionType,
            extractionMethod
        ));

        return extractedData;
    }

    /**
     * DB에서 조회한 데이터로 ExtractedData 재구성 (Static Factory Method)
     *
     * @param id ExtractedData ID
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
     * @param createdAt 생성 시간
     * @return ExtractedData Aggregate
     */
    public static ExtractedData reconstitute(
        ExtractedDataId id,
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
        LocalDateTime extractedAt,
        LocalDateTime createdAt
    ) {
        return new ExtractedData(
            id,
            extractedUuid,
            fileId,
            tenantId,
            organizationId,
            extractionType,
            extractionMethod,
            version,
            traceId,
            textData,
            structuredData,
            previewData,
            confidenceScore,
            qualityScore,
            validationStatus,
            notes,
            extractedAt,
            createdAt
        );
    }

    // Getters (Law of Demeter 준수)

    public ExtractedDataId getId() {
        return id;
    }

    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public String getExtractedUuid() {
        return extractedUuid;
    }

    public Long getFileAssetId() {
        return fileAssetId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    /**
     * Domain Event 등록 (내부 사용)
     *
     * @param event 등록할 도메인 이벤트
     */
    protected void registerEvent(Object event) {
        this.domainEvents.add(event);
    }

    /**
     * 도메인 이벤트 조회 (Persistence Layer 전용)
     *
     * @return 등록된 도메인 이벤트 목록 (읽기 전용)
     */
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 도메인 이벤트 초기화 (Persistence Layer 전용)
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
