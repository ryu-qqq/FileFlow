package com.ryuqq.fileflow.domain.vo;

import java.util.regex.Pattern;

/**
 * Checksum Value Object
 * <p>
 * 파일 체크섬을 검증하고 캡슐화합니다.
 * </p>
 *
 * <p>
 * 지원 알고리즘:
 * - SHA-256: 64자 16진수 문자열
 * - MD5: 32자 16진수 문자열
 * </p>
 */
public record Checksum(String algorithm, String value) {

    /**
     * SHA-256 알고리즘 이름
     */
    private static final String SHA256_ALGORITHM = "SHA-256";

    /**
     * MD5 알고리즘 이름
     */
    private static final String MD5_ALGORITHM = "MD5";

    /**
     * SHA-256 체크섬 길이 (64자)
     */
    private static final int SHA256_LENGTH = 64;

    /**
     * MD5 체크섬 길이 (32자)
     */
    private static final int MD5_LENGTH = 32;

    /**
     * 16진수 문자열 패턴 (소문자/대문자 모두 허용)
     */
    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$");

    /**
     * 알고리즘과 값으로 Checksum 생성 (표준 of 패턴)
     *
     * @param algorithm 알고리즘 이름 (SHA-256, MD5)
     * @param value 체크섬 값
     * @return Checksum VO
     */
    public static Checksum of(String algorithm, String value) {
        if (SHA256_ALGORITHM.equals(algorithm)) {
            return sha256(value);
        } else if (MD5_ALGORITHM.equals(algorithm)) {
            return md5(value);
        } else {
            throw new IllegalArgumentException("지원하지 않는 알고리즘입니다: " + algorithm);
        }
    }

    /**
     * SHA-256 체크섬 생성 팩토리 메서드
     *
     * @param value SHA-256 체크섬 값 (64자 16진수)
     * @return Checksum VO
     */
    public static Checksum sha256(String value) {
        validateHexString(value);
        validateLength(value, SHA256_LENGTH, SHA256_ALGORITHM);
        return new Checksum(SHA256_ALGORITHM, value);
    }

    /**
     * MD5 체크섬 생성 팩토리 메서드
     *
     * @param value MD5 체크섬 값 (32자 16진수)
     * @return Checksum VO
     */
    public static Checksum md5(String value) {
        validateHexString(value);
        validateLength(value, MD5_LENGTH, MD5_ALGORITHM);
        return new Checksum(MD5_ALGORITHM, value);
    }

    /**
     * 16진수 문자열 검증
     */
    private static void validateHexString(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("체크섬 값은 null이거나 빈 값일 수 없습니다");
        }
        if (!HEX_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("체크섬은 16진수 문자열이어야 합니다: " + value);
        }
    }

    /**
     * 길이 검증
     */
    private static void validateLength(String value, int expectedLength, String algorithm) {
        if (value.length() != expectedLength) {
            throw new IllegalArgumentException(
                    String.format("%s 체크섬은 %d자의 16진수 문자열이어야 합니다 (현재: %d자)",
                            algorithm, expectedLength, value.length())
            );
        }
    }
}
