package com.ryuqq.fileflow.domain.session.vo;

import java.util.Objects;

/**
 * 업로드 요청 출처 Value Object.
 *
 * <p>파일 업로드를 요청한 서비스명을 나타냅니다. (예: "commerce-service", "admin-service")
 *
 * @param value 서비스명 문자열
 */
public record UploadSource(String value) {

    private static final int MAX_LENGTH = 100;

    public UploadSource {
        Objects.requireNonNull(value, "source must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("source must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "source must not exceed " + MAX_LENGTH + " characters");
        }
    }

    public static UploadSource of(String value) {
        return new UploadSource(value);
    }
}
