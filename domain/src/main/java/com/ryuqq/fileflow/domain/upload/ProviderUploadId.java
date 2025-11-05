package com.ryuqq.fileflow.domain.upload;

/**
 * ProviderUploadId Value Object
 * 클라우드 제공자(S3)의 Multipart Upload ID를 나타내는 값 객체
 *
 * <p>Provider Upload ID는 S3에서 Multipart Upload 세션을 식별하는 고유 ID입니다.
 * S3의 InitiateMultipartUpload API에서 반환되며, 모든 Part 업로드 및 완료 요청에 필요합니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>Provider Upload ID는 필수 값입니다 (S3에서 반환)</li>
 *   <li>빈 문자열이나 공백만 있는 값은 허용되지 않습니다</li>
 *   <li>S3 Upload ID는 일반적으로 긴 문자열 형식입니다</li>
 * </ul>
 *
 * @param value Provider Upload ID (S3 Upload ID)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record ProviderUploadId(String value) {

    /**
     * Compact 생성자 - 유효성 검증
     *
     * @throws IllegalArgumentException Upload ID가 null이거나 빈 문자열인 경우
     */
    public ProviderUploadId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Provider Upload ID는 필수입니다");
        }
        // 공백 제거 (S3 응답에서 trailing/leading 공백 가능성)
        value = value.trim();
    }

    /**
     * Static Factory Method
     *
     * @param value Provider Upload ID
     * @return ProviderUploadId 인스턴스
     * @throws IllegalArgumentException Upload ID가 null이거나 빈 문자열인 경우
     */
    public static ProviderUploadId of(String value) {
        return new ProviderUploadId(value);
    }

    /**
     * 다른 Provider Upload ID와 동일한지 비교
     *
     * @param other 비교할 Provider Upload ID
     * @return 동일하면 true
     */
    public boolean matches(ProviderUploadId other) {
        if (other == null) {
            return false;
        }
        return this.value.equals(other.value);
    }

    /**
     * 문자열 값과 직접 비교
     * S3 응답과 비교할 때 사용
     *
     * @param uploadIdValue 비교할 Upload ID 값
     * @return 동일하면 true
     */
    public boolean matches(String uploadIdValue) {
        if (uploadIdValue == null || uploadIdValue.isBlank()) {
            return false;
        }
        return this.value.equals(uploadIdValue.trim());
    }
}
