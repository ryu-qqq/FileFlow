package com.ryuqq.fileflow.adapter.out.persistence.asset.entity;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * FileAssetStatusHistory JPA Entity.
 *
 * <p>파일 에셋 상태 변경 이력을 저장합니다. Append-Only 테이블로 수정/삭제가 없습니다.
 */
@Entity
@Table(name = "file_asset_status_history")
public class FileAssetStatusHistoryJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "file_asset_id", nullable = false, length = 36)
    private String fileAssetId;

    @Column(name = "from_status", length = 20)
    @Enumerated(EnumType.STRING)
    private FileAssetStatus fromStatus;

    @Column(name = "to_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FileAssetStatus toStatus;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "actor", length = 100)
    private String actor;

    @Column(name = "actor_type", length = 20)
    private String actorType;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(name = "duration_millis")
    private Long durationMillis;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected FileAssetStatusHistoryJpaEntity() {}

    private FileAssetStatusHistoryJpaEntity(
            UUID id,
            String fileAssetId,
            FileAssetStatus fromStatus,
            FileAssetStatus toStatus,
            String message,
            String actor,
            String actorType,
            Instant changedAt,
            Long durationMillis,
            Instant createdAt) {
        this.id = id;
        this.fileAssetId = fileAssetId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.message = message;
        this.actor = actor;
        this.actorType = actorType;
        this.changedAt = changedAt;
        this.durationMillis = durationMillis;
        this.createdAt = createdAt;
    }

    public static FileAssetStatusHistoryJpaEntity of(
            UUID id,
            String fileAssetId,
            FileAssetStatus fromStatus,
            FileAssetStatus toStatus,
            String message,
            String actor,
            String actorType,
            Instant changedAt,
            Long durationMillis,
            Instant createdAt) {
        return new FileAssetStatusHistoryJpaEntity(
                id,
                fileAssetId,
                fromStatus,
                toStatus,
                message,
                actor,
                actorType,
                changedAt,
                durationMillis,
                createdAt);
    }

    public UUID getId() {
        return id;
    }

    public String getFileAssetId() {
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
