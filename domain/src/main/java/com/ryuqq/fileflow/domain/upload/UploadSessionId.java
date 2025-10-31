package com.ryuqq.fileflow.domain.upload;

/**
 * UploadSessionId Value Object
 * 업로드 세션의 고유 식별자를 나타내는 값 객체
 *
 * <p>업로드 세션 ID는 업로드 프로세스 전체를 추적하는 핵심 식별자입니다.
 * Multipart Upload, External Download 등 다양한 도메인에서 참조됩니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>업로드 세션 ID는 필수 값입니다</li>
 *   <li>양수여야 합니다 (0 제외)</li>
 *   <li>다른 ID 타입과 혼동되지 않도록 Type Safety를 보장합니다</li>
 * </ul>
 *
 * @param value 업로드 세션 ID (양수)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record UploadSessionId(Long value) {

    /**
     * Compact 생성자 - 유효성 검증
     *
     * @throws IllegalArgumentException ID가 null이거나 0 이하인 경우
     */
    public UploadSessionId {
        if (value == null) {
            throw new IllegalArgumentException("Upload Session ID는 필수입니다");
        }
        if (value <= 0) {
            throw new IllegalArgumentException(
                    String.format("Upload Session ID는 양수여야 합니다: %d", value)
            );
        }
    }

    /**
     * Static Factory Method
     *
     * @param value 업로드 세션 ID
     * @return UploadSessionId 인스턴스
     * @throws IllegalArgumentException ID가 유효하지 않은 경우
     */
    public static UploadSessionId of(Long value) {
        return new UploadSessionId(value);
    }

    /**
     * 다른 Upload Session ID와 동일한지 비교
     *
     * @param other 비교할 Upload Session ID
     * @return 동일하면 true
     */
    public boolean matches(UploadSessionId other) {
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
