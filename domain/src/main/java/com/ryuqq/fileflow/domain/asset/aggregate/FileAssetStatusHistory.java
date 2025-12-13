package com.ryuqq.fileflow.domain.asset.aggregate;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatusHistoryId;
import java.time.Clock;
import java.time.Instant;

/**
 * FileAssetStatusHistory Aggregate.
 *
 * <p>FileAsset 상태 변경 이력을 추적합니다.
 *
 * <ul>
 *   <li>상태 변경 원인 및 행위자 기록
 *   <li>SLA 모니터링 (각 단계별 소요 시간)
 *   <li>실패 원인 분석
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class FileAssetStatusHistory {

    // ===== 식별 정보 =====
    private final FileAssetStatusHistoryId id;
    private final FileAssetId fileAssetId;

    // ===== 상태 변경 정보 =====
    private final FileAssetStatus fromStatus;
    private final FileAssetStatus toStatus;
    private final String message;

    // ===== 변경 주체 정보 =====
    private final String actor;
    private final String actorType;

    // ===== 시간 정보 =====
    private final Instant changedAt;
    private final Long durationMillis;

    private FileAssetStatusHistory(
            FileAssetStatusHistoryId id,
            FileAssetId fileAssetId,
            FileAssetStatus fromStatus,
            FileAssetStatus toStatus,
            String message,
            String actor,
            String actorType,
            Instant changedAt,
            Long durationMillis) {
        validateNotNull(id, "FileAssetStatusHistoryId");
        validateNotNull(fileAssetId, "FileAssetId");
        validateNotNull(toStatus, "ToStatus");
        validateNotNull(changedAt, "ChangedAt");

        this.id = id;
        this.fileAssetId = fileAssetId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.message = message;
        this.actor = actor;
        this.actorType = actorType;
        this.changedAt = changedAt;
        this.durationMillis = durationMillis;
    }

    // ===== 팩토리 메서드 =====

    /**
     * 새로운 상태 변경 히스토리 생성.
     *
     * @param fileAssetId 파일 에셋 ID
     * @param fromStatus 이전 상태 (최초 생성 시 null)
     * @param toStatus 변경된 상태
     * @param message 상태 메시지
     * @param actor 변경 주체
     * @param actorType 변경 주체 타입
     * @param durationMillis 이전 상태에서 소요된 시간
     * @param clock 시간 소스
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory forNew(
            FileAssetId fileAssetId,
            FileAssetStatus fromStatus,
            FileAssetStatus toStatus,
            String message,
            String actor,
            String actorType,
            Long durationMillis,
            Clock clock) {
        return new FileAssetStatusHistory(
                FileAssetStatusHistoryId.forNew(),
                fileAssetId,
                fromStatus,
                toStatus,
                message,
                actor,
                actorType,
                clock.instant(),
                durationMillis);
    }

    /**
     * 시스템에 의한 상태 변경 히스토리 생성 (편의 메서드).
     *
     * @param fileAssetId 파일 에셋 ID
     * @param fromStatus 이전 상태
     * @param toStatus 변경된 상태
     * @param message 상태 메시지
     * @param durationMillis 이전 상태에서 소요된 시간
     * @param clock 시간 소스
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory forSystemChange(
            FileAssetId fileAssetId,
            FileAssetStatus fromStatus,
            FileAssetStatus toStatus,
            String message,
            Long durationMillis,
            Clock clock) {
        return forNew(
                fileAssetId,
                fromStatus,
                toStatus,
                message,
                "system",
                "SYSTEM",
                durationMillis,
                clock);
    }

    /**
     * n8n에 의한 상태 변경 히스토리 생성 (편의 메서드).
     *
     * @param fileAssetId 파일 에셋 ID
     * @param fromStatus 이전 상태
     * @param toStatus 변경된 상태
     * @param message 상태 메시지
     * @param durationMillis 이전 상태에서 소요된 시간
     * @param clock 시간 소스
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory forN8nChange(
            FileAssetId fileAssetId,
            FileAssetStatus fromStatus,
            FileAssetStatus toStatus,
            String message,
            Long durationMillis,
            Clock clock) {
        return forNew(
                fileAssetId, fromStatus, toStatus, message, "n8n", "N8N", durationMillis, clock);
    }

    /**
     * DB에서 복원 (Persistence Layer용).
     *
     * @param id 히스토리 ID
     * @param fileAssetId 파일 에셋 ID
     * @param fromStatus 이전 상태
     * @param toStatus 변경된 상태
     * @param message 상태 메시지
     * @param actor 변경 주체
     * @param actorType 변경 주체 타입
     * @param changedAt 변경 시각
     * @param durationMillis 이전 상태에서 소요된 시간
     * @return FileAssetStatusHistory
     */
    public static FileAssetStatusHistory reconstitute(
            FileAssetStatusHistoryId id,
            FileAssetId fileAssetId,
            FileAssetStatus fromStatus,
            FileAssetStatus toStatus,
            String message,
            String actor,
            String actorType,
            Instant changedAt,
            Long durationMillis) {
        return new FileAssetStatusHistory(
                id,
                fileAssetId,
                fromStatus,
                toStatus,
                message,
                actor,
                actorType,
                changedAt,
                durationMillis);
    }

    // ===== 비즈니스 메서드 =====

    /**
     * 실패 상태인지 확인.
     *
     * @return FAILED 상태이면 true
     */
    public boolean isFailure() {
        return toStatus == FileAssetStatus.FAILED;
    }

    /**
     * 최초 생성 히스토리인지 확인.
     *
     * @return fromStatus가 null이면 true
     */
    public boolean isInitialCreation() {
        return fromStatus == null;
    }

    /**
     * SLA 위반 여부 확인.
     *
     * @param slaMillis SLA 기준 (밀리초)
     * @return SLA 초과 시 true
     */
    public boolean exceedsSla(long slaMillis) {
        return durationMillis != null && durationMillis > slaMillis;
    }

    // ===== Private Helper =====

    private void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "는 null일 수 없습니다.");
        }
    }

    // ===== Getter =====

    public FileAssetStatusHistoryId getId() {
        return id;
    }

    public FileAssetId getFileAssetId() {
        return fileAssetId;
    }

    public FileAssetStatus getFromStatus() {
        return fromStatus;
    }

    public FileAssetStatus getToStatus() {
        return toStatus;
    }

    public String getMessage() {
        return message;
    }

    public String getActor() {
        return actor;
    }

    public String getActorType() {
        return actorType;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public Long getDurationMillis() {
        return durationMillis;
    }
}
