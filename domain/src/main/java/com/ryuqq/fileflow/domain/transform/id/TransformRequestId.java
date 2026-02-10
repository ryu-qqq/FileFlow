package com.ryuqq.fileflow.domain.transform.id;

import java.util.Objects;

/**
 * TransformRequest 식별자.
 *
 * @param value UUID v7 문자열 (Application Layer에서 생성)
 */
public record TransformRequestId(String value) {

    public TransformRequestId {
        Objects.requireNonNull(value, "TransformRequestId must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("TransformRequestId must not be blank");
        }
    }

    public static TransformRequestId of(String value) {
        return new TransformRequestId(value);
    }
}
