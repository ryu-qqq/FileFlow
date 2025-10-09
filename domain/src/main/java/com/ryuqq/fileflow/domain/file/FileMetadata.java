package com.ryuqq.fileflow.domain.file;

import com.ryuqq.fileflow.domain.upload.vo.FileId;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 파일 메타데이터를 표현하는 Domain Entity
 *
 * 식별자:
 * - fileId + metadataKey 조합으로 유일하게 식별됨
 * - 같은 파일에 대해 같은 키를 가진 메타데이터는 하나만 존재
 *
 * 불변성:
 * - 모든 필드는 final이며 생성 후 변경 불가
 * - 값 변경이 필요한 경우 withValue()로 새 인스턴스 생성
 *
 * 메타데이터 타입:
 * - STRING: 일반 문자열 (예: format="JPEG")
 * - NUMBER: 숫자 (예: width=1920)
 * - BOOLEAN: 불리언 (예: has_alpha=true)
 * - JSON: 복잡한 객체 (예: exif_data={...})
 *
 * @author sangwon-ryu
 */
public final class FileMetadata {

    private final FileId fileId;
    private final String metadataKey;
    private final String metadataValue;
    private final MetadataType valueType;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private FileMetadata(
            FileId fileId,
            String metadataKey,
            String metadataValue,
            MetadataType valueType,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.fileId = fileId;
        this.metadataKey = metadataKey;
        this.metadataValue = metadataValue;
        this.valueType = valueType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 새로운 FileMetadata를 생성합니다.
     *
     * @param fileId 파일 ID
     * @param metadataKey 메타데이터 키 (예: "width", "duration")
     * @param metadataValue 메타데이터 값
     * @param valueType 값의 데이터 타입
     * @return FileMetadata 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static FileMetadata create(
            FileId fileId,
            String metadataKey,
            String metadataValue,
            MetadataType valueType
    ) {
        return create(fileId, metadataKey, metadataValue, valueType, Clock.systemDefaultZone());
    }

    /**
     * 새로운 FileMetadata를 생성합니다 (테스트용 Clock 주입).
     *
     * @param fileId 파일 ID
     * @param metadataKey 메타데이터 키
     * @param metadataValue 메타데이터 값
     * @param valueType 값의 데이터 타입
     * @param clock 시간 생성용 Clock
     * @return FileMetadata 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static FileMetadata create(
            FileId fileId,
            String metadataKey,
            String metadataValue,
            MetadataType valueType,
            Clock clock
    ) {
        validateFileId(fileId);
        validateMetadataKey(metadataKey);
        validateMetadataValue(metadataValue);
        validateValueType(valueType);
        validateTypeCompatibility(metadataValue, valueType);

        LocalDateTime now = LocalDateTime.now(clock);

        return new FileMetadata(
                fileId,
                metadataKey,
                metadataValue,
                valueType,
                now,
                now
        );
    }

    /**
     * 기존 FileMetadata를 재구성합니다 (DB에서 로드할 때 사용).
     *
     * @param fileId 파일 ID
     * @param metadataKey 메타데이터 키
     * @param metadataValue 메타데이터 값
     * @param valueType 값의 데이터 타입
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata reconstitute(
            FileId fileId,
            String metadataKey,
            String metadataValue,
            MetadataType valueType,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        validateFileId(fileId);
        validateMetadataKey(metadataKey);
        validateMetadataValue(metadataValue);
        validateValueType(valueType);
        validateCreatedAt(createdAt);
        validateUpdatedAt(updatedAt);

        return new FileMetadata(
                fileId,
                metadataKey,
                metadataValue,
                valueType,
                createdAt,
                updatedAt
        );
    }

    // ========== Business Logic Methods ==========

    /**
     * 메타데이터 값을 변경한 새로운 인스턴스를 반환합니다.
     *
     * @param newValue 새로운 값
     * @return 값이 변경된 새 FileMetadata 인스턴스
     * @throws IllegalArgumentException 값이 타입과 호환되지 않는 경우
     */
    public FileMetadata withValue(String newValue) {
        return withValue(newValue, Clock.systemDefaultZone());
    }

    /**
     * 메타데이터 값을 변경한 새로운 인스턴스를 반환합니다 (테스트용 Clock 주입).
     *
     * @param newValue 새로운 값
     * @param clock 시간 생성용 Clock
     * @return 값이 변경된 새 FileMetadata 인스턴스
     */
    public FileMetadata withValue(String newValue, Clock clock) {
        validateMetadataValue(newValue);
        validateTypeCompatibility(newValue, this.valueType);

        return new FileMetadata(
                this.fileId,
                this.metadataKey,
                newValue,
                this.valueType,
                this.createdAt,
                LocalDateTime.now(clock)
        );
    }

    /**
     * 메타데이터 값을 문자열로 반환합니다.
     *
     * @return 메타데이터 값
     */
    public String asString() {
        return metadataValue;
    }

    /**
     * 메타데이터 값을 숫자로 변환하여 반환합니다.
     *
     * @return 숫자 값
     * @throws IllegalStateException 값이 NUMBER 타입이 아니거나 변환할 수 없는 경우
     */
    public Double asNumber() {
        if (valueType != MetadataType.NUMBER) {
            throw new IllegalStateException(
                    "Cannot convert to number: value type is " + valueType
            );
        }

        try {
            return Double.parseDouble(metadataValue);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Cannot parse value as number: " + metadataValue, e
            );
        }
    }

    /**
     * 메타데이터 값을 불리언으로 변환하여 반환합니다.
     *
     * @return 불리언 값
     * @throws IllegalStateException 값이 BOOLEAN 타입이 아닌 경우
     */
    public Boolean asBoolean() {
        if (valueType != MetadataType.BOOLEAN) {
            throw new IllegalStateException(
                    "Cannot convert to boolean: value type is " + valueType
            );
        }

        return Boolean.parseBoolean(metadataValue);
    }

    /**
     * 메타데이터 값이 특정 키와 일치하는지 확인합니다.
     *
     * @param key 비교할 키
     * @return 키가 일치하면 true
     */
    public boolean hasKey(String key) {
        return this.metadataKey.equals(key);
    }

    /**
     * 메타데이터 값이 특정 타입인지 확인합니다.
     *
     * @param type 비교할 타입
     * @return 타입이 일치하면 true
     */
    public boolean isType(MetadataType type) {
        return this.valueType == type;
    }

    // ========== Validation Methods ==========

    private static void validateFileId(FileId fileId) {
        if (fileId == null) {
            throw new IllegalArgumentException("FileId cannot be null");
        }
    }

    private static void validateMetadataKey(String metadataKey) {
        if (metadataKey == null || metadataKey.trim().isEmpty()) {
            throw new IllegalArgumentException("MetadataKey cannot be null or empty");
        }
        if (metadataKey.length() > 100) {
            throw new IllegalArgumentException("MetadataKey cannot exceed 100 characters");
        }
    }

    private static void validateMetadataValue(String metadataValue) {
        if (metadataValue == null) {
            throw new IllegalArgumentException("MetadataValue cannot be null");
        }
    }

    private static void validateValueType(MetadataType valueType) {
        if (valueType == null) {
            throw new IllegalArgumentException("ValueType cannot be null");
        }
    }

    private static void validateTypeCompatibility(String value, MetadataType type) {
        if (!type.isCompatible(value)) {
            throw new IllegalArgumentException(
                    String.format("Value '%s' is not compatible with type %s", value, type)
            );
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt cannot be null");
        }
    }

    private static void validateUpdatedAt(LocalDateTime updatedAt) {
        if (updatedAt == null) {
            throw new IllegalArgumentException("UpdatedAt cannot be null");
        }
    }

    // ========== Getters ==========

    public FileId getFileId() {
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

    // ========== Override Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileMetadata that = (FileMetadata) o;
        return Objects.equals(fileId, that.fileId)
                && Objects.equals(metadataKey, that.metadataKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, metadataKey);
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "fileId=" + fileId +
                ", metadataKey='" + metadataKey + '\'' +
                ", metadataValue='" + metadataValue + '\'' +
                ", valueType=" + valueType +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
