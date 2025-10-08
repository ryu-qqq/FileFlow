package com.ryuqq.fileflow.domain.upload.vo;

import java.util.Objects;
import java.util.Set;

/**
 * 파일 무결성 검증을 위한 체크섬 Value Object
 *
 * 불변성:
 * - record 타입으로 모든 필드는 final이며 생성 후 변경 불가
 * - 해시 알고리즘과 해시 값을 함께 저장하여 검증 가능
 *
 * 지원 알고리즘:
 * - SHA-256 (기본 권장)
 * - SHA-512
 * - MD5 (레거시 지원, 보안상 권장하지 않음)
 */
public record CheckSum(String value, String algorithm) {

    private static final Set<String> SUPPORTED_ALGORITHMS = Set.of(
            "SHA-256",
            "SHA-512",
            "MD5"
    );

    private static final int SHA256_LENGTH = 64;  // 32 bytes * 2 (hex)
    private static final int SHA512_LENGTH = 128; // 64 bytes * 2 (hex)
    private static final int MD5_LENGTH = 32;     // 16 bytes * 2 (hex)

    /**
     * Compact constructor로 검증 로직 수행
     */
    public CheckSum {
        validateValue(value);
        validateAlgorithm(algorithm);
        validateValueFormat(value, algorithm);
    }

    /**
     * SHA-256 해시 값으로 CheckSum을 생성합니다.
     *
     * @param value SHA-256 해시 값 (64자 16진수 문자열)
     * @return CheckSum 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 해시 값인 경우
     */
    public static CheckSum sha256(String value) {
        return new CheckSum(value, "SHA-256");
    }

    /**
     * SHA-512 해시 값으로 CheckSum을 생성합니다.
     *
     * @param value SHA-512 해시 값 (128자 16진수 문자열)
     * @return CheckSum 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 해시 값인 경우
     */
    public static CheckSum sha512(String value) {
        return new CheckSum(value, "SHA-512");
    }

    /**
     * MD5 해시 값으로 CheckSum을 생성합니다.
     *
     * 주의: MD5는 보안상 권장하지 않으며, 레거시 시스템 호환성을 위해서만 제공됩니다.
     *
     * @param value MD5 해시 값 (32자 16진수 문자열)
     * @return CheckSum 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 해시 값인 경우
     */
    public static CheckSum md5(String value) {
        return new CheckSum(value, "MD5");
    }

    /**
     * 다른 CheckSum과 일치하는지 확인합니다.
     *
     * @param other 비교할 CheckSum
     * @return 일치 여부
     */
    public boolean matches(CheckSum other) {
        if (other == null) {
            return false;
        }
        return this.algorithm.equals(other.algorithm) &&
               this.value.equalsIgnoreCase(other.value);
    }

    // ========== Validation Methods ==========

    private static void validateValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("CheckSum value cannot be null or empty");
        }
    }

    private static void validateAlgorithm(String algorithm) {
        if (algorithm == null || algorithm.trim().isEmpty()) {
            throw new IllegalArgumentException("Algorithm cannot be null or empty");
        }
        if (!SUPPORTED_ALGORITHMS.contains(algorithm)) {
            throw new IllegalArgumentException(
                    "Unsupported algorithm: " + algorithm +
                    ". Supported algorithms: " + SUPPORTED_ALGORITHMS
            );
        }
    }

    private static void validateValueFormat(String value, String algorithm) {
        String trimmedValue = value.trim();

        // 16진수 문자만 포함하는지 검증
        if (!trimmedValue.matches("^[0-9a-fA-F]+$")) {
            throw new IllegalArgumentException(
                    "CheckSum value must contain only hexadecimal characters (0-9, a-f, A-F)"
            );
        }

        // 알고리즘별 길이 검증
        int expectedLength = switch (algorithm) {
            case "SHA-256" -> SHA256_LENGTH;
            case "SHA-512" -> SHA512_LENGTH;
            case "MD5" -> MD5_LENGTH;
            default -> throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        };

        if (trimmedValue.length() != expectedLength) {
            throw new IllegalArgumentException(
                    "Invalid " + algorithm + " hash length. Expected: " + expectedLength +
                    ", but got: " + trimmedValue.length()
            );
        }
    }

    // ========== Override Methods ==========

    /**
     * 해시 값을 소문자로 정규화하여 반환합니다.
     *
     * @return 소문자 해시 값
     */
    public String normalizedValue() {
        return value.toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckSum checkSum = (CheckSum) o;
        return algorithm.equals(checkSum.algorithm) &&
               value.equalsIgnoreCase(checkSum.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithm, value.toLowerCase());
    }

    @Override
    public String toString() {
        return "CheckSum{" +
                "algorithm='" + algorithm + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
