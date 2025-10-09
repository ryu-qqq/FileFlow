package com.ryuqq.fileflow.domain.file;

import com.ryuqq.fileflow.domain.upload.vo.FileId;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 파일 간 관계를 표현하는 Domain Entity
 *
 * Aggregate Root:
 * - 파일 파생 관계의 일관성 경계를 정의
 * - 원본 파일과 대상 파일 간의 관계를 관리
 * - 관계별 메타데이터를 저장하여 추가 정보 제공
 *
 * 생명주기:
 * - 파일 변환, 최적화, 썸네일 생성 등의 과정에서 생성
 * - 원본 파일이나 대상 파일이 삭제되면 관계도 정리
 *
 * 불변성:
 * - 관계 정보는 생성 후 변경되지 않음
 * - 메타데이터는 불변 맵으로 관리
 *
 * @author sangwon-ryu
 */
public final class FileRelationship {

    private final Long id;
    private final FileId sourceFileId;
    private final FileId targetFileId;
    private final FileRelationshipType relationshipType;
    private final Map<String, Object> relationshipMetadata;
    private final LocalDateTime createdAt;

    private FileRelationship(
            Long id,
            FileId sourceFileId,
            FileId targetFileId,
            FileRelationshipType relationshipType,
            Map<String, Object> relationshipMetadata,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.sourceFileId = sourceFileId;
        this.targetFileId = targetFileId;
        this.relationshipType = relationshipType;
        this.relationshipMetadata = new HashMap<>(relationshipMetadata);
        this.createdAt = createdAt;
    }

    /**
     * 새로운 파일 관계를 생성합니다.
     *
     * @param sourceFileId 원본 파일 ID
     * @param targetFileId 대상 파일 ID
     * @param relationshipType 관계 유형
     * @param relationshipMetadata 관계 메타데이터
     * @return FileRelationship 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static FileRelationship create(
            FileId sourceFileId,
            FileId targetFileId,
            FileRelationshipType relationshipType,
            Map<String, Object> relationshipMetadata
    ) {
        return create(sourceFileId, targetFileId, relationshipType, relationshipMetadata, Clock.systemDefaultZone());
    }

    /**
     * 새로운 파일 관계를 생성합니다 (테스트용 Clock 주입).
     *
     * @param sourceFileId 원본 파일 ID
     * @param targetFileId 대상 파일 ID
     * @param relationshipType 관계 유형
     * @param relationshipMetadata 관계 메타데이터
     * @param clock 시간 생성용 Clock
     * @return FileRelationship 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static FileRelationship create(
            FileId sourceFileId,
            FileId targetFileId,
            FileRelationshipType relationshipType,
            Map<String, Object> relationshipMetadata,
            Clock clock
    ) {
        validateSourceFileId(sourceFileId);
        validateTargetFileId(targetFileId);
        validateDifferentFiles(sourceFileId, targetFileId);
        validateRelationshipType(relationshipType);
        validateRelationshipMetadata(relationshipMetadata);

        LocalDateTime createdAt = LocalDateTime.now(clock);

        return new FileRelationship(
                null,
                sourceFileId,
                targetFileId,
                relationshipType,
                relationshipMetadata,
                createdAt
        );
    }

    /**
     * 기존 파일 관계를 재구성합니다 (DB에서 로드할 때 사용).
     *
     * @param id 관계 ID
     * @param sourceFileId 원본 파일 ID
     * @param targetFileId 대상 파일 ID
     * @param relationshipType 관계 유형
     * @param relationshipMetadata 관계 메타데이터
     * @param createdAt 생성 시간
     * @return FileRelationship 인스턴스
     */
    public static FileRelationship reconstitute(
            Long id,
            FileId sourceFileId,
            FileId targetFileId,
            FileRelationshipType relationshipType,
            Map<String, Object> relationshipMetadata,
            LocalDateTime createdAt
    ) {
        validateId(id);
        validateSourceFileId(sourceFileId);
        validateTargetFileId(targetFileId);
        validateDifferentFiles(sourceFileId, targetFileId);
        validateRelationshipType(relationshipType);
        validateRelationshipMetadata(relationshipMetadata);
        validateCreatedAt(createdAt);

        return new FileRelationship(
                id,
                sourceFileId,
                targetFileId,
                relationshipType,
                relationshipMetadata,
                createdAt
        );
    }

    // ========== Business Logic Methods ==========

    /**
     * 썸네일 관계인지 확인합니다.
     *
     * @return 썸네일 관계이면 true
     */
    public boolean isThumbnail() {
        return relationshipType.isThumbnail();
    }

    /**
     * 파일 변환 관계인지 확인합니다.
     *
     * @return 변환 관계이면 true
     */
    public boolean isTransformation() {
        return relationshipType.isTransformation();
    }

    /**
     * 파일 파생 관계인지 확인합니다.
     *
     * @return 파생 관계이면 true
     */
    public boolean isDerivative() {
        return relationshipType.isDerivative();
    }

    /**
     * 버전 관계인지 확인합니다.
     *
     * @return 버전 관계이면 true
     */
    public boolean isVersion() {
        return relationshipType.isVersion();
    }

    /**
     * 특정 파일 ID가 이 관계에 포함되는지 확인합니다.
     *
     * @param fileId 확인할 파일 ID
     * @return 포함되면 true
     */
    public boolean involves(FileId fileId) {
        return sourceFileId.equals(fileId) || targetFileId.equals(fileId);
    }

    /**
     * 주어진 파일 ID가 원본 파일인지 확인합니다.
     *
     * @param fileId 확인할 파일 ID
     * @return 원본 파일이면 true
     */
    public boolean isSourceFile(FileId fileId) {
        return sourceFileId.equals(fileId);
    }

    /**
     * 주어진 파일 ID가 대상 파일인지 확인합니다.
     *
     * @param fileId 확인할 파일 ID
     * @return 대상 파일이면 true
     */
    public boolean isTargetFile(FileId fileId) {
        return targetFileId.equals(fileId);
    }

    /**
     * 메타데이터에서 특정 키의 값을 가져옵니다.
     *
     * @param key 메타데이터 키
     * @return 메타데이터 값 (없으면 null)
     */
    public Object getMetadataValue(String key) {
        return relationshipMetadata.get(key);
    }

    /**
     * 메타데이터가 비어있는지 확인합니다.
     *
     * @return 비어있으면 true
     */
    public boolean hasMetadata() {
        return !relationshipMetadata.isEmpty();
    }

    // ========== Validation Methods ==========

    private static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Id must be a positive number");
        }
    }

    private static void validateSourceFileId(FileId sourceFileId) {
        if (sourceFileId == null) {
            throw new IllegalArgumentException("Source file ID cannot be null");
        }
    }

    private static void validateTargetFileId(FileId targetFileId) {
        if (targetFileId == null) {
            throw new IllegalArgumentException("Target file ID cannot be null");
        }
    }

    private static void validateDifferentFiles(FileId sourceFileId, FileId targetFileId) {
        if (sourceFileId.equals(targetFileId)) {
            throw new IllegalArgumentException("Source and target file IDs must be different");
        }
    }

    private static void validateRelationshipType(FileRelationshipType relationshipType) {
        if (relationshipType == null) {
            throw new IllegalArgumentException("Relationship type cannot be null");
        }
    }

    private static void validateRelationshipMetadata(Map<String, Object> relationshipMetadata) {
        if (relationshipMetadata == null) {
            throw new IllegalArgumentException("Relationship metadata cannot be null");
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt cannot be null");
        }
        if (createdAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("CreatedAt cannot be in the future");
        }
    }

    // ========== Getters ==========

    public Long getId() {
        return id;
    }

    public FileId getSourceFileId() {
        return sourceFileId;
    }

    public FileId getTargetFileId() {
        return targetFileId;
    }

    public FileRelationshipType getRelationshipType() {
        return relationshipType;
    }

    public Map<String, Object> getRelationshipMetadata() {
        return new HashMap<>(relationshipMetadata);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ========== Override Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileRelationship that = (FileRelationship) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FileRelationship{" +
                "id=" + id +
                ", sourceFileId=" + sourceFileId +
                ", targetFileId=" + targetFileId +
                ", relationshipType=" + relationshipType +
                ", relationshipMetadata=" + relationshipMetadata +
                ", createdAt=" + createdAt +
                '}';
    }
}
