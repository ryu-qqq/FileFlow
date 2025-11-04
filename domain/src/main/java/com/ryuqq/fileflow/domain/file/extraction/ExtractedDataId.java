package com.ryuqq.fileflow.domain.file.extraction;

/**
 * ExtractedData ID
 * 추출된 데이터의 고유 식별자 (Value Object)
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>Java Record: 불변성 보장</li>
 *   <li>Compact Constructor: 컴팩트 생성자에서 검증</li>
 *   <li>명시적 네이밍: value() 메서드로 Long 값 접근</li>
 *   <li>Zero External Dependencies: Pure Java 구현</li>
 * </ul>
 *
 * @param value ExtractedData ID (양수)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record ExtractedDataId(Long value) {

    /**
     * Compact Constructor
     * Null 및 음수 검증
     *
     * @throws IllegalArgumentException value가 null 또는 음수인 경우
     */
    public ExtractedDataId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ExtractedDataId must be positive");
        }
    }

    /**
     * 정적 팩토리 메서드
     *
     * @param value ExtractedData ID
     * @return ExtractedDataId
     */
    public static ExtractedDataId of(Long value) {
        return new ExtractedDataId(value);
    }

}
