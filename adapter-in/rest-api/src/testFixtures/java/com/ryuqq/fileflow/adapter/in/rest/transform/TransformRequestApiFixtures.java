package com.ryuqq.fileflow.adapter.in.rest.transform;

import com.ryuqq.fileflow.adapter.in.rest.transform.dto.command.CreateTransformRequestApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.transform.dto.response.TransformRequestApiResponse;
import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;
import java.time.Instant;

/**
 * TransformRequest API 테스트 Fixtures.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public final class TransformRequestApiFixtures {

    private TransformRequestApiFixtures() {}

    // ===== 공통 상수 =====
    public static final String TRANSFORM_REQUEST_ID = "tr_test_abc123";
    public static final String SOURCE_ASSET_ID = "asset_abc123";
    public static final String SOURCE_CONTENT_TYPE = "image/jpeg";
    public static final String TRANSFORM_TYPE = "RESIZE";
    public static final Integer WIDTH = 800;
    public static final Integer HEIGHT = 600;
    public static final Integer QUALITY = 85;
    public static final String TARGET_FORMAT = "webp";
    public static final String STATUS_PENDING = "PENDING";
    public static final String RESULT_ASSET_ID = null;
    public static final String LAST_ERROR = null;
    public static final Instant CREATED_AT = Instant.parse("2026-02-09T09:30:00Z");
    public static final Instant COMPLETED_AT = null;

    // ===== Request Fixtures =====

    public static CreateTransformRequestApiRequest createTransformRequestRequest() {
        return new CreateTransformRequestApiRequest(
                SOURCE_ASSET_ID, TRANSFORM_TYPE, WIDTH, HEIGHT, QUALITY, TARGET_FORMAT);
    }

    // ===== Application Response Fixtures =====

    public static TransformRequestResponse transformRequestResponse() {
        return new TransformRequestResponse(
                TRANSFORM_REQUEST_ID,
                SOURCE_ASSET_ID,
                SOURCE_CONTENT_TYPE,
                TRANSFORM_TYPE,
                WIDTH,
                HEIGHT,
                QUALITY,
                TARGET_FORMAT,
                STATUS_PENDING,
                RESULT_ASSET_ID,
                LAST_ERROR,
                CREATED_AT,
                COMPLETED_AT);
    }

    // ===== API Response Fixtures =====

    public static TransformRequestApiResponse transformRequestApiResponse() {
        return new TransformRequestApiResponse(
                TRANSFORM_REQUEST_ID,
                SOURCE_ASSET_ID,
                SOURCE_CONTENT_TYPE,
                TRANSFORM_TYPE,
                WIDTH,
                HEIGHT,
                QUALITY,
                TARGET_FORMAT,
                STATUS_PENDING,
                RESULT_ASSET_ID,
                LAST_ERROR,
                "2026-02-09T18:30:00+09:00",
                null);
    }
}
