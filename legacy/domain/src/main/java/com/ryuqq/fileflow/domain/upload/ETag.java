package com.ryuqq.fileflow.domain.upload;

/**
 * ETag Value Object
 * S3에서 반환하는 파일의 고유 식별자 (Entity Tag)
 *
 * <p>ETag는 파일의 MD5 해시값 또는 고유 식별자로, 파일 무결성 검증에 사용됩니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>ETag는 필수 값입니다 (S3에서 반환)</li>
 *   <li>빈 문자열이나 공백만 있는 값은 허용되지 않습니다</li>
 *   <li>일반적으로 32자 hex string (MD5) 또는 Multipart ETag 형식</li>
 * </ul>
 *
 * @param value ETag 값 (S3에서 반환된 원본 값)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record ETag(String value) {

    /**
     * Compact 생성자 - 유효성 검증
     *
     * @throws IllegalArgumentException ETag가 null이거나 빈 문자열인 경우
     */
    public ETag {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ETag는 필수입니다");
        }
        // 공백 제거 (S3 응답에서 trailing/leading 공백 가능성)
        value = value.trim();
    }

    /**
     * Static Factory Method
     *
     * @param value ETag 값
     * @return ETag 인스턴스
     * @throws IllegalArgumentException ETag가 null이거나 빈 문자열인 경우
     */
    public static ETag of(String value) {
        return new ETag(value);
    }

    /**
     * MD5 ETag 여부 확인
     * 일반적인 S3 Single Part Upload는 32자 hex string
     *
     * @return MD5 형식이면 true
     */
    public boolean isMd5() {
        return value.matches("^[a-fA-F0-9]{32}$");
    }

    /**
     * Multipart ETag 여부 확인
     * Multipart Upload는 "{hex}-{partCount}" 형식
     * 예: "3858f62230ac3c915f300c664312c11f-9"
     *
     * @return Multipart ETag 형식이면 true
     */
    public boolean isMultipart() {
        return value.matches("^[a-fA-F0-9]+-\\d+$");
    }

    /**
     * ETag를 따옴표로 감싸서 반환
     * HTTP ETag 헤더 형식: "etag-value"
     *
     * @return 따옴표로 감싼 ETag
     */
    public String toQuoted() {
        return "\"" + value + "\"";
    }
}
