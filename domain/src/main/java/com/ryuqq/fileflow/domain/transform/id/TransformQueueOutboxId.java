package com.ryuqq.fileflow.domain.transform.id;

public record TransformQueueOutboxId(String value) {

    public static TransformQueueOutboxId of(String value) {
        return new TransformQueueOutboxId(value);
    }
}
