package com.ryuqq.fileflow.application.transform.dto.bundle;

import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.vo.ImageDimension;
import java.time.Instant;

public record TransformCompletionBundle(
        Asset resultAsset,
        TransformRequest request,
        ImageDimension dimension,
        Instant completedAt) {}
