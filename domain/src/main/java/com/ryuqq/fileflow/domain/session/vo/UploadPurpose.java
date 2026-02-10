package com.ryuqq.fileflow.domain.session.vo;

import java.util.Objects;

/**
 * 업로드 용도 Value Object.
 *
 * <p>파일의 비즈니스 용도를 나타냅니다. (예: "product-image", "user-avatar", "document")
 *
 * @param value 용도 문자열
 */
public record UploadPurpose(String value) {

    private static final int MAX_LENGTH = 100;

    public UploadPurpose {
        Objects.requireNonNull(value, "purpose must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("purpose must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "purpose must not exceed " + MAX_LENGTH + " characters");
        }
    }

    public static UploadPurpose of(String value) {
        return new UploadPurpose(value);
    }
}
