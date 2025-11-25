package com.ryuqq.fileflow.domain.session.vo;

import java.util.regex.Pattern;

/**
 * 체크섬 (무결성 검증) Value Object.
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>체크섬은 null이거나 빈 문자열일 수 없다.
 *   <li>MD5 또는 SHA256 형식이어야 한다.
 *   <li>MD5: 32자 16진수
 *   <li>SHA256: 64자 16진수
 * </ul>
 *
 * @param key 체크섬 값 (MD5 또는 SHA256 해시)
 */
public record CheckSum(String key) {

    // MD5: 32자 16진수
    private static final Pattern MD5_PATTERN = Pattern.compile("^[a-fA-F0-9]{32}$");

    // SHA256: 64자 16진수
    private static final Pattern SHA256_PATTERN = Pattern.compile("^[a-fA-F0-9]{64}$");

    /** Compact Constructor (검증 로직). */
    public CheckSum {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("체크섬은 null이거나 빈 문자열일 수 없습니다.");
        }

        if (!isMD5Format(key) && !isSHA256Format(key)) {
            throw new IllegalArgumentException(
                    String.format(
                            "체크섬은 MD5(32자) 또는 SHA256(64자) 형식이어야 합니다: %s (%d자)", key, key.length()));
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param key 체크섬 값 (MD5 또는 SHA256)
     * @return CheckSum
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static CheckSum of(String key) {
        return new CheckSum(key);
    }

    /**
     * MD5 형식인지 확인한다.
     *
     * @return MD5 형식이면 true
     */
    public boolean isMD5() {
        return isMD5Format(key);
    }

    /**
     * SHA256 형식인지 확인한다.
     *
     * @return SHA256 형식이면 true
     */
    public boolean isSHA256() {
        return isSHA256Format(key);
    }

    /**
     * 알고리즘 타입을 반환한다.
     *
     * @return "MD5" 또는 "SHA256"
     */
    public String getAlgorithm() {
        return isMD5() ? "MD5" : "SHA256";
    }

    /**
     * 다른 체크섬과 일치하는지 확인한다 (대소문자 무시).
     *
     * @param other 비교할 체크섬
     * @return 일치하면 true
     */
    public boolean matches(CheckSum other) {
        if (other == null) {
            return false;
        }
        return key.equalsIgnoreCase(other.key);
    }

    /**
     * 문자열 체크섬과 일치하는지 확인한다 (대소문자 무시).
     *
     * @param otherKey 비교할 체크섬 문자열
     * @return 일치하면 true
     */
    public boolean matchesKey(String otherKey) {
        if (otherKey == null || otherKey.isBlank()) {
            return false;
        }
        return key.equalsIgnoreCase(otherKey);
    }

    /**
     * MD5 형식 검증 (private).
     *
     * @param value 검증할 문자열
     * @return MD5 형식이면 true
     */
    private static boolean isMD5Format(String value) {
        return MD5_PATTERN.matcher(value).matches();
    }

    /**
     * SHA256 형식 검증 (private).
     *
     * @param value 검증할 문자열
     * @return SHA256 형식이면 true
     */
    private static boolean isSHA256Format(String value) {
        return SHA256_PATTERN.matcher(value).matches();
    }
}
