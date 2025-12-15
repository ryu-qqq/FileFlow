package com.ryuqq.fileflow.domain.asset.fixture;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatusHistoryId;
import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import java.time.Instant;

/**
 * FileAssetStatusHistory Aggregate Test Fixture.
 *
 * @author development-team
 * @since 1.0.0
 */
public class FileAssetStatusHistoryFixture {

    private FileAssetStatusHistoryFixture() {
        throw new AssertionError("Utility class");
    }

    private static final FileAssetId DEFAULT_FILE_ASSET_ID =
            FileAssetId.of("550e8400-e29b-41d4-a716-446655440001");
    private static final Long DEFAULT_DURATION_MILLIS = 1000L;

    /**
     * 기본 상태 히스토리 (PENDING → PROCESSING).
     *
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory aStatusHistory() {
        return FileAssetStatusHistory.forSystemChange(
                DEFAULT_FILE_ASSET_ID,
                FileAssetStatus.PENDING,
                FileAssetStatus.PROCESSING,
                "처리 시작",
                DEFAULT_DURATION_MILLIS,
                ClockFixture.defaultClock());
    }

    /**
     * 실패 상태 히스토리 (PROCESSING → FAILED).
     *
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory aFailedHistory() {
        return FileAssetStatusHistory.forSystemChange(
                DEFAULT_FILE_ASSET_ID,
                FileAssetStatus.PROCESSING,
                FileAssetStatus.FAILED,
                "처리 실패: 파일 손상",
                DEFAULT_DURATION_MILLIS,
                ClockFixture.defaultClock());
    }

    /**
     * 최초 생성 히스토리 (null → PENDING).
     *
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory anInitialHistory() {
        return FileAssetStatusHistory.forNew(
                DEFAULT_FILE_ASSET_ID,
                null,
                FileAssetStatus.PENDING,
                "파일 생성됨",
                "system",
                "SYSTEM",
                null,
                ClockFixture.defaultClock());
    }

    /**
     * 완료 상태 히스토리 (PROCESSING → COMPLETED).
     *
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory aCompletedHistory() {
        return FileAssetStatusHistory.forSystemChange(
                DEFAULT_FILE_ASSET_ID,
                FileAssetStatus.PROCESSING,
                FileAssetStatus.COMPLETED,
                "처리 완료",
                DEFAULT_DURATION_MILLIS,
                ClockFixture.defaultClock());
    }

    /**
     * PROCESSING 상태 변경 히스토리 (PENDING → PROCESSING).
     *
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory aProcessingHistory() {
        return FileAssetStatusHistory.forSystemChange(
                DEFAULT_FILE_ASSET_ID,
                FileAssetStatus.PENDING,
                FileAssetStatus.PROCESSING,
                "이미지 처리 시작",
                DEFAULT_DURATION_MILLIS,
                ClockFixture.defaultClock());
    }

    /**
     * RESIZED 상태 변경 히스토리 (PROCESSING → RESIZED).
     *
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory aResizedHistory() {
        return FileAssetStatusHistory.forSystemChange(
                DEFAULT_FILE_ASSET_ID,
                FileAssetStatus.PROCESSING,
                FileAssetStatus.RESIZED,
                "이미지 리사이징 완료",
                DEFAULT_DURATION_MILLIS,
                ClockFixture.defaultClock());
    }

    /**
     * n8n 상태 변경 히스토리 (PROCESSING → N8N_PROCESSING).
     *
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory aN8nHistory() {
        return FileAssetStatusHistory.forN8nChange(
                DEFAULT_FILE_ASSET_ID,
                FileAssetStatus.PROCESSING,
                FileAssetStatus.N8N_PROCESSING,
                "n8n 워크플로우 시작",
                DEFAULT_DURATION_MILLIS,
                ClockFixture.defaultClock());
    }

    /**
     * SLA 초과 히스토리.
     *
     * @param durationMillis 소요 시간 (밀리초)
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory aSlowHistory(long durationMillis) {
        return FileAssetStatusHistory.forSystemChange(
                DEFAULT_FILE_ASSET_ID,
                FileAssetStatus.PENDING,
                FileAssetStatus.PROCESSING,
                "처리 시작 (지연)",
                durationMillis,
                ClockFixture.defaultClock());
    }

    /**
     * 커스텀 파일 ID를 가진 히스토리.
     *
     * @param fileAssetId 파일 에셋 ID
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory aHistoryWithFileAssetId(FileAssetId fileAssetId) {
        return FileAssetStatusHistory.forSystemChange(
                fileAssetId,
                FileAssetStatus.PENDING,
                FileAssetStatus.PROCESSING,
                "처리 시작",
                DEFAULT_DURATION_MILLIS,
                ClockFixture.defaultClock());
    }

    /**
     * 영속화된 히스토리 (reconstitute).
     *
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory anExistingHistory() {
        return FileAssetStatusHistory.reconstitute(
                FileAssetStatusHistoryId.of("550e8400-e29b-41d4-a716-446655440002"),
                DEFAULT_FILE_ASSET_ID,
                FileAssetStatus.PENDING,
                FileAssetStatus.PROCESSING,
                "처리 시작",
                "system",
                "SYSTEM",
                Instant.parse("2025-12-02T10:00:00Z"),
                DEFAULT_DURATION_MILLIS);
    }
}
