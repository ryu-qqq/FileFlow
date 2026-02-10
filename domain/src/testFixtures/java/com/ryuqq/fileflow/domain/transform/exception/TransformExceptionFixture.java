package com.ryuqq.fileflow.domain.transform.exception;

import java.util.Map;

public class TransformExceptionFixture {

    public static TransformException aTransformNotFound() {
        return new TransformException(TransformErrorCode.TRANSFORM_NOT_FOUND);
    }

    public static TransformException aNotImageFile() {
        return new TransformException(TransformErrorCode.NOT_IMAGE_FILE);
    }

    public static TransformException anInvalidParams() {
        return new TransformException(TransformErrorCode.INVALID_TRANSFORM_PARAMS);
    }

    public static TransformException anInvalidStatus() {
        return new TransformException(TransformErrorCode.INVALID_TRANSFORM_STATUS);
    }

    public static TransformException aTransformNotFoundWithDetail(String transformId) {
        return new TransformException(
                TransformErrorCode.TRANSFORM_NOT_FOUND,
                "Transform not found: " + transformId,
                Map.of("transformId", transformId));
    }
}
