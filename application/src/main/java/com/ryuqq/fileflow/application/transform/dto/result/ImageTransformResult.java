package com.ryuqq.fileflow.application.transform.dto.result;

import com.ryuqq.fileflow.domain.asset.vo.FileInfo;
import com.ryuqq.fileflow.domain.transform.vo.ImageDimension;

public record ImageTransformResult(
        boolean success,
        String s3Key,
        String bucket,
        FileInfo fileInfo,
        ImageDimension dimension,
        String errorMessage) {

    public static ImageTransformResult success(
            String s3Key, String bucket, FileInfo fileInfo, ImageDimension dimension) {
        return new ImageTransformResult(true, s3Key, bucket, fileInfo, dimension, null);
    }

    public static ImageTransformResult failure(String errorMessage) {
        return new ImageTransformResult(false, null, null, null, null, errorMessage);
    }
}
