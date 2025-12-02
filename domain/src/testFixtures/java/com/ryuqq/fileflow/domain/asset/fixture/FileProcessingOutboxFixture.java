package com.ryuqq.fileflow.domain.asset.fixture;

import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import com.ryuqq.fileflow.domain.asset.vo.FileProcessingOutboxId;
import com.ryuqq.fileflow.domain.asset.vo.OutboxStatus;
import java.time.LocalDateTime;

/**
 * FileProcessingOutbox Aggregate Test Fixture.
 *
 * @author development-team
 * @since 1.0.0
 */
public class FileProcessingOutboxFixture {

    private FileProcessingOutboxFixture() {
        throw new AssertionError("Utility class");
    }

    private static final Long DEFAULT_FILE_ASSET_ID = 1L;
    private static final String DEFAULT_PAYLOAD = "{\"fileAssetId\":1}";

    /**
     * PENDING 상태의 Outbox.
     *
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox aPendingOutbox() {
        return FileProcessingOutbox.forProcessRequest(
                DEFAULT_FILE_ASSET_ID, "PROCESS_REQUEST", DEFAULT_PAYLOAD);
    }

    /**
     * SENT 상태의 Outbox.
     *
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox aSentOutbox() {
        FileProcessingOutbox outbox =
                FileProcessingOutbox.forProcessRequest(DEFAULT_FILE_ASSET_ID, "PROCESS_REQUEST", DEFAULT_PAYLOAD);
        outbox.markAsSent();
        return outbox;
    }

    /**
     * FAILED 상태의 Outbox.
     *
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox aFailedOutbox() {
        FileProcessingOutbox outbox =
                FileProcessingOutbox.forProcessRequest(DEFAULT_FILE_ASSET_ID, "PROCESS_REQUEST", DEFAULT_PAYLOAD);
        outbox.markAsFailed("Connection timeout");
        return outbox;
    }

    /**
     * 재시도 소진(Exhausted) 상태의 Outbox.
     *
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox anExhaustedOutbox() {
        FileProcessingOutbox outbox =
                FileProcessingOutbox.forProcessRequest(DEFAULT_FILE_ASSET_ID, "PROCESS_REQUEST", DEFAULT_PAYLOAD);
        outbox.markAsFailed("Error 1");
        outbox.markAsFailed("Error 2");
        outbox.markAsFailed("Error 3");
        return outbox;
    }

    /**
     * 상태 변경 알림용 Outbox.
     *
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox aStatusChangeOutbox() {
        return FileProcessingOutbox.forStatusChange(
                DEFAULT_FILE_ASSET_ID,
                "PENDING",
                "PROCESSING",
                "{\"fileAssetId\":1,\"from\":\"PENDING\",\"to\":\"PROCESSING\"}");
    }

    /**
     * 재처리 요청용 Outbox.
     *
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox aRetryRequestOutbox() {
        return FileProcessingOutbox.forRetryRequest(
                DEFAULT_FILE_ASSET_ID, "Processing failed", "{\"fileAssetId\":1,\"reason\":\"retry\"}");
    }

    /**
     * 커스텀 파일 ID를 가진 Outbox.
     *
     * @param fileAssetId 파일 에셋 ID
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox anOutboxWithFileAssetId(Long fileAssetId) {
        return FileProcessingOutbox.forProcessRequest(
                fileAssetId, "PROCESS_REQUEST", "{\"fileAssetId\":" + fileAssetId + "}");
    }

    /**
     * 영속화된 Outbox (reconstitute).
     *
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox anExistingOutbox() {
        return FileProcessingOutbox.reconstitute(
                FileProcessingOutboxId.of("550e8400-e29b-41d4-a716-446655440002"),
                DEFAULT_FILE_ASSET_ID,
                "PROCESS_REQUEST",
                DEFAULT_PAYLOAD,
                OutboxStatus.PENDING,
                0,
                null,
                LocalDateTime.of(2025, 12, 2, 10, 0, 0),
                null);
    }
}
