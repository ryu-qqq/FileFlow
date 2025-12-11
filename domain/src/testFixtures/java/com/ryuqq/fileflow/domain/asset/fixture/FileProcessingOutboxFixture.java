package com.ryuqq.fileflow.domain.asset.fixture;

import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileProcessingOutboxId;
import com.ryuqq.fileflow.domain.asset.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import java.time.Instant;

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

    private static final FileAssetId DEFAULT_FILE_ASSET_ID =
            FileAssetId.of("550e8400-e29b-41d4-a716-446655440001");
    private static final String DEFAULT_PAYLOAD =
            "{\"fileAssetId\":\"550e8400-e29b-41d4-a716-446655440001\"}";

    /**
     * PENDING 상태의 Outbox.
     *
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox aPendingOutbox() {
        return FileProcessingOutbox.forProcessRequest(
                DEFAULT_FILE_ASSET_ID,
                "PROCESS_REQUEST",
                DEFAULT_PAYLOAD,
                ClockFixture.defaultClock());
    }

    /**
     * SENT 상태의 Outbox.
     *
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox aSentOutbox() {
        FileProcessingOutbox outbox =
                FileProcessingOutbox.forProcessRequest(
                        DEFAULT_FILE_ASSET_ID,
                        "PROCESS_REQUEST",
                        DEFAULT_PAYLOAD,
                        ClockFixture.defaultClock());
        outbox.markAsSent(ClockFixture.defaultClock());
        return outbox;
    }

    /**
     * FAILED 상태의 Outbox.
     *
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox aFailedOutbox() {
        FileProcessingOutbox outbox =
                FileProcessingOutbox.forProcessRequest(
                        DEFAULT_FILE_ASSET_ID,
                        "PROCESS_REQUEST",
                        DEFAULT_PAYLOAD,
                        ClockFixture.defaultClock());
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
                FileProcessingOutbox.forProcessRequest(
                        DEFAULT_FILE_ASSET_ID,
                        "PROCESS_REQUEST",
                        DEFAULT_PAYLOAD,
                        ClockFixture.defaultClock());
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
                "{\"fileAssetId\":\"550e8400-e29b-41d4-a716-446655440001\",\"from\":\"PENDING\",\"to\":\"PROCESSING\"}",
                ClockFixture.defaultClock());
    }

    /**
     * 재처리 요청용 Outbox.
     *
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox aRetryRequestOutbox() {
        return FileProcessingOutbox.forRetryRequest(
                DEFAULT_FILE_ASSET_ID,
                "Processing failed",
                "{\"fileAssetId\":\"550e8400-e29b-41d4-a716-446655440001\",\"reason\":\"retry\"}",
                ClockFixture.defaultClock());
    }

    /**
     * 커스텀 파일 ID를 가진 Outbox.
     *
     * @param fileAssetId 파일 에셋 ID
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox anOutboxWithFileAssetId(FileAssetId fileAssetId) {
        return FileProcessingOutbox.forProcessRequest(
                fileAssetId,
                "PROCESS_REQUEST",
                "{\"fileAssetId\":\"" + fileAssetId.getValue() + "\"}",
                ClockFixture.defaultClock());
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
                Instant.parse("2025-12-02T10:00:00Z"),
                null);
    }
}
