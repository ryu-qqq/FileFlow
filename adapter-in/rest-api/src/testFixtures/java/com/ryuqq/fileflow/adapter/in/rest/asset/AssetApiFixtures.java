package com.ryuqq.fileflow.adapter.in.rest.asset;

import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.AssetApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.AssetMetadataApiResponse;
import com.ryuqq.fileflow.application.asset.dto.response.AssetMetadataResponse;
import com.ryuqq.fileflow.application.asset.dto.response.AssetResponse;
import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import java.time.Instant;

/**
 * Asset API 테스트 Fixtures.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public final class AssetApiFixtures {

    private AssetApiFixtures() {}

    // ===== 공통 상수 =====
    public static final String ASSET_ID = "asset_test_abc123";
    public static final String METADATA_ID = "meta_test_abc123";
    public static final String S3_KEY = "public/2026/02/product-image.jpg";
    public static final String BUCKET = "fileflow-bucket";
    public static final String FILE_NAME = "product-image.jpg";
    public static final long FILE_SIZE = 1_048_576L;
    public static final String CONTENT_TYPE = "image/jpeg";
    public static final String ETAG = "\"d41d8cd98f00b204e9800998ecf8427e\"";
    public static final String EXTENSION = "jpg";
    public static final String ORIGIN_ID = "sess_abc123";
    public static final String PURPOSE = "PRODUCT_IMAGE";
    public static final String SOURCE = "commerce-api";
    public static final int WIDTH = 1920;
    public static final int HEIGHT = 1080;
    public static final String TRANSFORM_TYPE = "ORIGINAL";
    public static final Instant CREATED_AT = Instant.parse("2026-02-09T09:30:00Z");

    // ===== Application Response Fixtures =====

    public static AssetResponse assetResponse() {
        return new AssetResponse(
                ASSET_ID,
                S3_KEY,
                BUCKET,
                AccessType.PUBLIC,
                FILE_NAME,
                FILE_SIZE,
                CONTENT_TYPE,
                ETAG,
                EXTENSION,
                AssetOrigin.SINGLE_UPLOAD,
                ORIGIN_ID,
                PURPOSE,
                SOURCE,
                CREATED_AT);
    }

    public static AssetMetadataResponse assetMetadataResponse() {
        return new AssetMetadataResponse(
                METADATA_ID, ASSET_ID, WIDTH, HEIGHT, TRANSFORM_TYPE, CREATED_AT);
    }

    // ===== API Response Fixtures =====

    public static AssetApiResponse assetApiResponse() {
        return new AssetApiResponse(
                ASSET_ID,
                S3_KEY,
                BUCKET,
                "PUBLIC",
                FILE_NAME,
                FILE_SIZE,
                CONTENT_TYPE,
                ETAG,
                EXTENSION,
                "SINGLE_UPLOAD",
                ORIGIN_ID,
                PURPOSE,
                SOURCE,
                "2026-02-09T18:30:00+09:00");
    }

    public static AssetMetadataApiResponse assetMetadataApiResponse() {
        return new AssetMetadataApiResponse(
                METADATA_ID, ASSET_ID, WIDTH, HEIGHT, TRANSFORM_TYPE, "2026-02-09T18:30:00+09:00");
    }
}
