package com.ryuqq.fileflow.domain.upload;

/**
 * Multipart Upload ID Value Object
 *
 * <p>Multipart Upload의 식별자를 나타내는 불변 값 객체입니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용</li>
 *   <li>✅ ID는 Value Object로 래핑</li>
 *   <li>✅ 불변성 보장</li>
 * </ul>
 *
 * @param value Multipart Upload ID 원시 값
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record MultipartUploadId(Long value) {

    /**
     * Multipart Upload ID를 생성합니다.
     *
     * @param value Multipart Upload ID 원시 값
     * @throws IllegalArgumentException value가 null이거나 0 이하인 경우
     */
    public MultipartUploadId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Multipart Upload ID는 양수여야 합니다: " + value);
        }
    }

    /**
     * Static Factory Method
     *
     * @param value Multipart Upload ID 원시 값
     * @return MultipartUploadId 인스턴스
     * @throws IllegalArgumentException value가 유효하지 않은 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static MultipartUploadId of(Long value) {
        return new MultipartUploadId(value);
    }
}
