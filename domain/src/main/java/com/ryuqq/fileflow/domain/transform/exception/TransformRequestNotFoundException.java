package com.ryuqq.fileflow.domain.transform.exception;

import java.util.Map;

public class TransformRequestNotFoundException extends TransformException {

    public TransformRequestNotFoundException(String transformRequestId) {
        super(
                TransformErrorCode.TRANSFORM_NOT_FOUND,
                "변환 요청을 찾을 수 없습니다. transformRequestId: " + transformRequestId,
                Map.of(
                        "transformRequestId",
                        transformRequestId != null ? transformRequestId : "null"));
    }
}
