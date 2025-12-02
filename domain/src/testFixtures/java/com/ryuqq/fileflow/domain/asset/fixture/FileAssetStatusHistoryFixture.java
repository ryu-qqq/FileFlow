package com.ryuqq.fileflow.domain.asset.fixture;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatusHistoryId;
import java.time.LocalDateTime;

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

    private static final Long DEFAULT_FILE_ASSET_ID = 1L;
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
                DEFAULT_DURATION_MILLIS);
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
                DEFAULT_DURATION_MILLIS);
    }

    /**
     * 최초 생성 히스토리 (null → PENDING).
     *
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory anInitialHistory() {
        return FileAssetStatusHistory.forNew(
                DEFAULT_FILE_ASSET_ID, null, FileAssetStatus.PENDING, "파일 생성됨", "system", "SYSTEM", null);
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
                DEFAULT_DURATION_MILLIS);
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
                DEFAULT_DURATION_MILLIS);
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
                durationMillis);
    }

    /**
     * 커스텀 파일 ID를 가진 히스토리.
     *
     * @param fileAssetId 파일 에셋 ID
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory aHistoryWithFileAssetId(Long fileAssetId) {
        return FileAssetStatusHistory.forSystemChange(
                fileAssetId,
                FileAssetStatus.PENDING,
                FileAssetStatus.PROCESSING,
                "처리 시작",
                DEFAULT_DURATION_MILLIS);
    }

    /**
     * 영속화된 히스토리 (reconstitute).
     *
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory anExistingHistory() {
        return FileAssetStatusHistory.reconstitute(
                FileAssetStatusHistoryId.of("550e8400-e29b-41d4-a716-446655440001"),
                DEFAULT_FILE_ASSET_ID,
                FileAssetStatus.PENDING,
                FileAssetStatus.PROCESSING,
                "처리 시작",
                "system",
                "SYSTEM",
                LocalDateTime.of(2025, 12, 2, 10, 0, 0),
                DEFAULT_DURATION_MILLIS);
    }
}
