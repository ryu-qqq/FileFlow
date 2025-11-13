package com.ryuqq.fileflow.domain.upload;

/**
 * Checksum Value Object
 * 파일 Part의 무결성 검증을 위한 체크섬 값 객체
 *
 * <p>체크섬은 파일의 무결성을 검증하기 위한 해시값입니다.
 * S3에서는 SHA256 알고리즘을 사용하며, Base64로 인코딩됩니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>체크섬은 필수 값입니다</li>
 *   <li>빈 문자열이나 공백만 있는 값은 허용되지 않습니다</li>
 *   <li>Base64 인코딩된 SHA256 해시값 형식이어야 합니다</li>
 * </ul>
 *
 * @param value 체크섬 값 (Base64 인코딩된 SHA256 해시)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record Checksum(String value) {

    /**
     * Compact 생성자 - 유효성 검증
     *
     * @throws IllegalArgumentException 체크섬이 null이거나 빈 문자열인 경우
     */
    public Checksum {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("체크섬은 필수입니다");
        }
        // 공백 제거
        value = value.trim();
    }

    /**
     * Static Factory Method
     *
     * @param value 체크섬 값
     * @return Checksum 인스턴스
     * @throws IllegalArgumentException 체크섬이 null이거나 빈 문자열인 경우
     */
    public static Checksum of(String value) {
        return new Checksum(value);
    }

    /**
     * Base64 인코딩된 SHA256 해시 형식인지 확인
     * SHA256 해시는 32바이트이므로 Base64 인코딩 시 44자 (패딩 포함)
     *
     * @return Base64 인코딩된 SHA256 형식이면 true
     */
    public boolean isSha256() {
        // Base64 인코딩된 SHA256 (32바이트 = 44자)
        // Base64 문자셋: A-Z, a-z, 0-9, +, /, = (패딩)
        return value.matches("^[A-Za-z0-9+/]{43}=$");
    }

    /**
     * 다른 체크섬과 동일한지 비교
     * 파일 무결성 검증에 사용
     *
     * @param other 비교할 체크섬
     * @return 동일하면 true
     */
    public boolean matches(Checksum other) {
        if (other == null) {
            return false;
        }
        return this.value.equals(other.value);
    }

    /**
     * 문자열 체크섬과 동일한지 비교
     * 외부 시스템과의 검증에 사용
     *
     * @param checksumValue 비교할 체크섬 문자열
     * @return 동일하면 true
     */
    public boolean matches(String checksumValue) {
        if (checksumValue == null) {
            return false;
        }
        return this.value.equals(checksumValue.trim());
    }
}
