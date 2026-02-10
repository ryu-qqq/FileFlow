package com.ryuqq.fileflow.adapter.in.rest.download;

import com.ryuqq.fileflow.adapter.in.rest.download.dto.command.CreateDownloadTaskApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.response.DownloadTaskApiResponse;
import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import java.time.Instant;

/**
 * DownloadTask API 테스트 Fixtures.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public final class DownloadTaskApiFixtures {

    private DownloadTaskApiFixtures() {}

    // ===== 공통 상수 =====
    public static final String DOWNLOAD_TASK_ID = "dt_test_abc123";
    public static final String SOURCE_URL = "https://example.com/files/image.jpg";
    public static final String S3_KEY = "downloads/2026/02/image.jpg";
    public static final String BUCKET = "fileflow-bucket";
    public static final String PURPOSE = "PRODUCT_IMAGE";
    public static final String SOURCE = "commerce-api";
    public static final String CALLBACK_URL = "https://commerce-api.internal/callbacks/download";
    public static final String STATUS_PENDING = "PENDING";
    public static final String LAST_ERROR = null;
    public static final int RETRY_COUNT = 0;
    public static final int MAX_RETRIES = 3;
    public static final Instant CREATED_AT = Instant.parse("2026-02-09T09:30:00Z");
    public static final Instant STARTED_AT = null;
    public static final Instant COMPLETED_AT = null;

    // ===== Request Fixtures =====

    public static CreateDownloadTaskApiRequest createDownloadTaskRequest() {
        return new CreateDownloadTaskApiRequest(
                SOURCE_URL, S3_KEY, BUCKET, AccessType.PUBLIC, PURPOSE, SOURCE, CALLBACK_URL);
    }

    // ===== Application Response Fixtures =====

    public static DownloadTaskResponse downloadTaskResponse() {
        return new DownloadTaskResponse(
                DOWNLOAD_TASK_ID,
                SOURCE_URL,
                S3_KEY,
                BUCKET,
                AccessType.PUBLIC,
                PURPOSE,
                SOURCE,
                STATUS_PENDING,
                RETRY_COUNT,
                MAX_RETRIES,
                CALLBACK_URL,
                LAST_ERROR,
                CREATED_AT,
                STARTED_AT,
                COMPLETED_AT);
    }

    // ===== API Response Fixtures =====

    public static DownloadTaskApiResponse downloadTaskApiResponse() {
        return new DownloadTaskApiResponse(
                DOWNLOAD_TASK_ID,
                SOURCE_URL,
                S3_KEY,
                BUCKET,
                "PUBLIC",
                PURPOSE,
                SOURCE,
                STATUS_PENDING,
                RETRY_COUNT,
                MAX_RETRIES,
                CALLBACK_URL,
                LAST_ERROR,
                "2026-02-09T18:30:00+09:00",
                null,
                null);
    }
}
