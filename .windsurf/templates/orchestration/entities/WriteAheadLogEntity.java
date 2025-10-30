package com.ryuqq.adapter.out.persistence.{domain_lower}.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * {Domain} Write-Ahead Log Entity
 *
 * <p>크래시 복구를 위한 WAL (Write-Ahead Log) 패턴을 구현합니다.
 * Finalize 전에 PENDING 상태로 기록하고, 완료 후 COMPLETED로 변경합니다.</p>
 *
 * @author {author_name}
 * @since {version}
 */
@Entity
@Table(
    name = "{domain_lower}_write_ahead_logs",
    indexes = {
        @Index(name = "idx_{domain_lower}_wal_state_created", columnList = "state, created_at"),
        @Index(name = "idx_{domain_lower}_wal_op_id", columnList = "op_id")
    }
)
public class {Domain}WriteAheadLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "op_id", nullable = false, length = 255)
    private String opId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 50)
    private WriteAheadState state;

    @Column(name = "outcome_type", length = 50)
    private String outcomeType;

    @Column(name = "outcome_message", length = 1000)
    private String outcomeMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    protected {Domain}WriteAheadLogEntity() {}

    public {Domain}WriteAheadLogEntity(
        String opId,
        WriteAheadState state,
        String outcomeType,
        String outcomeMessage,
        LocalDateTime createdAt
    ) {
        this.opId = Objects.requireNonNull(opId);
        this.state = Objects.requireNonNull(state);
        this.outcomeType = outcomeType;
        this.outcomeMessage = outcomeMessage;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public void markCompleted() {
        this.state = WriteAheadState.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public String getOpId() { return opId; }
    public WriteAheadState getState() { return state; }
    public String getOutcomeType() { return outcomeType; }
    public String getOutcomeMessage() { return outcomeMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
