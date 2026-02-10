package com.ryuqq.fileflow.domain.session.vo;

import java.util.Objects;

/**
 * Presigned URL Value Object.
 *
 * <p>S3 Presigned PUT URL을 래핑합니다.
 *
 * @param value Presigned URL 문자열
 */
public record PresignedUrl(String value) {

    public PresignedUrl {
        Objects.requireNonNull(value, "presignedUrl must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("presignedUrl must not be blank");
        }
    }

    public static PresignedUrl of(String value) {
        return new PresignedUrl(value);
    }
}
