package com.ryuqq.fileflow.application.transform.assembler;

import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import org.springframework.stereotype.Component;

@Component
public class TransformAssembler {

    public TransformRequestResponse toResponse(TransformRequest request) {
        return new TransformRequestResponse(
                request.idValue(),
                request.sourceAssetIdValue(),
                request.sourceContentType(),
                request.type().name(),
                request.params().width(),
                request.params().height(),
                request.params().quality(),
                request.params().targetFormat(),
                request.status().name(),
                request.resultAssetIdValue(),
                request.lastError(),
                request.createdAt(),
                request.completedAt());
    }
}
