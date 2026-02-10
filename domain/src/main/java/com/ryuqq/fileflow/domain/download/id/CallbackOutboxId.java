package com.ryuqq.fileflow.domain.download.id;

/** CallbackOutbox 식별자. String 타입 - Application Factory에서 UUID v7으로 생성하여 주입. */
public record CallbackOutboxId(String value) {

    public static CallbackOutboxId of(String value) {
        return new CallbackOutboxId(value);
    }
}
