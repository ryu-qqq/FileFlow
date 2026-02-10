package com.ryuqq.fileflow.domain.transform.id;

public class TransformRequestIdFixture {

    public static TransformRequestId aTransformRequestId() {
        return TransformRequestId.of("transform-001");
    }

    public static TransformRequestId aTransformRequestId(String value) {
        return TransformRequestId.of(value);
    }
}
