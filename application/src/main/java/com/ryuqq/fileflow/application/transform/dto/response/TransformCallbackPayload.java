package com.ryuqq.fileflow.application.transform.dto.response;

public record TransformCallbackPayload(
        String transformRequestId,
        String status,
        String sourceAssetId,
        String resultAssetId,
        String transformType,
        Integer width,
        Integer height,
        Integer quality,
        String targetFormat,
        String errorMessage) {

    public static TransformCallbackPayload ofCompleted(
            String transformRequestId,
            String sourceAssetId,
            String resultAssetId,
            String transformType,
            Integer width,
            Integer height,
            Integer quality,
            String targetFormat) {
        return new TransformCallbackPayload(
                transformRequestId,
                "COMPLETED",
                sourceAssetId,
                resultAssetId,
                transformType,
                width,
                height,
                quality,
                targetFormat,
                null);
    }

    public static TransformCallbackPayload ofFailed(
            String transformRequestId, String sourceAssetId, String errorMessage) {
        return new TransformCallbackPayload(
                transformRequestId,
                "FAILED",
                sourceAssetId,
                null,
                null,
                null,
                null,
                null,
                null,
                errorMessage);
    }
}
