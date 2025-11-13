package com.ryuqq.fileflow.domain.download;

/**
 * External Download ID Value Object
 *
 * <p>External Download의 식별자를 나타내는 불변 값 객체입니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용</li>
 *   <li>✅ ID는 Value Object로 래핑</li>
 *   <li>✅ 불변성 보장</li>
 * </ul>
 *
 * @param value External Download ID 원시 값
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record ExternalDownloadId(Long value) {

    /**
     * External Download ID를 생성합니다.
     *
     * @param value External Download ID 원시 값
     * @throws IllegalArgumentException value가 null이거나 0 이하인 경우
     */
    public ExternalDownloadId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("External Download ID는 양수여야 합니다: " + value);
        }
    }

    public static ExternalDownloadId of(Long value) {
        return new ExternalDownloadId(value);
    }
}
