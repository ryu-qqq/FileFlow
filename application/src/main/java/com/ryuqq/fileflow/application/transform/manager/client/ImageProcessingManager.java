package com.ryuqq.fileflow.application.transform.manager.client;

import com.ryuqq.fileflow.application.transform.dto.result.ImageProcessingResult;
import com.ryuqq.fileflow.application.transform.port.out.client.ImageTransformClient;
import com.ryuqq.fileflow.domain.transform.vo.TransformParams;
import com.ryuqq.fileflow.domain.transform.vo.TransformType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ImageProcessingManager {

    private static final Logger log = LoggerFactory.getLogger(ImageProcessingManager.class);

    private final ImageTransformClient imageTransformClient;

    public ImageProcessingManager(ImageTransformClient imageTransformClient) {
        this.imageTransformClient = imageTransformClient;
    }

    public ImageProcessingResult process(
            byte[] sourceImageBytes, TransformType type, TransformParams params) {
        log.info("이미지 처리 시작: type={}, inputSize={}", type, sourceImageBytes.length);
        ImageProcessingResult result = imageTransformClient.process(sourceImageBytes, type, params);
        log.info(
                "이미지 처리 완료: type={}, outputSize={}, {}x{}",
                type,
                result.fileSize(),
                result.width(),
                result.height());
        return result;
    }
}
