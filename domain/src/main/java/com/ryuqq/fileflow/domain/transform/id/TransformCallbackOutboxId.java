package com.ryuqq.fileflow.domain.transform.id;

/** TransformCallbackOutbox 식별자. String 타입 - Application Factory에서 UUID v7으로 생성하여 주입. */
public record TransformCallbackOutboxId(String value) {

    public static TransformCallbackOutboxId of(String value) {
        return new TransformCallbackOutboxId(value);
    }
}
