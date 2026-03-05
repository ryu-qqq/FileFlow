package com.ryuqq.fileflow.domain.transform.aggregate;

import com.ryuqq.fileflow.domain.asset.id.AssetId;
import com.ryuqq.fileflow.domain.transform.id.TransformRequestId;
import com.ryuqq.fileflow.domain.transform.vo.TransformParams;
import com.ryuqq.fileflow.domain.transform.vo.TransformType;
import java.time.Instant;

public class TransformRequestFixture {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    public static TransformRequest aResizeRequest() {
        return TransformRequest.forNew(
                TransformRequestId.of("transform-001"),
                AssetId.of("asset-001"),
                "image/jpeg",
                TransformType.RESIZE,
                TransformParams.forResize(800, 600, true),
                null,
                NOW);
    }

    public static TransformRequest aConvertRequest() {
        return TransformRequest.forNew(
                TransformRequestId.of("transform-002"),
                AssetId.of("asset-001"),
                "image/jpeg",
                TransformType.CONVERT,
                TransformParams.forConvert("webp"),
                null,
                NOW);
    }

    public static TransformRequest aCompressRequest() {
        return TransformRequest.forNew(
                TransformRequestId.of("transform-003"),
                AssetId.of("asset-001"),
                "image/png",
                TransformType.COMPRESS,
                TransformParams.forCompress(80),
                null,
                NOW);
    }

    public static TransformRequest aThumbnailRequest() {
        return TransformRequest.forNew(
                TransformRequestId.of("transform-004"),
                AssetId.of("asset-001"),
                "image/jpeg",
                TransformType.THUMBNAIL,
                TransformParams.forThumbnail(150, 150),
                null,
                NOW);
    }

    public static TransformRequest aResizeRequestWithCallback() {
        return TransformRequest.forNew(
                TransformRequestId.of("transform-cb-001"),
                AssetId.of("asset-001"),
                "image/jpeg",
                TransformType.RESIZE,
                TransformParams.forResize(800, 600, true),
                "https://callback.example.com/transform-done",
                NOW);
    }

    public static TransformRequest aProcessingRequestWithCallback() {
        TransformRequest request = aResizeRequestWithCallback();
        request.start(NOW.plusSeconds(10));
        return request;
    }

    public static TransformRequest aProcessingRequest() {
        TransformRequest request = aResizeRequest();
        request.start(NOW.plusSeconds(10));
        return request;
    }

    public static TransformRequest aCompletedRequest() {
        TransformRequest request = aProcessingRequest();
        request.complete(AssetId.of("result-001"), 800, 600, NOW.plusSeconds(30));
        return request;
    }

    public static TransformRequest aFailedRequest() {
        TransformRequest request = aProcessingRequest();
        request.fail("Processing error", NOW.plusSeconds(30));
        return request;
    }
}
