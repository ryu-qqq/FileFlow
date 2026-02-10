package com.ryuqq.fileflow.application.transform.port.in.query;

import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;

public interface GetTransformRequestUseCase {

    TransformRequestResponse execute(String transformRequestId);
}
