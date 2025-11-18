package com.ryuqq.fileflow.domain.file.vo;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * ExternalUrl Value Object
 * <p>
 * 외부 URL을 검증하고 캡슐화합니다.
 * </p>
 *
 * <p>
 * 검증 규칙:
 * - HTTPS 프로토콜 필수
 * - 유효한 URL 형식
 * - Null/Empty 불가
 * </p>
 */
public record ExternalUrl(String value) {

    /**
     * HTTPS 프로토콜 접두사
     */
    private static final String HTTPS_PROTOCOL = "https";

    /**
     * Compact Constructor (Record 검증 패턴)
     */
    public ExternalUrl {
        validateNotNullOrEmpty(value);
        validateUrlFormat(value);
        validateHttpsProtocol(value);
    }

    /**
     * 정적 팩토리 메서드
     *
     * @param value 외부 URL
     * @return ExternalUrl VO
     */
    public static ExternalUrl of(String value) {
        return new ExternalUrl(value);
    }

    /**
     * Null 또는 Empty 검증
     */
    private static void validateNotNullOrEmpty(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("외부 URL은 null이거나 빈 값일 수 없습니다");
        }
    }

    /**
     * URL 형식 검증
     */
    private static void validateUrlFormat(String value) {
        try {
            new URL(value);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("유효하지 않은 URL 형식입니다: " + value, e);
        }
    }

    /**
     * HTTPS 프로토콜 검증
     */
    private static void validateHttpsProtocol(String value) {
        try {
            URL url = new URL(value);
            if (!HTTPS_PROTOCOL.equalsIgnoreCase(url.getProtocol())) {
                throw new IllegalArgumentException(
                        String.format("외부 URL은 HTTPS 프로토콜만 허용됩니다 (현재: %s)", url.getProtocol())
                );
            }
        } catch (MalformedURLException e) {
            // validateUrlFormat에서 이미 검증했으므로 여기서는 발생하지 않음
            throw new IllegalArgumentException("유효하지 않은 URL 형식입니다: " + value, e);
        }
    }
}
