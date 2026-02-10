package com.ryuqq.fileflow.application.transform;

import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;
import java.time.Instant;

/**
 * Transform Application Response 테스트 Fixtures.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public final class TransformResponseFixtures {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private TransformResponseFixtures() {}

    // ===== TransformRequestResponse Fixtures =====

    public static TransformRequestResponse resizeResponse() {
        return resizeResponse("transform-001");
    }

    public static TransformRequestResponse resizeResponse(String transformRequestId) {
        return new TransformRequestResponse(
                transformRequestId,
                "asset-001",
                "image/jpeg",
                "RESIZE",
                800,
                600,
                null,
                null,
                "QUEUED",
                null,
                null,
                NOW,
                null);
    }

    public static TransformRequestResponse convertResponse(String transformRequestId) {
        return new TransformRequestResponse(
                transformRequestId,
                "asset-001",
                "image/jpeg",
                "CONVERT",
                null,
                null,
                null,
                "webp",
                "QUEUED",
                null,
                null,
                NOW,
                null);
    }

    public static TransformRequestResponse completedResponse(String transformRequestId) {
        return new TransformRequestResponse(
                transformRequestId,
                "asset-001",
                "image/jpeg",
                "RESIZE",
                800,
                600,
                null,
                null,
                "COMPLETED",
                "result-001",
                null,
                NOW,
                NOW.plusSeconds(30));
    }
}
