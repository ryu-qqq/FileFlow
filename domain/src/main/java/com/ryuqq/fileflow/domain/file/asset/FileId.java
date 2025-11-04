package com.ryuqq.fileflow.domain.file.asset;

/**
 * FileId Value Object
 * 파일 에셋의 고유 식별자를 나타내는 값 객체
 *
 * <p>파일 에셋 ID는 업로드 완료된 파일을 추적하는 핵심 식별자입니다.
 * FileVariant, FileRelationship, FileAccessLog 등 다양한 도메인에서 참조됩니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>파일 에셋 ID는 필수 값입니다</li>
 *   <li>양수여야 합니다 (0 제외)</li>
 *   <li>다른 ID 타입과 혼동되지 않도록 Type Safety를 보장합니다</li>
 * </ul>
 *
 * @param value 파일 에셋 ID (양수)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record FileId(Long value) {

    /**
     * Compact 생성자 - 유효성 검증
     *
     * @throws IllegalArgumentException ID가 null이거나 0 이하인 경우
     */
    public FileId {
        if (value == null) {
            throw new IllegalArgumentException("File Asset ID는 필수입니다");
        }
        if (value <= 0) {
            throw new IllegalArgumentException(
                    String.format("File Asset ID는 양수여야 합니다: %d", value)
            );
        }
    }

    /**
     * Static Factory Method
     *
     * @param value 파일 에셋 ID
     * @return FileId 인스턴스
     * @throws IllegalArgumentException ID가 유효하지 않은 경우
     */
    public static FileId of(Long value) {
        return new FileId(value);
    }

    /**
     * 다른 File Asset ID와 동일한지 비교
     *
     * @param other 비교할 File Asset ID
     * @return 동일하면 true
     */
    public boolean matches(FileId other) {
        if (other == null) {
            return false;
        }
        return this.value.equals(other.value);
    }

    /**
     * Long 값과 직접 비교
     * 외부 시스템과의 통합에 사용
     *
     * @param idValue 비교할 ID 값
     * @return 동일하면 true
     */
    public boolean matches(Long idValue) {
        if (idValue == null) {
            return false;
        }
        return this.value.equals(idValue);
    }
}
