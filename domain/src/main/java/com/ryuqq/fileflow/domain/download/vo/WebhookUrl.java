package com.ryuqq.fileflow.domain.download.vo;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Webhook 콜백 URL Value Object.
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>HTTP 또는 HTTPS 프로토콜만 허용
 *   <li>null이거나 빈 문자열 불가
 *   <li>선택적 필드이므로 nullable로 사용 가능
 * </ul>
 *
 * @param value URL 문자열
 */
public record WebhookUrl(String value) {

    private static final Pattern HTTP_HTTPS_PATTERN =
            Pattern.compile("^https?://.*", Pattern.CASE_INSENSITIVE);

    /** Compact Constructor (검증 로직). */
    public WebhookUrl {
        Objects.requireNonNull(value, "WebhookUrl must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("WebhookUrl은 비어있을 수 없습니다.");
        }
        if (!HTTP_HTTPS_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "WebhookUrl은 http:// 또는 https://로 시작해야 합니다: " + value);
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param value URL 문자열
     * @return WebhookUrl
     * @throws NullPointerException value가 null인 경우
     * @throws IllegalArgumentException 유효하지 않은 URL인 경우
     */
    public static WebhookUrl of(String value) {
        return new WebhookUrl(value);
    }
}
