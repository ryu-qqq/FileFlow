package com.ryuqq.fileflow.application.transform.port.out.client;

import com.ryuqq.fileflow.application.transform.dto.result.ImageProcessingResult;
import com.ryuqq.fileflow.domain.transform.vo.TransformParams;
import com.ryuqq.fileflow.domain.transform.vo.TransformType;

public interface ImageTransformClient {

    ImageProcessingResult process(
            byte[] sourceImageBytes, TransformType type, TransformParams params);
}
